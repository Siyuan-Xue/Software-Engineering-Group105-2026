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
 *   1. analyzeResumeForOptimization() - used on the Resumes page.
 *      Focuses on improving the student's resume quality.
 *   2. batchScoreJobs()               - used on the Vacancies page.
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

    /** Vacancy descriptor passed to AI batch scoring prompts. */
    public record JobInfo(String id, String title, String description, String department) {}
    /** Resume descriptor passed to AI batch scoring prompts. */
    public record ResumeInfo(String id, String title, String department, String degree, String gpa, String bio) {}
    /** One applicant row for MO multi-applicant ranking (same vacancy). */
    public record MoApplicantSnippet(
            String applicationId,
            String statusDisplay,
            String resumeTitle,
            String department,
            String degree,
            String gpa,
            String bioExcerpt,
            String coverLetterExcerpt) {}

    private final String apiKey;
    private final String vlModel;
    private final String textModel;
    private final ObjectMapper mapper = new ObjectMapper();
    private final URI apiUri;
    private final ChatTransport transport;

    public QwenAiService(String apiKey, String vlModel, String textModel) {
        this(apiKey, vlModel, textModel, URI.create(API_URL), defaultTransport());
    }

    QwenAiService(String apiKey, String vlModel, String textModel, URI apiUri, ChatTransport transport) {
        this.apiKey     = apiKey;
        this.vlModel    = (vlModel    != null && !vlModel.isBlank())    ? vlModel    : DEFAULT_VL_MODEL;
        this.textModel  = (textModel  != null && !textModel.isBlank())  ? textModel  : DEFAULT_TEXT_MODEL;
        this.apiUri = apiUri;
        this.transport = transport;
    }

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

    /**
     * Scores how well the student's resume matches each of the given TA jobs.
     *
     * @param title      resume title (may be null)
     * @param department applicant's department
     * @param degree     degree level
     * @param gpa        GPA string
     * @param bio        personal statement
     * @param jobs       list of jobs to score
     * @return map of jobId to score (0-100). Missing entries mean scoring failed for that job.
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

        // Disable model thinking so JSON parsing receives a clean object.
        String raw = chat(textModel, buildBatchSystemPrompt(), parts, 600, 0.0, true);
        return parseBatchScores(raw, jobs);
    }

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
        sb.append("Do not add a standalone legal/privacy disclaimer block; the application collects consent in the UI before this call.\n");
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

    /**
     * Scores each of the user's resumes against a single TA job posting.
     * Returns a map of resumeId to score (0-100).
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
            sb.append("- Description: ").append(jobDescription, 0, Math.min(jobDescription.length(), 800)).append("\n");
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
        // Disable model thinking so JSON parsing receives a clean object.
        String raw = chat(textModel, buildBatchSystemPrompt(), parts, 400, 0.0, true);

        List<String> ids = resumes.stream().map(ResumeInfo::id).toList();
        return parseScoreMap(raw, ids);
    }

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
        sb.append("- Resume title: ").append(nvl(title)).append("\n");
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

    /**
     * Suggests whether a module owner might lean toward offer vs reject, with reasoning.
     * Not a hiring decision; not legal advice.
     */
    public String suggestMoOfferRejectAdvice(
            String jobTitle,
            String moduleCode,
            String jobDescription,
            String jobLabelsJoined,
            String applicationStatusDisplay,
            String applicantCoverLetter,
            String resumeTitle,
            String resumeDepartment,
            String resumeDegree,
            String resumeGpa,
            String resumeBio,
            String applicantDisplayName) throws Exception {

        if (!isConfigured()) {
            throw new IllegalStateException("Qwen API key is not configured.");
        }

        String sys = """
                You are an assistant helping a university module organiser review Teaching Assistant applications.
                You MUST NOT make the hiring decision. You MUST NOT claim legal compliance or give legal advice.
                The user message includes the current workflow status (e.g. Submitted, Under Review, Offer Pending, Accepted, Rejected, Declined).
                If the status is Accepted, Rejected, or Declined, treat this as retrospective documentation or consistency review only:
                do not instruct the organiser to reverse an official outcome; frame "Suggested lean" as what the materials would have suggested
                or how the case reads in hindsight, and note any training/audit talking points if relevant.
                If the status is still open (Submitted, Under Review, Offer Pending), you may phrase "Suggested lean" as preliminary guidance only.
                Do not output a separate legal/privacy disclaimer section; the product shows legal terms in the UI, not in your reply.
                Output in Markdown with these sections exactly:
                ## Summary
                ## Suggested lean (Offer / Reject / Unclear)
                ## Reasoning (bullet list, max 6 bullets)
                ## Risks or follow-up checks
                """;

        StringBuilder user = new StringBuilder();
        user.append("Vacancy: ").append(nvl(jobTitle)).append(" (").append(nvl(moduleCode)).append(")\n");
        user.append("Vacancy labels: ").append(nvl(jobLabelsJoined)).append("\n");
        user.append("Vacancy description:\n").append(trunc(nvl(jobDescription), 2500)).append("\n\n");
        user.append("Application status: ").append(nvl(applicationStatusDisplay)).append("\n");
        user.append("Applicant (resume owner display): ").append(nvl(applicantDisplayName)).append("\n\n");
        user.append("Resume snapshot — title: ").append(nvl(resumeTitle));
        user.append(", dept: ").append(nvl(resumeDepartment));
        user.append(", degree: ").append(nvl(resumeDegree));
        user.append(", GPA: ").append(nvl(resumeGpa)).append("\n");
        if (resumeBio != null && !resumeBio.isBlank()) {
            user.append("Bio excerpt:\n").append(trunc(resumeBio, 1200)).append("\n\n");
        }
        if (applicantCoverLetter != null && !applicantCoverLetter.isBlank()) {
            user.append("Cover letter / message from applicant:\n")
                    .append(trunc(applicantCoverLetter, 2000)).append("\n\n");
        }
        user.append("Based only on the above, give concise guidance for the organiser.\n");

        ArrayNode parts = mapper.createArrayNode();
        addText(parts, user.toString());
        return chat(textModel, sys, parts, 2200, 0.35, false);
    }

    /**
     * Draft a short motivation paragraph or cover letter snippet for one vacancy.
     */
    public String draftTaCoverLetterMotivation(
            String jobTitle,
            String moduleCode,
            String jobDescription,
            String jobLabelsJoined,
            String resumeTitle,
            String resumeDepartment,
            String resumeDegree,
            String resumeGpa,
            String resumeBio) throws Exception {

        if (!isConfigured()) {
            throw new IllegalStateException("Qwen API key is not configured.");
        }

        String sys = """
                You help students draft text for Teaching Assistant applications.
                Produce a concise draft the student can edit. Use Markdown.
                Sections: ## Draft (2 short paragraphs max), ## Optional closing line, ## Tips (3 bullets max).
                Do not invent grades, publications, or experience not hinted in the profile.
                Do not add legal or privacy disclaimer lines; the application shows those in the UI before calling you.
                """;

        StringBuilder user = new StringBuilder();
        user.append("Target vacancy: ").append(nvl(jobTitle)).append(" (").append(nvl(moduleCode)).append(")\n");
        user.append("Vacancy labels: ").append(nvl(jobLabelsJoined)).append("\n");
        user.append("Vacancy description:\n").append(trunc(nvl(jobDescription), 2500)).append("\n\n");
        user.append("Student profile — resume title: ").append(nvl(resumeTitle));
        user.append(", dept: ").append(nvl(resumeDepartment));
        user.append(", degree: ").append(nvl(resumeDegree));
        user.append(", GPA: ").append(nvl(resumeGpa)).append("\n");
        if (resumeBio != null && !resumeBio.isBlank()) {
            user.append("Bio / statement:\n").append(trunc(resumeBio, 2000)).append("\n");
        }
        user.append("\nWrite the draft in the student's voice (first person), suitable for pasting into a cover letter field.\n");

        ArrayNode parts = mapper.createArrayNode();
        addText(parts, user.toString());
        return chat(textModel, sys, parts, 1800, 0.45, false);
    }

    /**
     * Ranks multiple applicants for the same TA vacancy (JSON array). Triaging aid only; not a hiring decision.
     *
     * @return raw model text expected to contain a single JSON array
     */
    public String rankMoApplicantsForJobJson(
            String jobTitle,
            String moduleCode,
            String jobDescription,
            String jobLabelsJoined,
            List<MoApplicantSnippet> applicants) throws Exception {

        if (!isConfigured()) {
            throw new IllegalStateException("Qwen API key is not configured.");
        }
        if (applicants == null || applicants.size() < 2) {
            throw new IllegalArgumentException("At least two applicants are required.");
        }

        String sys = """
                You help a university module organiser compare Teaching Assistant applicants for ONE vacancy.
                You MUST NOT decide who is hired. You MUST NOT output markdown or prose outside JSON.
                Respond ONLY with a JSON array. Each element must be exactly:
                {"applicationId":"<uuid as given>","rank":<integer starting at 1 for best match>,"fitScore":<0-100>,"note":"<plain text, max 100 chars>"}
                Rules: include every applicationId exactly once; ranks must be 1..N with no gaps or duplicates; do not add legal/privacy text.
                """;

        StringBuilder user = new StringBuilder();
        user.append("Vacancy: ").append(nvl(jobTitle)).append(" (").append(nvl(moduleCode)).append(")\n");
        user.append("Labels: ").append(nvl(jobLabelsJoined)).append("\n");
        user.append("Description:\n").append(trunc(nvl(jobDescription), 2200)).append("\n\n");
        user.append("Applicants (rank using only these materials):\n");
        int limit = Math.min(applicants.size(), 18);
        for (int i = 0; i < limit; i++) {
            MoApplicantSnippet a = applicants.get(i);
            user.append("---\n");
            user.append("applicationId: ").append(nvl(a.applicationId())).append("\n");
            user.append("status: ").append(nvl(a.statusDisplay())).append("\n");
            user.append("resumeTitle: ").append(nvl(a.resumeTitle())).append("\n");
            user.append("dept: ").append(nvl(a.department())).append(", degree: ").append(nvl(a.degree()));
            user.append(", GPA: ").append(nvl(a.gpa())).append("\n");
            if (a.bioExcerpt() != null && !a.bioExcerpt().isBlank()) {
                user.append("bio: ").append(trunc(a.bioExcerpt(), 400)).append("\n");
            }
            if (a.coverLetterExcerpt() != null && !a.coverLetterExcerpt().isBlank()) {
                user.append("coverLetter: ").append(trunc(a.coverLetterExcerpt(), 500)).append("\n");
            }
        }
        if (applicants.size() > limit) {
            user.append("\n(Additional applicants omitted for length — rank only those listed above.)\n");
        }
        user.append("\nReturn ONLY the JSON array.\n");

        ArrayNode parts = mapper.createArrayNode();
        addText(parts, user.toString());
        return chat(textModel, sys, parts, 3200, 0.2, true);
    }

    private static String trunc(String s, int max) {
        if (s == null || s.length() <= max) {
            return s == null ? "" : s;
        }
        return s.substring(0, max) + "\n...[truncated]";
    }

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

        String requestBody = mapper.writeValueAsString(body);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(apiUri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(90))
                .build();

        HttpResponse<String> resp = transport.send(req, requestBody);

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

    private static ChatTransport defaultTransport() {
        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        return (request, body) -> http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @FunctionalInterface
    interface ChatTransport {
        HttpResponse<String> send(HttpRequest request, String requestBody) throws Exception;
    }
}
