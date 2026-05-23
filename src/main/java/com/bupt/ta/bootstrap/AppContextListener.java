package com.bupt.ta.bootstrap;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.facade.DatabaseProvider;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Jakarta bootstrap listener that publishes the JSON-backed {@link com.bupt.ta.db.facade.TaDatabase} singleton on
 * {@link jakarta.servlet.ServletContext} before servlet {@code init} runs.
 *
 * <p>{@link AppConfig#resolveDataDirectory()} chooses the writable data root from JVM {@link System#getProperty(String)},
 * {@link System#getenv(String)}, then sensible defaults aligned with embedded Tomcat layouts.</p>
 *
 * @see DatabaseProvider#init(jakarta.servlet.ServletContext, java.nio.file.Path)
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    /**
     * Delegates facade construction and JSON store wiring to {@link DatabaseProvider}.
     *
     * @param sce servlet context bootstrap event exposing mutable application attributes
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabaseProvider.init(sce.getServletContext(), AppConfig.resolveDataDirectory());
    }
}
