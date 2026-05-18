package com.bupt.ta.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConfigTest {

    @Test
    void createObjectMapperShouldSerializeJson() throws Exception {
        JsonNode node = AppConfig.createObjectMapper().readTree("{\"name\":\"qm-hire\"}");

        assertEquals("qm-hire", node.get("name").asText());
    }

    @Test
    void resolveDataDirectoryShouldProvideUsableDefaults() {
        assertNotNull(AppConfig.createObjectMapper());
        assertNotNull(AppConfig.resolveDataDirectory());
        assertFalse(AppConfig.resolveDataDirectory().toString().isBlank());
    }
}
