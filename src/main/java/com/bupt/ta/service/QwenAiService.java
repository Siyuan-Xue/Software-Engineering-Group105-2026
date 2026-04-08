package com.bupt.ta.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calls the DashScope-compatible Qwen VL API.
 *
 * Two main functions:
 *   1. analyzeResumeForOptimization() – used on the Resumes page.
 *      Focuses on improving the student's resume quality.
 *   2. batchScoreJobs()               – used on the Vacancies page.
 *      Rates how well the student matches each open TA position (returns 0-100 score).
 *
 * Configuration (set in D:\Programs\Tomcat 11.0\bin\setenv.bat):
 *   set QWEN_API_KEY=sk-xxxxxxxxxxxxxxxx
 *   set QWEN_MODEL=qwen-vl-max-latest        (optional, default shown)
 *   set QWEN_TEXT_MODEL=qwen-plus-latest     (optional, used for text-only batch scoring)
 */
public class QwenAiService {

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    // Default VL model: Qwen2.5-VL 72B (multimodal, for image resume analysis)
    private static final String DEFAULT_VL_MODEL   = "qwen2.5-vl-72b-instruct";
    // Default text model: Qwen3.5-Plus (text-only, for batch scoring & ranking)
    private static final String DEFAULT_TEXT_MODEL = "qwen3.5-plus";
    private static final int MAX_TEXT_CHARS   = 6000;
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    // ── Descriptors used for batch scoring ────────────────────────────────────
    public record JobInfo(String id, String title, String description, String department) {}
    public record ResumeInfo(String id, String title, String department, String degree, String gpa, String bio) {}

    private final String apiKey;
    private final String vlModel;
    private final String textModel;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public QwenAiService(String apiKey, String vlModel, String textModel) {
        this.apiKey     = apiKey;
        this.vlModel    = (vlModel    != null && !vlModel.isBlank())    ? vlModel    : DEFAULT_VL_MODEL;
        this.textModel  = (textModel  != null && !textModel.isBlank())  ? textModel  : DEFAULT_TEXT_MODEL;
    }

    // ── static factory / config resolution ───────────────────────────────────

    public static String resolveApiKey() {
        String v = System.getenv("QWEN_API_KEY");
        return (v != null && !v.isBlank()) ? v : null;
    }

    public static String resolveVlModel() {
        String v = System.getProperty("QWEN_MODEL");
        if (v != null && !v.isBlank()) return v;
        v = System.getenv("QWEN_MODEL");
        return (v != null && !v.isBlank()) ? v : DEFAULT_VL_MODEL;
    }

    public static String resolveTextModel() {
        String v = System.getProperty("QWEN_TEXT_MODEL");
        if (v != null && !v.isBlank()) return v;
        v = System.getenv("QWEN_TEXT_MODEL");
        return (v != null && !v.isBlank()) ? v : DEFAULT_TEXT_MODEL;
    }

