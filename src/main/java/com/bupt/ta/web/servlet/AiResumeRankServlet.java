package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.service.QwenAiService;
import com.bupt.ta.service.QwenAiService.ResumeInfo;
import com.bupt.ta.service.ResumeService;
import com.bupt.ta.web.security.AiRequestGuard;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Authenticated JSON endpoint {@code POST /ai-resume-rank}.
 *
 * <p>Given {@code jobId}, {@link QwenAiService} scores every resume belonging to the session principal and emits a deterministic ranking array powering apply-modals.</p>
 *
 * @see AiRequestGuard
 */
@WebServlet("/ai-resume-rank")
public class AiResumeRankServlet extends HttpServlet {

    private TaDatabase    database;
    private ResumeService resumeService;
    private ObjectMapper  mapper;

    /** Wires resume catalogue access used for prompting heuristics. */
    @Override
    public void init() throws ServletException {
        this.database      = DatabaseProvider.get(getServletContext());
        this.resumeService = new ResumeService(database);
        this.mapper        = new ObjectMapper();
    }

    /** Executes ranking prompts and emits JSON payloads understood by AJAX clients on vacancy detail surfaces. */
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
            json.put("error", "AI ranking is not available: QWEN_API_KEY is not configured.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        String rawJobId = req.getParameter("jobId");
        if (rawJobId == null || rawJobId.isBlank()) {
            json.put("ok", false);
            json.put("error", "jobId is required.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        Job job;
        try {
            UUID jobId = UUID.fromString(rawJobId.trim());
            Optional<Job> opt = database.jobs().findById(jobId);
            if (opt.isEmpty()) {
                json.put("ok", false);
                json.put("error", "Vacancy not found.");
                mapper.writeValue(resp.getWriter(), json);
                return;
            }
            job = opt.get();
        } catch (IllegalArgumentException e) {
            json.put("ok", false);
            json.put("error", "Invalid jobId format.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        List<Resume> resumes = resumeService.listByUserId(user.getId());
        if (resumes.isEmpty()) {
            json.put("ok", true);
            json.put("hasResumes", false);
            json.set("rankings", mapper.createArrayNode());
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        // Build ResumeInfo list
        List<ResumeInfo> resumeInfos = new ArrayList<>();
        for (Resume r : resumes) {
            resumeInfos.add(new ResumeInfo(
                    r.getId().toString(),
                    nvl(r.getTitle(), "Untitled Resume"),
                    r.getDepartment(),
                    r.getDegreeLevel() != null ? r.getDegreeLevel().name() : null,
                    r.getGpa() != null ? r.getGpa().toPlainString() : null,
                    r.getBio()
            ));
        }

        try {
            AiRequestGuard.appendAudit(database, user, "ta-resume-rank", job.getId());
            QwenAiService ai = new QwenAiService(
                    apiKey,
                    QwenAiService.resolveVlModel(),
                    QwenAiService.resolveTextModel());

            String dept = job.getModuleCode() != null &&
                    job.getModuleCode().toUpperCase().startsWith("MATH") ? "MATH" : "CS";

            Map<String, Integer> scores = ai.rankResumesForJob(
                    nvl(job.getTitle(), "TA Position"),
                    nvl(job.getDescription(), ""),
                    dept,
                    resumeInfos
            );

            // Find the top score to mark as recommended
            int topScore = scores.values().stream().mapToInt(Integer::intValue).max().orElse(0);

            ArrayNode rankings = mapper.createArrayNode();
            for (ResumeInfo ri : resumeInfos) {
                int score = scores.getOrDefault(ri.id(), -1);
                ObjectNode entry = mapper.createObjectNode();
                entry.put("resumeId",    ri.id());
                entry.put("title",       ri.title());
                entry.put("score",       score);
                entry.put("recommended", score >= 0 && score == topScore && score > 50);
                rankings.add(entry);
            }

            json.put("ok", true);
            json.put("hasResumes", true);
            json.set("rankings", rankings);

        } catch (Exception e) {
            json.put("ok", false);
            json.put("error", "AI ranking failed: " + e.getMessage());
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
