package com.bupt.ta.web.servlet;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.ResumeSkill;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.bupt.ta.domain.value.AvailabilitySlot;
import com.bupt.ta.domain.value.JobQuery;
import com.bupt.ta.service.QwenAiService;
import com.bupt.ta.service.ResumeService;
import com.bupt.ta.util.Labels;
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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TA resume management endpoint for profile data, uploads, and skill bindings.
 */
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize       = 1024 * 1024 * 15,
    maxRequestSize    = 1024 * 1024 * 20
)
@WebServlet("/resumes")
public class ResumesServlet extends HttpServlet {

    private static final String VIEW_PATH   = "/portal/resumes.jsp";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());

    private TaDatabase    database;
    private ResumeService resumeService;
    private ObjectMapper  objectMapper;
    private Path uploadDir;

    @Override
    public void init() throws ServletException {
        this.database      = DatabaseProvider.get(getServletContext());
        this.resumeService = new ResumeService(database);
        this.objectMapper  = AppConfig.createObjectMapper();
        this.uploadDir = AppConfig.resolveDataDirectory().resolve("resumes").resolve("uploads");

        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new ServletException("Cannot create upload directory", e);
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        req.setAttribute("qwenConfigured", QwenAiService.resolveApiKey() != null);
        req.setAttribute("qwenVlModel", QwenAiService.resolveVlModel());

        String queryPageState = normalizeParam(req.getParameter("pageState"));
        try {
            List<Resume> resumes = resumeService.listByUserId(user.getId());
            req.setAttribute("resumes", resumes);
            req.setAttribute("skills", database.skills().findAll().stream()
                    .sorted(Comparator.comparing(Skill::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .toList());
            req.setAttribute("proficiencyLevels", ProficiencyLevel.values());
            req.setAttribute("resumeSkillViewsByResumeId", buildResumeSkillViews(resumes));
            req.setAttribute("availabilityJsonByResumeId", buildAvailabilityJsonViews(resumes));
            req.setAttribute("daysOfWeek", DayOfWeek.values());
            if ("uploadSuccess".equals(queryPageState)) {
                req.setAttribute("pageState", "uploadSuccess");
            } else if ("uploadFailure".equals(queryPageState)) {
                req.setAttribute("pageState", "uploadFailure");
                req.setAttribute("errorMessage", normalizeParam(req.getParameter("errorMessage")));
            } else {
                req.setAttribute("pageState", "normal");
            }
        } catch (Exception ex) {
            getServletContext().log("Failed to load resumes", ex);
            applyLoadError(req);
        }

        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private void applyLoadError(HttpServletRequest req) {
        req.setAttribute("pageState", "loadError");
        req.setAttribute("resumes", List.of());
        req.setAttribute("successMessage", null);
        req.setAttribute("errorMessage", I18n.message(req, "msg.resumesLoadFailed"));
    }

    private static String normalizeParam(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = currentUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        String action = req.getParameter("action");

        // Handle JSON-returning actions before redirect-based form actions.
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
                case "duplicate" -> handleDuplicate(req, user);
                case "skills" -> handleSkillsSave(req, user);
                case "delete" -> handleDelete(req, user);
                default -> throw new IllegalArgumentException(I18n.message(req, "msg.resumeUnknownActionPrefix") + action);
            }
            resp.sendRedirect(req.getContextPath() + "/resumes");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/resumes?pageState=uploadFailure&errorMessage="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }


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
            String savedName  = user.getId() + "_" + UUID.randomUUID() + ext;
            Path   uploadPath = uploadDir.resolve(savedName);

            try (InputStream is = filePart.getInputStream()) {
                Files.copy(is, uploadPath, StandardCopyOption.REPLACE_EXISTING);
            }

            Resume resume = new Resume();
            resume.setUserId(user.getId());
            resume.setTitle(uniqueResumeTitle(user.getId(), I18n.message(req, "msg.resumeUntitled"), null));
            resume.setDepartment(user.getDepartment() != null ? user.getDepartment() : "");
            resume.setDegreeLevel(DegreeLevel.BACHELOR);
            resume.setGpa(new BigDecimal("0.00"));
            resume.setMaxWeeklyHours(15);
            resume.setBio("");
            resume.setUploadedFilePath(uploadPath.toString());
            resume.setOriginalFileName(originalName);
            resume.setLabels(Labels.parseList(req.getParameter("labels"), 24));

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
        resume.setLabels(Labels.parseList(req.getParameter("labels"), 24));
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
            // Card buttons pass a resumeId; the sidebar action falls back to the latest uploaded resume.
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
            JobQuery query = new JobQuery();
            query.setNow(Instant.now());
            List<Job> openJobs = database.jobs().listOpen(query);
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
            BigDecimal gpa = new BigDecimal(gpaStr);
            if (gpa.compareTo(BigDecimal.ZERO) < 0 || gpa.compareTo(new BigDecimal("4.00")) > 0) {
                throw new IllegalArgumentException("GPA must be between 0 and 4.");
            }
            resume.setGpa(gpa);
        }
        String hoursStr = req.getParameter("maxWeeklyHours");
        if (hoursStr != null && !hoursStr.isBlank()) {
            resume.setMaxWeeklyHours(Integer.parseInt(hoursStr));
        }
        resume.setBio(req.getParameter("bio"));
        resume.setAvailabilitySlots(parseAvailabilitySlots(req));
        resume.setLabels(Labels.parseList(req.getParameter("resumeLabels"), 24));
        resumeService.save(resume);
    }

    private void handleDuplicate(HttpServletRequest req, User user) {
        UUID id = UUID.fromString(req.getParameter("resumeId"));
        Resume original = ensureOwnedResume(user, id);
        Resume copy = resumeService.duplicate(original.getId());
        for (ResumeSkill originalSkill : database.resumeSkills().listByResumeId(original.getId())) {
            ResumeSkill copiedSkill = new ResumeSkill();
            copiedSkill.setResumeId(copy.getId());
            copiedSkill.setSkillId(originalSkill.getSkillId());
            copiedSkill.setProficiency(originalSkill.getProficiency());
            copiedSkill.setYearsExp(originalSkill.getYearsExp());
            database.resumeSkills().save(copiedSkill);
        }
    }

    private void handleSkillsSave(HttpServletRequest req, User user) {
        UUID resumeId = UUID.fromString(req.getParameter("resumeId"));
        ensureOwnedResume(user, resumeId);
        String[] skillIds = req.getParameterValues("skillId");
        String[] proficiencies = req.getParameterValues("proficiency");
        String[] years = req.getParameterValues("yearsExp");
        List<ResumeSkill> selected = new ArrayList<>();
        Set<UUID> seenSkillIds = new HashSet<>();
        if (skillIds != null) {
            for (int i = 0; i < skillIds.length; i++) {
                String rawSkillId = skillIds[i];
                if (rawSkillId == null || rawSkillId.isBlank()) {
                    continue;
                }
                UUID skillId = UUID.fromString(rawSkillId.trim());
                Skill skill = database.skills().findById(skillId)
                        .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + skillId));
                if (!skill.isActive()) {
                    throw new IllegalArgumentException("Inactive skill cannot be assigned: " + skill.getName());
                }
                if (!seenSkillIds.add(skillId)) {
                    continue;
                }
                ResumeSkill resumeSkill = new ResumeSkill();
                resumeSkill.setSkillId(skillId);
                resumeSkill.setProficiency(parseProficiency(proficiencies, i));
                resumeSkill.setYearsExp(parseYears(years, i));
                selected.add(resumeSkill);
            }
        }
        resumeService.replaceSkills(user.getId(), resumeId, selected);
    }

    private void handleDelete(HttpServletRequest req, User user) throws Exception {
        UUID id = UUID.fromString(req.getParameter("resumeId"));
        Resume owned = ensureOwnedResume(user, id);
        if (owned.getUploadedFilePath() != null) {
            try { Files.deleteIfExists(Path.of(owned.getUploadedFilePath())); }
            catch (IOException ignored) { /* best-effort */ }
        }
        resumeService.delete(user.getId(), id);
    }


    private Map<UUID, List<Map<String, Object>>> buildResumeSkillViews(List<Resume> resumes) {
        Map<UUID, Skill> skillsById = database.skills().findAll().stream()
                .collect(Collectors.toMap(Skill::getId, skill -> skill));
        return resumes.stream().collect(Collectors.toMap(
                Resume::getId,
                resume -> database.resumeSkills().listByResumeId(resume.getId()).stream()
                        .map(resumeSkill -> {
                            Skill skill = skillsById.get(resumeSkill.getSkillId());
                            return Map.<String, Object>of(
                                    "skillId", resumeSkill.getSkillId(),
                                    "name", skill == null ? "Unknown skill" : skill.getName(),
                                    "category", skill == null || skill.getCategory() == null ? "" : skill.getCategory().name(),
                                    "proficiency", resumeSkill.getProficiency() == null ? ProficiencyLevel.BEGINNER : resumeSkill.getProficiency(),
                                    "yearsExp", resumeSkill.getYearsExp()
                            );
                        })
                        .toList()
        ));
    }

    private Map<UUID, String> buildAvailabilityJsonViews(List<Resume> resumes) {
        return resumes.stream().collect(Collectors.toMap(
                Resume::getId,
                resume -> {
                    try {
                        return objectMapper.writeValueAsString(resume.getAvailabilitySlots());
                    } catch (Exception ex) {
                        return "[]";
                    }
                }
        ));
    }

    private Resume ensureOwnedResume(User user, UUID resumeId) {
        return resumeService.listByUserId(user.getId()).stream()
                .filter(resume -> resume.getId().equals(resumeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resume not found."));
    }

    private ProficiencyLevel parseProficiency(String[] values, int index) {
        if (values == null || index >= values.length || values[index] == null || values[index].isBlank()) {
            return ProficiencyLevel.BEGINNER;
        }
        return ProficiencyLevel.valueOf(values[index].trim());
    }

    private int parseYears(String[] values, int index) {
        if (values == null || index >= values.length || values[index] == null || values[index].isBlank()) {
            return 0;
        }
        return Math.max(0, Integer.parseInt(values[index].trim()));
    }

    private List<AvailabilitySlot> parseAvailabilitySlots(HttpServletRequest req) {
        String[] days = req.getParameterValues("availabilityDay");
        String[] starts = req.getParameterValues("availabilityStart");
        String[] ends = req.getParameterValues("availabilityEnd");
        List<AvailabilitySlot> slots = new ArrayList<>();
        if (days == null) {
            return slots;
        }
        for (int i = 0; i < days.length; i++) {
            String day = valueAt(days, i);
            String start = valueAt(starts, i);
            String end = valueAt(ends, i);
            if (day == null && start == null && end == null) {
                continue;
            }
            if (day == null || start == null || end == null) {
                throw new IllegalArgumentException("Availability slots require day, start time, and end time.");
            }
            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setDayOfWeek(DayOfWeek.valueOf(day));
            slot.setStartTime(LocalTime.parse(start));
            slot.setEndTime(LocalTime.parse(end));
            if (!slot.getEndTime().isAfter(slot.getStartTime())) {
                throw new IllegalArgumentException("Availability end time must be after start time.");
            }
            slots.add(slot);
        }
        return slots;
    }

    private String uniqueResumeTitle(UUID userId, String baseTitle, UUID excludingId) {
        String base = baseTitle == null || baseTitle.isBlank() ? "Untitled" : baseTitle.trim();
        String candidate = base;
        int suffix = 2;
        while (titleExists(userId, candidate, excludingId)) {
            candidate = base + " " + suffix++;
        }
        return candidate;
    }

    private boolean titleExists(UUID userId, String title, UUID excludingId) {
        return resumeService.listByUserId(userId).stream()
                .filter(resume -> excludingId == null || !excludingId.equals(resume.getId()))
                .anyMatch(resume -> title.equalsIgnoreCase(resume.getTitle()));
    }

    private static String valueAt(String[] values, int index) {
        if (values == null || index >= values.length || values[index] == null || values[index].isBlank()) {
            return null;
        }
        return values[index].trim();
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null ? (User) s.getAttribute("currentUser") : null;
    }
}
