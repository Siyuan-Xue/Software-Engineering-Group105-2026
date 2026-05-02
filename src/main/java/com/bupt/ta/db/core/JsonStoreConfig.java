package com.bupt.ta.db.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Objects;

public final class JsonStoreConfig {
    private final Path dataDir;
    private final ObjectMapper objectMapper;

    private JsonStoreConfig(Path dataDir, ObjectMapper objectMapper) {
        this.dataDir = Objects.requireNonNull(dataDir, "dataDir must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    public static JsonStoreConfig of(Path dataDir, ObjectMapper objectMapper) {
        return new JsonStoreConfig(dataDir, objectMapper);
    }

    public Path getDataDir() {
        return dataDir;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
