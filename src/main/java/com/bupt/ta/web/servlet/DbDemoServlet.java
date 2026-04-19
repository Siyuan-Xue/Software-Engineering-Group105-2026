package com.bupt.ta.web.servlet;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.service.DbDemoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@WebServlet("/db-demo")
public class DbDemoServlet extends HttpServlet {
    private static final String VIEW_PATH = "/WEB-INF/jsp/db-demo.jsp";
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter DATETIME_INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private DbDemoService dbDemoService;

    @Override
    public void init() {
        this.dbDemoService = new DbDemoService(DatabaseProvider.get(getServletContext()));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        applyFlashMessages(req);
        safelyApplyEditSelection(req);
        populatePageData(req);
        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String operation = normalize(req.getParameter("operation"));
        String section = normalize(req.getParameter("section"));
        String targetSection = section == null ? "overview-section" : section;

        try {
            String successMessage = handleOperation(operation, req);
            resp.sendRedirect(buildRedirectUrl(req, targetSection, "successMessage", successMessage));
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            repopulateSubmittedEntity(req, operation);
            populatePageData(req);
            req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
        }
    }

    private String handleOperation(String operation, HttpServletRequest req) {
        if (operation == null) {
            throw new ConstraintViolationException("An operation is required");
        }

        return switch (operation) {
            case "user-save" -> {
                User user = readUserFromRequest(req);
                boolean isUpdate = user.getId() != null;
                dbDemoService.saveUser(user, req.getParameter("plainPassword"));
                yield isUpdate ? "User updated through TaDatabase." : "User created through TaDatabase.";
            }
            case "resume-save" -> {
                Resume resume = readResumeFromRequest(req);
                boolean isUpdate = resume.getId() != null;
                dbDemoService.saveResume(resume, req.getParameter("availabilitySlotsJson"));
                yield isUpdate ? "Resume updated and persisted to JSON." : "Resume created and persisted to JSON.";
            }
            case "job-save" -> {
                Job job = readJobFromRequest(req);
                boolean isUpdate = job.getId() != null;
                dbDemoService.saveJob(job);
                yield isUpdate ? "Job updated through JobService." : "Job created through JobService.";
            }
            case "application-submit" -> {
                UUID resumeId = requireUuid(req.getParameter("resumeId"), "Resume is required");
                UUID jobId = requireUuid(req.getParameter("jobId"), "Job is required");
                dbDemoService.submitApplication(resumeId, jobId, req.getParameter("coverLetter"));
                yield "Application submitted. Applications, notifications, and audit logs were updated.";
            }
            case "application-review" -> {
                dbDemoService.startReview(requireUuid(req.getParameter("applicationId"), "Application is required"));
                yield "Application moved to REVIEWING.";
            }
            case "application-offer" -> {
                dbDemoService.sendOffer(requireUuid(req.getParameter("applicationId"), "Application is required"));
                yield "Offer sent to the applicant.";
            }
            case "application-accept" -> {
                dbDemoService.acceptOffer(requireUuid(req.getParameter("applicationId"), "Application is required"));
                yield "Offer accepted. Workload, notifications, and audit logs were updated.";
            }
            case "application-decline" -> {
                dbDemoService.declineOffer(requireUuid(req.getParameter("applicationId"), "Application is required"));
                yield "Offer declined by the applicant.";
            }
            case "application-withdraw" -> {
                dbDemoService.withdraw(requireUuid(req.getParameter("applicationId"), "Application is required"));
                yield "Application withdrawn by the applicant.";
            }
            case "application-reject" -> {
                dbDemoService.reject(
                        requireUuid(req.getParameter("applicationId"), "Application is required"),
                        req.getParameter("rejectionNote"));
                yield "Application rejected by the recruiter.";
            }
            case "job-cancel" -> {
                dbDemoService.cancelJob(requireUuid(req.getParameter("jobId"), "Job is required"));
                yield "Job cancelled. In-progress applications were withdrawn.";
            }
            default -> throw new ConstraintViolationException("Unsupported db-demo operation: " + operation);
        };
    }

