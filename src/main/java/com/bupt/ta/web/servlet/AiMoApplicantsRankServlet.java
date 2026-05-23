package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.service.QwenAiService;
import com.bupt.ta.web.security.AiRequestGuard;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JSON comparator {@code POST /ai-mo-applicants-rank}.
 *
 * <p>{@code jobId} targets a recruiter-owned vacancy; at least two in-flight applications seed {@link QwenAiService}'s pairwise ranking JSON payloads.</p>
 *
 * @see AiRequestGuard
 */
@WebServlet("/ai-mo-applicants-rank")
public class AiMoApplicantsRankServlet extends HttpServlet {

    private TaDatabase database;
    private ObjectMapper mapper;

    /** Hydrates collaborators used for entitlement checks before AI fan-out. */
    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.mapper = new ObjectMapper();
    }

    /** Invokes multi-applicant ranking prompts and relays structured text payloads for SPA-side validation. */
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
            json.put("error", "AI is not available: QWEN_API_KEY is not configured on the server.");
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

        UUID jobId;
        try {
            jobId = UUID.fromString(rawJobId.trim());
        } catch (IllegalArgumentException ex) {
            json.put("ok", false);
            json.put("error", "Invalid jobId.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        Job job = database.jobs().findById(jobId).orElse(null);
        if (job == null || job.getPostedBy() == null || !job.getPostedBy().equals(user.getId())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            json.put("ok", false);
            json.put("error", "You can only rank applicants for your own vacancies.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        List<Application> apps = database.applications().listByJobId(jobId).stream()
                .filter(a -> a.getStatus() != ApplicationStatus.WITHDRAWN)
                .toList();

        if (apps.size() < 2) {
            json.put("ok", false);
            json.put("error", "Need at least two non-withdrawn applications for this vacancy.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        String labels = job.getLabels() == null || job.getLabels().isEmpty()
                ? ""
                : String.join(", ", job.getLabels());

        List<QwenAiService.MoApplicantSnippet> snippets = new ArrayList<>();
        Set<String> allowed = new HashSet<>();
        for (Application app : apps) {
            Resume resume = database.resumes().findById(app.getResumeId()).orElse(null);
            if (resume == null) {
                continue;
            }
            String degree = resume.getDegreeLevel() != null ? resume.getDegreeLevel().name() : "";
            String gpa = resume.getGpa() != null ? resume.getGpa().toPlainString() : "";
            snippets.add(new QwenAiService.MoApplicantSnippet(
                    app.getId().toString(),
                    toDisplayStatus(app.getStatus()),
                    safe(resume.getTitle(), "Resume"),
                    safe(resume.getDepartment(), ""),
                    degree,
                    gpa,
                    safeExcerpt(resume.getBio(), 600),
                    safeExcerpt(app.getCoverLetter(), 800)));
            allowed.add(app.getId().toString());
        }

        if (snippets.size() < 2) {
            json.put("ok", false);
            json.put("error", "Not enough resume data to compare applicants.");
            mapper.writeValue(resp.getWriter(), json);
            return;
        }

        try {
            AiRequestGuard.appendAudit(database, user, "mo-applicants-rank", jobId);
            QwenAiService ai = new QwenAiService(
                    apiKey,
                    QwenAiService.resolveVlModel(),
                    QwenAiService.resolveTextModel());
            String raw = ai.rankMoApplicantsForJobJson(
                    job.getTitle(),
                    job.getModuleCode(),
                    job.getDescription(),
                    labels,
                    snippets);

            ArrayNode rankings = parseAndNormalizeRankings(raw, allowed);
            json.put("ok", true);
            json.set("rankings", rankings);
            json.put("vacancyTitle", job.getTitle() == null ? "" : job.getTitle());
        } catch (Exception ex) {
            json.put("ok", false);
            json.put("error", ex.getMessage() == null ? "AI ranking failed." : ex.getMessage());
        }

        mapper.writeValue(resp.getWriter(), json);
    }

    private ArrayNode parseAndNormalizeRankings(String raw, Set<String> allowedIds) throws IOException {
        JsonNode root = extractJsonArray(mapper, raw);
        if (!root.isArray()) {
            throw new IOException("Model did not return a JSON array.");
        }

        List<ObjectNode> rows = new ArrayList<>();
        for (JsonNode el : root) {
            if (!el.isObject()) {
                continue;
            }
            String aid = el.path("applicationId").asText("").trim();
            if (!allowedIds.contains(aid)) {
                continue;
            }
            int rank = el.path("rank").asInt(-1);
            int fit = el.path("fitScore").asInt(-1);
            String note = el.path("note").asText("");
            if (note.length() > 160) {
                note = note.substring(0, 157) + "...";
            }
            ObjectNode row = mapper.createObjectNode();
            row.put("applicationId", aid);
            row.put("rank", rank);
            row.put("fitScore", Math.max(0, Math.min(100, fit)));
            row.put("note", note);
            rows.add(row);
        }

        rows.sort(Comparator.comparingInt(r -> r.path("rank").asInt(9999)));

        Set<String> seen = new HashSet<>();
        List<ObjectNode> dedup = new ArrayList<>();
        for (ObjectNode r : rows) {
            String id = r.path("applicationId").asText();
            if (seen.add(id)) {
                dedup.add(r);
            }
        }

        if (dedup.size() < allowedIds.size()) {
            Set<String> missing = allowedIds.stream()
                    .filter(id -> dedup.stream().noneMatch(n -> id.equals(n.path("applicationId").asText())))
                    .collect(Collectors.toSet());
            int nextRank = dedup.stream().mapToInt(n -> n.path("rank").asInt(0)).max().orElse(0) + 1;
            for (String id : missing) {
                ObjectNode row = mapper.createObjectNode();
                row.put("applicationId", id);
                row.put("rank", nextRank++);
                row.put("fitScore", -1);
                row.put("note", "Not ranked by model — please review manually.");
                dedup.add(row);
            }
            dedup.sort(Comparator.comparingInt(r -> r.path("rank").asInt(9999)));
        }

        ArrayNode out = mapper.createArrayNode();
        for (ObjectNode r : dedup) {
            out.add(r);
        }
        return out;
    }

    private static JsonNode extractJsonArray(ObjectMapper mapper, String raw) throws IOException {
        String t = raw == null ? "" : raw.trim();
        if (t.startsWith("```")) {
            int firstNl = t.indexOf('\n');
            if (firstNl >= 0) {
                t = t.substring(firstNl + 1).trim();
            }
            if (t.endsWith("```")) {
                t = t.substring(0, t.length() - 3).trim();
            }
        }
        int lo = t.indexOf('[');
        int hi = t.lastIndexOf(']');
        if (lo < 0 || hi <= lo) {
            throw new IOException("Could not find a JSON array in the model response.");
        }
        return mapper.readTree(t.substring(lo, hi + 1));
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

    private static String safe(String v, String fb) {
        return v == null || v.isBlank() ? fb : v;
    }

    private static String safeExcerpt(String v, int max) {
        if (v == null || v.isBlank()) {
            return "";
        }
        String s = v.trim();
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private static User currentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object v = session.getAttribute("currentUser");
        return v instanceof User u ? u : null;
    }
}
