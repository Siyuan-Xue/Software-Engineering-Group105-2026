package com.bupt.ta.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Session-bound CSRF token helpers shared by the filter and JSP bootstrap.
 */
public final class CsrfTokens {
    public static final String SESSION_ATTR = "csrfToken";
    public static final String REQUEST_ATTR = "csrfToken";
    public static final String PARAMETER_NAME = "_csrf";
    public static final String PARAMETER_ATTR = "csrfParameterName";
    public static final String HEADER_NAME = "X-CSRF-Token";
    public static final String HEADER_ATTR = "csrfHeaderName";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder TOKEN_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private CsrfTokens() {
    }

    public static String ensureToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object existing = session.getAttribute(SESSION_ATTR);
        if (existing instanceof String token && !token.isBlank()) {
            expose(request, token);
            return token;
        }
        String token = newToken();
        session.setAttribute(SESSION_ATTR, token);
        expose(request, token);
        return token;
    }

    public static void expose(HttpServletRequest request, String token) {
        request.setAttribute(REQUEST_ATTR, token);
        request.setAttribute(PARAMETER_ATTR, PARAMETER_NAME);
        request.setAttribute(HEADER_ATTR, HEADER_NAME);
    }

    public static boolean matches(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_ATTR);
        if (!(expected instanceof String token) || token.isBlank()) {
            return false;
        }
        String provided = request.getHeader(HEADER_NAME);
        if (provided == null || provided.isBlank()) {
            provided = queryParameter(request.getQueryString(), PARAMETER_NAME);
        }
        if (provided == null || provided.isBlank()) {
            provided = request.getParameter(PARAMETER_NAME);
        }
        return provided != null && constantTimeEquals(token, provided.trim());
    }

    public static boolean isUnsafeMethod(String method) {
        if (method == null) {
            return false;
        }
        return switch (method.toUpperCase()) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return TOKEN_ENCODER.encodeToString(bytes);
    }

    private static boolean constantTimeEquals(String expected, String provided) {
        byte[] a = expected.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] b = provided.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int diff = a.length ^ b.length;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    private static String queryParameter(String queryString, String name) {
        if (queryString == null || queryString.isBlank()) {
            return null;
        }
        for (String pair : queryString.split("&")) {
            int idx = pair.indexOf('=');
            String rawName = idx >= 0 ? pair.substring(0, idx) : pair;
            if (!name.equals(urlDecode(rawName))) {
                continue;
            }
            return idx >= 0 ? urlDecode(pair.substring(idx + 1)) : "";
        }
        return null;
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), java.nio.charset.StandardCharsets.UTF_8);
    }
}
