package com.bupt.ta.web.filter;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.support.ServletHarness;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthFilterTest {
    @TempDir
    Path tempDir;

    @Test
    void publicRoutesShouldPassThroughWithoutCurrentUser() throws Exception {
        ServletHarness harness = new ServletHarness().route("/login");

        new AuthFilter().doFilter(harness.request(), harness.response(), harness.chain());

        assertEquals(1, harness.chainCalls());
        assertEquals("en", harness.requestAttribute("language"));
        assertEquals("en", harness.requestAttribute("langTag"));
        assertNotNull(harness.requestAttribute("i18n"));
    }

    @Test
    void protectedRoutesShouldRedirectUnauthenticatedUsers() throws Exception {
        ServletHarness harness = new ServletHarness().route("/dashboard");

        new AuthFilter().doFilter(harness.request(), harness.response(), harness.chain());

        assertEquals(0, harness.chainCalls());
        assertTrue(harness.redirectLocation().startsWith("/login?errorMessage="));
    }

    @Test
    @SuppressWarnings("unchecked")
    void authenticatedRoutesShouldExposeSharedRequestContextAndUnreadCount() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        User user = TestData.user(TestData.uniqueEmail("filter-auth"), UserRole.TA, "Alice Example");
        user.setPhone("123");
        user.setDepartment("CS");
        user.setStudentId("S100");
        user.setBio("Bio");
        user.setPreferredLanguage("zh");
        user.setPreferredAppearance("dark");
        user = db.users().save(user);
        Notification notification = TestData.notification(user.getId(), NotificationType.SYSTEM, "Unread");
        db.notifications().save(notification);

        ServletHarness harness = new ServletHarness()
                .bindDatabase(db)
                .currentUser(user)
                .route("/dashboard");

        new AuthFilter().doFilter(harness.request(), harness.response(), harness.chain());

        assertEquals(1, harness.chainCalls());
        assertEquals("zh", harness.requestAttribute("language"));
        assertEquals("dark", harness.requestAttribute("appearance"));
        assertEquals("TA", harness.requestAttribute("userRole"));
        assertEquals(100, harness.requestAttribute("profileCompletionPercentage"));
        assertEquals(1, harness.requestAttribute("unreadNotificationCount"));
        Map<String, Object> profile = (Map<String, Object>) harness.requestAttribute("userProfile");
        assertEquals("Alice", profile.get("firstName"));
        assertEquals("Example", profile.get("lastName"));
        assertEquals("CS", profile.get("department"));
        assertEquals("zh", harness.sessionAttribute("language"));
    }
}
