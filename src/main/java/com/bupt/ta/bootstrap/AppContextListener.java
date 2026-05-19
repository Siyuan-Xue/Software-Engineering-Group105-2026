package com.bupt.ta.bootstrap;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.facade.DatabaseProvider;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
/**
 * Initializes and releases the shared JSON database for the web application.
 */
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabaseProvider.init(sce.getServletContext(), AppConfig.resolveDataDirectory());
    }
}
