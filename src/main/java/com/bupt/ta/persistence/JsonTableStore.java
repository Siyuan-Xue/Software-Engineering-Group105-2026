package com.bupt.ta.persistence;

import com.bupt.ta.config.DatabaseConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JsonTableStore<T extends PersistableEntity> {
    private static final int CURRENT_VERSION = 1;

    private final Path tablePath;
    private final Class<T> entityType;
    private final ObjectMapper objectMapper;
    private final ObjectWriter objectWriter;
    private final JavaType envelopeType;
    private final Map<UUID, T> rows = new LinkedHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile boolean loaded;

    public JsonTableStore(DatabaseConfig config, String fileName, Class<T> entityType) {
        this.tablePath = config.getDataDirectory().resolve(fileName);
        this.entityType = entityType;
        this.objectMapper = config.getObjectMapper();
        this.objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        this.envelopeType = objectMapper.getTypeFactory()
                .constructParametricType(TableEnvelope.class, entityType);
    }

    public Path getTablePath() {
        return tablePath;
    }

    public void initialize() {
        loadIfNeeded();
    }

    public List<T> findAll() {
        loadIfNeeded();
        lock.readLock().lock();
        try {
            return rows.values().stream()
                    .map(this::copy)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<T> findById(UUID id) {
        loadIfNeeded();
        lock.readLock().lock();
        try {
            T entity = rows.get(id);
            return entity == null ? Optional.empty() : Optional.of(copy(entity));
        } finally {
            lock.readLock().unlock();
        }
    }

    public T save(T entity) {
        loadIfNeeded();
        lock.writeLock().lock();
        try {
            T workingCopy = copy(entity);
            Instant now = Instant.now();
            UUID id = workingCopy.getId();
            T existing = id == null ? null : rows.get(id);

            if (id == null) {
                workingCopy.setId(UUID.randomUUID());
                if (workingCopy.getCreatedAt() == null) {
                    workingCopy.setCreatedAt(now);
                }
            } else if (existing != null && workingCopy.getCreatedAt() == null) {
                workingCopy.setCreatedAt(existing.getCreatedAt());
            } else if (workingCopy.getCreatedAt() == null) {
                workingCopy.setCreatedAt(now);
            }

            workingCopy.setUpdatedAt(now);
            rows.put(workingCopy.getId(), copy(workingCopy));
            persist();
            return copy(workingCopy);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean delete(UUID id) {
        loadIfNeeded();
        lock.writeLock().lock();
        try {
            T removed = rows.remove(id);
            if (removed == null) {
                return false;
            }
            persist();
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void loadIfNeeded() {
        if (loaded) {
            return;
        }

        synchronized (this) {
            if (loaded) {
                return;
            }
            try {
                Files.createDirectories(tablePath.getParent());
                if (Files.notExists(tablePath)) {
                    writeEnvelope(tablePath, new TableEnvelope<>(CURRENT_VERSION, new ArrayList<>()));
                }

                TableEnvelope<T> envelope = objectMapper.readValue(tablePath.toFile(), envelopeType);
                rows.clear();
                if (envelope.getRows() != null) {
                    for (T row : envelope.getRows()) {
                        if (row.getId() == null) {
                            throw new DataAccessException("Encountered row without id in " + tablePath);
                        }
                        rows.put(row.getId(), copy(row));
                    }
                }
                loaded = true;
            } catch (IOException e) {
                throw new DataAccessException("Failed to load table " + tablePath, e);
            }
        }
    }

    private void persist() {
        Path tempPath = tablePath.resolveSibling(tablePath.getFileName() + ".tmp");
        TableEnvelope<T> envelope = new TableEnvelope<>(CURRENT_VERSION, findSnapshot());
        try {
            writeEnvelope(tempPath, envelope);
            Files.move(tempPath, tablePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new DataAccessException("Failed to persist table " + tablePath, e);
        } finally {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
                // Ignore cleanup failures after an already handled write attempt.
            }
        }
    }

    protected void writeEnvelope(Path path, TableEnvelope<T> envelope) throws IOException {
        objectWriter.writeValue(path.toFile(), envelope);
    }

    private List<T> findSnapshot() {
        List<T> snapshot = new ArrayList<>(rows.size());
        for (T entity : rows.values()) {
            snapshot.add(copy(entity));
        }
        return snapshot;
    }

    private T copy(T entity) {
        return objectMapper.convertValue(entity, entityType);
    }
}
