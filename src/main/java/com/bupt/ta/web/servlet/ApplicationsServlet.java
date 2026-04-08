package com.bupt.ta.web.servlet;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.dto.ApplicationDTO;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.Resume;
import com.bupt.ta.model.User;
import com.bupt.ta.repository.ApplicationRepository;
import com.bupt.ta.repository.JobRepository;
import com.bupt.ta.repository.ResumeRepository;
import com.bupt.ta.persistence.json.JsonApplicationRepository;
import com.bupt.ta.persistence.json.JsonJobRepository;
import com.bupt.ta.persistence.json.JsonResumeRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/applications")
public class ApplicationsServlet extends HttpServlet {

    private ApplicationRepository applicationRepository;
    private JobRepository jobRepository;
    private ResumeRepository resumeRepository;

    @Override
    public void init() throws ServletException {
        DatabaseConfig config = DatabaseConfig.defaultConfig();
        // 初始化所需的各层 Repository
        this.applicationRepository = new JsonApplicationRepository(config);
        this.jobRepository = new JsonJobRepository(config);
        this.resumeRepository = new JsonResumeRepository(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 鉴权验证
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?errorMessage=Please log in to access this page");
            return;
        }

        // 3. 处理 URL 传来的反馈消息
        String successMessage = req.getParameter("successMessage");
        if (successMessage != null && !successMessage.isEmpty()) {
            req.setAttribute("successMessage", successMessage);
        }
        String errorMessage = req.getParameter("errorMessage");
        if (errorMessage != null && !errorMessage.isEmpty()) {
            req.setAttribute("errorMessage", errorMessage);
        }

        // 4. 获取筛选参数 (搜索关键字、状态)
        String keywordStr = req.getParameter("keyword");
        String statusFilterStr = req.getParameter("status");

        // 5. 拼装用户的 ApplicationDTO 列表
        List<ApplicationDTO> dtoList = new ArrayList<>();
        
        try {
            // 获取用户所有的简历
            List<Resume> userResumes = resumeRepository.listByUserId(currentUser.getId());
            
            // 遍历简历，获取每个简历关联的申请记录
            for (Resume resume : userResumes) {
                List<Application> apps = applicationRepository.listByResumeId(resume.getId());
                for (Application app : apps) {
                    // 【修复】处理 Optional
                    Optional<Job> jobOpt = jobRepository.findById(app.getJobId());
                    Job job = jobOpt.orElse(null); // 如果不存在则返回 null

                    ApplicationDTO dto = new ApplicationDTO();
                    dto.setApplicationId(app.getId());
                    dto.setVacancyTitle(job != null ? job.getTitle() : "Unknown Job");
                    
                    // 【修复】Job.java 中字段名为 moduleCode 而不是 courseCode
                    dto.setCourseCode(job != null ? job.getModuleCode() : "N/A");
                    
                    // 【修复】Job.java 并没有 department 字段，按照联调 Checklist 我们这里直接返回硬编码补齐缺口
                    dto.setDepartment("None department, wait for Xue"); 
                    
                    dto.setStatus(app.getStatus().name());
                    // 假设底层 BaseEntity 或对象内部有 createdAt() 可以提供申请时间。
                    dto.setAppliedDate(app.getCreatedAt()); 
                    dto.setResumeName(resume.getTitle());
                    
                    dtoList.add(dto);
                }
            }

            // 6. 前端如果有搜索和过滤请求，进行基础流式过滤
            if (keywordStr != null && !keywordStr.trim().isEmpty()) {
                final String kw = keywordStr.toLowerCase();
                dtoList = dtoList.stream()
                        .filter(dto -> dto.getVacancyTitle().toLowerCase().contains(kw) 
                                    || dto.getCourseCode().toLowerCase().contains(kw))
                        .collect(Collectors.toList());
            }
            if (statusFilterStr != null && !statusFilterStr.trim().isEmpty()) {
                dtoList = dtoList.stream()
                        .filter(dto -> dto.getStatus().equalsIgnoreCase(statusFilterStr))
                        .collect(Collectors.toList());
            }

            // 7. 将列表传入请求，分发给前端 jsp
            req.setAttribute("applications", dtoList);
            req.getRequestDispatcher("/portal/applications.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace(); // 打印日志方便后端排查
            req.setAttribute("errorMessage", "Failed to load applications. Please try again.");
            req.getRequestDispatcher("/portal/applications.jsp").forward(req, resp);
        }
    }
}