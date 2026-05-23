package com.bupt.ta.web.servlet;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.service.TaAccountService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Self-service onboarding flow at {@code /register}.
 *
 * <p>{@link TaAccountService#registerUser} enforces uniqueness, password symmetry, and field constraints. Successful submissions
 * redirect prospective TAs back to login with translated flash messaging; violations repopulate the form via request attributes.</p>
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private TaAccountService accountService;

    /** Acquires persistence through {@link DatabaseProvider#get(jakarta.servlet.ServletContext)} for account mutations. */
    @Override
    public void init() throws ServletException {
        TaDatabase database = DatabaseProvider.get(getServletContext());
        this.accountService = new TaAccountService(database);
    }

    /** Renders {@code register.jsp}. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forwardForm(req, resp);
    }

    /** Persists a provisional TA applicant via {@link TaAccountService}. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String language = resolveLanguage(req);
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String fullName = req.getParameter("fullName");
        String phone = req.getParameter("phone");
        String department = req.getParameter("department");
        String studentId = req.getParameter("studentId");
        UserRole role = parseSelfRegistrationRole(req.getParameter("role"));

        try {
            accountService.registerUser(email, password, confirmPassword, fullName, phone, department, studentId, role);
            String ok = URLEncoder.encode(I18n.message(language, "auth.registerSuccess"), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/login?successMessage=" + ok);
        } catch (ConstraintViolationException e) {
            req.setAttribute("errorMessage", mapRegisterError(language, e.getMessage()));
            req.setAttribute("email", email != null ? email : "");
            req.setAttribute("fullName", fullName != null ? fullName : "");
            req.setAttribute("phone", phone != null ? phone : "");
            req.setAttribute("department", department != null ? department : "");
            req.setAttribute("studentId", studentId != null ? studentId : "");
            req.setAttribute("role", role.name());
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        }
    }

    private static UserRole parseSelfRegistrationRole(String raw) {
        if (raw == null || raw.isBlank()) {
            return UserRole.TA;
        }
        try {
            UserRole.valueOf(raw.trim().toUpperCase());
            return UserRole.TA;
        } catch (IllegalArgumentException ex) {
            return UserRole.TA;
        }
    }

    private void forwardForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    private static String resolveLanguage(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object language = session.getAttribute(I18n.SESSION_LANGUAGE_ATTR);
            if (language instanceof String value) {
                return I18n.normalizeLanguage(value);
            }
        }
        Object language = req.getAttribute("language");
        if (language instanceof String value) {
            return I18n.normalizeLanguage(value);
        }
        return I18n.DEFAULT_LANGUAGE;
    }

    private static String mapRegisterError(String language, String code) {
        if (code != null && code.startsWith("Duplicate email")) {
            return I18n.message(language, "auth.registerEmailTaken");
        }
        if (code == null) {
            return I18n.message(language, "msg.appSubmitFailed");
        }
        String key = switch (code) {
            case TaAccountService.ERR_EMAIL_TAKEN -> "auth.registerEmailTaken";
            case TaAccountService.ERR_PASSWORD_MISMATCH -> "msg.passwordMismatch";
            case TaAccountService.ERR_PASSWORD_TOO_SHORT -> "msg.passwordTooShort";
            case TaAccountService.ERR_EMAIL_REQUIRED -> "auth.registerEmailRequired";
            case TaAccountService.ERR_FULL_NAME_REQUIRED -> "msg.fullNameRequired";
            case TaAccountService.ERR_PASSWORD_REQUIRED -> "msg.newPasswordRequired";
            case TaAccountService.ERR_CONFIRM_REQUIRED -> "msg.confirmPasswordRequired";
            default -> "msg.appSubmitFailed";
        };
        return I18n.message(language, key);
    }
}
