package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.service.QwenAiService;
import com.bupt.ta.service.QwenAiService.JobInfo;
import com.bupt.ta.service.ResumeService;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Authenticated JSON endpoint {@code POST /ai-match}.
 *
 * <p>{@code ids[]} enumerates vacancy keys that {@link QwenAiService} batch scores against an inferred primary resume snapshot.
 * Consent gating is enforced via {@link AiRequestGuard}; missing DashScope secrets produce structured error payloads rather than redirects.</p>
 */
@WebServlet("/ai-match")
public class AiMatchServlet extends HttpServlet {

    private TaDatabase    database;
    private ResumeService resumeService;
    private ObjectMapper  mapper;

    /** Acquires collaborators for resume discovery and DashScope-compatible JSON replies. */
    @Override
    public void init() throws ServletException {
        this.database      = DatabaseProvider.get(getServletContext());
        this.resumeService = new ResumeService(database);
        this.mapper        = new ObjectMapper();
    }

    /** Streams heuristic vacancy fit scores keyed by vacancy UUID strings. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        ObjectNode json = mapper.createObjectNode();

        User user = currentUser(req);
        if (user == null) {
            resp.setStatus(401);
            json.put("ok", false);
            json.put("error", "Please log in first.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }
        if (!AiRequestGuard.requireConsent(req, resp, mapper)) {
            return;
        }

        String apiKey = QwenAiService.resolveApiKey();
        if (apiKey == null) {
            json.put("ok", false);
            json.put("error", "AI matching is not available: QWEN_API_KEY is not configured.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        String[] rawIds = req.getParameterValues("ids[]");
        if (rawIds == null || rawIds.length == 0) {
            json.put("ok", false);
            json.put("error", "No vacancy IDs provided.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        List<JobInfo> jobInfos = new ArrayList<>();
        for (String raw : rawIds) {
            try {
                UUID id = UUID.fromString(raw.trim());
                Optional<Job> opt = database.jobs().findById(id);
                opt.ifPresent(job -> jobInfos.add(new JobInfo(
                        job.getId().toString(),
                        nvl(job.getTitle(), "Untitled"),
                        nvl(job.getDescription(), ""),
                        nvl(job.getModuleCode(), "CS").toUpperCase().startsWith("MATH") ? "MATH" : "CS"
                )));
            } catch (IllegalArgumentException ignored) { /* skip bad UUIDs */ }
        }

        if (jobInfos.isEmpty()) {
            json.put("ok", false);
            json.put("error", "No valid vacancies found for the given IDs.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        List<Resume> resumes = resumeService.listByUserId(user.getId());

        String dept   = user.getDepartment();
        String degree = null;
        BigDecimal bestGpa = null;
        StringBuilder bioBuilder = new StringBuilder();

        for (Resume r : resumes) {
            if (r.getDepartment() != null && !r.getDepartment().isBlank()) dept = r.getDepartment();
            if (r.getDegreeLevel() != null) degree = r.getDegreeLevel().name();
            if (r.getGpa() != null) {
                if (bestGpa == null || r.getGpa().compareTo(bestGpa) > 0) bestGpa = r.getGpa();
            }
            if (r.getBio() != null && !r.getBio().isBlank()) {
                if (bioBuilder.length() > 0) bioBuilder.append(" | ");
                bioBuilder.append(r.getBio());
            }
        }

        String gpa = bestGpa != null ? bestGpa.toPlainString() : null;
        String bio = bioBuilder.length() > 0
                ? bioBuilder.substring(0, Math.min(bioBuilder.length(), 600)) : null;
        String title = resumes.isEmpty() ? null :
                resumes.get(resumes.size() - 1).getTitle();

        try {
            AiRequestGuard.appendAudit(database, user, "ta-job-match", user.getId());
            QwenAiService ai = new QwenAiService(
                    apiKey,
                    QwenAiService.resolveVlModel(),
                    QwenAiService.resolveTextModel());

            Map<String, Integer> scores = ai.batchScoreJobs(title, dept, degree, gpa, bio, jobInfos);

            json.put("ok", true);
            json.put("hasResume", !resumes.isEmpty());
            ObjectNode scoresNode = mapper.createObjectNode();
            scores.forEach(scoresNode::put);
            json.set("scores", scoresNode);

        } catch (Exception e) {
            json.put("ok", false);
            json.put("error", "AI scoring failed: " + e.getMessage());
        }

        mapper.writeValue(resp.getWriter(), json);
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null ? (User) s.getAttribute("currentUser") : null;
    }

    private static String nvl(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}
