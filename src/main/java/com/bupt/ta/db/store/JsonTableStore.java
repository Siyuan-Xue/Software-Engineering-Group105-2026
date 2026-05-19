package com.bupt.ta.db.store;

import com.bupt.ta.db.core.DatabaseCorruptionException;
import com.bupt.ta.db.core.DatabaseException;
import com.bupt.ta.domain.entity.AbstractEntity;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 * Generic in-memory table abstraction synchronized to a JSON file.
 */
public class JsonTableStore<T extends AbstractEntity> {
    private final TableDescriptor<T> descriptor;
    private final ObjectMapper mapper;
    private final AtomicJsonFileWriter writer;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<UUID, T> snapshot = new LinkedHashMap<>();
    private final JavaType envelopeType;

    private volatile boolean loaded;

    public JsonTableStore(TableDescriptor<T> descriptor, ObjectMapper mapper, AtomicJsonFileWriter writer) {
        this.descriptor = descriptor;
        this.mapper = mapper;
        this.writer = writer;
        this.envelopeType = mapper.getTypeFactory().constructParametricType(TableEnvelope.class, descriptor.getRowType());
    }

    public TableEnvelope<T> load() {
        ensureLoaded();
        lock.readLock().lock();
        try {
            return new TableEnvelope<>(descriptor.getVersion(), copyList(snapshot.values()));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<T> list() {
        ensureLoaded();
        lock.readLock().lock();
        try {
            return copyList(snapshot.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<T> findById(UUID id) {
        ensureLoaded();
        lock.readLock().lock();
        try {
            T row = snapshot.get(id);
            return row == null ? Optional.empty() : Optional.of(copy(row));
        } finally {
            lock.readLock().unlock();
        }
    }

    public T save(T row) {
        ensureLoaded();
        lock.writeLock().lock();
        try {
            T copy = copy(row);
            Instant now = Instant.now();
            if (copy.getId() == null) {
                copy.setId(UUID.randomUUID());
                if (copy.getCreatedAt() == null) {
                    copy.setCreatedAt(now);
                }
            } else if (snapshot.containsKey(copy.getId()) && copy.getCreatedAt() == null) {
                copy.setCreatedAt(snapshot.get(copy.getId()).getCreatedAt());
            } else if (copy.getCreatedAt() == null) {
                copy.setCreatedAt(now);
            }
            copy.setUpdatedAt(now);
            snapshot.put(copy.getId(), copy(copy));
            persistLocked();
            return copy(copy);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void delete(UUID id) {
        ensureLoaded();
        lock.writeLock().lock();
        try {
            snapshot.remove(id);
            persistLocked();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean exists(Predicate<T> filter) {
        ensureLoaded();
        lock.readLock().lock();
        try {
            return snapshot.values().stream().map(this::copy).anyMatch(filter);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<T> query(Predicate<T> filter) {
        ensureLoaded();
        lock.readLock().lock();
        try {
            return snapshot.values().stream()
                    .map(this::copy)
                    .filter(filter)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void replaceAll(List<T> rows) {
        ensureLoaded();
        lock.writeLock().lock();
        try {
            snapshot.clear();
            Instant now = Instant.now();
            for (T row : rows) {
                T copy = copy(row);
                if (copy.getId() == null) {
                    copy.setId(UUID.randomUUID());
                }
                if (copy.getCreatedAt() == null) {
                    copy.setCreatedAt(now);
                }
                if (copy.getUpdatedAt() == null) {
                    copy.setUpdatedAt(now);
                }
                snapshot.put(copy.getId(), copy(copy));
            }
            persistLocked();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (this) {
            if (loaded) {
                return;
            }
            try {
                if (Files.notExists(descriptor.getFilePath())) {
                    writer.writeAtomically(descriptor.getFilePath(),
                            new TableEnvelope<>(descriptor.getVersion(), new ArrayList<>()), mapper);
                }
                TableEnvelope<T> envelope = mapper.readValue(descriptor.getFilePath().toFile(), envelopeType);
                snapshot.clear();
                if (envelope.getRows() != null) {
                    for (T row : envelope.getRows()) {
                        if (row.getId() == null) {
                            throw new DatabaseCorruptionException(
                                    "Encountered row without id in " + descriptor.getTableName());
                        }
                        snapshot.put(row.getId(), copy(row));
                    }
                }
                loaded = true;
            } catch (DatabaseCorruptionException e) {
                throw e;
            } catch (IOException e) {
                throw new DatabaseCorruptionException(
                        "Failed to load table " + descriptor.getFilePath(), e);
            }
        }
    }

    private void persistLocked() {
        writer.writeAtomically(descriptor.getFilePath(),
                new TableEnvelope<>(descriptor.getVersion(), copyList(snapshot.values())),
                mapper);
    }

    private List<T> copyList(Iterable<T> rows) {
        List<T> copies = new ArrayList<>();
        for (T row : rows) {
            copies.add(copy(row));
        }
        return copies;
    }

    private T copy(T row) {
        try {
            return mapper.convertValue(row, descriptor.getRowType());
        } catch (IllegalArgumentException e) {
            throw new DatabaseException("Failed to copy row for " + descriptor.getTableName(), e);
        }
    }
}