    private void populatePageData(HttpServletRequest req) {
        req.setAttribute("tableCounts", dbDemoService.listTableCounts());
        req.setAttribute("users", dbDemoService.listUsers());
        req.setAttribute("recruiterUsers", dbDemoService.listRecruiters());
        req.setAttribute("resumes", dbDemoService.listResumes());
        req.setAttribute("jobs", dbDemoService.listJobs());
        req.setAttribute("applications", dbDemoService.listApplications());
        req.setAttribute("recentNotifications", dbDemoService.listRecentNotifications(10));
        req.setAttribute("recentAuditLogs", dbDemoService.listRecentAuditLogs(10));
        req.setAttribute("recentWorkloadRecords", dbDemoService.listRecentWorkloadRecords(10));

        req.setAttribute("roles", UserRole.values());
        req.setAttribute("degreeLevels", DegreeLevel.values());
        req.setAttribute("jobTypes", JobType.values());
        req.setAttribute("jobStatuses", JobStatus.values());

        req.setAttribute("userLabelsById", dbDemoService.buildUserLabels());
        req.setAttribute("resumeLabelsById", dbDemoService.buildResumeLabels());
        req.setAttribute("jobLabelsById", dbDemoService.buildJobLabels());
        req.setAttribute("dataDirectory", AppConfig.resolveDataDirectory().toString());
        req.setAttribute("defaultDeadlineValue", LocalDateTime.now().plusDays(7).format(DATETIME_INPUT_FORMAT));

        populateEditingDateFields(req);
    }

    private void applyFlashMessages(HttpServletRequest req) {
        if (req.getAttribute("successMessage") == null) {
            req.setAttribute("successMessage", normalize(req.getParameter("successMessage")));
        }
        if (req.getAttribute("errorMessage") == null) {
            req.setAttribute("errorMessage", normalize(req.getParameter("errorMessage")));
        }
    }

