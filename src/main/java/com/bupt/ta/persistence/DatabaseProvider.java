package com.bupt.ta.persistence;

import jakarta.servlet.ServletContext;

/**
 * Legacy indirection wrapping {@link com.bupt.ta.db.facade.DatabaseProvider}.
 *
 * @deprecated Retained solely for callers still importing {@code com.bupt.ta.persistence}; migrate to
 *             {@linkplain com.bupt.ta.db.facade.DatabaseProvider the primary facade APIs}.
 */
@Deprecated
public final class DatabaseProvider {

    private DatabaseProvider() {
    }

    /**
     * @see com.bupt.ta.db.facade.DatabaseProvider#bind(jakarta.servlet.ServletContext, com.bupt.ta.db.facade.TaDatabase)
     */
    public static void bind(ServletContext servletContext, TaDatabase database) {
        com.bupt.ta.db.facade.DatabaseProvider.bind(servletContext, database.unwrap());
    }

    /**
     * @see com.bupt.ta.db.facade.DatabaseProvider#get(jakarta.servlet.ServletContext)
     */
    public static TaDatabase get(ServletContext servletContext) {
        return new TaDatabase(com.bupt.ta.db.facade.DatabaseProvider.get(servletContext));
    }
}
