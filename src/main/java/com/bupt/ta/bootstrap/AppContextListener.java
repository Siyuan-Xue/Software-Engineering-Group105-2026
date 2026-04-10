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

    private static final String MO_USER_EMAIL = "mo@example.com";
    private static final String ADMIN_USER_EMAIL = "admin@example.com";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        TaDatabase database = TaDatabase.open(DatabaseConfig.defaultConfig());
        ensureDefaultUser(database, sce);
        ensureUser(database, sce, MO_USER_EMAIL, "Module Organiser", UserRole.MO);
        ensureUser(database, sce, ADMIN_USER_EMAIL, "System Admin", UserRole.ADMIN);
        DatabaseProvider.bind(sce.getServletContext(), database);
    }

    private void ensureDefaultUser(TaDatabase database, ServletContextEvent sce) {
        ensureUser(database, sce, DEFAULT_USER_EMAIL, DEFAULT_USER_FULL_NAME, UserRole.TA);
    }

    private void ensureUser(TaDatabase database, ServletContextEvent sce, String email, String fullName, UserRole role) {
        if (database.users().findByEmail(email).isPresent()) {
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hashPassword(DEFAULT_USER_PASSWORD));
        user.setRole(role);
        user.setFullName(fullName);
        user.setActive(true);
        database.users().save(user);

        sce.getServletContext().log(role.name() + " user created: " + email);
    }
}
