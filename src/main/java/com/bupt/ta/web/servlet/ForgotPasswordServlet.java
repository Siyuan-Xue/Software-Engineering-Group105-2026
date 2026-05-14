package com.bupt.ta.web.servlet;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
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

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private TaAccountService accountService;

    @Override
    public void init() throws ServletException {
        TaDatabase database = DatabaseProvider.get(getServletContext());
        this.accountService = new TaAccountService(database);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/forgot-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String language = resolveLanguage(req);
        String email = req.getParameter("email");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            boolean updated = accountService.resetPasswordForTa(email, newPassword, confirmPassword);
            if (updated) {
                String ok = URLEncoder.encode(I18n.message(language, "auth.taResetSuccess"), StandardCharsets.UTF_8);
                resp.sendRedirect(req.getContextPath() + "/login?successMessage=" + ok);
            } else {
                req.setAttribute("errorMessage", I18n.message(language, "auth.taResetFailed"));
                req.setAttribute("email", email != null ? email : "");
                req.getRequestDispatcher("/forgot-password.jsp").forward(req, resp);
            }
        } catch (ConstraintViolationException e) {
            req.setAttribute("errorMessage", mapTaAccountError(language, e.getMessage()));
            req.setAttribute("email", email != null ? email : "");
            req.getRequestDispatcher("/forgot-password.jsp").forward(req, resp);
        }
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

    private static String mapTaAccountError(String language, String code) {
        if (code == null) {
            return I18n.message(language, "auth.taResetFailed");
        }
        String key = switch (code) {
            case TaAccountService.ERR_PASSWORD_MISMATCH -> "msg.passwordMismatch";
            case TaAccountService.ERR_PASSWORD_TOO_SHORT -> "msg.passwordTooShort";
            case TaAccountService.ERR_EMAIL_REQUIRED -> "auth.registerEmailRequired";
            case TaAccountService.ERR_PASSWORD_REQUIRED -> "msg.newPasswordRequired";
            case TaAccountService.ERR_CONFIRM_REQUIRED -> "msg.confirmPasswordRequired";
            default -> "auth.taResetFailed";
        };
        return I18n.message(language, key);
    }
}
