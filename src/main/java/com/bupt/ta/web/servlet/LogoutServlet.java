package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
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
 * Safe sign-out endpoint mapped to {@code /logout}.
 *
 * <p>Only {@code POST} mutates server state: the previous session is discarded, lightweight {@link I18n}
 * preference keys are copied into a fresh session, and the browser is redirected to {@code /login} with an encoded logout confirmation.</p>
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    /** Rejects stateless GET probes in favour of an explicit POST sign-out handshake. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Use POST to sign out.");
    }

    /** Invalidates the active session while cloning language or appearance selections into the replacement session. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String language = I18n.DEFAULT_LANGUAGE;
        String appearance = I18n.DEFAULT_APPEARANCE;
        if (session != null) {
            Object value = session.getAttribute(I18n.SESSION_LANGUAGE_ATTR);
            if (value instanceof String savedLanguage) {
                language = I18n.normalizeLanguage(savedLanguage);
            }
            Object appearanceValue = session.getAttribute(I18n.SESSION_APPEARANCE_ATTR);
            if (appearanceValue instanceof String savedAppearance) {
                appearance = I18n.normalizeAppearance(savedAppearance);
            }
        }
                
        if (session != null) {
            session.invalidate(); 
        }

        HttpSession newSession = req.getSession(true);
        newSession.setAttribute(I18n.SESSION_LANGUAGE_ATTR, language);
        newSession.setAttribute(I18n.SESSION_APPEARANCE_ATTR, appearance);

        String contextPath = req.getContextPath();
        String successMsg = URLEncoder.encode(I18n.message(language, "auth.loggedOut"), StandardCharsets.UTF_8);
        resp.sendRedirect(contextPath + "/login?successMessage=" + successMsg);
    }
}
