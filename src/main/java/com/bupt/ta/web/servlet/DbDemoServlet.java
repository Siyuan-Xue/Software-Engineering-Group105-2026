package com.bupt.ta.web.servlet;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.Resume;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.ApplicationStatus;
import com.bupt.ta.model.enums.DegreeLevel;
import com.bupt.ta.model.enums.JobStatus;
import com.bupt.ta.model.enums.JobType;
import com.bupt.ta.model.enums.UserRole;
import com.bupt.ta.persistence.DatabaseProvider;
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
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/db-demo")
public class DbDemoServlet extends HttpServlet {
    private static final String VIEW_PATH = "/WEB-INF/jsp/db-demo.jsp";
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private DbDemoService dbDemoService;

    public DbDemoServlet() {
    }

    DbDemoServlet(DbDemoService dbDemoService) {
        this.dbDemoService = dbDemoService;
    }

    @Override
    public void init() {
        if (dbDemoService == null) {
            dbDemoService = DbDemoService.from(DatabaseProvider.get(getServletContext()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        applyMessageAttributes(request);
        forwardPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String entity = normalize(request.getParameter("entity"));
        String operation = normalize(request.getParameter("operation"));

        try {
            String successMessage = handlePost(entity, operation, request);
            response.sendRedirect(buildRedirectUrl(request, "successMessage", successMessage));
        } catch (RuntimeException e) {
            request.setAttribute("errorMessage", e.getMessage());
            if (isUpsertOperation(operation)) {
                request.setAttribute("editEntity", entity);
                try {
                    populateEditingEntityFromSubmission(request, entity);
                } catch (RuntimeException ignored) {
                    clearEditingAttributes(request);
                }
            }
            forwardPage(request, response);
        }
    }

    private String handlePost(String entity, String operation, HttpServletRequest request) {
        if (entity == null || operation == null) {
            throw new IllegalArgumentException("Both entity and operation are required");
        }

        return switch (entity) {
            case "user" -> handleUserOperation(operation, request);
            case "resume" -> handleResumeOperation(operation, request);
            case "job" -> handleJobOperation(operation, request);
            case "application" -> handleApplicationOperation(operation, request);
            default -> throw new IllegalArgumentException("Unsupported entity: " + entity);
        };
    }

    private String handleUserOperation(String operation, HttpServletRequest request) {
        return switch (operation) {
            case "create", "update" -> {
                User user = readUserFromRequest(request);
                boolean isUpdate = user.getId() != null;
                dbDemoService.saveUser(user);
                yield isUpdate ? "User updated successfully" : "User created successfully";
            }
            case "deactivate" -> {
                UUID id = requireUuid(request.getParameter("id"), "User id is required");
                dbDemoService.deactivateUser(id);
                yield "User deactivated successfully";
            }
            default -> throw new IllegalArgumentException("Unsupported user operation: " + operation);
        };
    }

    private String handleResumeOperation(String operation, HttpServletRequest request) {
        return switch (operation) {
            case "create", "update" -> {
                Resume resume = readResumeFromRequest(request);
                boolean isUpdate = resume.getId() != null;
                dbDemoService.saveResume(resume);
                yield isUpdate ? "Resume updated successfully" : "Resume created successfully";
            }
            case "delete" -> {
                UUID id = requireUuid(request.getParameter("id"), "Resume id is required");
                dbDemoService.deleteResume(id);
                yield "Resume deleted successfully";
            }
            default -> throw new IllegalArgumentException("Unsupported resume operation: " + operation);
        };
    }

    private String handleJobOperation(String operation, HttpServletRequest request) {
        return switch (operation) {
            case "create", "update" -> {
                Job job = readJobFromRequest(request);
                boolean isUpdate = job.getId() != null;
                dbDemoService.saveJob(job);
                yield isUpdate ? "Job updated successfully" : "Job created successfully";
            }
            case "delete" -> {
                UUID id = requireUuid(request.getParameter("id"), "Job id is required");
                dbDemoService.deleteJob(id);
                yield "Job deleted successfully";
            }
            default -> throw new IllegalArgumentException("Unsupported job operation: " + operation);
        };
    }

    private String handleApplicationOperation(String operation, HttpServletRequest request) {
        return switch (operation) {
            case "create", "update" -> {
                Application application = readApplicationFromRequest(request);
                boolean isUpdate = application.getId() != null;
                dbDemoService.saveApplication(application);
                yield isUpdate ? "Application updated successfully" : "Application created successfully";
            }
            case "delete" -> {
                UUID id = requireUuid(request.getParameter("id"), "Application id is required");
                dbDemoService.deleteApplication(id);
                yield "Application deleted successfully";
            }
            default -> throw new IllegalArgumentException("Unsupported application operation: " + operation);
        };
    }

    private void forwardPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        populatePageData(request);
        request.getRequestDispatcher(VIEW_PATH).forward(request, response);
    }

    private void populatePageData(HttpServletRequest request) {
        List<User> users = dbDemoService.listUsers();
        List<Resume> resumes = dbDemoService.listResumes();
        List<Job> jobs = dbDemoService.listJobs();
        List<Application> applications = dbDemoService.listApplications();

        request.setAttribute("users", users);
        request.setAttribute("recruiterUsers", users.stream()
                .filter(user -> user.getRole() == UserRole.MO || user.getRole() == UserRole.ADMIN)
                .toList());
        request.setAttribute("resumes", resumes);
        request.setAttribute("jobs", jobs);
        request.setAttribute("applications", applications);

        request.setAttribute("roles", UserRole.values());
        request.setAttribute("degreeLevels", DegreeLevel.values());
        request.setAttribute("jobTypes", JobType.values());
        request.setAttribute("jobStatuses", JobStatus.values());
        request.setAttribute("applicationStatuses", ApplicationStatus.values());

        request.setAttribute("userLabelsById", buildUserLabels(users));
        request.setAttribute("resumeLabelsById", buildResumeLabels(resumes));
        request.setAttribute("jobLabelsById", buildJobLabels(jobs));
        request.setAttribute("dataDirectory", AppConfig.resolveDataDirectory().toString());

        safelyApplyEditSelectionFromRequest(request);
        populateEditingDateTimeValues(request);
    }

    private void safelyApplyEditSelectionFromRequest(HttpServletRequest request) {
        try {
            applyEditSelectionFromRequest(request);
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            clearEditingAttributes(request);
        }
    }

    private void applyMessageAttributes(HttpServletRequest request) {
        if (request.getAttribute("successMessage") == null) {
            request.setAttribute("successMessage", normalize(request.getParameter("successMessage")));
        }
        if (request.getAttribute("errorMessage") == null) {
            request.setAttribute("errorMessage", normalize(request.getParameter("errorMessage")));
        }
    }

    private void applyEditSelectionFromRequest(HttpServletRequest request) {
        if (request.getAttribute("editEntity") != null) {
            return;
        }

        String editEntity = normalize(request.getParameter("editEntity"));
        String editIdValue = normalize(request.getParameter("editId"));
        if (editEntity == null || editIdValue == null) {
            return;
        }

        UUID editId = requireUuid(editIdValue, "Edit id is invalid");
        request.setAttribute("editEntity", editEntity);

        switch (editEntity) {
            case "user" -> request.setAttribute("editingUser",
                    dbDemoService.findUser(editId).orElseThrow(() -> new IllegalArgumentException("User not found: " + editId)));
            case "resume" -> request.setAttribute("editingResume",
                    dbDemoService.findResume(editId).orElseThrow(() -> new IllegalArgumentException("Resume not found: " + editId)));
            case "job" -> request.setAttribute("editingJob",
                    dbDemoService.findJob(editId).orElseThrow(() -> new IllegalArgumentException("Job not found: " + editId)));
            case "application" -> request.setAttribute("editingApplication",
                    dbDemoService.findApplication(editId).orElseThrow(() -> new IllegalArgumentException("Application not found: " + editId)));
            default -> throw new IllegalArgumentException("Unsupported editEntity: " + editEntity);
        }
    }

    private void populateEditingEntityFromSubmission(HttpServletRequest request, String entity) {
        if (entity == null) {
            return;
        }

        switch (entity) {
            case "user" -> request.setAttribute("editingUser", readUserFromRequest(request));
            case "resume" -> request.setAttribute("editingResume", readResumeFromRequest(request));
            case "job" -> request.setAttribute("editingJob", readJobFromRequest(request));
            case "application" -> request.setAttribute("editingApplication", readApplicationFromRequest(request));
            default -> {
                // Ignore unsupported entities because validation will already surface the error.
            }
        }
    }

    private void clearEditingAttributes(HttpServletRequest request) {
        request.removeAttribute("editEntity");
        request.removeAttribute("editingUser");
        request.removeAttribute("editingResume");
        request.removeAttribute("editingJob");
        request.removeAttribute("editingApplication");
    }

    private void populateEditingDateTimeValues(HttpServletRequest request) {
        Job editingJob = (Job) request.getAttribute("editingJob");
        if (editingJob != null) {
            request.setAttribute("editingJobDeadlineValue", formatInstantForInput(editingJob.getDeadline()));
        }

        Application editingApplication = (Application) request.getAttribute("editingApplication");
        if (editingApplication != null) {
            request.setAttribute("editingApplicationReviewedAtValue", formatInstantForInput(editingApplication.getReviewedAt()));
            request.setAttribute("editingApplicationTaRespondedAtValue", formatInstantForInput(editingApplication.getTaRespondedAt()));
        }
    }

    private User readUserFromRequest(HttpServletRequest request) {
        UUID id = optionalUuid(request.getParameter("id"));
        User user = id == null ? new User() : dbDemoService.findUser(id).orElseGet(User::new);
        user.setId(id);
        user.setEmail(normalize(request.getParameter("email")));
        user.setPasswordHash(normalize(request.getParameter("passwordHash")));
        user.setFullName(normalize(request.getParameter("fullName")));
        user.setPhone(normalize(request.getParameter("phone")));
        user.setRole(parseEnum(request.getParameter("role"), UserRole.class, "User role is invalid"));
        if (id != null) {
            dbDemoService.findUser(id).ifPresent(existing -> user.setActive(existing.isActive()));
        }
        return user;
    }

    private Resume readResumeFromRequest(HttpServletRequest request) {
        Resume resume = new Resume();
        resume.setId(optionalUuid(request.getParameter("id")));
        resume.setUserId(requireUuid(request.getParameter("userId"), "Resume userId is required"));
        resume.setTitle(normalize(request.getParameter("title")));
        resume.setDepartment(normalize(request.getParameter("department")));
        resume.setDegreeLevel(parseEnum(request.getParameter("degreeLevel"), DegreeLevel.class, "Resume degreeLevel is invalid"));
        resume.setGpa(parseBigDecimal(request.getParameter("gpa"), "Resume GPA is invalid"));
        resume.setBio(request.getParameter("bio"));
        resume.setMaxWeeklyHours(parseInteger(request.getParameter("maxWeeklyHours"), "Resume maxWeeklyHours is invalid"));
        resume.setAvailabilityJson(request.getParameter("availabilityJson"));
        return resume;
    }

    private Job readJobFromRequest(HttpServletRequest request) {
        Job job = new Job();
        job.setId(optionalUuid(request.getParameter("id")));
        job.setPostedBy(requireUuid(request.getParameter("postedBy"), "Job postedBy is required"));
        job.setTitle(normalize(request.getParameter("title")));
        job.setModuleCode(normalize(request.getParameter("moduleCode")));
        job.setType(parseEnum(request.getParameter("type"), JobType.class, "Job type is invalid"));
        job.setStatus(parseEnum(request.getParameter("status"), JobStatus.class, "Job status is invalid"));
        job.setDescription(request.getParameter("description"));
        job.setRequiredHours(parseInteger(request.getParameter("requiredHours"), "Job requiredHours is invalid"));
        job.setSlots(parseInteger(request.getParameter("slots"), "Job slots is invalid"));
        job.setStartDate(parseLocalDate(request.getParameter("startDate"), "Job startDate is invalid"));
        job.setEndDate(parseLocalDate(request.getParameter("endDate"), "Job endDate is invalid"));
        job.setDeadline(parseInstant(request.getParameter("deadline"), "Job deadline is invalid"));
        return job;
    }

    private Application readApplicationFromRequest(HttpServletRequest request) {
        Application application = new Application();
        application.setId(optionalUuid(request.getParameter("id")));
        application.setResumeId(requireUuid(request.getParameter("resumeId"), "Application resumeId is required"));
        application.setJobId(requireUuid(request.getParameter("jobId"), "Application jobId is required"));
        application.setStatus(parseEnum(request.getParameter("status"), ApplicationStatus.class, "Application status is invalid"));
        application.setCoverLetter(request.getParameter("coverLetter"));
        application.setReviewedBy(optionalUuid(request.getParameter("reviewedBy")));
        application.setReviewedAt(parseInstant(request.getParameter("reviewedAt"), "Application reviewedAt is invalid"));
        application.setMoNotes(request.getParameter("moNotes"));
        application.setTaRespondedAt(parseInstant(request.getParameter("taRespondedAt"), "Application taRespondedAt is invalid"));
        return application;
    }

    private Map<UUID, String> buildUserLabels(List<User> users) {
        Map<UUID, String> labels = new LinkedHashMap<>();
        for (User user : users) {
            labels.put(user.getId(), user.getFullName() + " (" + user.getEmail() + ")");
        }
        return labels;
    }

    private Map<UUID, String> buildResumeLabels(List<Resume> resumes) {
        Map<UUID, String> labels = new LinkedHashMap<>();
        for (Resume resume : resumes) {
            labels.put(resume.getId(), resume.getTitle());
        }
        return labels;
    }

    private Map<UUID, String> buildJobLabels(List<Job> jobs) {
        Map<UUID, String> labels = new LinkedHashMap<>();
        for (Job job : jobs) {
            labels.put(job.getId(), job.getTitle());
        }
        return labels;
    }

    private String buildRedirectUrl(HttpServletRequest request, String messageKey, String message) {
        return request.getContextPath() + "/db-demo?" + messageKey + "="
                + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }

    private String formatInstantForInput(Instant value) {
        if (value == null) {
            return "";
        }
        return LocalDateTime.ofInstant(value, DEFAULT_ZONE).toString();
    }

    private boolean isUpsertOperation(String operation) {
        return "create".equals(operation) || "update".equals(operation);
    }

    private UUID requireUuid(String value, String message) {
        UUID uuid = optionalUuid(value);
        if (uuid == null) {
            throw new IllegalArgumentException(message);
        }
        return uuid;
    }

    private UUID optionalUuid(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID value: " + normalized, e);
        }
    }

    private BigDecimal parseBigDecimal(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    private int parseInteger(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    private LocalDate parseLocalDate(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    private Instant parseInstant(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(normalized).atZone(DEFAULT_ZONE).toInstant();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumType, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
