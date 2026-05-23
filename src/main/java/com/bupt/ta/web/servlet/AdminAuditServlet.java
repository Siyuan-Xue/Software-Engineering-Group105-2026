package com.bupt.ta.web.servlet;

import com.bupt.ta.db.core.JsonMapperFactory;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.value.AuditLogQuery;
import com.bupt.ta.i18n.I18n;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Searchable administrator audit explorer mounted at {@code /admin/audit}.
 *
 * <p>Optional {@code export=csv} swaps the HTML dispatcher for streamed CSV payloads sized up to bounded windows while persisting supplementary audit breadcrumbs.</p>
 */
@WebServlet("/admin/audit")
public class AdminAuditServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/admin_audit.jsp";
    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private TaDatabase database;
    private final ObjectMapper mapper = JsonMapperFactory.create();

    /** Binds DAO helpers shared between tabular renders and exporter utilities. */
    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
    }

    /** Filters audit rows through {@link com.bupt.ta.domain.value.AuditLogQuery} or triggers CSV exporters. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireCurrentUser(req);
        if (!isAdmin(currentUser)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        AuditLogQuery query = buildQuery(req);
        if ("csv".equalsIgnoreCase(normalize(req.getParameter("export")))) {
            exportCsv(req, resp, currentUser, query);
            return;
        }

        List<AuditLog> logs = database.auditLogs().search(query);
        req.setAttribute("logs", logs.stream().map(this::toView).toList());
        req.setAttribute("auditActions", AuditAction.values());
        req.setAttribute("entityTypes", EntityType.values());
        req.setAttribute("users", database.users().findAll());
        req.setAttribute("successMessage", normalize(req.getParameter("successMessage")));
        req.setAttribute("errorMessage", normalize(req.getParameter("errorMessage")));
        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private AuditLogQuery buildQuery(HttpServletRequest req) {
        AuditLogQuery query = new AuditLogQuery();
        query.setSize(parseInt(req.getParameter("size"), 100));
        query.setOperatorId(parseUuidOrNull(req.getParameter("operatorId")));
        query.setAction(parseEnumOrNull(AuditAction.class, req.getParameter("action")));
        query.setEntityType(parseEnumOrNull(EntityType.class, req.getParameter("entityType")));
        query.setFrom(parseDateStart(req.getParameter("from")));
        query.setTo(parseDateEnd(req.getParameter("to")));
        return query;
    }

    private Map<String, Object> toView(AuditLog log) {
        User operator = log.getOperatorId() == null ? null : database.users().findById(log.getOperatorId()).orElse(null);
        return Map.of(
                "operatedAt", log.getOperatedAt() == null ? "" : DISPLAY_TIME.format(log.getOperatedAt()),
                "operator", operator == null ? "" : safe(operator.getFullName(), operator.getEmail()),
                "operatorId", log.getOperatorId() == null ? "" : log.getOperatorId().toString(),
                "action", log.getAction() == null ? "" : log.getAction().name(),
                "entityType", log.getEntityType() == null ? "" : log.getEntityType().name(),
                "entityId", log.getEntityId() == null ? "" : log.getEntityId().toString(),
                "oldValue", log.getOldValue() == null ? "" : log.getOldValue().toString(),
                "newValue", log.getNewValue() == null ? "" : log.getNewValue().toString()
        );
    }

    private void exportCsv(HttpServletRequest req, HttpServletResponse resp, User operator, AuditLogQuery query)
            throws IOException {
        query.setSize(10_000);
        appendExportAudit(operator, req.getQueryString());
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"audit_logs.csv\"");
        try (PrintWriter writer = resp.getWriter()) {
            writer.println("operated_at,operator_id,action,entity_type,entity_id,old_value,new_value");
            for (AuditLog log : database.auditLogs().search(query)) {
                writer.println(csv(log.getOperatedAt() == null ? "" : log.getOperatedAt().toString()) + ","
                        + csv(log.getOperatorId()) + ","
                        + csv(log.getAction()) + ","
                        + csv(log.getEntityType()) + ","
                        + csv(log.getEntityId()) + ","
                        + csv(log.getOldValue()) + ","
                        + csv(log.getNewValue()));
            }
        }
    }

    private void appendExportAudit(User operator, String queryString) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operator.getId());
        log.setAction(AuditAction.EXPORT);
        log.setEntityType(EntityType.AUDIT_LOG);
        log.setEntityId(operator.getId());
        log.setNewValue(mapper.valueToTree(queryString == null ? "" : queryString));
        log.setOperatedAt(Instant.now());
        database.auditLogs().append(log);
    }

    private User requireCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("currentUser");
        return value instanceof User user ? user : null;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    private UUID parseUuidOrNull(String raw) {
        String value = normalize(raw);
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private <T extends Enum<T>> T parseEnumOrNull(Class<T> enumType, String raw) {
        String value = normalize(raw);
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Instant parseDateStart(String raw) {
        String value = normalize(raw);
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant parseDateEnd(String raw) {
        String value = normalize(raw);
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value).plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String csv(Object value) {
        String text = value == null ? "" : value.toString();
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
