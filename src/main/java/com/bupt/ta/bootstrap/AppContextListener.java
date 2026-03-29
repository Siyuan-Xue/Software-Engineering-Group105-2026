package com.bupt.ta.bootstrap;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.persistence.DatabaseProvider;
import com.bupt.ta.persistence.TaDatabase;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        TaDatabase database = TaDatabase.open(DatabaseConfig.defaultConfig());
        DatabaseProvider.bind(sce.getServletContext(), database);
    }
}
