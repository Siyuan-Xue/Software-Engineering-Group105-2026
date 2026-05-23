package com.bupt.ta.web.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Stateless helpers assembling HTTP redirect targets augmented with arbitrary query fragments.
 *
 * <p>Use when flash parameters must survive over {@link jakarta.servlet.http.HttpServletResponse#sendRedirect(String)}
 * without rewriting existing {@code ?} delimiters incorrectly.</p>
 */
public final class RedirectUrls {

    /** Prevents instantiation; only static collaborators are surfaced. */
    private RedirectUrls() {
    }

    /**
     * Appends or extends query components using {@link URLEncoder#encode(String, java.nio.charset.Charset)} semantics.
     *
     * @param path  absolute servlet-relative path optionally containing pre-existing selectors
     * @param key   non-encoded parameter name echoed verbatim
     * @param value raw message body encoded prior to concatenation ({@code null} short-circuits to unchanged {@code path})
     * @return merged redirect location or {@code null} should {@code path} prove {@code null}
     */
    public static String withQueryParam(String path, String key, String value) {
        if (path == null || key == null || value == null) {
            return path;
        }
        String separator = path.contains("?") ? "&" : "?";
        return path + separator + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
