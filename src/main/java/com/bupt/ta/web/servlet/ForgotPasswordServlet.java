package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Password recovery request page. The public form never changes credentials;
 * administrators reset passwords from the user-management screen.
 */
@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/forgot-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String language = resolveLanguage(req);
        String email = req.getParameter("email");
        req.setAttribute("successMessage", I18n.message(language, "auth.passwordResetRequestReceived"));
        req.setAttribute("email", email != null ? email : "");
        req.getRequestDispatcher("/forgot-password.jsp").forward(req, resp);
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

}
