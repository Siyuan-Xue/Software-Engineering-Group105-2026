package com.bupt.ta.web.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RedirectUrlsTest {
    @Test
    void withQueryParamUsesQuestionMarkForPathWithoutQuery() {
        String url = RedirectUrls.withQueryParam("/ta105/applications", "successMessage", "OK");
        assertTrue(url.startsWith("/ta105/applications?successMessage="));
    }

    @Test
    void withQueryParamUsesAmpersandWhenQueryAlreadyPresent() {
        String url = RedirectUrls.withQueryParam("/ta105/application/detail?applicationId=1", "errorMessage", "fail");
        assertTrue(url.contains("applicationId=1&errorMessage="));
    }
}
