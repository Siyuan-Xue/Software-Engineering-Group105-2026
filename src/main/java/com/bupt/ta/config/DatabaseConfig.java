package com.bupt.ta.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Factory for opening the configured application database.
 */
public final class DatabaseConfig {
    private final Path dataDirectory;
    private final ObjectMapper objectMapper;

    private DatabaseConfig(Path dataDirectory, ObjectMapper objectMapper) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    public static DatabaseConfig defaultConfig() {
        return new DatabaseConfig(AppConfig.resolveDataDirectory(), AppConfig.createObjectMapper());
    }

    public static DatabaseConfig of(Path dataDirectory, ObjectMapper objectMapper) {
        return new DatabaseConfig(dataDirectory, objectMapper);
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
