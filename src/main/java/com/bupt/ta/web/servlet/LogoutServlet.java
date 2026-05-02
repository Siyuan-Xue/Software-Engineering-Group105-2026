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

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 1. 获取当前会话（传入 false 表示如果当前没有会话，则不创建新会话）
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
                
        // 2. 如果会话存在，则将其作废并清除所有绑定的信息
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
