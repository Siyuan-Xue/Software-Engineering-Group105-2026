package com.bupt.ta.web.security;

import com.bupt.ta.db.core.JsonMapperFactory;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.EntityType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Server-side gate for AI endpoints: a browser-side modal is not enough, so
 * every AI request must carry an explicit consent marker and leave a small
 * audit trace without storing prompt or resume content.
 */
public final class AiRequestGuard {
    public static final String CONSENT_PARAM = "aiConsent";
    public static final String CONSENT_HEADER = "X-AI-Consent";

    private static final ObjectMapper MAPPER = JsonMapperFactory.create();

    private AiRequestGuard() {
    }

    public static boolean requireConsent(HttpServletRequest req, HttpServletResponse resp, ObjectMapper mapper)
            throws IOException {
        if (hasConsent(req)) {
            return true;
        }
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        ObjectNode json = mapper.createObjectNode();
        json.put("ok", false);
        json.put("error", "AI consent is required before personal data is sent to the model.");
        mapper.writeValue(resp.getWriter(), json);
        return false;
    }

    public static boolean hasConsent(HttpServletRequest req) {
        return isAccepted(req.getParameter(CONSENT_PARAM)) || isAccepted(req.getHeader(CONSENT_HEADER));
    }

    public static void appendAudit(TaDatabase database, User operator, String feature, UUID targetId) {
        if (database == null || operator == null) {
            return;
        }
        try {
            AuditLog log = new AuditLog();
            log.setOperatorId(operator.getId());
            log.setAction(AuditAction.AI_REQUEST);
            log.setEntityType(EntityType.AI_REQUEST);
            log.setEntityId(targetId != null ? targetId : operator.getId());
            ObjectNode value = MAPPER.createObjectNode();
            value.put("feature", feature);
            value.put("consent", true);
            value.put("personalDataSentToModel", true);
            log.setNewValue(value);
            log.setOperatedAt(Instant.now());
            database.auditLogs().append(log);
        } catch (RuntimeException ignored) {
            // AI functionality should not fail solely because audit persistence is unavailable.
        }
    }

    private static boolean isAccepted(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.equals("true")
                || normalized.equals("1")
                || normalized.equals("yes")
                || normalized.equals("accepted");
    }
}
