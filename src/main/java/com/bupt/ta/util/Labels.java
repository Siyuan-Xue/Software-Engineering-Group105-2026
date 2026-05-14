package com.bupt.ta.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses user-entered vacancy/resume tags from a single text field.
 * Separators: comma (EN/ZH), semicolon (EN/ZH), pipe (EN/fullwidth), or newline.
 */
public final class Labels {
    private Labels() {
    }

    public static List<String> parseList(String raw, int max) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String token : raw.split("[,，;；|｜\\n\\r]+")) {
            String t = token.strip();
            if (t.isEmpty()) {
                continue;
            }
            if (t.length() > 48) {
                t = t.substring(0, 48);
            }
            seen.add(t);
            if (seen.size() >= max) {
                break;
            }
        }
        return new ArrayList<>(seen);
    }
}
