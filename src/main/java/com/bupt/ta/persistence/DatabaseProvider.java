package com.bupt.ta.persistence;

import jakarta.servlet.ServletContext;

@Deprecated
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