    private void safelyApplyEditSelection(HttpServletRequest req) {
        String editEntity = normalize(req.getParameter("editEntity"));
        String editIdValue = normalize(req.getParameter("editId"));
        if (editEntity == null || editIdValue == null) {
            return;
        }

        UUID editId = requireUuid(editIdValue, "Edit id is invalid");
        req.setAttribute("editEntity", editEntity);

        switch (editEntity) {
            case "user" -> req.setAttribute("editingUser",
                    dbDemoService.findUser(editId).orElseThrow(() -> new ConstraintViolationException("User not found: " + editId)));
            case "resume" -> {
                Resume resume = dbDemoService.findResume(editId)
                        .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + editId));
                req.setAttribute("editingResume", resume);
                req.setAttribute("editingResumeAvailabilityJson", dbDemoService.availabilitySlotsJson(resume));
            }
            case "job" -> req.setAttribute("editingJob",
                    dbDemoService.findJob(editId).orElseThrow(() -> new ConstraintViolationException("Job not found: " + editId)));
            default -> throw new ConstraintViolationException("Unsupported edit entity: " + editEntity);
        }
    }

    private void repopulateSubmittedEntity(HttpServletRequest req, String operation) {
        if (operation == null) {
            return;
        }
        switch (operation) {
            case "user-save" -> {
                req.setAttribute("editEntity", "user");
                req.setAttribute("editingUser", readUserFromRequest(req));
            }
            case "resume-save" -> {
                req.setAttribute("editEntity", "resume");
                req.setAttribute("editingResume", readResumeFromRequest(req));
                req.setAttribute("editingResumeAvailabilityJson", valueOrEmpty(req.getParameter("availabilitySlotsJson")));
            }
            case "job-save" -> {
                req.setAttribute("editEntity", "job");
                req.setAttribute("editingJob", readJobFromRequest(req));
            }
            default -> {
            }
        }
    }

    private void populateEditingDateFields(HttpServletRequest req) {
        Object editingJob = req.getAttribute("editingJob");
        if (editingJob instanceof Job job) {
            req.setAttribute("editingJobStartDateValue", job.getStartDate() == null ? "" : job.getStartDate().toString());
            req.setAttribute("editingJobEndDateValue", job.getEndDate() == null ? "" : job.getEndDate().toString());
            req.setAttribute("editingJobDeadlineValue", formatInstantForInput(job.getDeadline()));
        }
    }

    private User readUserFromRequest(HttpServletRequest req) {
        User user = new User();
        user.setId(optionalUuid(req.getParameter("id")));
        user.setEmail(normalize(req.getParameter("email")));
        user.setFullName(normalize(req.getParameter("fullName")));
        user.setRole(parseEnum(UserRole.class, req.getParameter("role"), "User role is invalid"));
        user.setPhone(normalize(req.getParameter("phone")));
        user.setDepartment(normalize(req.getParameter("department")));
        user.setStudentId(normalize(req.getParameter("studentId")));
        user.setBio(normalize(req.getParameter("bio")));
        user.setActive(!"false".equalsIgnoreCase(req.getParameter("active")));
        return user;
    }

    private Resume readResumeFromRequest(HttpServletRequest req) {
        Resume resume = new Resume();
        resume.setId(optionalUuid(req.getParameter("id")));
        resume.setUserId(requireUuid(req.getParameter("userId"), "Resume owner is required"));
        resume.setTitle(normalize(req.getParameter("title")));
        resume.setDepartment(normalize(req.getParameter("department")));
        resume.setDegreeLevel(parseEnum(DegreeLevel.class, req.getParameter("degreeLevel"), "Degree level is invalid"));
        resume.setGpa(parseBigDecimal(req.getParameter("gpa")));
        resume.setMaxWeeklyHours(parseInt(req.getParameter("maxWeeklyHours"), 20));
        resume.setBio(normalize(req.getParameter("bio")));
        return resume;
    }

    private Job readJobFromRequest(HttpServletRequest req) {
        Job job = new Job();
        job.setId(optionalUuid(req.getParameter("id")));
        job.setPostedBy(requireUuid(req.getParameter("postedBy"), "Job poster is required"));
        job.setTitle(normalize(req.getParameter("title")));
        job.setType(parseEnum(JobType.class, req.getParameter("type"), "Job type is invalid"));
        job.setModuleCode(normalize(req.getParameter("moduleCode")));
        job.setDescription(normalize(req.getParameter("description")));
        job.setRequiredHours(parseInt(req.getParameter("requiredHours"), 10));
        job.setSlots(parseInt(req.getParameter("slots"), 1));
        job.setStatus(parseEnum(JobStatus.class, req.getParameter("status"), "Job status is invalid"));
        job.setStartDate(parseLocalDate(req.getParameter("startDate")));
        job.setEndDate(parseLocalDate(req.getParameter("endDate")));
        job.setDeadline(parseInstant(req.getParameter("deadline")));
        job.setHourlyRate(parseBigDecimal(req.getParameter("hourlyRate")));
        return job;
    }

    private UUID optionalUuid(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        return requireUuid(normalized, "UUID is invalid");
    }

    private UUID requireUuid(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new ConstraintViolationException(message);
        }
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ConstraintViolationException(message);
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, normalized);
        } catch (IllegalArgumentException ex) {
            throw new ConstraintViolationException(message);
        }
    }

    private int parseInt(String value, int defaultValue) {
        String normalized = normalize(value);
        if (normalized == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ex) {
            throw new ConstraintViolationException("Invalid number: " + value);
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            throw new ConstraintViolationException("Invalid decimal number: " + value);
        }
    }

    private LocalDate parseLocalDate(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new ConstraintViolationException("Invalid date: " + value);
        }
    }

    private Instant parseInstant(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(normalized, DATETIME_INPUT_FORMAT).atZone(DEFAULT_ZONE).toInstant();
        } catch (DateTimeParseException ex) {
            throw new ConstraintViolationException("Invalid datetime: " + value);
        }
    }

    private String formatInstantForInput(Instant instant) {
        if (instant == null) {
            return "";
        }
        return DATETIME_INPUT_FORMAT.format(instant.atZone(DEFAULT_ZONE).toLocalDateTime());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String buildRedirectUrl(HttpServletRequest req, String section, String key, String value) {
        String encodedValue = URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
        return req.getContextPath() + "/db-demo?" + key + "=" + encodedValue + "#" + section;
    }
}
