package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Activity;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Deadline;
import com.bupt.ta.model.User;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.Resume;
import com.bupt.ta.model.enums.ApplicationStatus;
import com.bupt.ta.model.enums.JobStatus;
import com.bupt.ta.model.enums.UserRole;

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
    private JobRepository jobRepository;
    private UserRepository userRepository;


    @Override
    public void init() throws ServletException {
        DatabaseConfig config = DatabaseConfig.defaultConfig();
        this.resumeRepository = new JsonResumeRepository(config);
        this.applicationRepository = new JsonApplicationRepository(config);
        this.jobRepository = new JsonJobRepository(config);
        this.userRepository = new JsonUserRepository(config);
    }

    private List<Application> listMyApplicationsByResumes(List<Resume> myResumes){
        List<Application> all_Applications = this.applicationRepository.listAll();
        List<Application> ret = new ArrayList<>();
        for(Application application: all_Applications){
            boolean isMyApplication = false;
            for(Resume resume: myResumes){
                if(application.getResumeId().equals(resume.getId())){
                    isMyApplication = true;
                    break;
                }
            }
            if (isMyApplication) {
                ret.add(application);
            }
        }
        return ret;
    }

    private List<Application> listMyApplicationsByJobs(List<Job> myJobs){
        List<Application> all_Applications = this.applicationRepository.listAll();
        List<Application> ret = new ArrayList<>();
        for(Application application: all_Applications){
            boolean isMyApplication = false;
            for(Job job: myJobs){
                if(application.getJobId().equals(job.getId())){
                    isMyApplication = true;
                    break;
                }
            }
            if (isMyApplication) {
                ret.add(application);
            }
        }
        return ret;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // 1. 获取当前登录用户对象 (AuthFilter 确保了 session 和 currentUser 必然存在)
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");
        
        String userRole = (String) req.getAttribute("userRole");
        String language = I18n.resolveLanguage(req);
        boolean zh = I18n.isChinese(language);

        if ("TA".equals(userRole)){
            List<Resume> myResumes = this.resumeRepository.listByUserId(currentUser.getId());

            List<Application> all_applications = listMyApplicationsByResumes(myResumes);
            int count = 0;
            for(Application application: all_applications){
                if(application.getStatus() == ApplicationStatus.PENDING || application.getStatus() == ApplicationStatus.REVIEWING){
                    ++count;
                }
            }
            req.setAttribute("savedResumesCount", myResumes.size());
            req.setAttribute("submittedApplicationsCount", all_applications.size());
            req.setAttribute("underReviewApplicationsCount", count);
        }
        else if ("MO".equals(userRole)){
            List<Job> myJobs = this.jobRepository.listByPoster(currentUser.getId());

            List<Application> all_applications = listMyApplicationsByJobs(myJobs);
            int count = 0;
            for(Application application: all_applications){
                if(application.getStatus() == ApplicationStatus.PENDING || application.getStatus() == ApplicationStatus.REVIEWING){
                    ++count;
                }
            }

            req.setAttribute("postedVacanciesCount", myJobs.size());
            req.setAttribute("receivedApplicationsCount", count);
        }
        else if ("ADMIN".equals(userRole)){
            List<User> users = this.userRepository.listAll();

            int totalTAsCount = 0;
            for(User user:users){
                if(user.getRole() == UserRole.TA){
                    ++totalTAsCount;
                }
            }

            List<Job> allJobs = jobRepository.listAll();

            int activeVacanciesCount = 0;
            for(Job job: allJobs){
                if(job.getStatus() == JobStatus.OPEN){
                    ++activeVacanciesCount;
                }
            }

            req.setAttribute("totalTAsCount", totalTAsCount);
            req.setAttribute("activeVacanciesCount", activeVacanciesCount);
        }


        // 4. 准备近期活动列表 (Mock Data)
        List<Activity> activities = new ArrayList<>();
        // TODO: 真实活动列表
        activities.add(new Activity(
                zh ? "更新了简历" : "Resume updated",
                zh ? "你修改了主修专业和联系方式" : "You updated your major and contact information",
                zh ? "2 小时前" : "2 hours ago",
                "", "", "", 
                null, null
        ));
        activities.add(new Activity(
                zh ? "提交了 TA 申请" : "TA application submitted",
                zh ? "申请了《计算机科学导论》的助教岗位" : "Applied for the Teaching Assistant role in Introduction to Computer Science",
                zh ? "1 天前" : "1 day ago",
                "", "", "", 
                zh ? "已提交" : "Submitted", "bg-success"
        ));
        req.setAttribute("recentActivities", activities);

        // 5. 准备即将到期的截止日期列表 (Mock Data)
        List<Deadline> deadlines = new ArrayList<>();
        // TODO: 真实的ddl列表

        deadlines.add(new Deadline(
                zh ? "《数据结构》助教申请截止" : "Data Structures TA application deadline",
                zh ? "只剩 2 天" : "2 days left",
                "bg-danger", "text-white"));
        deadlines.add(new Deadline(
                zh ? "提交本学期成绩单" : "Submit this term's transcript",
                zh ? "还有 1 周" : "1 week left",
                "bg-warning", "text-dark"));
        req.setAttribute("upcomingDeadlines", deadlines);

        // 6. 转发到控制台页面 (注意这里的路径要和前端文件的存放位置完全一致)
        req.getRequestDispatcher("/portal/dashboard.jsp").forward(req, resp);
    }
}