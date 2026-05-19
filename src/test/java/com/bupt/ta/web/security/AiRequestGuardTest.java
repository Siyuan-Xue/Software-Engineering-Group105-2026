package com.bupt.ta.web.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiRequestGuardTest {

    @Test
    void hasConsentShouldRequireExplicitAcceptedMarker() {
        FakeRequest fake = new FakeRequest();

        assertFalse(AiRequestGuard.hasConsent(fake.request()));

        fake.parameters.put(AiRequestGuard.CONSENT_PARAM, "true");
        assertTrue(AiRequestGuard.hasConsent(fake.request()));

        fake.parameters.clear();
        fake.headers.put(AiRequestGuard.CONSENT_HEADER, "accepted");
        assertTrue(AiRequestGuard.hasConsent(fake.request()));

        fake.headers.put(AiRequestGuard.CONSENT_HEADER, "no");
        assertFalse(AiRequestGuard.hasConsent(fake.request()));
    }

    private static final class FakeRequest {
        private final Map<String, String> parameters = new HashMap<>();
        private final Map<String, String> headers = new HashMap<>();

        HttpServletRequest request() {
            return (HttpServletRequest) Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{HttpServletRequest.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getParameter" -> parameters.get((String) args[0]);
                        case "getHeader" -> headers.get((String) args[0]);
                        default -> null;
                    });
        }
    }
}
