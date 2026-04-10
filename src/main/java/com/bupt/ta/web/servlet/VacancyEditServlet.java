package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/vacancy/edit")
public class VacancyEditServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null || !"MO".equals(currentUser.getRole().name())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only MO can edit vacancies");
            return;
        }

        String vacancyId = req.getParameter("vacancyId");
        if (vacancyId == null || vacancyId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.vacancyIdRequired"), StandardCharsets.UTF_8));
            return;
        }

        // Mock implementation: just redirect back with success message
        resp.sendRedirect(req.getContextPath() + "/vacancy?vacancyId=" + vacancyId + "&successMessage="
                + URLEncoder.encode(I18n.message(req, "msg.vacancyUpdated"), StandardCharsets.UTF_8));
    }
}
