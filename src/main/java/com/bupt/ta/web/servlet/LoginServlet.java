package com.bupt.ta.web.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private AuthService authService;

    @Override
    public void init() throws ServletException {
        // Use the database initialized by AppContextListener
        com.bupt.ta.persistence.TaDatabase database = com.bupt.ta.persistence.DatabaseProvider.get(getServletContext());
        this.authService = new AuthService(database.users());
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

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("errorMessage", "Email and password are required.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }


        Optional<User> userOpt = authService.authenticate(email, password);

        if (userOpt.isPresent()) {
            // 登录成功！
            User realUser = userOpt.get();
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", realUser);
            // 重定向到后台控制台
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            // 登录失败
            req.setAttribute("errorMessage", "Invalid email or password. Please try again.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}