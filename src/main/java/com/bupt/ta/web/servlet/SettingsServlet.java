package com.bupt.ta.web.servlet;

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
        } else {
            handleUpdateProfile(req, resp, currentUser);
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws IOException {
        try {
            String fullName  = require(req.getParameter("fullName"), "Full name is required.");
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
            req.getSession(true).setAttribute("currentUser", saved);

            redirect(resp, req, "updateSuccess", "successMessage", "Profile updated successfully.");
        } catch (RuntimeException ex) {
            redirect(resp, req, "updateFailure", "errorMessage",
                    ex.getMessage() != null ? ex.getMessage() : "Failed to save profile.");
        }
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws IOException {
        try {
            String currentPassword = require(req.getParameter("currentPassword"), "Current password is required.");
            String newPassword     = require(req.getParameter("newPassword"), "New password is required.");
            String confirmPassword = require(req.getParameter("confirmPassword"), "Please confirm your new password.");

            if (!PasswordUtil.checkPassword(currentPassword, currentUser.getPasswordHash())) {
                redirect(resp, req, "pwdFailure", "errorMessage", "Current password is incorrect.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                redirect(resp, req, "pwdFailure", "errorMessage", "New passwords do not match.");
                return;
            }
            if (newPassword.length() < 8) {
                redirect(resp, req, "pwdFailure", "errorMessage", "New password must be at least 8 characters.");
                return;
            }

            currentUser.setPasswordHash(PasswordUtil.hashPassword(newPassword));
            User saved = database.users().save(currentUser);
            req.getSession(true).setAttribute("currentUser", saved);

            redirect(resp, req, "pwdSuccess", "successMessage", "Password changed successfully.");
        } catch (RuntimeException ex) {
            redirect(resp, req, "pwdFailure", "errorMessage",
                    ex.getMessage() != null ? ex.getMessage() : "Failed to change password.");
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
}
