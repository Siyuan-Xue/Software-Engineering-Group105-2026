package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.service.AdminService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/workloads")
public class WorkloadsServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/workloads.jsp";

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

        try {
            String keyword = normalize(req.getParameter("keyword"));
            String department = normalize(req.getParameter("department"));
            String semester = normalize(req.getParameter("semester"));

            List<Map<String, Object>> allWorkloads = adminService.calculateTAWorkloads(semester, null, null);
            List<Map<String, Object>> workloads = adminService.filterTAWorkloads(allWorkloads, keyword, department);
            boolean filtersActive = keyword != null || department != null || semester != null;

            int totalAcceptedTAs = workloads.size();
            int totalWeeklyHours = workloads.stream().mapToInt(w -> (Integer) w.get("totalWeeklyHours")).sum();
            BigDecimal totalEstimatedIncome = BigDecimal.ZERO;
            for (Map<String, Object> v : workloads) {
                totalEstimatedIncome = totalEstimatedIncome.add((BigDecimal) v.get("totalEstimatedIncome"));
            }
            int overloadedTAs = (int) workloads.stream().filter(w -> "Overloaded".equals(w.get("workloadStatus"))).count();

            String pageState;
            if (workloads.isEmpty()) {
                pageState = allWorkloads.isEmpty() ? "empty" : (filtersActive ? "noSearchResults" : "empty");
            } else {
                pageState = "normal";
            }

            req.setAttribute("pageState", pageState);
            req.setAttribute("workloads", workloads);
            req.setAttribute("departmentOptions", adminService.listWorkloadDepartmentOptions(semester));
            req.setAttribute("semesterOptions", adminService.listWorkloadSemesterOptions());
            req.setAttribute("totalAcceptedTAs", totalAcceptedTAs);
            req.setAttribute("totalWeeklyHours", totalWeeklyHours);
            req.setAttribute("totalEstimatedIncome", totalEstimatedIncome);
            req.setAttribute("overloadedTAs", overloadedTAs);
        } catch (Exception ex) {
            getServletContext().log("Failed to load workloads", ex);
            req.setAttribute("pageState", "loadError");
            req.setAttribute("workloads", List.of());
            req.setAttribute("totalAcceptedTAs", 0);
            req.setAttribute("totalWeeklyHours", 0);
            req.setAttribute("totalEstimatedIncome", BigDecimal.ZERO);
            req.setAttribute("overloadedTAs", 0);
            req.setAttribute("departmentOptions", List.of());
            req.setAttribute("semesterOptions", List.of());
            req.setAttribute("errorMessage", I18n.message(req, "msg.workloadsLoadFailed"));
        }

        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
