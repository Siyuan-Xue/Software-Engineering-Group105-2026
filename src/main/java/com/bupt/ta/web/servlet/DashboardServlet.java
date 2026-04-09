package com.bupt.ta.web.servlet;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Activity;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Deadline;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.ApplicationStatus;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.bupt.ta.repository.*;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.persistence.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private ResumeRepository resumeRepository;
    private ApplicationRepository applicationRepository;

    @Override
    public void init() throws ServletException {
        DatabaseConfig config = DatabaseConfig.defaultConfig();
        this.resumeRepository = new JsonResumeRepository(config);
        this.applicationRepository = new JsonApplicationRepository(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // 1. 获取当前登录用户对象 (AuthFilter 确保了 session 和 currentUser 必然存在)
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        // 2. 设置用户基础信息
        //req.setAttribute("userName", currentUser.getFullName());

        // 3. 准备模拟统计数据 (后续会替换为真实的 Service 调用，例如 applicationService.count(...))
        List<Application> all_applications = this.applicationRepository.listAll();

        int count = 0;
        for(Application application: all_applications){
            if(application.getStatus() == ApplicationStatus.PENDING || application.getStatus() == ApplicationStatus.REVIEWING){
                ++count;
            }
        }

        req.setAttribute("savedResumesCount", this.resumeRepository.listAll().size());
        req.setAttribute("submittedApplicationsCount", all_applications.size());
        req.setAttribute("underReviewApplicationsCount", count);

        // 4. 准备近期活动列表 (Mock Data)
        List<Activity> activities = new ArrayList<>();
        // TODO: 真实活动列表
        activities.add(new Activity(
                "更新了简历", 
                "你修改了主修专业和联系方式", 
                "2 小时前", 
                "", "", "", 
                null, null
        ));
        activities.add(new Activity(
                "提交了 TA 申请", 
                "申请了《计算机科学导论》的助教岗位", 
                "1 天前", 
                "", "", "", 
                "已提交", "bg-success"
        ));
        req.setAttribute("recentActivities", activities);

        // 5. 准备即将到期的截止日期列表 (Mock Data)
        List<Deadline> deadlines = new ArrayList<>();
        // TODO: 真实的ddl列表

        deadlines.add(new Deadline("《数据结构》助教申请截止", "只剩 2 天", "bg-danger", "text-white"));
        deadlines.add(new Deadline("提交本学期成绩单", "还有 1 周", "bg-warning", "text-dark"));
        req.setAttribute("upcomingDeadlines", deadlines);

        // 6. 转发到控制台页面 (注意这里的路径要和前端文件的存放位置完全一致)
        req.getRequestDispatcher("/portal/dashboard.jsp").forward(req, resp);
    }
}