package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.service.QwenAiService;
import com.bupt.ta.web.security.AiRequestGuard;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JSON helper {@code POST /ai-mo-application-advice} providing coaching copy for recruiter review panes.
 *
 * <p>Requires Module Organiser sessions whose vacancies own the referenced {@code applicationId}.
 * Responses remain advisory; downstream UI layers render legal disclaimers separately.</p>
 *
 * @see AiRequestGuard
 */
@WebServlet("/ai-mo-application-advice")
public class AiMoApplicationAdviceServlet extends HttpServlet {

    private TaDatabase database;
    private ObjectMapper mapper;

    /** Acquires persistence access for deterministic ownership checks ahead of prompting. */
    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.mapper = new ObjectMapper();
    }

    /** Streams Markdown guidance payloads mirroring dashboard expectations. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        ObjectNode json = mapper.createObjectNode();

        User user = currentUser(req.getSession(false));
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            json.put("ok", false);
            json.put("error", "Please log in first.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }
        if (user.getRole() != UserRole.MO) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            json.put("ok", false);
            json.put("error", "Only module organisers can use this feature.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }
        if (!AiRequestGuard.requireConsent(req, resp, mapper)) {
            return;
        }

        String apiKey = QwenAiService.resolveApiKey();
        if (apiKey == null) {
            json.put("ok", false);
            json.put("error", "AI is not available: QWEN_API_KEY is not configured.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        String rawId = req.getParameter("applicationId");
        if (rawId == null || rawId.isBlank()) {
            json.put("ok", false);
            json.put("error", "applicationId is required.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        UUID applicationId;
        try {
            applicationId = UUID.fromString(rawId.trim());
        } catch (IllegalArgumentException ex) {
            json.put("ok", false);
            json.put("error", "Invalid applicationId.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        Application application = database.applications().findById(applicationId).orElse(null);
        if (application == null) {
            json.put("ok", false);
            json.put("error", "Application not found.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        Job job = database.jobs().findById(application.getJobId()).orElse(null);
        if (job == null || job.getPostedBy() == null || !job.getPostedBy().equals(user.getId())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            json.put("ok", false);
            json.put("error", "You can only request advice for applications to your own vacancies.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        Resume resume = database.resumes().findById(application.getResumeId()).orElse(null);
        if (resume == null) {
            json.put("ok", false);
            json.put("error", "Resume not found for this application.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        String applicantName = database.users().findById(resume.getUserId())
                .map(User::getFullName)
                .orElse("Unknown applicant");

        String labels = job.getLabels() == null || job.getLabels().isEmpty()
                ? ""
                : job.getLabels().stream().collect(Collectors.joining(", "));

        String statusDisplay = switch (application.getStatus()) {
            case PENDING -> "Submitted";
            case REVIEWING -> "Under Review";
            case OFFER_PENDING -> "Offer Pending";
            case ACCEPTED -> "Accepted";
            case REJECTED -> "Rejected";
            case DECLINED -> "Declined";
            case WITHDRAWN -> "Withdrawn";
        };

        String degree = resume.getDegreeLevel() != null ? resume.getDegreeLevel().name() : "";
        String gpa = resume.getGpa() != null ? resume.getGpa().toPlainString() : "";

        try {
            AiRequestGuard.appendAudit(database, user, "mo-application-advice", applicationId);
            QwenAiService ai = new QwenAiService(
                    apiKey,
                    QwenAiService.resolveVlModel(),
                    QwenAiService.resolveTextModel());
            String markdown = ai.suggestMoOfferRejectAdvice(
                    job.getTitle(),
                    job.getModuleCode(),
                    job.getDescription(),
                    labels,
                    statusDisplay,
                    application.getCoverLetter(),
                    resume.getTitle(),
                    resume.getDepartment(),
                    degree,
                    gpa,
                    resume.getBio(),
                    applicantName);
            json.put("ok", true);
            json.put("markdown", markdown);
        } catch (Exception ex) {
            json.put("ok", false);
            json.put("error", ex.getMessage() == null ? "AI request failed." : ex.getMessage());
        }

        mapper.writeValue(resp.getWriter(), json);
    }

    private static User currentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object v = session.getAttribute("currentUser");
        return v instanceof User u ? u : null;
    }
}