    public static QwenAiService create() {
        return new QwenAiService(resolveApiKey(), resolveVlModel(), resolveTextModel());
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. Resume Optimisation  (Resumes page)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Analyses a student's resume and returns coaching advice on how to improve it.
     * If an image/txt file is attached, it performs multimodal analysis.
     *
     * @param title        resume title
     * @param department   applicant's department
     * @param degreeLevel  e.g. "MASTER"
     * @param gpa          GPA string
     * @param bio          personal statement
     * @param uploadedFile path to uploaded file (jpg/png/txt/pdf/docx), or null
     * @param vacancyContext list of vacancy titles for context (to tailor suggestions)
     * @return markdown-formatted improvement advice
     */
    public String analyzeResumeForOptimization(
            String title,
            String department,
            String degreeLevel,
            String gpa,
            String bio,
            String uploadedFile,
            List<String> vacancyContext) throws Exception {

        if (!isConfigured()) throw new IllegalStateException("Qwen API key is not configured.");

        ArrayNode contentParts = mapper.createArrayNode();
        boolean hasVisualContent = false;

        if (uploadedFile != null && !uploadedFile.isBlank()) {
            Path fp = Path.of(uploadedFile);
            if (Files.exists(fp)) {
                String name = fp.getFileName().toString().toLowerCase();

                if ((name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))
                        && Files.size(fp) <= MAX_IMAGE_BYTES) {
                    byte[] bytes  = Files.readAllBytes(fp);
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    String mime   = name.endsWith(".png") ? "image/png" : "image/jpeg";
                    ObjectNode img = mapper.createObjectNode();
                    img.put("type", "image_url");
                    ObjectNode url = mapper.createObjectNode();
                    url.put("url", "data:" + mime + ";base64," + base64);
                    img.set("image_url", url);
                    contentParts.add(img);
                    hasVisualContent = true;
                } else if (name.endsWith(".txt")) {
                    String text = Files.readString(fp);
                    if (text.length() > MAX_TEXT_CHARS) text = text.substring(0, MAX_TEXT_CHARS) + "\n...[truncated]";
                    addText(contentParts, "Resume text content:\n" + text + "\n\n");
                } else if (name.endsWith(".pdf")) {
                    addText(contentParts, "[Attached PDF: " + fp.getFileName() + "]\n");
                } else if (name.endsWith(".docx") || name.endsWith(".doc")) {
                    addText(contentParts, "[Attached Word document: " + fp.getFileName() + "]\n");
                }
            }
        }

        addText(contentParts, buildOptimizationUserPrompt(title, department, degreeLevel, gpa, bio, hasVisualContent));

        String sysPrompt = buildOptimizationSystemPrompt(vacancyContext);
        // VL model: keep thinking enabled for richer resume coaching output
        return chat(vlModel, sysPrompt, contentParts, 1800, 0.65, false);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. Batch Job Match Scoring  (Vacancies page)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Scores how well the student's resume matches each of the given TA jobs.
     *
     * @param title      resume title (may be null)
     * @param department applicant's department
     * @param degree     degree level
     * @param gpa        GPA string
     * @param bio        personal statement
     * @param jobs       list of jobs to score
     * @return map of jobId → score (0-100). Missing entries mean scoring failed for that job.
     */
    public Map<String, Integer> batchScoreJobs(
            String title,
            String department,
            String degree,
            String gpa,
            String bio,
            List<JobInfo> jobs) throws Exception {

        if (!isConfigured()) throw new IllegalStateException("Qwen API key is not configured.");
        if (jobs == null || jobs.isEmpty()) return Map.of();

        String prompt = buildBatchScoringPrompt(title, department, degree, gpa, bio, jobs);
        ArrayNode parts = mapper.createArrayNode();
        addText(parts, prompt);

        // disable thinking: we need clean JSON without CoT traces
        String raw = chat(textModel, buildBatchSystemPrompt(), parts, 600, 0.0, true);
        return parseBatchScores(raw, jobs);
    }

    // ── private: resume optimisation helpers ──────────────────────────────────

    private String buildOptimizationSystemPrompt(List<String> vacancyContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert academic resume coach who specialises in helping students ");
        sb.append("craft outstanding resumes for Teaching Assistant (TA) positions at university.\n\n");
        sb.append("Your goal is to provide **detailed, actionable advice** on how to improve the student's resume. ");
        sb.append("Focus on: clarity, relevance, completeness, academic tone, and TA-specific requirements.\n\n");

        if (vacancyContext != null && !vacancyContext.isEmpty()) {
            sb.append("The student is targeting roles such as:\n");
            int limit = Math.min(vacancyContext.size(), 8);
            for (int i = 0; i < limit; i++) sb.append("- ").append(vacancyContext.get(i)).append("\n");
            sb.append("\nTailor your advice to these types of positions.\n\n");
        }

        sb.append("Format your response with these exact sections:\n");
        sb.append("1. **Resume Quality Score** – overall quality rating (e.g. 72/100) and one-line verdict\n");
        sb.append("2. **What's Working Well** – 3-4 specific strengths in the current resume\n");
        sb.append("3. **Critical Gaps** – the most important missing elements for TA applications\n");
        sb.append("4. **Rewrite Suggestions** – concrete before/after examples for weak sections\n");
        sb.append("5. **Keyword & Phrasing Tips** – specific language and keywords to add\n");
        sb.append("6. **Next Steps** – a numbered action plan (3-5 steps) the student should take today\n");
        return sb.toString();
    }

    private String buildOptimizationUserPrompt(
            String title, String dept, String degree, String gpa, String bio,
            boolean hasImage) {
        StringBuilder sb = new StringBuilder();
        if (hasImage) {
            sb.append("Please review the resume image shown above and the profile data below.\n\n");
        } else {
            sb.append("Please review the following resume profile and suggest how to improve it.\n\n");
        }
        sb.append("**Current Resume Profile**\n");
        sb.append("- Title: ").append(nvl(title)).append("\n");
        sb.append("- Department: ").append(nvl(dept)).append("\n");
        sb.append("- Degree Level: ").append(nvl(degree)).append("\n");
        sb.append("- GPA: ").append(nvl(gpa)).append("\n");
        if (bio != null && !bio.isBlank()) {
            sb.append("- Personal Statement / Bio:\n  ").append(bio.replace("\n", "\n  ")).append("\n");
        }
        sb.append("\nPlease focus on how to make this resume stronger for TA applications.\n");
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. Rank Resumes for a Specific Job  (vacancy_detail apply modal)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Scores each of the user's resumes against a single TA job posting.
     * Returns a map of resumeId → score (0-100).
     */
    public Map<String, Integer> rankResumesForJob(
            String jobTitle,
            String jobDescription,
            String jobDepartment,
            List<ResumeInfo> resumes) throws Exception {

        if (!isConfigured()) throw new IllegalStateException("Qwen API key is not configured.");
        if (resumes == null || resumes.isEmpty()) return Map.of();

        StringBuilder sb = new StringBuilder();
        sb.append("Rate how well each student resume matches the following TA job (integer 0-100).\n\n");
        sb.append("Target job:\n");
        sb.append("- Title: ").append(nvl(jobTitle)).append("\n");
        sb.append("- Department: ").append(nvl(jobDepartment)).append("\n");
        if (jobDescription != null && !jobDescription.isBlank()) {
            sb.append("- Description: ").append(jobDescription, 0, Math.min(jobDescription.length(), 200)).append("\n");
        }

        sb.append("\nResumes to score (use exact IDs as keys):\n");
        for (ResumeInfo r : resumes) {
            sb.append("ID \"").append(r.id()).append("\": ");
            sb.append(nvl(r.title())).append(", dept=").append(nvl(r.department()));
            sb.append(", degree=").append(nvl(r.degree())).append(", GPA=").append(nvl(r.gpa()));
            if (r.bio() != null && !r.bio().isBlank()) {
                sb.append(", bio=\"").append(r.bio(), 0, Math.min(r.bio().length(), 150)).append("\"");
            }
            sb.append("\n");
        }
        sb.append("\nRespond ONLY with a JSON object mapping each resume ID to a score integer, e.g.:\n");
        sb.append("{\"id-one\": 85, \"id-two\": 62}\n");

        ArrayNode parts = mapper.createArrayNode();
        addText(parts, sb.toString());
        // disable thinking: need clean JSON output
        String raw = chat(textModel, buildBatchSystemPrompt(), parts, 400, 0.0, true);

        List<String> ids = resumes.stream().map(ResumeInfo::id).toList();
        return parseScoreMap(raw, ids);
    }

    // ── private: batch scoring helpers ────────────────────────────────────────

    private String buildBatchSystemPrompt() {
        return """
            You are a precise JSON scoring engine. Your only task is to output a valid JSON object.
            Do NOT include any explanation, markdown, or text outside the JSON.
            Score integers must be between 0 and 100.
            """;
    }

    private String buildBatchScoringPrompt(
            String title, String dept, String degree, String gpa, String bio,
            List<JobInfo> jobs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Rate how well this student matches each TA job (integer 0-100).\n\n");
        sb.append("Student profile:\n");
        sb.append("- Department: ").append(nvl(dept)).append("\n");
        sb.append("- Degree: ").append(nvl(degree)).append("\n");
        sb.append("- GPA: ").append(nvl(gpa)).append("\n");
        if (bio != null && !bio.isBlank()) sb.append("- Bio: ").append(bio, 0, Math.min(bio.length(), 400)).append("\n");

        sb.append("\nJobs to score (use the exact IDs as keys):\n");
        for (JobInfo j : jobs) {
            sb.append("ID \"").append(j.id()).append("\": ")
              .append(j.title())
              .append(" [").append(j.department()).append("]");
            if (j.description() != null && !j.description().isBlank()) {
                sb.append(" — ").append(j.description(), 0, Math.min(j.description().length(), 120));
            }
            sb.append("\n");
        }

        sb.append("\nRespond ONLY with a JSON object mapping each ID to a score integer, e.g.:\n");
        sb.append("{\"id-one\": 85, \"id-two\": 62}\n");
        return sb.toString();
    }

    /**
     * Extracts the first JSON object from the model's response and parses scores.
     * Falls back to -1 (parse failed) for any ID missing from the response.
     */
    private Map<String, Integer> parseBatchScores(String raw, List<JobInfo> jobs) {
        List<String> ids = jobs.stream().map(JobInfo::id).toList();
        return parseScoreMap(raw, ids);
    }

    private Map<String, Integer> parseScoreMap(String raw, List<String> ids) {
        Map<String, Integer> result = new HashMap<>();

        Matcher m = Pattern.compile("\\{[^\\{\\}]*\\}").matcher(raw);
        String jsonBlock = m.find() ? m.group() : raw;

        try {
            JsonNode node = mapper.readTree(jsonBlock);
            for (String id : ids) {
                JsonNode val = node.get(id);
                if (val != null && val.isNumber()) {
                    result.put(id, Math.max(0, Math.min(100, val.intValue())));
                }
            }
        } catch (Exception ignored) { /* fall through */ }

        for (String id : ids) {
            result.putIfAbsent(id, -1);
        }
        return result;
    }

    // ── private: shared HTTP helper ───────────────────────────────────────────

    /**
     * Sends a chat completion request and returns the assistant's message content.
     *
     * @param disableThinking if true, adds enable_thinking=false (required for Qwen3 text
     *                        models when we need clean JSON output without CoT traces)
     */
    private String chat(String model, String systemPrompt, ArrayNode contentParts,
                        int maxTokens, double temperature, boolean disableThinking) throws Exception {
        ArrayNode messages = mapper.createArrayNode();

        ObjectNode sysMsg = mapper.createObjectNode();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.set("content", contentParts);
        messages.add(userMsg);

        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);
        body.set("messages", messages);
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);

        // Qwen3 models enable chain-of-thought thinking by default.
        // For JSON scoring calls we must disable it, otherwise the <think>...</think>
        // block appears in the response and breaks JSON parsing.
        if (disableThinking) {
            body.put("enable_thinking", false);
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .timeout(Duration.ofSeconds(90))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            String errBody = resp.body();
            try {
                JsonNode err = mapper.readTree(errBody);
                String msg = err.path("error").path("message").asText();
                if (!msg.isBlank()) throw new IOException("Qwen API error: " + msg);
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ignored) { /* fall through */ }
            throw new IOException("Qwen API returned HTTP " + resp.statusCode());
        }

        JsonNode root = mapper.readTree(resp.body());
        return root.path("choices").path(0).path("message").path("content")
                   .asText("No response from model.");
    }

    private void addText(ArrayNode parts, String text) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "text");
        node.put("text", text);
        parts.add(node);
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "N/A" : s;
    }
}
