package com.bupt.ta.web.servlet;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.User;
import com.bupt.ta.persistence.json.JsonUserRepository;
import com.bupt.ta.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet  {
    @Override
    public void init() throws ServletException {

    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/portal/dashboard.jsp").forward(req, resp);
    }
}
