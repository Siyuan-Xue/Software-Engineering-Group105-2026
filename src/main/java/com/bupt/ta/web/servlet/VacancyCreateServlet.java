package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.service.JobService;
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

@WebServlet("/vacancy/create")
public class VacancyCreateServlet extends HttpServlet {

    private JobService jobService;

    @Override
    public void init() throws ServletException {
        TaDatabase database = DatabaseProvider.get(getServletContext());
        this.jobService = new JobService(database);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

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
        job.setStatus(JobStatus.OPEN);
        job.setType(JobType.MODULE_SUPPORT);

        try {
            if (hoursPerWeekStr != null && !hoursPerWeekStr.trim().isEmpty()) {
                job.setRequiredHours(Integer.parseInt(hoursPerWeekStr.trim()));
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

        jobService.save(job);

        resp.sendRedirect(req.getContextPath() + "/vacancies?successMessage="
                + URLEncoder.encode(I18n.message(req, "msg.vacancyCreated"), StandardCharsets.UTF_8));
    }
}
