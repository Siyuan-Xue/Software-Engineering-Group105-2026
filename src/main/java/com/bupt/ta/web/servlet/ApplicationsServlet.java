package com.bupt.ta.web.servlet;

import com.bupt.ta.dto.ApplicationDTO;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@WebServlet("/applications")
public class ApplicationsServlet extends HttpServlet {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    private TaDatabase database;

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "auth.loginRequired"), StandardCharsets.UTF_8));
            return;
        }

        req.setAttribute("successMessage", param(req, "successMessage"));
        req.setAttribute("errorMessage", param(req, "errorMessage"));

        String userRole = (String) req.getAttribute("userRole");
        
        java.util.function.Predicate<Application> roleFilter;
        if ("MO".equalsIgnoreCase(userRole)) {
            // MO 逻辑：获取该用户发布的所有 Job ID
            Set<UUID> postedJobIds = database.jobs().listByPoster(currentUser.getId()).stream()
                    .map(Job::getId)
                    .collect(java.util.stream.Collectors.toSet());
            
            roleFilter = app -> postedJobIds.contains(app.getJobId());
        } else {
            // TA 逻辑（默认）：获取该用户所有的 Resume ID
            Set<UUID> myResumeIds = database.resumes().listByUserId(currentUser.getId()).stream()
                    .map(Resume::getId)
                    .collect(java.util.stream.Collectors.toSet());
            
            roleFilter = app -> myResumeIds.contains(app.getResumeId());
        }

        String keyword = param(req, "keyword");
        String statusFilter = param(req, "status");
        String dateOrder = param(req, "date");

        List<ApplicationDTO> applications = database.applications().findAll().stream()
                .filter(roleFilter)
                .map(this::toDto)
                .filter(dto -> matchesKeyword(dto, keyword))
                .filter(dto -> matchesStatus(dto, statusFilter))
                .sorted(resolveComparator(dateOrder))
                .toList();

        if (applications.isEmpty()) {
            req.setAttribute("pageState", keyword != null || statusFilter != null ? "noFilterResults" : "empty");
        } else {
            req.setAttribute("pageState", "normal");
        }
        req.setAttribute("applications", applications);
        req.getRequestDispatcher("/portal/applications.jsp").forward(req, resp);
    }

    private ApplicationDTO toDto(Application application) {
        Resume resume = database.resumes().findById(application.getResumeId()).orElse(null);
        Job job = database.jobs().findById(application.getJobId()).orElse(null);

        ApplicationDTO dto = new ApplicationDTO();
        dto.setApplicationId(application.getId());
        dto.setVacancyId(job == null ? null : job.getId());
        dto.setVacancyTitle(job == null ? "Unknown Job" : safe(job.getTitle(), "Unknown Job"));
        dto.setCourseCode(job == null ? "N/A" : safe(job.getModuleCode(), "N/A"));
        dto.setDepartment(job == null ? null : resolveDepartment(job));
        dto.setStatus(toDisplayStatus(application.getStatus()));
        dto.setAppliedDate(application.getCreatedAt() == null ? "" : DATE_FORMATTER.format(application.getCreatedAt()));
        dto.setResumeName(resume == null ? "Unknown Resume" : safe(resume.getTitle(), "Unknown Resume"));
        return dto;
    }

    private boolean matchesKeyword(ApplicationDTO dto, String keyword) {
        if (keyword == null) {
            return true;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        return safe(dto.getVacancyTitle(), "").toLowerCase(Locale.ROOT).contains(normalized)
                || safe(dto.getCourseCode(), "").toLowerCase(Locale.ROOT).contains(normalized)
                || safe(dto.getDepartment(), "").toLowerCase(Locale.ROOT).contains(normalized);
    }

    private boolean matchesStatus(ApplicationDTO dto, String statusFilter) {
        return statusFilter == null || statusFilter.equalsIgnoreCase(dto.getStatus());
    }

    private Comparator<ApplicationDTO> resolveComparator(String dateOrder) {
        Comparator<ApplicationDTO> comparator = Comparator.comparing(ApplicationDTO::getAppliedDate, Comparator.nullsLast(String::compareTo));
        return "oldest".equalsIgnoreCase(dateOrder) ? comparator : comparator.reversed();
    }

    private String resolveDepartment(Job job) {
        String moduleCode = safe(job.getModuleCode(), "");
        return moduleCode.toUpperCase(Locale.ROOT).startsWith("MATH") ? "MATH" : "CS";
    }

    private String toDisplayStatus(ApplicationStatus status) {
        return switch (status) {
            case PENDING -> "Submitted";
            case REVIEWING -> "Under Review";
            case OFFER_PENDING -> "Offer Pending";
            case ACCEPTED -> "Accepted";
            case REJECTED -> "Rejected";
            case DECLINED -> "Declined";
            case WITHDRAWN -> "Withdrawn";
        };
    }

    private String param(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
