package com.bupt.ta.web.servlet;

import com.bupt.ta.db.core.JsonMapperFactory;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.util.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@WebServlet("/admin/users")
public class AdminUsersServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/admin_users.jsp";

    private TaDatabase database;
    private final ObjectMapper mapper = JsonMapperFactory.create();

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireCurrentUser(req);
        if (!isAdmin(currentUser)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }
        req.setAttribute("users", database.users().findAll());
        req.setAttribute("userRoles", UserRole.values());
        req.setAttribute("currentUserId", currentUser.getId());
        req.setAttribute("successMessage", normalize(req.getParameter("successMessage")));
        req.setAttribute("errorMessage", normalize(req.getParameter("errorMessage")));
        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = requireCurrentUser(req);
        if (!isAdmin(currentUser)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        String action = normalize(req.getParameter("action"));
        try {
            switch (action == null ? "" : action) {
                case "create" -> createUser(req, currentUser);
                case "update" -> updateUser(req, currentUser);
                case "activate" -> setActive(req, currentUser, true);
                case "deactivate" -> setActive(req, currentUser, false);
                default -> throw new IllegalArgumentException(message(req, "Unsupported user action.", "不支持的用户操作。"));
            }
            redirect(req, resp, "successMessage", message(req, "User changes saved.", "用户变更已保存。"));
        } catch (RuntimeException ex) {
            String fallback = message(req, "Unable to save user changes.", "无法保存用户变更。");
            redirect(req, resp, "errorMessage", ex.getMessage() == null ? fallback : ex.getMessage());
        }
    }

    private void createUser(HttpServletRequest req, User operator) {
        String password = require(req.getParameter("password"),
                message(req, "Initial password is required.", "初始密码不能为空。"));
        if (password.length() < 6) {
            throw new IllegalArgumentException(message(req, "Password must be at least 6 characters.", "密码至少需要 6 位。"));
        }

        User user = new User();
        applyEditableFields(req, user, true);
        user.setActive(true);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        User saved = database.users().save(user);
        appendAudit(operator.getId(), AuditAction.CREATE, saved.getId(), null, saved);
    }

    private void updateUser(HttpServletRequest req, User operator) {
        UUID userId = parseUuid(req.getParameter("userId"));
        User current = database.users().findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(message(req, "User not found.", "用户不存在。")));
        User updated = mapper.convertValue(current, User.class);

        applyEditableFields(req, updated, !operator.getId().equals(userId));
        String password = normalize(req.getParameter("password"));
        if (password != null) {
            if (password.length() < 6) {
                throw new IllegalArgumentException(message(req, "Password must be at least 6 characters.", "密码至少需要 6 位。"));
            }
            updated.setPasswordHash(PasswordUtil.hashPassword(password));
        }
        if (current.getRole() == UserRole.ADMIN && updated.getRole() != UserRole.ADMIN) {
            ensureAnotherActiveAdmin(current.getId(), req);
        }

        User saved = database.users().save(updated);
        appendAudit(operator.getId(), AuditAction.UPDATE, saved.getId(), current, saved);
    }

    private void setActive(HttpServletRequest req, User operator, boolean active) {
        UUID userId = parseUuid(req.getParameter("userId"));
        if (operator.getId().equals(userId) && !active) {
            throw new IllegalArgumentException(message(req, "You cannot deactivate your own account.", "不能停用自己的账号。"));
        }
        User current = database.users().findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(message(req, "User not found.", "用户不存在。")));
        if (current.getRole() == UserRole.ADMIN && !active) {
            throw new IllegalArgumentException(message(req, "Admin accounts cannot be deactivated here.", "此处不能停用管理员账号。"));
        }

        User updated = mapper.convertValue(current, User.class);
        updated.setActive(active);
        User saved = database.users().save(updated);
        appendAudit(operator.getId(), AuditAction.STATUS_CHANGE, saved.getId(), current, saved);
    }

    private void applyEditableFields(HttpServletRequest req, User user, boolean allowRoleChange) {
        user.setEmail(require(req.getParameter("email"), message(req, "Email is required.", "邮箱不能为空。")));
        user.setFullName(require(req.getParameter("fullName"), message(req, "Full name is required.", "姓名不能为空。")));
        if (allowRoleChange) {
            user.setRole(parseRole(req.getParameter("role")));
        }
        user.setPhone(normalize(req.getParameter("phone")));
        user.setDepartment(normalize(req.getParameter("department")));
        user.setStudentId(normalize(req.getParameter("studentId")));
    }

    private void ensureAnotherActiveAdmin(UUID excludedUserId, HttpServletRequest req) {
        long admins = database.users().findAll().stream()
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .filter(User::isActive)
                .filter(user -> !excludedUserId.equals(user.getId()))
                .count();
        if (admins == 0) {
            throw new IllegalArgumentException(message(req,
                    "At least one active admin must remain.",
                    "系统至少需要保留一个活跃管理员。"));
        }
    }

    private UserRole parseRole(String raw) {
        String value = require(raw, "Role is required.");
        try {
            return UserRole.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Role is invalid.");
        }
    }

    private UUID parseUuid(String raw) {
        String value = require(raw, "User ID is required.");
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("User ID is invalid.");
        }
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

    private String require(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String key, String value) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/users?" + key + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private String message(HttpServletRequest req, String en, String zh) {
        return I18n.isChinese(I18n.resolveLanguage(req)) ? zh : en;
    }

    private void appendAudit(UUID operatorId, AuditAction action, UUID entityId, Object oldValue, Object newValue) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setEntityType(EntityType.USER);
        log.setEntityId(entityId);
        if (oldValue != null) {
            log.setOldValue(mapper.valueToTree(oldValue));
        }
        if (newValue != null) {
            log.setNewValue(mapper.valueToTree(newValue));
        }
        log.setOperatedAt(Instant.now());
        database.auditLogs().append(log);
    }
}
