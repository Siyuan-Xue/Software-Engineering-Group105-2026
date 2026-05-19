package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.support.ServletHarness;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminUsersServletTest {
    @TempDir
    Path tempDir;

    @Test
    void doGetShouldForbidNonAdminsAndPopulateAdminView() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        User ta = db.users().save(TestData.user(TestData.uniqueEmail("admin-view-ta"), UserRole.TA, "TA User"));
        AdminUsersServlet servlet = init(new AdminUsersServlet(), db);

        ServletHarness forbidden = new ServletHarness().currentUser(ta);
        servlet.doGet(forbidden.request(), forbidden.response());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, forbidden.status());

        User admin = db.users().save(TestData.user(TestData.uniqueEmail("admin-view"), UserRole.ADMIN, "Admin User"));
        ServletHarness allowed = new ServletHarness().currentUser(admin)
                .parameter("successMessage", "Saved")
                .parameter("errorMessage", "Nope");
        servlet.doGet(allowed.request(), allowed.response());
        assertEquals("/portal/admin_users.jsp", allowed.forwardedPath());
        assertEquals(admin.getId(), allowed.requestAttribute("currentUserId"));
        assertEquals("Saved", allowed.requestAttribute("successMessage"));
        assertEquals("Nope", allowed.requestAttribute("errorMessage"));
    }

    @Test
    void adminCanCreateUserAndAuditTheChange() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        User admin = db.users().save(TestData.user(TestData.uniqueEmail("admin-create"), UserRole.ADMIN, "Admin Create"));
        AdminUsersServlet servlet = init(new AdminUsersServlet(), db);
        String email = TestData.uniqueEmail("created-user");
        ServletHarness harness = new ServletHarness()
                .currentUser(admin)
                .parameter("action", "create")
                .parameter("email", email)
                .parameter("fullName", "Created User")
                .parameter("role", "MO")
                .parameter("password", "password123")
                .parameter("department", "CS");

        servlet.doPost(harness.request(), harness.response());

        assertTrue(harness.redirectLocation().startsWith("/admin/users?successMessage="));
        User created = db.users().findByEmail(email).orElseThrow();
        assertEquals(UserRole.MO, created.getRole());
        assertEquals("CS", created.getDepartment());
        assertTrue(db.auditLogs().findAll().stream()
                .anyMatch(log -> admin.getId().equals(log.getOperatorId())
                        && created.getId().equals(log.getEntityId())
                        && log.getAction() == AuditAction.CREATE));
    }

    @Test
    void adminCannotDeactivateOwnAccountOrCreateWeakPassword() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        User admin = db.users().save(TestData.user(TestData.uniqueEmail("admin-self"), UserRole.ADMIN, "Self Admin"));
        AdminUsersServlet servlet = init(new AdminUsersServlet(), db);

        ServletHarness selfDeactivate = new ServletHarness()
                .currentUser(admin)
                .parameter("action", "deactivate")
                .parameter("userId", admin.getId().toString());
        servlet.doPost(selfDeactivate.request(), selfDeactivate.response());
        assertTrue(selfDeactivate.redirectLocation().contains("errorMessage="));
        assertTrue(db.users().findById(admin.getId()).orElseThrow().isActive());

        ServletHarness weakPassword = new ServletHarness()
                .currentUser(admin)
                .parameter("action", "create")
                .parameter("email", TestData.uniqueEmail("weak-user"))
                .parameter("fullName", "Weak User")
                .parameter("role", "TA")
                .parameter("password", "short");
        servlet.doPost(weakPassword.request(), weakPassword.response());
        assertTrue(weakPassword.redirectLocation().contains("errorMessage="));
    }

    private AdminUsersServlet init(AdminUsersServlet servlet, TaDatabase db) throws Exception {
        ServletHarness initHarness = new ServletHarness().bindDatabase(db);
        servlet.init(initHarness.servletConfig());
        return servlet;
    }
}
