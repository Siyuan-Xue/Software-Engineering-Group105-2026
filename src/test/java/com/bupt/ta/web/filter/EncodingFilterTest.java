package com.bupt.ta.web.filter;

import com.bupt.ta.support.ServletHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncodingFilterTest {

    @Test
    void shouldApplyUtf8EncodingBeforeContinuing() throws Exception {
        ServletHarness harness = new ServletHarness().route("/dashboard");

        new EncodingFilter().doFilter(harness.request(), harness.response(), harness.chain());

        assertEquals(1, harness.chainCalls());
        assertEquals("UTF-8", harness.characterEncoding());
    }
}
