package com.bupt.ta.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void factoryShouldPreserveProvidedDirectoryAndMapper() {
        ObjectMapper mapper = AppConfig.createObjectMapper();

        DatabaseConfig config = DatabaseConfig.of(tempDir, mapper);

        assertEquals(tempDir, config.getDataDirectory());
        assertSame(mapper, config.getObjectMapper());
    }

    @Test
    void factoryShouldRejectNullInputsAndDefaultShouldResolveValues() {
        ObjectMapper mapper = AppConfig.createObjectMapper();

        assertThrows(NullPointerException.class, () -> DatabaseConfig.of(null, mapper));
        assertThrows(NullPointerException.class, () -> DatabaseConfig.of(tempDir, null));
        assertNotNull(DatabaseConfig.defaultConfig().getDataDirectory());
        assertNotNull(DatabaseConfig.defaultConfig().getObjectMapper());
    }
}
