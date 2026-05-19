package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.support.ServletHarness;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterServletTest {
    @TempDir
    Path tempDir;

    @Test
    void doGetShouldForwardRegistrationForm() throws Exception {
        RegisterServlet servlet = new RegisterServlet();
        ServletHarness harness = new ServletHarness();

        servlet.doGet(harness.request(), harness.response());

        assertEquals("/register.jsp", harness.forwardedPath());
    }

    @Test
    void successfulSelfRegistrationShouldAlwaysCreateTaAndRedirectToLogin() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        RegisterServlet servlet = init(new RegisterServlet(), db);
        String email = TestData.uniqueEmail("register");
        ServletHarness harness = new ServletHarness()
                .parameter("email", email)
                .parameter("password", "password123")
                .parameter("confirmPassword", "password123")
                .parameter("fullName", "Register User")
                .parameter("role", "ADMIN");

        servlet.doPost(harness.request(), harness.response());

        assertTrue(harness.redirectLocation().startsWith("/login?successMessage="));
        assertEquals(UserRole.TA, db.users().findByEmail(email).orElseThrow().getRole());
    }

    @Test
    void invalidRegistrationShouldPreserveFormFieldsAndLocalizedError() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        RegisterServlet servlet = init(new RegisterServlet(), db);
        ServletHarness harness = new ServletHarness()
                .requestAttribute("language", "zh")
                .parameter("email", "candidate@example.test")
                .parameter("password", "password123")
                .parameter("confirmPassword", "different")
                .parameter("fullName", "Candidate")
                .parameter("phone", "123")
                .parameter("department", "CS")
                .parameter("studentId", "S1")
                .parameter("role", "MO");

        servlet.doPost(harness.request(), harness.response());

        assertEquals("/register.jsp", harness.forwardedPath());
        assertEquals(I18n.message("zh", "msg.passwordMismatch"), harness.requestAttribute("errorMessage"));
        assertEquals("candidate@example.test", harness.requestAttribute("email"));
        assertEquals("Candidate", harness.requestAttribute("fullName"));
        assertEquals("TA", harness.requestAttribute("role"));
    }

    private RegisterServlet init(RegisterServlet servlet, TaDatabase db) throws Exception {
        ServletHarness initHarness = new ServletHarness().bindDatabase(db);
        servlet.init(initHarness.servletConfig());
        return servlet;
    }
}
