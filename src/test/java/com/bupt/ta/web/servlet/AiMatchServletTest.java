package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.support.ServletHarness;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import com.bupt.ta.web.security.AiRequestGuard;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiMatchServletTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldReturnUnauthorizedJsonWhenUserIsMissing() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        AiMatchServlet servlet = init(new AiMatchServlet(), db);
        ServletHarness harness = new ServletHarness().method("POST");

        servlet.doPost(harness.request(), harness.response());

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, harness.status());
        assertEquals("application/json;charset=UTF-8", harness.contentType());
        assertTrue(harness.responseBody().contains("Please log in first"));
    }

    @Test
    void shouldRejectMissingAiConsentBeforeReadingExternalConfiguration() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        User user = db.users().save(TestData.user(TestData.uniqueEmail("ai-match"), UserRole.TA, "AI User"));
        AiMatchServlet servlet = init(new AiMatchServlet(), db);
        ServletHarness harness = new ServletHarness()
                .method("POST")
                .currentUser(user);

        servlet.doPost(harness.request(), harness.response());

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, harness.status());
        assertTrue(harness.responseBody().contains("AI consent is required"));
    }

    @Test
    void consentCanBeProvidedByParameterOrHeader() {
        ServletHarness param = new ServletHarness().parameter(AiRequestGuard.CONSENT_PARAM, "accepted");
        ServletHarness header = new ServletHarness().header(AiRequestGuard.CONSENT_HEADER, "yes");

        assertTrue(AiRequestGuard.hasConsent(param.request()));
        assertTrue(AiRequestGuard.hasConsent(header.request()));
    }

    private AiMatchServlet init(AiMatchServlet servlet, TaDatabase db) throws Exception {
        ServletHarness initHarness = new ServletHarness().bindDatabase(db);
        servlet.init(initHarness.servletConfig());
        return servlet;
    }
}
