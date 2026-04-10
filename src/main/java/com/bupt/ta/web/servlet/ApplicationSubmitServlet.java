package com.bupt.ta.web.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.ApplicationStatus;
import com.bupt.ta.persistence.DatabaseProvider;
import com.bupt.ta.persistence.TaDatabase;
import com.bupt.ta.service.ApplicationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;

@WebServlet("/application")
public class ApplicationSubmitServlet extends HttpServlet {

    private ApplicationService applicationService;

    @Override
    public void init() throws ServletException {
        TaDatabase database = DatabaseProvider.get(getServletContext());
        this.applicationService = new ApplicationService(database.applications());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 鉴权：确认用户已登录
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?errorMessage=Please log in to access this page");
            return;
        }

        // 2. 获取并校验参数
        String vacancyIdStr = req.getParameter("vacancyId");
        String resumeIdStr = req.getParameter("resumeId");

        if (vacancyIdStr == null || vacancyIdStr.isEmpty() || resumeIdStr == null || resumeIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/vacancy?vacancyId=" + vacancyIdStr 
                    + "&errorMessage=Missing required application data.");
            return;
        }

        try {
            UUID vacancyId = UUID.fromString(vacancyIdStr);
            UUID resumeId = UUID.fromString(resumeIdStr);

            // 3. 构建申请对象并保存
            Application newApplication = new Application();
            newApplication.setJobId(vacancyId);
            newApplication.setResumeId(resumeId);
            newApplication.setStatus(ApplicationStatus.PENDING);
            // newApplication.setId() 以及 createdAt 会由 BaseEntity 或底层持久化机制自动处理
            
            applicationService.save(newApplication);

            // 4. 成功后重定向到申请列表页并带上 successMessage (符合 Contract 要求)
            resp.sendRedirect(req.getContextPath() + "/applications?successMessage=Application submitted successfully!");

        } catch (IllegalArgumentException e) {
            // UUID 格式错误处理
            resp.sendRedirect(req.getContextPath() + "/vacancy?vacancyId=" + vacancyIdStr 
                    + "&errorMessage=Invalid vacancy or resume ID.");
        } catch (Exception e) {
            // 其他后端异常处理
            resp.sendRedirect(req.getContextPath() + "/vacancy?vacancyId=" + vacancyIdStr 
                    + "&errorMessage=Failed to submit application. Please try again later.");
        }
    }
}