package com.bupt.ta.web.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utilities for safely appending query parameters to redirect targets.
 */
public final class RedirectUrls {
    private RedirectUrls() {
    }

    public static String withQueryParam(String path, String key, String value) {
        if (path == null || key == null || value == null) {
            return path;
        }
        String separator = path.contains("?") ? "&" : "?";
        return path + separator + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
