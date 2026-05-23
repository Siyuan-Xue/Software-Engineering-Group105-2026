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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * MO-only creation pipeline at {@code /vacancy/create} binding HTML forms to persisted {@link com.bupt.ta.domain.entity.Job} rows.
 *
 * <p>POST bodies describe scheduling metadata, remuneration defaults, attached {@link JobRequirement} selections, and tag expansions routed through {@link JobService}.</p>
 */
@WebServlet("/vacancy/create")
public class VacancyCreateServlet extends HttpServlet {

    private TaDatabase database;
    private JobService jobService;

    /** Resolves collaborator services validating MO-only publication rules. */
    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.jobService = new JobService(database);
    }

    /** Creates vacancies after verifying MO session ownership. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");

        if (currentUser == null || !"MO".equals(currentUser.getRole().name())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only MO can create vacancies");
            return;
        }

        String title = req.getParameter("title");
        String courseCode = req.getParameter("courseCode");
        String description = req.getParameter("description");
        String hoursPerWeekStr = req.getParameter("hoursPerWeek");
        String hourlyRateStr = req.getParameter("hourlyRate");
        String deadlineStr = req.getParameter("deadline");
        String term = req.getParameter("term");
        String typeRaw = req.getParameter("type");
        String slotsRaw = req.getParameter("slots");
        String statusRaw = req.getParameter("status");
        String startDateRaw = req.getParameter("startDate");
        String endDateRaw = req.getParameter("endDate");

        if (title == null || title.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.vacancyTitleRequired"), StandardCharsets.UTF_8));
            return;
        }

        Job job = new Job();
        job.setTitle(title.trim());
        job.setModuleCode(courseCode != null ? courseCode.trim() : "");
        job.setDescription(description != null ? description.trim() : "");
        job.setPostedBy(currentUser.getId());
        job.setStatus(parseStatus(statusRaw, JobStatus.OPEN));
        job.setType(parseType(typeRaw));

        try {
            if (hoursPerWeekStr != null && !hoursPerWeekStr.trim().isEmpty()) {
                job.setRequiredHours(Integer.parseInt(hoursPerWeekStr.trim()));
            }
        } catch (NumberFormatException ignored) {}

        try {
            if (slotsRaw != null && !slotsRaw.trim().isEmpty()) {
                job.setSlots(Math.max(1, Integer.parseInt(slotsRaw.trim())));
            }
        } catch (NumberFormatException ignored) {}

        try {
            if (hourlyRateStr != null && !hourlyRateStr.trim().isEmpty()) {
                job.setHourlyRate(new BigDecimal(hourlyRateStr.trim()));
            }
        } catch (NumberFormatException ignored) {}

        try {
            if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
                String d = deadlineStr.trim();
                if (d.length() == 16 && d.charAt(10) == ' ') {
                    d = d.substring(0, 10) + "T" + d.substring(11);
                }
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                job.setDeadline(java.time.LocalDateTime.parse(d, formatter)
                        .atZone(ZoneId.systemDefault()).toInstant());
            }
        } catch (DateTimeParseException ignored) {}

        if (job.getDeadline() == null) {
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.vacancyDeadlineInvalid"), StandardCharsets.UTF_8));
            return;
        }
        if (!job.getDeadline().isAfter(Instant.now())) {
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                    + URLEncoder.encode("Deadline must be in the future.", StandardCharsets.UTF_8));
            return;
        }

        if (term != null && !term.trim().isEmpty()) {
            String[] parts = term.trim().split(" ");
            if (parts.length == 2) {
                try {
                    int year = Integer.parseInt(parts[1]);
                    if ("Spring".equalsIgnoreCase(parts[0])) {
                        job.setStartDate(LocalDate.of(year, 1, 1));
                    } else if ("Fall".equalsIgnoreCase(parts[0])) {
                        job.setStartDate(LocalDate.of(year, 8, 1));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        try {
            if (startDateRaw != null && !startDateRaw.isBlank()) {
                job.setStartDate(LocalDate.parse(startDateRaw.trim()));
            }
            if (endDateRaw != null && !endDateRaw.isBlank()) {
                job.setEndDate(LocalDate.parse(endDateRaw.trim()));
            }
        } catch (DateTimeParseException ignored) {}

        job.setLabels(Labels.parseList(req.getParameter("labels"), 24));

        Job saved = jobService.save(job);
        replaceRequirements(req, currentUser.getId(), saved.getId());

        resp.sendRedirect(req.getContextPath() + "/vacancies?successMessage="
                + URLEncoder.encode(I18n.message(req, "msg.vacancyCreated"), StandardCharsets.UTF_8));
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

    private JobType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            return JobType.MODULE_SUPPORT;
        }
        try {
            return JobType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return JobType.MODULE_SUPPORT;
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
}
