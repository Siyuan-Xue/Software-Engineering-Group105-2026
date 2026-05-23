package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.model.Activity;
import com.bupt.ta.model.Deadline;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Role-aware landing experience at {@code /dashboard}.
 *
 * <p>{@code TA}, {@code MO}, and {@code ADMIN} dashboards aggregate metrics from JSON-backed repositories, hydrate recent activity
 * models, and forward to {@code portal/dashboard.jsp}. Unsupported roles degrade into an informational empty shell.</p>
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/dashboard.jsp";

    private TaDatabase database;

    /** Pulls singleton database access for cross-aggregate queries. */
    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
    }

    /** Hydrates KPI cards plus upcoming deadlines inferred from vacancy metadata. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String language = I18n.resolveLanguage(req);
        boolean zh = I18n.isChinese(language);
        String userRole = (String) req.getAttribute("userRole");

        try {
            boolean roleLoaded = false;
            if ("TA".equals(userRole)) {
                roleLoaded = true;
                List<Resume> resumes = database.resumes().listByUserId(currentUser.getId());
                Set<UUID> resumeIds = resumes.stream().map(Resume::getId).collect(java.util.stream.Collectors.toSet());
                List<Application> applications = database.applications().findAll().stream()
                        .filter(application -> resumeIds.contains(application.getResumeId()))
                        .toList();
                long underReviewCount = applications.stream()
                        .filter(application -> application.getStatus() == ApplicationStatus.PENDING
                                || application.getStatus() == ApplicationStatus.REVIEWING
                                || application.getStatus() == ApplicationStatus.OFFER_PENDING)
                        .count();
                req.setAttribute("savedResumesCount", resumes.size());
                req.setAttribute("submittedApplicationsCount", applications.size());
                req.setAttribute("underReviewApplicationsCount", underReviewCount);
                req.setAttribute("recentActivities", taActivities(zh, applications, resumes));
                req.setAttribute("upcomingDeadlines", taDeadlines(zh));
            } else if ("MO".equals(userRole)) {
                roleLoaded = true;
                List<Job> jobs = database.jobs().listByPoster(currentUser.getId());
                Set<UUID> jobIds = jobs.stream().map(Job::getId).collect(java.util.stream.Collectors.toSet());
                List<Application> applications = database.applications().findAll().stream()
                        .filter(application -> jobIds.contains(application.getJobId()))
                        .toList();
                long underReviewCount = applications.stream()
                        .filter(application -> application.getStatus() == ApplicationStatus.PENDING
                                || application.getStatus() == ApplicationStatus.REVIEWING)
                        .count();
                req.setAttribute("postedVacanciesCount", jobs.size());
                req.setAttribute("receivedApplicationsCount", applications.size());
                req.setAttribute("underReviewCount", underReviewCount);
                req.setAttribute("recentActivities", moActivities(zh, jobs, applications));
                req.setAttribute("upcomingDeadlines", moDeadlines(zh, jobs));
            } else if ("ADMIN".equals(userRole)) {
                roleLoaded = true;
                long totalTas = database.users().findAll().stream().filter(user -> user.getRole() == UserRole.TA).count();
                long activeVacancies = database.jobs().findAll().stream().filter(job -> job.getStatus() == JobStatus.OPEN).count();
                req.setAttribute("totalTAsCount", totalTas);
                req.setAttribute("activeVacanciesCount", activeVacancies);
                req.setAttribute("recentActivities", adminActivities(zh));
                req.setAttribute("upcomingDeadlines", adminDeadlines(zh));
            }
            if (roleLoaded) {
                req.setAttribute("pageState", "normal");
            } else {
                applyUnsupportedRole(req);
            }
        } catch (Exception ex) {
            getServletContext().log("Failed to load dashboard", ex);
            applyLoadError(req);
        }

        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private void applyLoadError(HttpServletRequest req) {
        req.setAttribute("pageState", "loadError");
        req.setAttribute("errorMessage", I18n.message(req, "msg.dashboardLoadFailed"));
        clearDashboardMetrics(req);
    }

    /** Uses an explicit empty state for unsupported roles such as the public demo user. */
    private void applyUnsupportedRole(HttpServletRequest req) {
        req.setAttribute("pageState", "emptyActivities");
        clearDashboardMetrics(req);
    }

    private void clearDashboardMetrics(HttpServletRequest req) {
        req.setAttribute("recentActivities", List.of());
        req.setAttribute("upcomingDeadlines", List.of());
        req.setAttribute("savedResumesCount", 0);
        req.setAttribute("submittedApplicationsCount", 0);
        req.setAttribute("underReviewApplicationsCount", 0);
        req.setAttribute("postedVacanciesCount", 0);
        req.setAttribute("receivedApplicationsCount", 0);
        req.setAttribute("underReviewCount", 0);
        req.setAttribute("totalTAsCount", 0);
        req.setAttribute("activeVacanciesCount", 0);
    }

    private List<Activity> taActivities(boolean zh, List<Application> applications, List<Resume> resumes) {
        List<Activity> activities = new ArrayList<>();
        applications.stream()
                .sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(2)
                .forEach(application -> activities.add(new Activity(
                        zh ? "提交了岗位申请" : "Application submitted",
                        zh ? "你的申请状态为 " + application.getStatus() : "Your latest application status is " + application.getStatus(),
                        timeAgo(application.getCreatedAt(), zh),
                        "", "", "",
                        badgeLabel(application.getStatus(), zh),
                        "bg-primary/10"
                )));
        resumes.stream()
                .sorted(Comparator.comparing(Resume::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(1)
                .forEach(resume -> activities.add(new Activity(
                        zh ? "更新了简历" : "Resume updated",
                        zh ? "你更新了 " + resume.getTitle() : "You updated " + resume.getTitle(),
                        timeAgo(resume.getUpdatedAt(), zh),
                        "", "", "",
                        null, null
                )));
        return activities;
    }

    private List<Deadline> taDeadlines(boolean zh) {
        return database.jobs().listOpen(queryNow()).stream()
                .sorted(Comparator.comparing(Job::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(3)
                .map(job -> new Deadline(
                        safe(job.getTitle(), zh ? "岗位截止日期" : "Vacancy deadline"),
                        deadlineCountdown(job.getDeadline(), zh),
                        "bg-danger",
                        "text-white"))
                .toList();
    }

    private List<Activity> moActivities(boolean zh, List<Job> jobs, List<Application> applications) {
        List<Activity> activities = new ArrayList<>();
        jobs.stream().sorted(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(2)
                .forEach(job -> activities.add(new Activity(
                        zh ? "发布了岗位" : "Vacancy posted",
                        zh ? "你发布了 " + safe(job.getTitle(), "岗位") : "You posted " + safe(job.getTitle(), "a vacancy"),
                        timeAgo(job.getCreatedAt(), zh),
                        "", "", "",
                        null, null
                )));
        applications.stream().sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(1)
                .forEach(application -> activities.add(new Activity(
                        zh ? "收到了新申请" : "New application received",
                        zh ? "有新的 TA 申请等待处理" : "A new TA application is waiting for review",
                        timeAgo(application.getCreatedAt(), zh),
                        "", "", "",
                        badgeLabel(application.getStatus(), zh),
                        "bg-primary/10"
                )));
        return activities;
    }

    private List<Deadline> moDeadlines(boolean zh, List<Job> jobs) {
        return jobs.stream()
                .filter(job -> job.getDeadline() != null && job.getDeadline().isAfter(Instant.now()))
                .sorted(Comparator.comparing(Job::getDeadline))
                .limit(3)
                .map(job -> new Deadline(
                        safe(job.getTitle(), zh ? "岗位截止日期" : "Vacancy deadline"),
                        deadlineCountdown(job.getDeadline(), zh),
                        "bg-warning",
                        "text-dark"))
                .toList();
    }

    private List<Activity> adminActivities(boolean zh) {
        return List.of(new Activity(
                zh ? "数据库已初始化" : "Database initialized",
                zh ? "Sprint 3 JSON 数据库系统已就绪" : "The Sprint 3 JSON database system is ready",
                zh ? "刚刚" : "Just now",
                "", "", "",
                null, null
        ));
    }

    private List<Deadline> adminDeadlines(boolean zh) {
        return database.jobs().listOpen(queryNow()).stream()
                .sorted(Comparator.comparing(Job::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(3)
                .map(job -> new Deadline(
                        safe(job.getTitle(), zh ? "岗位截止日期" : "Vacancy deadline"),
                        deadlineCountdown(job.getDeadline(), zh),
                        "bg-warning",
                        "text-dark"))
                .toList();
    }

    private com.bupt.ta.domain.value.JobQuery queryNow() {
        com.bupt.ta.domain.value.JobQuery query = new com.bupt.ta.domain.value.JobQuery();
        query.setNow(Instant.now());
        return query;
    }

    private String badgeLabel(ApplicationStatus status, boolean zh) {
        return switch (status) {
            case PENDING -> zh ? "已提交" : "Submitted";
            case REVIEWING -> zh ? "审核中" : "Under Review";
            case OFFER_PENDING -> zh ? "待确认" : "Offer Pending";
            case ACCEPTED -> zh ? "已录用" : "Accepted";
            case REJECTED -> zh ? "已拒绝" : "Rejected";
            case DECLINED -> zh ? "已拒绝录用" : "Declined";
            case WITHDRAWN -> zh ? "已撤回" : "Withdrawn";
        };
    }

    private String timeAgo(Instant instant, boolean zh) {
        if (instant == null) {
            return zh ? "未知时间" : "Unknown";
        }
        long hours = Math.max(1, Duration.between(instant, Instant.now()).toHours());
        if (hours < 24) {
            return zh ? hours + " 小时前" : hours + " hours ago";
        }
        long days = Math.max(1, hours / 24);
        return zh ? days + " 天前" : days + " days ago";
    }

    private String deadlineCountdown(Instant deadline, boolean zh) {
        if (deadline == null) {
            return zh ? "时间待定" : "TBD";
        }
        long hours = Duration.between(Instant.now(), deadline).toHours();
        if (hours < 24) {
            return zh ? "只剩 " + Math.max(hours, 0) + " 小时" : Math.max(hours, 0) + " hours left";
        }
        long days = Math.max(1, hours / 24);
        return zh ? "还有 " + days + " 天" : days + " days left";
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
