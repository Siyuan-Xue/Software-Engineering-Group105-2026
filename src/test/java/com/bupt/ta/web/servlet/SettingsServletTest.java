package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.support.ServletHarness;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import com.bupt.ta.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsServletTest {
    @TempDir
    Path tempDir;

    @Test
    void doGetShouldRedirectAnonymousUsersAndForwardProfileForCurrentUser() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        SettingsServlet servlet = init(new SettingsServlet(), db);
        ServletHarness anonymous = new ServletHarness();
        servlet.doGet(anonymous.request(), anonymous.response());
        assertEquals("/login", anonymous.redirectLocation());

        User user = db.users().save(TestData.user(TestData.uniqueEmail("settings-get"), UserRole.TA, "Settings User"));
        ServletHarness authenticated = new ServletHarness()
                .currentUser(user)
                .parameter("successMessage", "Saved");
        servlet.doGet(authenticated.request(), authenticated.response());

        assertEquals("/portal/settings.jsp", authenticated.forwardedPath());
        assertEquals("normal", authenticated.requestAttribute("pageState"));
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) authenticated.requestAttribute("userProfile");
        assertEquals("Settings", profile.get("firstName"));
        assertEquals("User", profile.get("lastName"));
    }

    @Test
    void updatePreferencesShouldNormalizeValuesAndStoreSessionAttributes() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        User user = db.users().save(TestData.user(TestData.uniqueEmail("settings-pref"), UserRole.TA, "Settings Pref"));
        SettingsServlet servlet = init(new SettingsServlet(), db);
        ServletHarness harness = new ServletHarness()
                .currentUser(user)
                .parameter("action", "updatePreferences")
                .parameter("preferredLanguage", "ZH")
                .parameter("preferredAppearance", "DARK");

        servlet.doPost(harness.request(), harness.response());

        User saved = db.users().findById(user.getId()).orElseThrow();
        assertEquals("zh", saved.getPreferredLanguage());
        assertEquals("dark", saved.getPreferredAppearance());
        assertEquals("zh", harness.sessionAttribute("language"));
        assertEquals("dark", harness.sessionAttribute("appearance"));
        assertTrue(harness.redirectLocation().startsWith("/settings?state=prefSuccess"));
    }

    @Test
    void changePasswordShouldValidateCurrentPasswordAndPersistNewHash() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        User user = db.users().save(TestData.user(TestData.uniqueEmail("settings-pwd"), UserRole.TA, "Settings Password"));
        SettingsServlet servlet = init(new SettingsServlet(), db);
        ServletHarness wrong = new ServletHarness()
                .currentUser(user)
                .parameter("action", "changePassword")
                .parameter("currentPassword", "wrong")
                .parameter("newPassword", "newpassword123")
                .parameter("confirmPassword", "newpassword123");
        servlet.doPost(wrong.request(), wrong.response());
        assertTrue(wrong.redirectLocation().startsWith("/settings?state=pwdFailure"));

        ServletHarness correct = new ServletHarness()
                .currentUser(user)
                .parameter("action", "changePassword")
                .parameter("currentPassword", "password123")
                .parameter("newPassword", "newpassword123")
                .parameter("confirmPassword", "newpassword123");
        servlet.doPost(correct.request(), correct.response());

        assertTrue(correct.redirectLocation().startsWith("/settings?state=pwdSuccess"));
        assertTrue(PasswordUtil.checkPassword("newpassword123",
                db.users().findById(user.getId()).orElseThrow().getPasswordHash()));
    }

    private SettingsServlet init(SettingsServlet servlet, TaDatabase db) throws Exception {
        ServletHarness initHarness = new ServletHarness().bindDatabase(db);
        servlet.init(initHarness.servletConfig());
        return servlet;
    }
}
