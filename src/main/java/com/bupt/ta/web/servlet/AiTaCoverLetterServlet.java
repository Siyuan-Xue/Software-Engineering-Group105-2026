package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
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
 * JSON API: POST /ai-ta-cover-letter
 * Body parameters: vacancyId and resumeId are both required. TA only; resume must belong to current user.
 */
@WebServlet("/ai-ta-cover-letter")
public class AiTaCoverLetterServlet extends HttpServlet {

    private TaDatabase database;
    private ObjectMapper mapper;

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.mapper = new ObjectMapper();
    }

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
        if (user.getRole() != UserRole.TA) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            json.put("ok", false);
            json.put("error", "Only teaching assistants can use this feature.");
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

        String rawVacancy = req.getParameter("vacancyId");
        String rawResume = req.getParameter("resumeId");
        if (rawVacancy == null || rawVacancy.isBlank() || rawResume == null || rawResume.isBlank()) {
            json.put("ok", false);
            json.put("error", "vacancyId and resumeId are required.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        UUID vacancyId;
        UUID resumeId;
        try {
            vacancyId = UUID.fromString(rawVacancy.trim());
            resumeId = UUID.fromString(rawResume.trim());
        } catch (IllegalArgumentException ex) {
            json.put("ok", false);
            json.put("error", "Invalid vacancyId or resumeId.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        Resume resume = database.resumes().findById(resumeId).orElse(null);
        if (resume == null || !user.getId().equals(resume.getUserId())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            json.put("ok", false);
            json.put("error", "Resume not found or does not belong to you.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        Job job = database.jobs().findById(vacancyId).orElse(null);
        if (job == null) {
            json.put("ok", false);
            json.put("error", "Vacancy not found.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        String labels = job.getLabels() == null || job.getLabels().isEmpty()
                ? ""
                : job.getLabels().stream().collect(Collectors.joining(", "));

        String degree = resume.getDegreeLevel() != null ? resume.getDegreeLevel().name() : "";
        String gpa = resume.getGpa() != null ? resume.getGpa().toPlainString() : "";

        try {
            AiRequestGuard.appendAudit(database, user, "ta-cover-letter", vacancyId);
            QwenAiService ai = new QwenAiService(
                    apiKey,
                    QwenAiService.resolveVlModel(),
                    QwenAiService.resolveTextModel());
            String markdown = ai.draftTaCoverLetterMotivation(
                    job.getTitle(),
                    job.getModuleCode(),
                    job.getDescription(),
                    labels,
                    resume.getTitle(),
                    resume.getDepartment(),
                    degree,
                    gpa,
                    resume.getBio());
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
