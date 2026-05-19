package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.service.MatchingService;
import com.bupt.ta.service.QwenAiService;
import com.bupt.ta.service.ResumeService;
import com.bupt.ta.util.ResumeFilePaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Renders vacancy details and TA application preparation data.
 */
@WebServlet("/vacancy")
public class VacancyDetailServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/vacancy_detail.jsp";
    private static final DateTimeFormatter DEADLINE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private TaDatabase database;
    private ResumeService resumeService;
    private MatchingService matchingService;

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.resumeService = new ResumeService(database);
        this.matchingService = new MatchingService(database);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            UUID vacancyId = parseVacancyId(req.getParameter("vacancyId"));
            if (vacancyId == null) {
                req.setAttribute("pageState", "normal");
                copyFlashFromQuery(req);
                attachQwenConfigured(req);
                req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
                return;
            }

            Map<UUID, User> userById = database.users()
                    .findAll()
                    .stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));

            User currentUser = resolveCurrentUser(req.getSession(false));
            Job job = database.jobs().findById(vacancyId).orElse(null);
            if (job != null) {
                req.setAttribute("vacancy", toView(job, userById, currentUser));
            }

            List<ResumeSelectionView> resumeViews = currentUser == null
                    ? List.of()
                    : resumeService.listByUserId(currentUser.getId())
                    .stream()
                    .map(resume -> new ResumeSelectionView(
                            resume.getId().toString(),
                            safe(resume.getTitle(), "Untitled Resume"),
                            safe(resume.getOriginalFileName(), ""),
                            ResumeFilePaths.isAvailable(resume),
                            job == null ? null : matchingService.computeCoverage(resume.getId(), job.getId())
                    ))
                    .toList();

            req.setAttribute("resumeList", resumeViews);
            req.setAttribute("pageState", "normal");
            copyFlashFromQuery(req);
        } catch (Exception ex) {
            getServletContext().log("Failed to load vacancy detail", ex);
            applyLoadError(req);
        }

        attachQwenConfigured(req);
        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private void applyLoadError(HttpServletRequest req) {
        req.setAttribute("pageState", "loadError");
        req.setAttribute("errorMessage", I18n.message(req, "msg.vacancyDetailLoadFailed"));
        req.removeAttribute("vacancy");
        req.setAttribute("resumeList", List.of());
        req.setAttribute("successMessage", null);
    }

    private static void attachQwenConfigured(HttpServletRequest req) {
        req.setAttribute("qwenConfigured", QwenAiService.resolveApiKey() != null);
    }

    private VacancyDetailView toView(Job job, Map<UUID, User> userById, User currentUser) {
        String moduleCode = safe(job.getModuleCode(), "N/A");
        String department = resolveDepartment(moduleCode);
        String title = safe(job.getTitle(), "Untitled Vacancy");
        String description = safe(job.getDescription(), "No description available yet.");
        int hoursPerWeek = Math.max(job.getRequiredHours(), 1);
        BigDecimal rate = job.getHourlyRate();
        String hourlyRate = (rate != null && rate.compareTo(BigDecimal.ZERO) > 0)
                ? rate.toPlainString() : "20.00";
        String deadline = job.getDeadline() == null ? "TBD" : DEADLINE_FORMATTER.format(job.getDeadline());
        String moduleOwner = "TBA";
        if (job.getPostedBy() != null && userById.containsKey(job.getPostedBy())) {
            moduleOwner = safe(userById.get(job.getPostedBy()).getFullName(), moduleOwner);
        }
        boolean isOwner = currentUser != null && currentUser.getId().equals(job.getPostedBy());
        boolean saved = currentUser != null && currentUser.getSavedJobIds().contains(job.getId());
        List<String> labels = job.getLabels();

        List<String> requirements = buildRequirements(job);
        return new VacancyDetailView(
                job.getId().toString(),
                moduleCode,
                title,
                description,
                department,
                hoursPerWeek,
                hourlyRate,
                deadline,
                moduleOwner,
                requirements,
                isOwner,
                labels,
                saved
        );
    }

    private void copyFlashFromQuery(HttpServletRequest req) {
        String s = req.getParameter("successMessage");
        if (s != null && !s.isBlank()) {
            req.setAttribute("successMessage", s);
        }
        String e = req.getParameter("errorMessage");
        if (e != null && !e.isBlank()) {
            req.setAttribute("errorMessage", e);
        }
    }

    private List<String> buildRequirements(Job job) {
        List<String> structuredRequirements = database.jobRequirements().listByJobId(job.getId()).stream()
                .map(requirement -> toRequirementLabel(requirement))
                .toList();
        if (!structuredRequirements.isEmpty()) {
            return structuredRequirements;
        }
        if (job.getType() == JobType.INVIGILATION) {
            return List.of(
                    "Strong attention to detail during invigilation sessions.",
                    "Ability to follow exam regulations and escalation procedures.",
                    "Punctual attendance for all assigned exam slots."
            );
        }
        if (job.getType() == JobType.MODULE_SUPPORT) {
            return List.of(
                    "Solid understanding of the module's core concepts.",
                    "Good communication skills for tutorials and student Q&A.",
                    "Reliable weekly availability that matches teaching needs."
            );
        }
        return List.of(
                "Relevant subject knowledge for the advertised module.",
                "Professional communication and teamwork skills.",
                "Ability to meet deadlines and follow role responsibilities."
        );
    }

    private String toRequirementLabel(JobRequirement requirement) {
        String skillName = database.skills().findById(requirement.getSkillId())
                .map(Skill::getName)
                .orElse("Unknown skill");
        String prefix = requirement.isRequired() ? "Required" : "Preferred";
        String level = requirement.getMinProficiency() == null ? "" : " (" + requirement.getMinProficiency() + "+)";
        return prefix + ": " + skillName + level;
    }

    private UUID parseVacancyId(String rawVacancyId) {
        if (rawVacancyId == null || rawVacancyId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawVacancyId.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private User resolveCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("currentUser");
        if (value instanceof User user) {
            return user;
        }
        return null;
    }

    private String resolveDepartment(String moduleCode) {
        String upper = moduleCode == null ? "" : moduleCode.toUpperCase(Locale.ROOT);
        if (upper.startsWith("MATH")) {
            return "MATH";
        }
        return "CS";
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * View model for the vacancy detail header and application form.
     */
    public static final class VacancyDetailView {
        private final String vacancyId;
        private final String courseCode;
        private final String title;
        private final String description;
        private final String department;
        private final int hoursPerWeek;
        private final String hourlyRate;
        private final String deadline;
        private final String moduleOwner;
        private final List<String> requirements;
        private final boolean isOwner;
        private final List<String> labels;
        private final boolean saved;

        public VacancyDetailView(String vacancyId,
                                 String courseCode,
                                 String title,
                                 String description,
                                 String department,
                                 int hoursPerWeek,
                                 String hourlyRate,
                                 String deadline,
                                 String moduleOwner,
                                 List<String> requirements,
                                 boolean isOwner,
                                 List<String> labels,
                                 boolean saved) {
            this.vacancyId = vacancyId;
            this.courseCode = courseCode;
            this.title = title;
            this.description = description;
            this.department = department;
            this.hoursPerWeek = hoursPerWeek;
            this.hourlyRate = hourlyRate;
            this.deadline = deadline;
            this.moduleOwner = moduleOwner;
            this.requirements = requirements;
            this.isOwner = isOwner;
            this.labels = labels;
            this.saved = saved;
        }

        public String getVacancyId() {
            return vacancyId;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getDepartment() {
            return department;
        }

        public int getHoursPerWeek() {
            return hoursPerWeek;
        }

        public String getHourlyRate() {
            return hourlyRate;
        }

        public String getDeadline() {
            return deadline;
        }

        public String getModuleOwner() {
            return moduleOwner;
        }

        public List<String> getRequirements() {
            return requirements;
        }

        public boolean isOwner() {
            return isOwner;
        }

        public List<String> getLabels() {
            return labels;
        }

        public boolean isSaved() {
            return saved;
        }
    }

    /**
     * View model for a TA resume option on the application form.
     */
    public static final class ResumeSelectionView {
        private final String resumeId;
        private final String resumeName;
        private final String originalFileName;
        private final boolean fileAvailable;
        private final MatchingService.SkillCoverageView skillCoverage;

        public ResumeSelectionView(String resumeId, String resumeName, String originalFileName, boolean fileAvailable,
                                   MatchingService.SkillCoverageView skillCoverage) {
            this.resumeId = resumeId;
            this.resumeName = resumeName;
            this.originalFileName = originalFileName;
            this.fileAvailable = fileAvailable;
            this.skillCoverage = skillCoverage;
        }

        public String getResumeId() {
            return resumeId;
        }

        public String getResumeName() {
            return resumeName;
        }

        public String getOriginalFileName() {
            return originalFileName;
        }

        public boolean isFileAvailable() {
            return fileAvailable;
        }

        public MatchingService.SkillCoverageView getSkillCoverage() {
            return skillCoverage;
        }
    }
}
