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
 * Mandatory server-side consent checks for servlet handlers that invoke external AI.
 *
 * <p>Browsers may show a disclaimer modal, but that is not authoritative. Requests must repeat
 * consent via {@value #CONSENT_PARAM} or {@value #CONSENT_HEADER} before personal data leaves
 * the server. Successful calls should record a concise audit row via {@link #appendAudit} without
 * logging raw prompts or document bodies.</p>
 */
public final class AiRequestGuard {

    /** Form/query parameter name conveying end-user consent. */
    public static final String CONSENT_PARAM = "aiConsent";

    /** HTTP header name conveying consent for non-form clients. */
    public static final String CONSENT_HEADER = "X-AI-Consent";

    private static final ObjectMapper MAPPER = JsonMapperFactory.create();

    private AiRequestGuard() {
    }

    /**
     * Validates consent and, when absent, terminates the exchange with HTTP 400 and JSON payload.
     *
     * @param req   incoming servlet request (may declare consent via parameter or header)
     * @param resp  response used only when consent is missing; status and entity type are set explicitly
     * @param mapper same mapper the servlet uses to serialise bodies (keeps naming consistent)
     * @return {@code true} when the caller should proceed with AI work; {@code false} when the
     *         response has already been committed with an error payload
     * @throws IOException if streaming JSON to the writer fails
     */
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

    /**
     * Non-destructive check for consent markers.
     *
     * @param req current HTTP servlet request
     * @return {@code true} if either the parameter ({@link #CONSENT_PARAM}) or header
     *         ({@link #CONSENT_HEADER}) carries an affirmative value recognised by {@link #isAccepted}
     */
    public static boolean hasConsent(HttpServletRequest req) {
        return isAccepted(req.getParameter(CONSENT_PARAM)) || isAccepted(req.getHeader(CONSENT_HEADER));
    }

    /**
     * Appends a tamper-evident audit log describing an AI invocation (metadata only).
     *
     * <p>Failures to persist are swallowed so degraded storage does not break user-visible AI flows.</p>
     *
     * @param database live facade; skipped when {@code null}
     * @param operator account performing the AI action; skipped when {@code null}
     * @param feature  short programmatic label ( servlet name / business operation )
     * @param targetId primary entity acted upon, or {@code null} to default to operator id
     */
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
            // Omit AI failure when audit persistence is offline; callers keep normal UX.
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
