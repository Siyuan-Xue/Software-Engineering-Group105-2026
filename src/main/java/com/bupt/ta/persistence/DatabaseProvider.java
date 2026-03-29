package com.bupt.ta.persistence;

import jakarta.servlet.ServletContext;

public final class DatabaseProvider {
    public static final String ATTRIBUTE_NAME = TaDatabase.class.getName();

    private DatabaseProvider() {
    }

    public static void bind(ServletContext servletContext, TaDatabase database) {
        servletContext.setAttribute(ATTRIBUTE_NAME, database);
    }

    public static TaDatabase get(ServletContext servletContext) {
        Object attribute = servletContext.getAttribute(ATTRIBUTE_NAME);
        if (attribute instanceof TaDatabase database) {
            return database;
        }
        throw new IllegalStateException("TaDatabase has not been initialized in ServletContext");
    }
}
