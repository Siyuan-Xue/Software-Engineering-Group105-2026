package com.bupt.ta.db.facade;

import com.bupt.ta.db.core.JsonMapperFactory;
import com.bupt.ta.db.core.JsonStoreConfig;
import jakarta.servlet.ServletContext;

import java.nio.file.Path;

/**
 * Application-scoped registry for {@link TaDatabase} used by servlet {@code init()} methods.
 *
 * <p>{@link #init} executes once per container lifecycle (see {@linkplain com.bupt.ta.bootstrap.AppContextListener});
 * callers must not initialise twice unless tests replace implementations via {@link #bind}.</p>
 */
public final class DatabaseProvider {

    /**
     * {@link ServletContext#setAttribute(String, Object)} key identifying the facade instance.
     */
    public static final String CONTEXT_KEY = TaDatabase.class.getName();

    private DatabaseProvider() {
    }

    /**
     * Loads JSON-backed tables under {@code dataDir}, seeds empty stores if required, then exposes the facade.
     *
     * @param servletContext Jakarta application scope carrier
     * @param dataDir        directory containing {@code users.json} and sibling table files
     */
    public static void init(ServletContext servletContext, Path dataDir) {
        servletContext.setAttribute(CONTEXT_KEY,
                FileTaDatabase.open(JsonStoreConfig.of(dataDir, JsonMapperFactory.create())));
    }

    /**
     * Overrides the facade (integration tests substitute in-memory twins).
     *
     * @param servletContext writable application scope
     * @param database       facade to expose through {@link #get}
     */
    public static void bind(ServletContext servletContext, TaDatabase database) {
        servletContext.setAttribute(CONTEXT_KEY, database);
    }

    /**
     * Retrieves the previously bound facade.
     *
     * @param servletContext active servlet context obtained from servlet or filter lifecycle
     * @return non-null facade when {@link #init} succeeded
     * @throws IllegalStateException when bindings are missing or the attribute has an unexpected type
     */
    public static TaDatabase get(ServletContext servletContext) {
        Object value = servletContext.getAttribute(CONTEXT_KEY);
        if (value instanceof TaDatabase database) {
            return database;
        }
        throw new IllegalStateException("TaDatabase has not been initialized in ServletContext");
    }
}
