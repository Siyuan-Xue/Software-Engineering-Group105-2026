package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.model.User;
import com.bupt.ta.persistence.DatabaseProvider;
import com.bupt.ta.persistence.TaDatabase;
import com.bupt.ta.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/settings")
public class SettingsServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/settings.jsp";

    private TaDatabase database;

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
    }

    // ── GET ──────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String state = normalize(req.getParameter("state"));
        req.setAttribute("pageState", state == null ? "normal" : state);
        req.setAttribute("successMessage", normalize(req.getParameter("successMessage")));
        req.setAttribute("errorMessage", normalize(req.getParameter("errorMessage")));
        req.setAttribute("userProfile", buildUserProfile(currentUser));
        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = requireCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = normalize(req.getParameter("action"));
        if ("changePassword".equals(action)) {
            handleChangePassword(req, resp, currentUser);
        } else if ("updatePreferences".equals(action)) {
            handleUpdatePreferences(req, resp, currentUser);
        } else {
            handleUpdateProfile(req, resp, currentUser);
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws IOException {
        String language = resolveLanguage(req, currentUser);
        try {
            String fullName  = require(req.getParameter("fullName"), I18n.message(language, "msg.fullNameRequired"));
            String phone     = normalize(req.getParameter("phone"));
            String department = normalize(req.getParameter("department"));
            String studentId = normalize(req.getParameter("studentId"));
            String bio       = normalize(req.getParameter("bio"));
            boolean notificationsEnabled = "on".equalsIgnoreCase(req.getParameter("notificationsEnabled"));

            currentUser.setFullName(fullName);
            currentUser.setPhone(phone);
            currentUser.setDepartment(department);
            currentUser.setStudentId(studentId);
            currentUser.setBio(bio);
            currentUser.setNotificationsEnabled(notificationsEnabled);

            User saved = database.users().save(currentUser);
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", saved);
            session.setAttribute(I18n.SESSION_LANGUAGE_ATTR, language);
            session.setAttribute(I18n.SESSION_APPEARANCE_ATTR, I18n.normalizeAppearance(saved.getPreferredAppearance()));

            redirect(resp, req, "updateSuccess", "successMessage", I18n.message(language, "msg.profileUpdated"));
        } catch (RuntimeException ex) {
            redirect(resp, req, "updateFailure", "errorMessage",
                    ex.getMessage() != null ? ex.getMessage() : I18n.message(language, "msg.profileSaveFailed"));
        }
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws IOException {
        String language = resolveLanguage(req, currentUser);
        try {
            String currentPassword = require(req.getParameter("currentPassword"), I18n.message(language, "msg.currentPasswordRequired"));
            String newPassword     = require(req.getParameter("newPassword"), I18n.message(language, "msg.newPasswordRequired"));
            String confirmPassword = require(req.getParameter("confirmPassword"), I18n.message(language, "msg.confirmPasswordRequired"));

            if (!PasswordUtil.checkPassword(currentPassword, currentUser.getPasswordHash())) {
                redirect(resp, req, "pwdFailure", "errorMessage", I18n.message(language, "msg.currentPasswordIncorrect"));
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                redirect(resp, req, "pwdFailure", "errorMessage", I18n.message(language, "msg.passwordMismatch"));
                return;
            }
            if (newPassword.length() < 8) {
                redirect(resp, req, "pwdFailure", "errorMessage", I18n.message(language, "msg.passwordTooShort"));
                return;
            }

            currentUser.setPasswordHash(PasswordUtil.hashPassword(newPassword));
            User saved = database.users().save(currentUser);
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", saved);
            session.setAttribute(I18n.SESSION_LANGUAGE_ATTR, language);
            session.setAttribute(I18n.SESSION_APPEARANCE_ATTR, I18n.normalizeAppearance(saved.getPreferredAppearance()));

            redirect(resp, req, "pwdSuccess", "successMessage", I18n.message(language, "msg.passwordChanged"));
        } catch (RuntimeException ex) {
            redirect(resp, req, "pwdFailure", "errorMessage",
                    ex.getMessage() != null ? ex.getMessage() : I18n.message(language, "msg.passwordChangeFailed"));
        }
    }

    private void handleUpdatePreferences(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws IOException {
        String preferredLanguage = normalize(req.getParameter("preferredLanguage"));
        String preferredAppearance = normalize(req.getParameter("preferredAppearance"));
        if (preferredLanguage == null) {
            String language = resolveLanguage(req, currentUser);
            redirect(resp, req, "prefFailure", "errorMessage", I18n.message(language, "msg.languageRequired"));
            return;
        }
        if (preferredAppearance == null) {
            String language = resolveLanguage(req, currentUser);
            redirect(resp, req, "prefFailure", "errorMessage", I18n.message(language, "msg.appearanceRequired"));
            return;
        }

        String language = I18n.normalizeLanguage(preferredLanguage);
        String appearance = I18n.normalizeAppearance(preferredAppearance);
        try {
            currentUser.setPreferredLanguage(language);
            currentUser.setPreferredAppearance(appearance);
            User saved = database.users().save(currentUser);
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", saved);
            session.setAttribute(I18n.SESSION_LANGUAGE_ATTR, language);
            session.setAttribute(I18n.SESSION_APPEARANCE_ATTR, appearance);
            redirect(resp, req, "prefSuccess", "successMessage", I18n.message(language, "msg.preferencesUpdated"));
        } catch (RuntimeException ex) {
            redirect(resp, req, "prefFailure", "errorMessage",
                    ex.getMessage() != null ? ex.getMessage() : I18n.message(language, "msg.preferencesUpdateFailed"));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> buildUserProfile(User user) {
        Map<String, Object> profile = new HashMap<>();
        String fullName = safe(user.getFullName(), "Student User");
        String[] parts  = fullName.split("\\s+", 2);

        profile.put("firstName", parts[0]);
        profile.put("lastName", parts.length > 1 ? parts[1] : "");
        profile.put("fullName", fullName);
        profile.put("email", user.getEmail());
        profile.put("phone", safe(user.getPhone(), ""));
        profile.put("department", safe(user.getDepartment(), ""));
        profile.put("studentId", safe(user.getStudentId(), ""));
        profile.put("bio", safe(user.getBio(), ""));
        profile.put("notificationsEnabled", user.isNotificationsEnabled());
        profile.put("preferredLanguage", I18n.normalizeLanguage(user.getPreferredLanguage()));
        profile.put("preferredAppearance", I18n.normalizeAppearance(user.getPreferredAppearance()));
        return profile;
    }

    private void redirect(HttpServletResponse resp, HttpServletRequest req,
                          String state, String msgKey, String msg) throws IOException {
        String enc = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/settings?state=" + state + "&" + msgKey + "=" + enc);
    }

    private User requireCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute("currentUser");
        return value instanceof User u ? u : null;
    }

    private String require(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) throw new IllegalArgumentException(message);
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String resolveLanguage(HttpServletRequest req, User currentUser) {
        if (currentUser != null && currentUser.getPreferredLanguage() != null) {
            return I18n.normalizeLanguage(currentUser.getPreferredLanguage());
        }
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object language = session.getAttribute(I18n.SESSION_LANGUAGE_ATTR);
            if (language instanceof String value) {
                return I18n.normalizeLanguage(value);
            }
        }
        return I18n.DEFAULT_LANGUAGE;
    }
}
