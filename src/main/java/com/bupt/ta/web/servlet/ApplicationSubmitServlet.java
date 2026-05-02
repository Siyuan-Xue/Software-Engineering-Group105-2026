package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.service.ApplicationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@WebServlet("/application")
public class ApplicationSubmitServlet extends HttpServlet {
    private ApplicationService applicationService;

    @Override
    public void init() throws ServletException {
        TaDatabase database = DatabaseProvider.get(getServletContext());
        this.applicationService = new ApplicationService(database);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "auth.loginRequired"), StandardCharsets.UTF_8));
            return;
        }

        String vacancyIdStr = req.getParameter("vacancyId");
        String resumeIdStr = req.getParameter("resumeId");
        if (vacancyIdStr == null || vacancyIdStr.isBlank() || resumeIdStr == null || resumeIdStr.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/vacancy?vacancyId=" + encodeSafe(vacancyIdStr)
                    + "&errorMessage=" + URLEncoder.encode(I18n.message(req, "msg.appDataMissing"), StandardCharsets.UTF_8));
            return;
        }

        try {
            applicationService.submit(
                    currentUser.getId(),
                    UUID.fromString(resumeIdStr.trim()),
                    UUID.fromString(vacancyIdStr.trim()),
                    req.getParameter("coverLetter"));
            resp.sendRedirect(req.getContextPath() + "/applications?successMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.appSubmitted"), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException ex) {
            resp.sendRedirect(req.getContextPath() + "/vacancy?vacancyId=" + encodeSafe(vacancyIdStr)
                    + "&errorMessage=" + URLEncoder.encode(I18n.message(req, "msg.appInvalidIds"), StandardCharsets.UTF_8));
        } catch (RuntimeException ex) {
            resp.sendRedirect(req.getContextPath() + "/vacancy?vacancyId=" + encodeSafe(vacancyIdStr)
                    + "&errorMessage=" + URLEncoder.encode(
                    ex.getMessage() == null ? I18n.message(req, "msg.appSubmitFailed") : ex.getMessage(),
                    StandardCharsets.UTF_8));
        }
    }

    private String encodeSafe(String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
