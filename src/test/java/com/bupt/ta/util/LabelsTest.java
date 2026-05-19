package com.bupt.ta.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LabelsTest {

    @Test
    void parseListShouldSplitTrimDeduplicateTruncateAndLimit() {
        String longLabel = "x".repeat(60);

        List<String> labels = Labels.parseList(" Java, SQL；Java｜Python\n" + longLabel + "|Ignored", 4);

        assertEquals(List.of("Java", "SQL", "Python", "x".repeat(48)), labels);
    }

    @Test
    void parseListShouldHandleBlankAndZeroLimitInputs() {
        assertTrue(Labels.parseList(null, 5).isEmpty());
        assertTrue(Labels.parseList("   ", 5).isEmpty());
        assertTrue(Labels.parseList("Java,SQL", 0).isEmpty());
    }
}
