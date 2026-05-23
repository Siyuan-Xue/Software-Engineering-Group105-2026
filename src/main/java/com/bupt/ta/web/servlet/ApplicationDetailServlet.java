package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.MatchScore;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.MatchingService;
import com.bupt.ta.util.ApplicationSubmissionFiles;
import com.bupt.ta.util.ResumeFilePaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Renders the application detail view for the vacancy owner or the applicant.
 *
 * <p>Module Organisers receive the full screening view, including rule-based and
 * fallback match scores. Teaching Assistants receive only applicant-facing status
 * and skill-gap feedback because analytical scores remain hidden behind MO-only visibility controls.</p>
 *
 * @see MatchingService
 * @see ApplicationService
 */
@WebServlet("/application/detail")
public class ApplicationDetailServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/application_detail.jsp";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    private TaDatabase database;
    private ApplicationService applicationService;
    private MatchingService matchingService;

    /** Instantiates services sharing the servlet-scoped facade. */
    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.applicationService = new ApplicationService(database);
        this.matchingService = new MatchingService(database);
    }

    /** Requires {@code applicationId} UUID parameters and renders role-scoped payloads. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "auth.loginRequired"), StandardCharsets.UTF_8));
            return;
        }
        String rawId = req.getParameter("applicationId");
        if (rawId == null || rawId.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/applications?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.appDecisionMissing"), StandardCharsets.UTF_8));
            return;
        }

        UUID applicationId;
        try {
            applicationId = UUID.fromString(rawId.trim());
        } catch (IllegalArgumentException ex) {
            resp.sendRedirect(req.getContextPath() + "/applications?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.appInvalidIds"), StandardCharsets.UTF_8));
            return;
        }

        Application application = database.applications().findById(applicationId).orElse(null);
        if (application == null) {
            resp.sendRedirect(req.getContextPath() + "/applications?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.appNotFound"), StandardCharsets.UTF_8));
            return;
        }

        Resume resume = database.resumes().findById(application.getResumeId()).orElse(null);
        boolean canMoManage = false;
        boolean canTaView = false;
        if (currentUser.getRole() == UserRole.MO) {
            try {
                applicationService.assertMoOwnsApplication(currentUser.getId(), application);
                canMoManage = true;
            } catch (RuntimeException ex) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
                return;
            }
        } else if (currentUser.getRole() == UserRole.TA && resume != null && currentUser.getId().equals(resume.getUserId())) {
            canTaView = true;
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You can only view applicant details for your own applications or vacancies");
            return;
        }

        if ("download".equalsIgnoreCase(param(req, "mode"))) {
            streamResumeFile(application, resp);
            return;
        }

        Job job = database.jobs().findById(application.getJobId()).orElse(null);
        User applicant = resume == null ? null : database.users().findById(resume.getUserId()).orElse(null);
        MatchScore matchScore = database.matchScores().findByApplicationId(application.getId()).orElse(null);

        req.setAttribute("successMessage", param(req, "successMessage"));
        req.setAttribute("errorMessage", param(req, "errorMessage"));
        req.setAttribute("applicationId", application.getId());
        req.setAttribute("canMoManage", canMoManage);
        req.setAttribute("canTaView", canTaView);
        req.setAttribute("canTaRespond", canTaView && application.getStatus() == ApplicationStatus.OFFER_PENDING);
        req.setAttribute("canRunMatchAnalysis", canMoManage && application.getStatus() == ApplicationStatus.REVIEWING);
        req.setAttribute("applicationStatus", toDisplayStatus(application.getStatus()));
        req.setAttribute("applicationStatusRaw", application.getStatus().name());
        req.setAttribute("appliedDate", application.getCreatedAt() == null
                ? ""
                : DATE_FORMATTER.format(application.getCreatedAt()));
        req.setAttribute("coverLetter", application.getCoverLetter());
        req.setAttribute("moNotes", application.getMoNotes());
        req.setAttribute("vacancyTitle", job == null ? "Unknown Job" : nullToEmpty(job.getTitle()));
        req.setAttribute("courseCode", job == null ? "N/A" : nullToEmpty(job.getModuleCode()));
        req.setAttribute("applicantName", applicant == null ? "Unknown applicant" : nullToEmpty(applicant.getFullName()));
        req.setAttribute("applicantEmail", applicant == null ? "" : nullToEmpty(applicant.getEmail()));
        req.setAttribute("studentId", applicant == null ? "" : nullToEmpty(applicant.getStudentId()));
        boolean fileAvailable = ApplicationSubmissionFiles.isAvailable(application)
                || (resume != null && ResumeFilePaths.isAvailable(resume));
        req.setAttribute("resumeFileAvailable", fileAvailable);
        if (resume != null) {
            req.setAttribute("resumeTitle", nullToEmpty(resume.getTitle()));
            req.setAttribute("resumeDepartment", nullToEmpty(resume.getDepartment()));
            req.setAttribute("resumeBio", nullToEmpty(resume.getBio()));
            req.setAttribute("resumeDegree", resume.getDegreeLevel() == null ? "" : resume.getDegreeLevel().name());
            req.setAttribute("resumeGpa", resume.getGpa() == null ? "" : resume.getGpa().toPlainString());
        }
        String displayFileName = application.getSubmittedFileName();
        if (displayFileName == null || displayFileName.isBlank()) {
            displayFileName = resume == null ? "" : nullToEmpty(resume.getOriginalFileName());
        }
        req.setAttribute("resumeFileName", displayFileName);
        if (matchScore != null && canMoManage) {
            req.setAttribute("matchScore", matchScore);
            req.setAttribute("matchComputedAt", matchScore.getComputedAt() == null
                    ? ""
                    : DATE_FORMATTER.format(matchScore.getComputedAt()));
        } else if (matchScore != null && canTaView) {
            List<String> suggestions = matchScore.getMissingSkillSuggestions();
            req.setAttribute("taSkillGapSuggestions", suggestions);
            req.setAttribute("taFeedbackAvailable", true);
        }
        if (resume != null && job != null && canMoManage) {
            req.setAttribute("skillCoverage", matchingService.computeCoverage(resume.getId(), job.getId()));
        }

        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private void streamResumeFile(Application application, HttpServletResponse resp) throws IOException {
        Path filePath = ApplicationSubmissionFiles.resolve(application);
        Resume resume = database.resumes().findById(application.getResumeId()).orElse(null);
        if (filePath == null && resume != null) {
            filePath = ResumeFilePaths.resolve(resume);
        }
        if (filePath == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resume file not found on disk");
            return;
        }

        String fileName = application.getSubmittedFileName();
        if (fileName == null || fileName.isBlank()) {
            fileName = resume != null && resume.getOriginalFileName() != null && !resume.getOriginalFileName().isBlank()
                    ? resume.getOriginalFileName()
                    : filePath.getFileName().toString();
        }
        resp.setContentType(contentTypeFor(fileName));
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Content-Disposition", contentDispositionAttachment(fileName));
        Files.copy(filePath, resp.getOutputStream());
    }

    private static String contentDispositionAttachment(String fileName) {
        String fallback = fileName == null ? "resume" : fileName.replace("\"", "").replace("\\", "");
        String encoded = URLEncoder.encode(fallback, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + fallback + "\"; filename*=UTF-8''" + encoded;
    }

    private static String contentTypeFor(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".txt")) {
            return "text/plain;charset=UTF-8";
        }
        if (lower.endsWith(".doc")) {
            return "application/msword";
        }
        if (lower.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";
    }

    private static String toDisplayStatus(ApplicationStatus status) {
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

    private static String param(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
