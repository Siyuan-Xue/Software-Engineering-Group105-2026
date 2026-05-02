package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.User;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@WebServlet("/vacancy/edit")
public class VacancyEditServlet extends HttpServlet {
    private TaDatabase database;
    private JobService jobService;

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.jobService = new JobService(database);
    }

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
            redirectError(req, resp, null, I18n.message(req, "msg.vacancyIdRequired"));
            return;
        }

        try {
            UUID vacancyId = UUID.fromString(vacancyIdValue.trim());
            Job job = database.jobs().findById(vacancyId).orElse(null);
            if (job == null) {
                redirectError(req, resp, vacancyIdValue, I18n.message(req, "msg.vacancyIdRequired"));
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

            jobService.update(currentUser.getId(), job);
            resp.sendRedirect(req.getContextPath() + "/vacancy?vacancyId=" + vacancyId + "&successMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.vacancyUpdated"), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            redirectError(req, resp, vacancyIdValue, ex.getMessage() == null ? I18n.message(req, "msg.vacancyUpdated") : ex.getMessage());
        }
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

    private void redirectError(HttpServletRequest req, HttpServletResponse resp, String vacancyId, String message) throws IOException {
        String target = vacancyId == null || vacancyId.isBlank()
                ? req.getContextPath() + "/vacancies"
                : req.getContextPath() + "/vacancy?vacancyId=" + URLEncoder.encode(vacancyId, StandardCharsets.UTF_8);
        String separator = target.contains("?") ? "&" : "?";
        resp.sendRedirect(target + separator + "errorMessage=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
