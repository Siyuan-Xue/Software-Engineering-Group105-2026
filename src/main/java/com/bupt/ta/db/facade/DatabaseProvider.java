package com.bupt.ta.db.facade;

import com.bupt.ta.db.core.JsonMapperFactory;
import com.bupt.ta.db.core.JsonStoreConfig;
import jakarta.servlet.ServletContext;

import java.nio.file.Path;

/**
 * Servlet-context holder for the shared application database instance.
 */
public final class DatabaseProvider {
    public static final String CONTEXT_KEY = TaDatabase.class.getName();

    private DatabaseProvider() {
    }

    public static void init(ServletContext servletContext, Path dataDir) {
        servletContext.setAttribute(CONTEXT_KEY,
                FileTaDatabase.open(JsonStoreConfig.of(dataDir, JsonMapperFactory.create())));
    }

    public static void bind(ServletContext servletContext, TaDatabase database) {
        servletContext.setAttribute(CONTEXT_KEY, database);
    }

    public static TaDatabase get(ServletContext servletContext) {
        Object value = servletContext.getAttribute(CONTEXT_KEY);
        if (value instanceof TaDatabase database) {
            return database;
        }
        throw new IllegalStateException("TaDatabase has not been initialized in ServletContext");
    }
}
