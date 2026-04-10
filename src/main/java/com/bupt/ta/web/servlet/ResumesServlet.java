package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.Resume;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.DegreeLevel;
import com.bupt.ta.persistence.DatabaseProvider;
import com.bupt.ta.persistence.TaDatabase;
import com.bupt.ta.service.QwenAiService;
import com.bupt.ta.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize       = 1024 * 1024 * 15,
    maxRequestSize    = 1024 * 1024 * 20
)
@WebServlet("/resumes")
public class ResumesServlet extends HttpServlet {

    private static final String VIEW_PATH   = "/portal/resumes.jsp";
    private static final String UPLOAD_DIR  = "data/resumes/uploads";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());

    private TaDatabase    database;
    private ResumeService resumeService;
    private ObjectMapper  objectMapper;

    @Override
    public void init() throws ServletException {
        this.database      = DatabaseProvider.get(getServletContext());
        this.resumeService = new ResumeService(database.resumes());
        this.objectMapper  = new ObjectMapper();

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new ServletException("Cannot create upload directory", e);
        }
    }

    // ── GET ────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        List<Resume> resumes = resumeService.listByUserId(user.getId());
        req.setAttribute("resumes", resumes);
        req.setAttribute("pageState", "normal");
        req.setAttribute("qwenConfigured", QwenAiService.resolveApiKey() != null);
        req.setAttribute("qwenVlModel", QwenAiService.resolveVlModel());
        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    // ── POST ───────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = currentUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        String action = req.getParameter("action");

        // JSON-returning actions – handle before the redirect block
        if ("aiReview".equals(action)) {
            handleAIReview(req, resp, user);
            return;
        }
        if ("upload".equals(action)) {
            handleFileUpload(req, resp, user);
            return;
        }

        // Redirect-based actions
        try {
            switch (action != null ? action : "") {
                case "save"   -> handleManualSave(req, user);
                case "rename" -> handleRename(req, user);
                case "delete" -> handleDelete(req, user);
                default -> throw new IllegalArgumentException(I18n.message(req, "msg.resumeUnknownActionPrefix") + action);
            }
            resp.sendRedirect(req.getContextPath() + "/resumes");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/resumes?pageState=uploadFailure&errorMessage="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    // ── action handlers ────────────────────────────────────────────────────

    /**
     * Handles file upload and returns JSON:
     *   success: {"ok": true, "resumeId": "...", "originalFileName": "..."}
     *   failure: {"ok": false, "error": "message"}
     *
     * Uses absolute paths to avoid Part.write() resolving against Tomcat's temp dir.
     */
    private void handleFileUpload(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        ObjectNode json = objectMapper.createObjectNode();

        try {
            Part filePart = req.getPart("resumeFile");
            if (filePart == null || filePart.getSize() == 0) {
                json.put("ok", false);
                json.put("error", I18n.message(req, "msg.resumeNoFile"));
                objectMapper.writeValue(resp.getWriter(), json);
                return;
            }

            String originalName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String ext = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase() : "";

            if (!ext.matches("\\.(pdf|doc|docx|jpg|jpeg|png|txt)$")) {
                json.put("ok", false);
                json.put("error", I18n.message(req, "msg.resumeUnsupportedType"));
                objectMapper.writeValue(resp.getWriter(), json);
                return;
            }

            // Use absolute path so Files.copy() (not Part.write) goes to the right place
            Path uploadDir  = Paths.get(UPLOAD_DIR).toAbsolutePath();
            Files.createDirectories(uploadDir);
            String savedName  = user.getId() + "_" + UUID.randomUUID() + ext;
            Path   uploadPath = uploadDir.resolve(savedName);

            try (InputStream is = filePart.getInputStream()) {
                Files.copy(is, uploadPath, StandardCopyOption.REPLACE_EXISTING);
            }

            Resume resume = new Resume();
            resume.setUserId(user.getId());
            resume.setTitle(I18n.message(req, "msg.resumeUntitled"));   // user renames in the modal after upload
            resume.setDepartment(user.getDepartment() != null ? user.getDepartment() : "");
            resume.setDegreeLevel(DegreeLevel.BACHELOR);
            resume.setGpa(new BigDecimal("0.00"));
            resume.setMaxWeeklyHours(15);
            resume.setBio("");
            resume.setUploadedFilePath(uploadPath.toString());
            resume.setOriginalFileName(originalName);

            Resume saved = resumeService.save(resume);

            json.put("ok", true);
            json.put("resumeId", saved.getId().toString());
            json.put("originalFileName", originalName);

        } catch (Exception e) {
            json.put("ok", false);
            json.put("error", I18n.message(req, "msg.resumeUploadFailedPrefix") + e.getMessage());
        }

        objectMapper.writeValue(resp.getWriter(), json);
    }

    /** Only updates the title of an existing resume (used from the rename modal). */
    private void handleRename(HttpServletRequest req, User user) throws Exception {
        String resumeId = req.getParameter("resumeId");
        if (resumeId == null || resumeId.isBlank()) {
            throw new IllegalArgumentException(I18n.message(req, "msg.resumeIdRequired"));
        }
        String title = req.getParameter("title");
        if (title == null || title.isBlank()) title = I18n.message(req, "msg.resumeUntitled");

        Resume resume = resumeService.listByUserId(user.getId()).stream()
                .filter(r -> r.getId().toString().equals(resumeId.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(I18n.message(req, "msg.resumeNotFound")));
        resume.setTitle(title.trim());
        resumeService.save(resume);
    }

    private void handleAIReview(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");
        ObjectNode json = objectMapper.createObjectNode();

        String apiKey = QwenAiService.resolveApiKey();
        if (apiKey == null) {
            json.put("ok", false);
            json.put("error", I18n.message(req, "msg.aiKeyMissing"));
            objectMapper.writeValue(resp.getWriter(), json);
            return;
        }

        try {
            List<Resume> resumes = resumeService.listByUserId(user.getId());

            // Determine target resume:
            //   card button → specific resumeId passed in the request
            //   sidebar button → most recent resume that has an uploaded file (fallback: latest overall)
            String resumeIdParam = req.getParameter("resumeId");
            Resume target = null;

            if (resumeIdParam != null && !resumeIdParam.isBlank()) {
                final String rid = resumeIdParam.trim();
                target = resumes.stream()
                        .filter(r -> r.getId().toString().equals(rid))
                        .findFirst()
                        .orElse(null);
            }

            if (target == null) {
                // Prefer a resume that has an uploaded file (sidebar use-case)
                target = resumes.stream()
                        .filter(r -> r.getUploadedFilePath() != null && !r.getUploadedFilePath().isBlank())
                        .reduce((a, b) -> b)   // last (most recently uploaded)
                        .orElse(resumes.isEmpty() ? null : resumes.get(resumes.size() - 1));
            }

            final Resume chosen = target;

            // Gather open vacancies for context
            List<Job> openJobs = database.jobs().listOpen(Instant.now());
            List<String> vacancyTitles = openJobs.stream()
                    .map(Job::getTitle)
                    .collect(Collectors.toList());

            QwenAiService ai = new QwenAiService(apiKey, QwenAiService.resolveVlModel(), QwenAiService.resolveTextModel());

            String analysis = ai.analyzeResumeForOptimization(
                    chosen != null ? chosen.getTitle()      : null,
                    chosen != null ? chosen.getDepartment() : null,
                    chosen != null && chosen.getDegreeLevel() != null
                            ? chosen.getDegreeLevel().name() : null,
                    chosen != null && chosen.getGpa() != null
                            ? chosen.getGpa().toPlainString() : null,
                    chosen != null ? chosen.getBio()         : null,
                    chosen != null ? chosen.getUploadedFilePath() : null,
                    vacancyTitles
            );

            json.put("ok", true);
            json.put("analysis", analysis);
            json.put("model", QwenAiService.resolveVlModel());
            json.put("resumeTitle", chosen != null ? chosen.getTitle() : "Your Resume");
            json.put("hasFile", chosen != null && chosen.getUploadedFilePath() != null);

        } catch (Exception e) {
            json.put("ok", false);
            json.put("error", "AI analysis failed: " + e.getMessage());
        }

        objectMapper.writeValue(resp.getWriter(), json);
    }

    private void handleManualSave(HttpServletRequest req, User user) throws Exception {
        String resumeId = req.getParameter("resumeId");
        Resume resume;

        if (resumeId != null && !resumeId.isBlank()) {
            // Update existing
            resume = resumeService.listByUserId(user.getId()).stream()
                    .filter(r -> r.getId().toString().equals(resumeId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Resume not found."));
        } else {
            resume = new Resume();
            resume.setUserId(user.getId());
        }

        resume.setTitle(req.getParameter("title"));
        resume.setDepartment(req.getParameter("department"));
        String dl = req.getParameter("degreeLevel");
        if (dl != null && !dl.isBlank()) {
            resume.setDegreeLevel(DegreeLevel.valueOf(dl));
        }
        String gpaStr = req.getParameter("gpa");
        if (gpaStr != null && !gpaStr.isBlank()) {
            resume.setGpa(new BigDecimal(gpaStr));
        }
        String hoursStr = req.getParameter("maxWeeklyHours");
        if (hoursStr != null && !hoursStr.isBlank()) {
            resume.setMaxWeeklyHours(Integer.parseInt(hoursStr));
        }
        resume.setBio(req.getParameter("bio"));
        resumeService.save(resume);
    }

    private void handleDelete(HttpServletRequest req, User user) throws Exception {
        UUID id = UUID.fromString(req.getParameter("resumeId"));
        // Optionally: delete the uploaded file too
        List<Resume> resumes = resumeService.listByUserId(user.getId());
        resumes.stream()
               .filter(r -> r.getId().equals(id))
               .findFirst()
               .ifPresent(r -> {
                   if (r.getUploadedFilePath() != null) {
                       try { Files.deleteIfExists(Path.of(r.getUploadedFilePath())); }
                       catch (IOException ignored) { /* best-effort */ }
                   }
               });
        resumeService.delete(id);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null ? (User) s.getAttribute("currentUser") : null;
    }
}
