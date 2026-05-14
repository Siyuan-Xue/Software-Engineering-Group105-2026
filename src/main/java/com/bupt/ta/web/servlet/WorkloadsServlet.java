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

        List<Map<String, Object>> workloads = adminService.calculateTAWorkloads();
        
        int totalAcceptedTAs = workloads.size();
        int totalWeeklyHours = workloads.stream().mapToInt(w -> (Integer) w.get("totalWeeklyHours")).sum();
        java.math.BigDecimal totalEstimatedIncome = java.math.BigDecimal.ZERO;
        for (Map<String, Object> v : workloads) {
            totalEstimatedIncome = totalEstimatedIncome.add((java.math.BigDecimal) v.get("totalEstimatedIncome"));
        }
        int overloadedTAs = (int) workloads.stream().filter(w -> "Overloaded".equals(w.get("workloadStatus"))).count();

        req.setAttribute("workloads", workloads);
        req.setAttribute("totalAcceptedTAs", totalAcceptedTAs);
        req.setAttribute("totalWeeklyHours", totalWeeklyHours);
        req.setAttribute("totalEstimatedIncome", totalEstimatedIncome);
        req.setAttribute("overloadedTAs", overloadedTAs);
        
        req.getRequestDispatcher("/portal/workloads.jsp").forward(req, resp);
    }
}
