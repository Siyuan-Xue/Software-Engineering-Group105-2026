package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.support.ServletHarness;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginServletTest {
    @TempDir
    Path tempDir;

    @Test
    void doGetShouldForwardMessagesToLoginPage() throws Exception {
        LoginServlet servlet = new LoginServlet();
        ServletHarness harness = new ServletHarness()
                .parameter("errorMessage", "Bad")
                .parameter("successMessage", "Good");

        servlet.doGet(harness.request(), harness.response());

        assertEquals("/login.jsp", harness.forwardedPath());
        assertEquals("Bad", harness.requestAttribute("errorMessage"));
        assertEquals("Good", harness.requestAttribute("successMessage"));
    }

    @Test
    void missingCredentialsShouldForwardLocalizedError() throws Exception {
        LoginServlet servlet = new LoginServlet();
        ServletHarness harness = new ServletHarness()
                .requestAttribute("language", "zh")
                .parameter("email", "")
                .parameter("password", "");

        servlet.doPost(harness.request(), harness.response());

        assertEquals("/login.jsp", harness.forwardedPath());
        assertEquals(I18n.message("zh", "auth.emailPasswordRequired"),
                harness.requestAttribute("errorMessage"));
    }

    @Test
    void validLoginShouldCreateSessionRedirectAndAppendAudit() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        User user = db.users().save(TestData.user(TestData.uniqueEmail("login"), UserRole.TA, "Login User"));
        LoginServlet servlet = init(new LoginServlet(), db);
        ServletHarness harness = new ServletHarness()
                .bindDatabase(db)
                .parameter("email", user.getEmail())
                .parameter("password", "password123");

        servlet.doPost(harness.request(), harness.response());

        assertEquals("/dashboard", harness.redirectLocation());
        assertEquals(user.getId(), ((User) harness.sessionAttribute("currentUser")).getId());
        assertNotNull(harness.sessionAttribute(I18n.SESSION_LANGUAGE_ATTR));
        assertTrue(db.auditLogs().findAll().stream()
                .anyMatch(log -> user.getId().equals(log.getOperatorId())
                        && log.getAction() == AuditAction.LOGIN));
    }

    @Test
    void invalidOrInactiveLoginShouldForwardWithoutCreatingCurrentUser() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        User inactive = TestData.user(TestData.uniqueEmail("inactive-login"), UserRole.TA, "Inactive Login");
        inactive.setActive(false);
        inactive = db.users().save(inactive);
        LoginServlet servlet = init(new LoginServlet(), db);
        ServletHarness invalid = new ServletHarness()
                .bindDatabase(db)
                .parameter("email", inactive.getEmail())
                .parameter("password", "wrong");

        servlet.doPost(invalid.request(), invalid.response());
        assertEquals("/login.jsp", invalid.forwardedPath());
        assertEquals("Account inactive. Please contact an administrator.", invalid.requestAttribute("errorMessage"));
        assertEquals(null, invalid.sessionAttribute("currentUser"));

        ServletHarness missing = new ServletHarness()
                .bindDatabase(db)
                .parameter("email", TestData.uniqueEmail("missing-login"))
                .parameter("password", "password123");
        servlet.doPost(missing.request(), missing.response());
        assertEquals("/login.jsp", missing.forwardedPath());
        assertEquals(I18n.message("en", "auth.invalidCredentials"), missing.requestAttribute("errorMessage"));
    }

    private LoginServlet init(LoginServlet servlet, TaDatabase db) throws Exception {
        ServletHarness initHarness = new ServletHarness().bindDatabase(db);
        servlet.init(initHarness.servletConfig());
        return servlet;
    }
}
