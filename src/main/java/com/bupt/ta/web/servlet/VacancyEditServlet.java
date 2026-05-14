package com.bupt.ta.web.servlet;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.JobStatus;
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
import java.util.UUID;

@WebServlet("/vacancy/edit")
public class VacancyEditServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/vacancy_edit.jsp";
    private static final DateTimeFormatter DEADLINE_INPUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private TaDatabase database;
    private JobService jobService;

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.jobService = new JobService(database);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null || !"MO".equals(currentUser.getRole().name())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only MO can edit vacancies");
            return;
        }

        copyFlashFromQuery(req);

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
            req.setAttribute("termOptions", VacanciesServlet.buildTermOptions());
            String returnTo = req.getParameter("returnTo");
            req.setAttribute("editReturnTo", "list".equalsIgnoreCase(returnTo) ? "list" : "detail");
            req.setAttribute("pageState", "normal");
        } catch (IllegalArgumentException ex) {
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.vacancyIdRequired"), StandardCharsets.UTF_8));
            return;
        }

        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
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

            String statusRaw = req.getParameter("status");
            if (statusRaw != null && !statusRaw.isBlank()) {
                try {
                    JobStatus st = JobStatus.valueOf(statusRaw.trim().toUpperCase());
                    if (st != JobStatus.CANCELLED) {
                        job.setStatus(st);
                    }
                } catch (IllegalArgumentException ignored) {
                    // keep existing status
                }
            }

            job.setLabels(Labels.parseList(req.getParameter("labels"), 24));

            jobService.update(currentUser.getId(), job);

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
