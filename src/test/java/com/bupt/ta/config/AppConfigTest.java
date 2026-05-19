package com.bupt.ta.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void createObjectMapperShouldSerializeJson() throws Exception {
        JsonNode node = AppConfig.createObjectMapper().readTree("{\"name\":\"qm-hire\"}");

        assertEquals("qm-hire", node.get("name").asText());
    }

    @Test
    void resolveDataDirectoryShouldProvideUsableDefaults() {
        assertNotNull(AppConfig.createObjectMapper());
        assertNotNull(AppConfig.resolveDataDirectory());
        assertFalse(AppConfig.resolveDataDirectory().toString().isBlank());
    }

    @Test
    void resolveDataDirectoryShouldPreferExplicitPropertyOverContainerDefault() {
        String oldDataDir = System.getProperty(AppConfig.DATA_DIR_PROPERTY);
        String oldCatalinaBase = System.getProperty("catalina.base");
        try {
            System.setProperty(AppConfig.DATA_DIR_PROPERTY, " " + tempDir.resolve("explicit") + " ");
            System.setProperty("catalina.base", tempDir.resolve("catalina").toString());

            assertEquals(tempDir.resolve("explicit").toAbsolutePath().normalize(), AppConfig.resolveDataDirectory());
        } finally {
            restoreProperty(AppConfig.DATA_DIR_PROPERTY, oldDataDir);
            restoreProperty("catalina.base", oldCatalinaBase);
        }
    }

    @Test
    void resolveDataDirectoryShouldUseCatalinaBaseWhenNoExplicitDirectoryExists() {
        String envValue = System.getenv(AppConfig.DATA_DIR_ENV);
        assumeTrue(envValue == null || envValue.isBlank());
        String oldDataDir = System.getProperty(AppConfig.DATA_DIR_PROPERTY);
        String oldCatalinaBase = System.getProperty("catalina.base");
        try {
            System.clearProperty(AppConfig.DATA_DIR_PROPERTY);
            System.setProperty("catalina.base", tempDir.resolve("tomcat").toString());

            assertEquals(tempDir.resolve("tomcat/ta105-data").toAbsolutePath().normalize(),
                    AppConfig.resolveDataDirectory());
        } finally {
            restoreProperty(AppConfig.DATA_DIR_PROPERTY, oldDataDir);
            restoreProperty("catalina.base", oldCatalinaBase);
        }
    }

    private void restoreProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
