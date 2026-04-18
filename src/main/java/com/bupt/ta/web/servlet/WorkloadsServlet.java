package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.value.WorkloadAggregate;
import com.bupt.ta.service.AdminService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet("/workloads")
public class WorkloadsServlet extends HttpServlet {
    private TaDatabase database;
    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.adminService = new AdminService(database);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole().name())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        String semester = currentSemester();
        List<Map<String, Object>> workloads = adminService.workloadDashboard(semester).stream()
                .map(this::toView)
                .toList();
        req.setAttribute("workloads", workloads);
        req.getRequestDispatcher("/portal/workloads.jsp").forward(req, resp);
    }

    private Map<String, Object> toView(WorkloadAggregate aggregate) {
        User user = database.users().findById(aggregate.getTaId()).orElse(null);
        int activeJobs = database.workloadRecords().listByTaAndSemester(aggregate.getTaId(), aggregate.getSemester()).size();
        return Map.of(
                "taName", user == null ? "Unknown TA" : safe(user.getFullName(), "Unknown TA"),
                "department", user == null ? "CS" : safe(user.getDepartment(), "CS"),
                "activeJobsCount", activeJobs,
                "totalHoursPerWeek", aggregate.getAssignedHours(),
                "status", aggregate.getRemainingHours() < 0 ? "Overloaded" : "Active"
        );
    }

    private String currentSemester() {
        LocalDate today = LocalDate.now();
        return (today.getMonthValue() >= 8 ? "Fall " : "Spring ") + today.getYear();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
