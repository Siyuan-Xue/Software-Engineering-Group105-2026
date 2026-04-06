package com.bupt.ta.web.servlet;

import com.bupt.ta.config.DatabaseConfig; // 导入你的配置类
import com.bupt.ta.model.User;
import com.bupt.ta.persistence.json.JsonUserRepository; // 导入你提供的真实类
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
        // 1. 初始化数据库配置 (注意：这里需要根据你实际的 DatabaseConfig 构造方式来写)
        // 如果你的 DatabaseConfig 有无参构造，直接 new；如果需要传路径，就传具体的路径
        DatabaseConfig config = DatabaseConfig.defaultConfig(); 
        
        // 2. 组装 Repository 和 Service
        JsonUserRepository userRepository = new JsonUserRepository(config);
        this.authService = new AuthService(userRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String errorMessage = req.getParameter("errorMessage");
        if (errorMessage != null && !errorMessage.isEmpty()) {
            req.setAttribute("errorMessage", errorMessage);
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

        // 调用刚才写的真实 AuthService 进行验证
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