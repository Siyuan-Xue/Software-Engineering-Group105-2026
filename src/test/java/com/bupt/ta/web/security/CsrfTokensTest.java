package com.bupt.ta.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfTokensTest {

    @Test
    void ensureTokenShouldExposeSessionTokenToRequest() {
        FakeRequest fake = new FakeRequest();

        String token = CsrfTokens.ensureToken(fake.request());

        assertNotNull(token);
        assertEquals(token, fake.sessionAttributes.get(CsrfTokens.SESSION_ATTR));
        assertEquals(token, fake.requestAttributes.get(CsrfTokens.REQUEST_ATTR));
        assertEquals(CsrfTokens.PARAMETER_NAME, fake.requestAttributes.get(CsrfTokens.PARAMETER_ATTR));
        assertEquals(CsrfTokens.HEADER_NAME, fake.requestAttributes.get(CsrfTokens.HEADER_ATTR));
    }

    @Test
    void matchesShouldAcceptHeaderOrParameterOnlyWhenTokenMatches() {
        FakeRequest fake = new FakeRequest();
        String token = CsrfTokens.ensureToken(fake.request());

        fake.headers.put(CsrfTokens.HEADER_NAME, token);
        assertTrue(CsrfTokens.matches(fake.request()));

        fake.headers.clear();
        fake.parameters.put(CsrfTokens.PARAMETER_NAME, token);
        assertTrue(CsrfTokens.matches(fake.request()));

        fake.parameters.put(CsrfTokens.PARAMETER_NAME, "wrong");
        assertFalse(CsrfTokens.matches(fake.request()));
    }

    @Test
    void unsafeMethodClassifierShouldOnlyFlagStateChangingMethods() {
        assertFalse(CsrfTokens.isUnsafeMethod("GET"));
        assertFalse(CsrfTokens.isUnsafeMethod("HEAD"));
        assertTrue(CsrfTokens.isUnsafeMethod("POST"));
        assertTrue(CsrfTokens.isUnsafeMethod("delete"));
    }

    private static final class FakeRequest {
        private final Map<String, Object> sessionAttributes = new HashMap<>();
        private final Map<String, Object> requestAttributes = new HashMap<>();
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, String> parameters = new HashMap<>();
        private final HttpSession session = session();

        HttpServletRequest request() {
            return (HttpServletRequest) Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{HttpServletRequest.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getSession" -> {
                            if (args == null || args.length == 0 || Boolean.TRUE.equals(args[0])) {
                                yield session;
                            }
                            yield sessionAttributes.isEmpty() ? null : session;
                        }
                        case "setAttribute" -> {
                            requestAttributes.put((String) args[0], args[1]);
                            yield null;
                        }
                        case "getHeader" -> headers.get((String) args[0]);
                        case "getParameter" -> parameters.get((String) args[0]);
                        default -> null;
                    });
        }

        private HttpSession session() {
            return (HttpSession) Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{HttpSession.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getAttribute" -> sessionAttributes.get((String) args[0]);
                        case "setAttribute" -> {
                            sessionAttributes.put((String) args[0], args[1]);
                            yield null;
                        }
                        default -> null;
                    });
        }
    }
}
