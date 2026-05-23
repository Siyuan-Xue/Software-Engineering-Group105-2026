package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.QwenAiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Paginated vacancy catalogue backing {@code /vacancies}.
 *
 * <p>Anonymous visitors observe open postings while authenticated users merge favourite markers, departmental filters,
 * AI availability flags ({@link QwenAiService}), and workload facets before rendering {@code portal/vacancies.jsp}.</p>
 */
@WebServlet("/vacancies")
public class VacanciesServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/vacancies.jsp";
    private static final DateTimeFormatter DEADLINE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private static final BigDecimal DEFAULT_HOURLY_RATE = new BigDecimal("20.00");
    static final int PAGE_SIZE = 6;

    private JobService jobService;
    private TaDatabase database;

    /** Wires vacancy business rules beside raw persistence for visibility toggles. */
    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.jobService = new JobService(database);
    }

    /** Builds server-side paging models ({@link #PAGE_SIZE}) and attaches filter artefacts to the view. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String keyword    = normalize(req.getParameter("keyword"));
            String department = normalize(req.getParameter("department"));
            String term       = normalize(req.getParameter("term"));
            JobType type      = parseJobType(req.getParameter("type"));
            Integer minHours  = parseInteger(req.getParameter("minHours"));
            Integer maxHours  = parseInteger(req.getParameter("maxHours"));
            int page          = parsePage(req.getParameter("page"));

            Map<UUID, User> userById = database.users()
                    .findAll()
                    .stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));

            HttpSession session = req.getSession(false);
            User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
            Set<UUID> savedIds = resolveSavedIds(session);

            Map<UUID, Set<String>> jobSkillNames = buildJobSkillNames();

            List<VacancyCardView> allCards = listVisibleJobsFor(currentUser)
                    .stream()
                    .filter(job -> keyword == null || matchesKeyword(job, keyword) || matchesJobTags(job, keyword, jobSkillNames))
                    .filter(job -> matchesDepartment(job, department))
                    .filter(job -> matchesTerm(job, term))
                    .filter(job -> type == null || job.getType() == type)
                    .filter(job -> minHours == null || Math.max(job.getRequiredHours(), 1) >= minHours)
                    .filter(job -> maxHours == null || Math.max(job.getRequiredHours(), 1) <= maxHours)
                    .map(job -> toCard(job, userById, savedIds, currentUser))
                    .sorted(Comparator.comparing(VacancyCardView::isSaved).reversed())
                    .toList();

            int totalCount = allCards.size();
            int totalPages = totalCount == 0 ? 1 : (int) Math.ceil((double) totalCount / PAGE_SIZE);
            page = Math.min(page, totalPages);

            List<VacancyCardView> pageCards = allCards.stream()
                    .skip((long) (page - 1) * PAGE_SIZE)
                    .limit(PAGE_SIZE)
                    .toList();

            req.setAttribute("vacancies", pageCards);
            req.setAttribute("totalCount", totalCount);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("hasMore", page < totalPages);
            req.setAttribute("termOptions", buildTermOptions());
            req.setAttribute("jobTypes", JobType.values());
            req.setAttribute("skills", database.skills().findAll().stream()
                    .sorted(Comparator.comparing(Skill::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .toList());
            req.setAttribute("proficiencyLevels", ProficiencyLevel.values());
            req.setAttribute("pageState", "normal");
            applyFlashFromQuery(req);
        } catch (Exception ex) {
            getServletContext().log("Failed to load vacancies", ex);
            applyLoadError(req);
        }

        req.setAttribute("qwenConfigured", QwenAiService.resolveApiKey() != null);
        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private List<Job> listVisibleJobsFor(User currentUser) {
        if (currentUser != null && currentUser.getRole() == UserRole.MO) {
            return jobService.listByPoster(currentUser.getId());
        }
        return jobService.listOpen(Instant.now());
    }

    private void applyLoadError(HttpServletRequest req) {
        req.setAttribute("pageState", "loadError");
        req.setAttribute("errorMessage", I18n.message(req, "msg.vacancyLoadFailed"));
        req.setAttribute("vacancies", List.of());
        req.setAttribute("totalCount", 0);
        req.setAttribute("currentPage", 1);
        req.setAttribute("totalPages", 1);
        req.setAttribute("hasMore", false);
        req.setAttribute("termOptions", List.of());
        req.setAttribute("jobTypes", JobType.values());
        req.setAttribute("skills", List.of());
        req.setAttribute("proficiencyLevels", ProficiencyLevel.values());
    }

    private void applyFlashFromQuery(HttpServletRequest req) {
        String s = req.getParameter("successMessage");
        if (s != null && !s.isBlank()) {
            req.setAttribute("successMessage", s);
        }
        String e = req.getParameter("errorMessage");
        if (e != null && !e.isBlank()) {
            req.setAttribute("errorMessage", e);
        }
    }


    private VacancyCardView toCard(Job job, Map<UUID, User> userById, Set<UUID> savedIds, User currentUser) {
        String department  = resolveDepartment(job);
        String deadline    = job.getDeadline() == null ? "TBD" : DEADLINE_FORMATTER.format(job.getDeadline());
        String courseCode  = safe(job.getModuleCode(), "N/A");
        String description = safe(job.getDescription(), "No description available yet.");
        int hoursPerWeek   = Math.max(job.getRequiredHours(), 1);
        String hourlyRate  = resolveHourlyRate(job);
        String moduleOwner = "TBA";
        if (job.getPostedBy() != null && userById.containsKey(job.getPostedBy())) {
            moduleOwner = safe(userById.get(job.getPostedBy()).getFullName(), moduleOwner);
        }
        boolean saved = savedIds.contains(job.getId());
        boolean isOwner = currentUser != null && currentUser.getId().equals(job.getPostedBy());
        List<String> labels = job.getLabels();
        int accepted = (int) database.applications().listByJobId(job.getId()).stream()
                .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                .count();
        int slotsRemaining = Math.max(0, Math.max(job.getSlots(), 1) - accepted);
        boolean applied = currentUser != null && "TA".equals(currentUser.getRole().name())
                && database.applications().existsByTaAndJob(currentUser.getId(), job.getId());

        return new VacancyCardView(
                job.getId().toString(),
                courseCode,
                safe(job.getTitle(), "Untitled Vacancy"),
                description,
                department,
                hoursPerWeek,
                hourlyRate,
                deadline,
                moduleOwner,
                saved,
                isOwner,
                labels,
                job.getType() == null ? JobType.MODULE_SUPPORT.name() : job.getType().name(),
                job.getStatus() == null ? "" : job.getStatus().name(),
                slotsRemaining,
                applied
        );
    }


    private boolean matchesKeyword(Job job, String keyword) {
        if (keyword == null) return true;
        String normalized = keyword.toLowerCase(Locale.ROOT);
        List<String> searchable = new ArrayList<>();
        searchable.add(job.getTitle());
        searchable.add(job.getModuleCode());
        searchable.add(job.getDescription());
        searchable.add(resolveDepartment(job));
        for (String lb : job.getLabels()) {
            searchable.add(lb);
        }
        return searchable.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.toLowerCase(Locale.ROOT))
                .anyMatch(v -> v.contains(normalized));
    }

    /**
     * Build a map from job ID to lowercased skill-name set (one query, cached for the request).
     */
    private Map<UUID, Set<String>> buildJobSkillNames() {
        Map<UUID, String> skillNames = database.skills().findAll().stream()
                .collect(Collectors.toMap(Skill::getId, s -> s.getName().toLowerCase(Locale.ROOT)));

        Map<UUID, Set<String>> result = new HashMap<>();
        for (JobRequirement req : database.jobRequirements().findAll()) {
            String name = skillNames.get(req.getSkillId());
            if (name != null) {
                result.computeIfAbsent(req.getJobId(), k -> new HashSet<>()).add(name);
            }
        }
        return result;
    }

    /**
     * Split keyword by whitespace into tokens. A job matches only when
     * <em>every</em> token is a substring of some skill name linked to the job
     * (case-insensitive AND logic).
     */
    private boolean matchesJobTags(Job job, String keyword, Map<UUID, Set<String>> jobSkillNames) {
        if (keyword == null || keyword.isBlank()) return false;
        String[] tokens = keyword.trim().split("\\s+");
        if (tokens.length == 0) return false;

        Set<String> skillSet = jobSkillNames.getOrDefault(job.getId(), Set.of());
        if (skillSet.isEmpty()) return false;

        return Arrays.stream(tokens)
                .allMatch(token -> {
                    String lower = token.toLowerCase(Locale.ROOT);
                    return skillSet.stream().anyMatch(s -> s.toLowerCase(Locale.ROOT).contains(lower));
                });
    }

    private boolean matchesDepartment(Job job, String department) {
        if (department == null) return true;
        return department.equalsIgnoreCase(resolveDepartment(job));
    }

    private boolean matchesTerm(Job job, String term) {
        if (term == null) return true;
        if (job.getStartDate() == null) return false;
        int year = job.getStartDate().getYear();
        int month = job.getStartDate().getMonthValue();
        String resolved = month >= 8 ? "Fall " + year : "Spring " + year;
        return term.equalsIgnoreCase(resolved);
    }


    /**
     * Generates a chronological list of semester options covering
     * 2 terms before the current term up to 2 terms after.
     * Example (called in April 2026, current = Spring 2026):
     *   Spring 2025, Fall 2025, Spring 2026, Fall 2026, Spring 2027
     */
    static List<String> buildTermOptions() {
        LocalDate today = LocalDate.now();
        int year  = today.getYear();
        int month = today.getMonthValue();

        // Represent each term as an ordinal: year*2 + (0=Spring, 1=Fall)
        int currentOrdinal = year * 2 + (month >= 8 ? 1 : 0);

        List<String> terms = new ArrayList<>();
        for (int delta = -2; delta <= 2; delta++) {
            int ord  = currentOrdinal + delta;
            int y    = ord / 2;
            boolean fall = (ord % 2 == 1);
            // Handle negative modulo edge case
            if (ord < 0 && ord % 2 != 0) {
                y = (ord - 1) / 2;
                fall = false;
            }
            terms.add((fall ? "Fall " : "Spring ") + y);
        }
        return terms;
    }


    private String resolveDepartment(Job job) {
        String moduleCode = normalize(job.getModuleCode());
        if (moduleCode == null) return "CS";
        return moduleCode.toUpperCase(Locale.ROOT).startsWith("MATH") ? "MATH" : "CS";
    }

    private String resolveHourlyRate(Job job) {
        BigDecimal rate = job.getHourlyRate();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return DEFAULT_HOURLY_RATE.toPlainString();
        }
        return rate.toPlainString();
    }

    private Set<UUID> resolveSavedIds(HttpSession session) {
        if (session == null) return Set.of();
        Object value = session.getAttribute("currentUser");
        if (value instanceof User user) {
            return user.getSavedJobIds();
        }
        return Set.of();
    }

    private int parsePage(String raw) {
        if (raw == null || raw.isBlank()) return 1;
        try {
            int p = Integer.parseInt(raw.trim());
            return Math.max(1, p);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private JobType parseJobType(String raw) {
        String normalized = normalize(raw);
        if (normalized == null) {
            return null;
        }
        try {
            return JobType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Integer parseInteger(String raw) {
        String normalized = normalize(raw);
        if (normalized == null) {
            return null;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * View model for a vacancy card in the listing page.
     */
    public static final class VacancyCardView {
        private final String vacancyId;
        private final String courseCode;
        private final String title;
        private final String description;
        private final String department;
        private final int hoursPerWeek;
        private final String hourlyRate;
        private final String deadline;
        private final String moduleOwner;
        private final boolean saved;
        private final boolean isOwner;
        private final List<String> labels;
        private final String type;
        private final String status;
        private final int slotsRemaining;
        private final boolean applied;

        public VacancyCardView(String vacancyId, String courseCode, String title,
                               String description, String department, int hoursPerWeek,
                               String hourlyRate, String deadline, String moduleOwner,
                               boolean saved, boolean isOwner, List<String> labels,
                               String type, String status, int slotsRemaining, boolean applied) {
            this.vacancyId   = vacancyId;
            this.courseCode  = courseCode;
            this.title       = title;
            this.description = description;
            this.department  = department;
            this.hoursPerWeek = hoursPerWeek;
            this.hourlyRate  = hourlyRate;
            this.deadline    = deadline;
            this.moduleOwner = moduleOwner;
            this.saved       = saved;
            this.isOwner     = isOwner;
            this.labels      = labels == null ? List.of() : List.copyOf(labels);
            this.type        = type;
            this.status      = status;
            this.slotsRemaining = slotsRemaining;
            this.applied     = applied;
        }

        public String getVacancyId()    { return vacancyId; }
        public String getCourseCode()   { return courseCode; }
        public String getTitle()        { return title; }
        public String getDescription()  { return description; }
        public String getDepartment()   { return department; }
        public int getHoursPerWeek()    { return hoursPerWeek; }
        public String getHourlyRate()   { return hourlyRate; }
        public String getDeadline()     { return deadline; }
        public String getModuleOwner()  { return moduleOwner; }
        public boolean isSaved()        { return saved; }
        public boolean isOwner()        { return isOwner; }
        public List<String> getLabels() { return labels; }
        public String getType()         { return type; }
        public String getStatus()       { return status; }
        public int getSlotsRemaining()  { return slotsRemaining; }
        public boolean isApplied()      { return applied; }
    }
}
