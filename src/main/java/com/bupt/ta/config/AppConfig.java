package com.bupt.ta.config;

import com.bupt.ta.db.core.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Application-wide configuration helpers for persistence and JSON processing.
 *
 * <p>The on-disk JSON store location is resolved in a fixed precedence order so
 * operators can override the default without code changes ({@link #resolveDataDirectory()}).</p>
 */
public final class AppConfig {

    /** JVM system property naming the absolute or relative JSON data directory. */
    public static final String DATA_DIR_PROPERTY = "ta105.data.dir";

    /** Process environment variable naming the absolute or relative JSON data directory. */
    public static final String DATA_DIR_ENV = "TA105_DATA_DIR";

    private AppConfig() {
    }

    /**
     * Builds the shared Jackson {@link ObjectMapper} used for JSON tables and REST-style payloads.
     *
     * @return mapper configured for this application (registered modules, Java 8 dates, etc.)
     */
    public static ObjectMapper createObjectMapper() {
        return JsonMapperFactory.create();
    }

    /**
     * Resolves the root directory holding JSON table files ({@code users.json}, etc.).
     *
     * <p>Precedence:</p>
     * <ol>
     *   <li>JVM {@value #DATA_DIR_PROPERTY}</li>
     *   <li>Environment {@value #DATA_DIR_ENV}</li>
     *   <li>When {@code catalina.base} is set (typical Tomcat): {@code catalina.base/ta105-data}</li>
     *   <li>Otherwise: {@code data} resolved against the JVM working directory</li>
     * </ol>
     * <p>The returned path is normalized ({@link Path#normalize()}) against the filesystem.</p>
     *
     * @return absolute directory used by {@link com.bupt.ta.db.facade.FileTaDatabase}
     */
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
