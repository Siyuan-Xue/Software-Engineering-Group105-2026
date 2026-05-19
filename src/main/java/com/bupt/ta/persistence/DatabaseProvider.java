package com.bupt.ta.persistence;

import jakarta.servlet.ServletContext;

@Deprecated
/**
 * Backward-compatible facade for legacy code that imports the persistence package.
 */
public final class DatabaseProvider {
    private DatabaseProvider() {
    }

    public static void bind(ServletContext servletContext, TaDatabase database) {
        com.bupt.ta.db.facade.DatabaseProvider.bind(servletContext, database.unwrap());
    }

    public static TaDatabase get(ServletContext servletContext) {
        return new TaDatabase(com.bupt.ta.db.facade.DatabaseProvider.get(servletContext));
    }
}
