package com.bupt.ta.web.servlet;

import com.bupt.ta.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/vacancy/create")
public class VacancyCreateServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null || !"MO".equals(currentUser.getRole().name())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only MO can create vacancies");
            return;
        }

        // Mock implementation: just redirect back with success message
        // In a real implementation, we would parse parameters and save to database
        
        String title = req.getParameter("title");
        if (title == null || title.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage=Title is required");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/vacancies?successMessage=Vacancy created successfully");
    }
}
