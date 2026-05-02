package com.bupt.ta.config;

import com.bupt.ta.db.core.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConfig {
    public static final String DATA_DIR_PROPERTY = "ta105.data.dir";
    public static final String DATA_DIR_ENV = "TA105_DATA_DIR";

    private AppConfig() {
    }

    public static ObjectMapper createObjectMapper() {
        return JsonMapperFactory.create();
    }

    public static Path resolveDataDirectory() {
        String propertyValue = System.getProperty(DATA_DIR_PROPERTY);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return Paths.get(propertyValue.trim()).toAbsolutePath().normalize();
        }

        String envValue = System.getenv(DATA_DIR_ENV);
        if (envValue != null && !envValue.isBlank()) {
            return Paths.get(envValue.trim()).toAbsolutePath().normalize();
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "ta105-data").toAbsolutePath().normalize();
        }

        return Paths.get("data").toAbsolutePath().normalize();
    }
}
