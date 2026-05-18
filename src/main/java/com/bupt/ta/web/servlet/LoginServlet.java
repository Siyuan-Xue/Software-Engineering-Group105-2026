package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private AuthService authService;
    private TaDatabase database;

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.authService = new AuthService(database);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String errorMessage = req.getParameter("errorMessage");
        if (errorMessage != null && !errorMessage.isEmpty()) {
            req.setAttribute("errorMessage", errorMessage);
        }
        String successMessage = req.getParameter("successMessage");
        if (successMessage != null && !successMessage.isEmpty()) {
            req.setAttribute("successMessage", successMessage);
        }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String language = resolveLanguage(req);

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("errorMessage", I18n.message(language, "auth.emailPasswordRequired"));
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }


        Optional<User> userOpt = authService.authenticate(email, password);

        if (userOpt.isPresent()) {
            // 登录成功！
            User realUser = userOpt.get();
            appendLoginAudit(realUser, true);
            HttpSession session = req.getSession(true);
            // session 身份供 AuthFilter 注入 request、后续页面与 Servlet 共用
            session.setAttribute("currentUser", realUser);
            session.setAttribute(I18n.SESSION_LANGUAGE_ATTR, I18n.normalizeLanguage(realUser.getPreferredLanguage()));
            session.setAttribute(I18n.SESSION_APPEARANCE_ATTR, I18n.normalizeAppearance(realUser.getPreferredAppearance()));
            // 重定向到后台控制台
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            // 登录失败
            Optional<User> foundUser = authService.findByEmail(email);
            foundUser.ifPresent(user -> appendLoginAudit(user, false));
            if (foundUser.isPresent() && !foundUser.get().isActive()) {
                req.setAttribute("errorMessage", "Account inactive. Please contact an administrator.");
            } else {
                req.setAttribute("errorMessage", I18n.message(language, "auth.invalidCredentials"));
            }
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }

    private String resolveLanguage(HttpServletRequest req) {
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

    private void appendLoginAudit(User user, boolean success) {
        try {
            AuditLog log = new AuditLog();
            log.setOperatorId(user.getId());
            log.setAction(AuditAction.LOGIN);
            log.setEntityType(EntityType.USER);
            log.setEntityId(user.getId());
            log.setNewValue(com.bupt.ta.db.core.JsonMapperFactory.create()
                    .valueToTree(success ? "SUCCESS" : "FAILURE"));
            log.setOperatedAt(Instant.now());
            database.auditLogs().append(log);
        } catch (RuntimeException ex) {
            getServletContext().log("Unable to append login audit", ex);
        }
    }
}
