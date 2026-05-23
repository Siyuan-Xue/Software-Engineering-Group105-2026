package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.bupt.ta.service.JobService;
import com.bupt.ta.util.Labels;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Owner-managed vacancy maintenance mapped to {@code /vacancy/edit}.
 *
 * <p>{@code GET} primes {@code vacancy_edit.jsp} with requirement metadata while {@code POST} snapshots edits, replaces structured requirements,
 * and coordinates status transitions exclusively for the recruiter who authored the vacancy.</p>
 */
@WebServlet("/vacancy/edit")
public class VacancyEditServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/vacancy_edit.jsp";
    private static final DateTimeFormatter DEADLINE_INPUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private TaDatabase database;
    private JobService jobService;

    /** Acquires persistence collaborators for guarded job updates. */
    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.jobService = new JobService(database);
    }

    /** Loads authoritative job rows plus requirement collections for authorised MO editors. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null || !"MO".equals(currentUser.getRole().name())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only MO can edit vacancies");
            return;
        }

        String vacancyIdValue = req.getParameter("vacancyId");
        if (vacancyIdValue == null || vacancyIdValue.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.vacancyIdRequired"), StandardCharsets.UTF_8));
            return;
        }

        try {
            UUID vacancyId = UUID.fromString(vacancyIdValue.trim());
            Job job = database.jobs().findById(vacancyId).orElse(null);
            if (job == null) {
                resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                        + URLEncoder.encode(I18n.message(req, "msg.vacancyIdRequired"), StandardCharsets.UTF_8));
                return;
            }
            if (!currentUser.getId().equals(job.getPostedBy())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only the vacancy owner can edit it");
                return;
            }

            req.setAttribute("editVacancyId", job.getId().toString());
            req.setAttribute("editTitle", job.getTitle() != null ? job.getTitle() : "");
            req.setAttribute("editCourseCode", job.getModuleCode() != null ? job.getModuleCode() : "");
            req.setAttribute("editDescription", job.getDescription() != null ? job.getDescription() : "");
            req.setAttribute("editHoursPerWeek", Math.max(job.getRequiredHours(), 1));
            req.setAttribute("editSlots", Math.max(job.getSlots(), 1));
            req.setAttribute("editType", job.getType() == null ? JobType.MODULE_SUPPORT.name() : job.getType().name());
            req.setAttribute("editStartDate", job.getStartDate() == null ? "" : job.getStartDate().toString());
            req.setAttribute("editEndDate", job.getEndDate() == null ? "" : job.getEndDate().toString());
            BigDecimal rate = job.getHourlyRate();
            req.setAttribute("editHourlyRate", rate != null ? rate.toPlainString() : "20.00");
            if (job.getDeadline() != null) {
                String dl = LocalDateTime.ofInstant(job.getDeadline(), ZoneId.systemDefault()).format(DEADLINE_INPUT);
                req.setAttribute("editDeadline", dl);
            } else {
                req.setAttribute("editDeadline", "");
            }
            req.setAttribute("editTerm", formatTerm(job.getStartDate()));
            req.setAttribute("editStatus", job.getStatus() != null ? job.getStatus().name() : JobStatus.OPEN.name());
            req.setAttribute("editLabels", String.join(", ", job.getLabels()));
            req.setAttribute("skills", database.skills().findAll().stream()
                    .sorted(Comparator.comparing(Skill::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .toList());
            req.setAttribute("proficiencyLevels", ProficiencyLevel.values());
            req.setAttribute("jobRequirements", buildRequirementViews(job.getId()));
            req.setAttribute("termOptions", VacanciesServlet.buildTermOptions());
            String returnTo = req.getParameter("returnTo");
            req.setAttribute("editReturnTo", "list".equalsIgnoreCase(returnTo) ? "list" : "detail");
            req.setAttribute("pageState", "normal");
            copyFlashFromQuery(req);
        } catch (IllegalArgumentException ex) {
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.vacancyIdRequired"), StandardCharsets.UTF_8));
            return;
        } catch (Exception ex) {
            getServletContext().log("Failed to load vacancy edit form", ex);
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.vacancyDetailLoadFailed"), StandardCharsets.UTF_8));
            return;
        }

        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    /** Persists job mutations, optionally updates lifecycle status, and rewrites requirement rows atomically via {@link JobService}. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null || !"MO".equals(currentUser.getRole().name())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only MO can edit vacancies");
            return;
        }

        String vacancyIdValue = req.getParameter("vacancyId");
        if (vacancyIdValue == null || vacancyIdValue.isBlank()) {
            redirectError(req, resp, null, I18n.message(req, "msg.vacancyIdRequired"), null);
            return;
        }

        String returnTo = req.getParameter("returnTo");

        try {
            UUID vacancyId = UUID.fromString(vacancyIdValue.trim());
            Job job = database.jobs().findById(vacancyId).orElse(null);
            if (job == null) {
                redirectError(req, resp, vacancyIdValue, I18n.message(req, "msg.vacancyIdRequired"), returnTo);
                return;
            }
            if (!currentUser.getId().equals(job.getPostedBy())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only the vacancy owner can edit it");
                return;
            }

            String title = req.getParameter("title");
            if (title != null && !title.isBlank()) {
                job.setTitle(title.trim());
            }
            String courseCode = req.getParameter("courseCode");
            if (courseCode != null) {
                job.setModuleCode(courseCode.trim());
            }
            String description = req.getParameter("description");
            if (description != null) {
                job.setDescription(description.trim());
            }
            String hoursPerWeek = req.getParameter("hoursPerWeek");
            if (hoursPerWeek != null && !hoursPerWeek.isBlank()) {
                job.setRequiredHours(Integer.parseInt(hoursPerWeek.trim()));
            }
            String slots = req.getParameter("slots");
            if (slots != null && !slots.isBlank()) {
                job.setSlots(Math.max(1, Integer.parseInt(slots.trim())));
            }
            String type = req.getParameter("type");
            if (type != null && !type.isBlank()) {
                job.setType(JobType.valueOf(type.trim().toUpperCase()));
            }
            String hourlyRate = req.getParameter("hourlyRate");
            if (hourlyRate != null && !hourlyRate.isBlank()) {
                job.setHourlyRate(new BigDecimal(hourlyRate.trim()));
            }
            String deadline = req.getParameter("deadline");
            if (deadline != null && !deadline.isBlank()) {
                String normalized = deadline.trim().replace(' ', 'T');
                job.setDeadline(LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .atZone(ZoneId.systemDefault()).toInstant());
            }
            String term = req.getParameter("term");
            if (term != null && !term.isBlank()) {
                applyTerm(job, term.trim());
            }
            String startDate = req.getParameter("startDate");
            if (startDate != null && !startDate.isBlank()) {
                job.setStartDate(LocalDate.parse(startDate.trim()));
            }
            String endDate = req.getParameter("endDate");
            if (endDate != null && !endDate.isBlank()) {
                job.setEndDate(LocalDate.parse(endDate.trim()));
            }

            JobStatus originalStatus = job.getStatus() == null ? JobStatus.DRAFT : job.getStatus();
            JobStatus desiredStatus = parseStatus(req.getParameter("status"), originalStatus);
            job.setStatus(originalStatus);

            job.setLabels(Labels.parseList(req.getParameter("labels"), 24));

            jobService.update(currentUser.getId(), job);
            if (desiredStatus != originalStatus) {
                jobService.changeStatus(currentUser.getId(), job.getId(), desiredStatus);
            }
            replaceRequirements(req, currentUser.getId(), job.getId());

            String ok = URLEncoder.encode(I18n.message(req, "msg.vacancyUpdated"), StandardCharsets.UTF_8);
            if ("list".equalsIgnoreCase(returnTo)) {
                resp.sendRedirect(req.getContextPath() + "/vacancies?successMessage=" + ok);
            } else {
                resp.sendRedirect(req.getContextPath() + "/vacancy?vacancyId=" + vacancyId + "&successMessage=" + ok);
            }
        } catch (Exception ex) {
            String msg = ex.getMessage() == null || ex.getMessage().isBlank()
                    ? I18n.message(req, "msg.vacancyUpdateFailed")
                    : ex.getMessage();
            redirectError(req, resp, vacancyIdValue, msg, returnTo);
        }
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

    private static String formatTerm(LocalDate startDate) {
        if (startDate == null) {
            return "";
        }
        int year = startDate.getYear();
        int month = startDate.getMonthValue();
        return (month >= 8 ? "Fall " : "Spring ") + year;
    }

    private void applyTerm(Job job, String term) {
        String[] parts = term.split(" ");
        if (parts.length != 2) {
            return;
        }
        int year = Integer.parseInt(parts[1]);
        if ("Spring".equalsIgnoreCase(parts[0])) {
            job.setStartDate(LocalDate.of(year, 1, 1));
        } else if ("Fall".equalsIgnoreCase(parts[0])) {
            job.setStartDate(LocalDate.of(year, 8, 1));
        }
    }

    private JobStatus parseStatus(String raw, JobStatus fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return JobStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private void replaceRequirements(HttpServletRequest req, UUID operatorId, UUID jobId) {
        String[] skillIds = req.getParameterValues("skillId");
        String[] requiredValues = req.getParameterValues("requiredSkill");
        String[] proficiencies = req.getParameterValues("minProficiency");
        Set<String> requiredIndexes = requiredValues == null ? Set.of() : Set.of(requiredValues);
        List<JobRequirement> requirements = new ArrayList<>();
        Set<UUID> seenSkillIds = new HashSet<>();
        if (skillIds != null) {
            for (int i = 0; i < skillIds.length; i++) {
                if (skillIds[i] == null || skillIds[i].isBlank()) {
                    continue;
                }
                UUID skillId = UUID.fromString(skillIds[i].trim());
                Skill skill = database.skills().findById(skillId)
                        .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + skillId));
                if (!skill.isActive()) {
                    throw new IllegalArgumentException("Inactive skill cannot be required: " + skill.getName());
                }
                if (!seenSkillIds.add(skillId)) {
                    continue;
                }
                JobRequirement requirement = new JobRequirement();
                requirement.setSkillId(skillId);
                requirement.setRequired(requiredIndexes.contains(String.valueOf(i)));
                requirement.setMinProficiency(parseProficiency(proficiencies, i));
                requirements.add(requirement);
            }
        }
        jobService.replaceRequirements(operatorId, jobId, requirements);
    }

    private ProficiencyLevel parseProficiency(String[] values, int index) {
        if (values == null || index >= values.length || values[index] == null || values[index].isBlank()) {
            return ProficiencyLevel.BEGINNER;
        }
        return ProficiencyLevel.valueOf(values[index].trim());
    }

    private List<Map<String, Object>> buildRequirementViews(UUID jobId) {
        Map<UUID, Skill> skillsById = database.skills().findAll().stream()
                .collect(Collectors.toMap(Skill::getId, skill -> skill));
        return database.jobRequirements().listByJobId(jobId).stream()
                .map(requirement -> {
                    Skill skill = skillsById.get(requirement.getSkillId());
                    return Map.<String, Object>of(
                            "skillId", requirement.getSkillId(),
                            "name", skill == null ? "Unknown skill" : skill.getName(),
                            "required", requirement.isRequired(),
                            "minProficiency", requirement.getMinProficiency() == null ? ProficiencyLevel.BEGINNER : requirement.getMinProficiency()
                    );
                })
                .toList();
    }

    private void redirectError(HttpServletRequest req, HttpServletResponse resp, String vacancyId, String message,
                               String returnTo) throws IOException {
        if (vacancyId == null || vacancyId.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                    + URLEncoder.encode(message, StandardCharsets.UTF_8));
            return;
        }
        StringBuilder target = new StringBuilder(req.getContextPath())
                .append("/vacancy/edit?vacancyId=")
                .append(URLEncoder.encode(vacancyId, StandardCharsets.UTF_8))
                .append("&errorMessage=")
                .append(URLEncoder.encode(message, StandardCharsets.UTF_8));
        if (returnTo != null && !returnTo.isBlank()) {
            target.append("&returnTo=").append(URLEncoder.encode(returnTo.trim(), StandardCharsets.UTF_8));
        }
        resp.sendRedirect(target.toString());
    }
}
