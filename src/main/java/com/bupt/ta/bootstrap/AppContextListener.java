package com.bupt.ta.bootstrap;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.UserRole;
import com.bupt.ta.persistence.DatabaseProvider;
import com.bupt.ta.persistence.TaDatabase;
import com.bupt.ta.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    private static final String DEFAULT_USER_EMAIL = "test@example.com";
    private static final String DEFAULT_USER_PASSWORD = "password";
    private static final String DEFAULT_USER_FULL_NAME = "Test User";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        TaDatabase database = TaDatabase.open(DatabaseConfig.defaultConfig());
        ensureDefaultUser(database, sce);
        DatabaseProvider.bind(sce.getServletContext(), database);
    }

    private void ensureDefaultUser(TaDatabase database, ServletContextEvent sce) {
        if (database.users().findByEmail(DEFAULT_USER_EMAIL).isPresent()) {
            return;
        }

        User defaultUser = new User();
        defaultUser.setEmail(DEFAULT_USER_EMAIL);
        defaultUser.setPasswordHash(PasswordUtil.hashPassword(DEFAULT_USER_PASSWORD));
        defaultUser.setRole(UserRole.TA);
        defaultUser.setFullName(DEFAULT_USER_FULL_NAME);
        defaultUser.setActive(true);
        database.users().save(defaultUser);

        sce.getServletContext().log("Default user created: " + DEFAULT_USER_EMAIL);
    }
}
