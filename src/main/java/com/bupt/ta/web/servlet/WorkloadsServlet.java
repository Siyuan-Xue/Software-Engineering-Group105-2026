package com.bupt.ta.web.servlet;

import com.bupt.ta.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/workloads")
public class WorkloadsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null || !"ADMIN".equals(currentUser.getRole().name())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

<<<<<<< Updated upstream
        // Mock Data for TA Workloads
        List<Map<String, Object>> workloads = new ArrayList<>();
        
        Map<String, Object> ta1 = new HashMap<>();
        ta1.put("taId", "TA001");
        ta1.put("taName", "Alice Smith");
        ta1.put("department", "CS");
        ta1.put("activeJobsCount", 2);
        ta1.put("totalHoursPerWeek", 15);
        ta1.put("status", "Active");
        workloads.add(ta1);

        Map<String, Object> ta2 = new HashMap<>();
        ta2.put("taId", "TA002");
        ta2.put("taName", "Bob Jones");
        ta2.put("department", "MATH");
        ta2.put("activeJobsCount", 1);
        ta2.put("totalHoursPerWeek", 10);
        ta2.put("status", "Active");
        workloads.add(ta2);
=======
        List<Map<String, Object>> workloads = adminService.calculateTAWorkloads();
        
        int totalAcceptedTAs = workloads.size();
        int totalWeeklyHours = workloads.stream().mapToInt(w -> (Integer) w.get("totalWeeklyHours")).sum();
        java.math.BigDecimal totalEstimatedIncome = java.math.BigDecimal.ZERO;
        for (Map<String, Object> v : workloads) {
            totalEstimatedIncome = totalEstimatedIncome.add((java.math.BigDecimal) v.get("totalEstimatedIncome"));
        }
        int overloadedTAs = (int) workloads.stream().filter(w -> "Overloaded".equals(w.get("workloadStatus"))).count();
>>>>>>> Stashed changes

        req.setAttribute("workloads", workloads);
        req.setAttribute("totalAcceptedTAs", totalAcceptedTAs);
        req.setAttribute("totalWeeklyHours", totalWeeklyHours);
        req.setAttribute("totalEstimatedIncome", totalEstimatedIncome);
        req.setAttribute("overloadedTAs", overloadedTAs);
        
        req.getRequestDispatcher("/portal/workloads.jsp").forward(req, resp);
    }
}
