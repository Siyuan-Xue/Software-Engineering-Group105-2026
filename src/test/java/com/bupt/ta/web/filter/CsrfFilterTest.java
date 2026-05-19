package com.bupt.ta.web.filter;

import com.bupt.ta.support.ServletHarness;
import com.bupt.ta.web.security.CsrfTokens;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfFilterTest {

    @Test
    void safeDynamicRequestsShouldEnsureTokenAndContinue() throws Exception {
        ServletHarness harness = new ServletHarness().route("/dashboard").method("GET");

        new CsrfFilter().doFilter(harness.request(), harness.response(), harness.chain());

        assertEquals(1, harness.chainCalls());
        assertNotNull(harness.requestAttribute(CsrfTokens.REQUEST_ATTR));
        assertEquals(CsrfTokens.PARAMETER_NAME, harness.requestAttribute(CsrfTokens.PARAMETER_ATTR));
        assertEquals(CsrfTokens.HEADER_NAME, harness.requestAttribute(CsrfTokens.HEADER_ATTR));
    }

    @Test
    void unsafeRequestsWithoutMatchingTokenShouldBeRejected() throws Exception {
        ServletHarness harness = new ServletHarness().route("/applications").method("POST");

        new CsrfFilter().doFilter(harness.request(), harness.response(), harness.chain());

        assertEquals(0, harness.chainCalls());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, harness.status());
        assertEquals("application/json;charset=UTF-8", harness.contentType());
        assertTrue(harness.responseBody().contains("Invalid CSRF token"));
    }

    @Test
    void unsafeRequestsWithHeaderTokenShouldContinue() throws Exception {
        ServletHarness harness = new ServletHarness().route("/applications").method("POST");
        harness.session().setAttribute(CsrfTokens.SESSION_ATTR, "known-token");
        harness.header(CsrfTokens.HEADER_NAME, "known-token");

        new CsrfFilter().doFilter(harness.request(), harness.response(), harness.chain());

        assertEquals(1, harness.chainCalls());
        assertEquals("known-token", harness.requestAttribute(CsrfTokens.REQUEST_ATTR));
    }

    @Test
    void staticAssetsShouldBypassCsrfChecks() throws Exception {
        ServletHarness harness = new ServletHarness().route("/js/app.js").method("POST");

        new CsrfFilter().doFilter(harness.request(), harness.response(), harness.chain());

        assertEquals(1, harness.chainCalls());
    }
}
