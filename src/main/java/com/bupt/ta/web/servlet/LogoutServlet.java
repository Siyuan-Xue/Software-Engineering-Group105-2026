package com.bupt.ta.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

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
                
        // 2. 如果会话存在，则将其作废并清除所有绑定的信息
        if (session != null) {
            session.invalidate(); 
        }

        String contextPath = req.getContextPath();
        String successMsg = "Success to logout";



        resp.sendRedirect(contextPath + "/login?successMessage=" + successMsg);
    }
}
