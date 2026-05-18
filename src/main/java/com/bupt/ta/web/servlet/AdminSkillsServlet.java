package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.core.JsonMapperFactory;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.domain.entity.ResumeSkill;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.SkillCategory;
import com.bupt.ta.domain.enums.UserRole;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/admin/skills")
public class AdminSkillsServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/admin_skills.jsp";

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

        req.setAttribute("skillCategories", SkillCategory.values());
        try {
            req.setAttribute("skills", buildSkillViews());
            req.setAttribute("successMessage", normalize(req.getParameter("successMessage")));
            req.setAttribute("errorMessage", normalize(req.getParameter("errorMessage")));
        } catch (Exception ex) {
            getServletContext().log("Failed to load admin skills", ex);
            req.setAttribute("pageState", "loadError");
            req.setAttribute("skills", List.of());
            req.setAttribute("successMessage", null);
            req.setAttribute("errorMessage", I18n.message(req, "msg.adminSkillsLoadFailed"));
        }

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
            if ("delete".equals(action)) {
                deleteSkill(req, currentUser);
                redirect(req, resp, "successMessage", message(req, "Skill deleted.", "技能已删除。"));
            } else if ("update".equals(action)) {
                updateSkill(req, currentUser);
                redirect(req, resp, "successMessage", message(req, "Skill updated.", "技能已更新。"));
            } else if ("activate".equals(action) || "deactivate".equals(action)) {
                setSkillActive(req, currentUser, "activate".equals(action));
                redirect(req, resp, "successMessage", message(req, "Skill status updated.", "技能状态已更新。"));
            } else {
                createSkill(req, currentUser);
                redirect(req, resp, "successMessage", message(req, "Skill created.", "技能已创建。"));
            }
        } catch (Exception ex) {
            String fallback = message(req, "Unable to save skill changes.", "无法保存技能变更。");
            redirect(req, resp, "errorMessage", ex.getMessage() == null ? fallback : ex.getMessage());
        }
    }

    private void createSkill(HttpServletRequest req, User operator) {
        String name = require(req.getParameter("name"), message(req, "Skill name is required.", "技能名称不能为空。"));
        SkillCategory category = parseCategory(req.getParameter("category"));
        String description = normalize(req.getParameter("description"));

        Skill skill = new Skill();
        skill.setName(name);
        skill.setCategory(category);
        skill.setDescription(description);
        Skill saved = database.skills().save(skill);
        appendAudit(operator.getId(), AuditAction.CREATE, saved.getId(), null, saved);
    }

    private void updateSkill(HttpServletRequest req, User operator) {
        UUID skillId = parseUuid(req.getParameter("skillId"));
        Skill current = database.skills().findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException(message(req, "Skill not found.", "技能不存在。")));

        Skill updated = mapper.convertValue(current, Skill.class);
        updated.setName(require(req.getParameter("name"), message(req, "Skill name is required.", "技能名称不能为空。")));
        updated.setCategory(parseCategory(req.getParameter("category")));
        updated.setDescription(normalize(req.getParameter("description")));

        Skill saved = database.skills().save(updated);
        appendAudit(operator.getId(), AuditAction.UPDATE, saved.getId(), current, saved);
    }

    private void setSkillActive(HttpServletRequest req, User operator, boolean active) {
        UUID skillId = parseUuid(req.getParameter("skillId"));
        Skill current = database.skills().findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException(message(req, "Skill not found.", "技能不存在。")));
        Skill updated = mapper.convertValue(current, Skill.class);
        updated.setActive(active);
        Skill saved = database.skills().save(updated);
        appendAudit(operator.getId(), AuditAction.STATUS_CHANGE, saved.getId(), current, saved);
    }

    private void deleteSkill(HttpServletRequest req, User operator) {
        UUID skillId = parseUuid(req.getParameter("skillId"));
        Skill current = database.skills().findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException(message(req, "Skill not found.", "技能不存在。")));

        long resumeUseCount = database.resumeSkills().findAll().stream()
                .filter(resumeSkill -> skillId.equals(resumeSkill.getSkillId()))
                .count();
        long requirementUseCount = database.jobRequirements().findAll().stream()
                .filter(requirement -> skillId.equals(requirement.getSkillId()))
                .count();
        if (resumeUseCount + requirementUseCount > 0) {
            throw new IllegalArgumentException(message(req,
                    "This skill is in use. Remove resume skills and job requirements before deleting it.",
                    "该技能仍被简历或岗位要求引用，请先移除引用后再删除。"));
        }

        database.executeAtomically(() -> {
            database.skills().delete(skillId);
            appendAudit(operator.getId(), AuditAction.DELETE, skillId, current, null);
        });
    }

    private List<Map<String, Object>> buildSkillViews() {
        List<ResumeSkill> resumeSkills = database.resumeSkills().findAll();
        List<JobRequirement> requirements = database.jobRequirements().findAll();
        return database.skills().findAll().stream()
                .sorted(Comparator.comparing(Skill::getName, String.CASE_INSENSITIVE_ORDER))
                .map(skill -> Map.<String, Object>of(
                        "id", skill.getId(),
                        "name", safe(skill.getName(), "Untitled skill"),
                        "category", skill.getCategory() == null ? "" : skill.getCategory().name(),
                        "description", safe(skill.getDescription(), ""),
                        "active", skill.isActive(),
                        "resumeUseCount", resumeSkills.stream()
                                .filter(item -> skill.getId().equals(item.getSkillId()))
                                .count(),
                        "requirementUseCount", requirements.stream()
                                .filter(item -> skill.getId().equals(item.getSkillId()))
                                .count()
                ))
                .toList();
    }

    private SkillCategory parseCategory(String raw) {
        String value = require(raw, "Skill category is required.");
        try {
            return SkillCategory.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Skill category is invalid.");
        }
    }

    private UUID parseUuid(String raw) {
        String value = require(raw, "Skill ID is required.");
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Skill ID is invalid.");
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

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String key, String value) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/skills?" + key + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8));
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

    private String message(HttpServletRequest req, String en, String zh) {
        return "zh".equals(req.getAttribute("language")) ? zh : en;
    }

    private void appendAudit(UUID operatorId, AuditAction action, UUID entityId, Object oldValue, Object newValue) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setEntityType(EntityType.SKILL);
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
