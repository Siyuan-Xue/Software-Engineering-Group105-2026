package com.bupt.ta.web.filter;

import com.bupt.ta.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局认证与公共数据过滤器
 * 1. 拦截所有请求，校验用户是否登录。
 * 2. 如果未登录，重定向到 /login。
 * 3. 如果已登录，在 request 中注入全局共享数据（Header、Sidebar 需要的字段）。
 */
@WebFilter("/*") // 拦截所有请求，我们在代码里手动放行静态资源和登录页
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化逻辑（留空即可）
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        String route = path.substring(contextPath.length());

        // 1. 定义白名单（放行登录页、注销动作、以及所有静态资源）
        if (route.equals("/") ||
            route.equals("/db-demo") ||
            route.equals("/login") || 
            route.equals("/logout") || 
            route.startsWith("/css/") || 
            route.startsWith("/js/") || 
            route.startsWith("/images/") ||
            route.startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. 检查会话（Session）中是否有用户
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        // 3. 如果未登录，重定向到登录页，并带上前端契约规定的 errorMessage
        if (currentUser == null) {
            String errorMsg = URLEncoder.encode("Please log in to access this page", StandardCharsets.UTF_8);
            resp.sendRedirect(contextPath + "/login?errorMessage=" + errorMsg);
            return;
        }

        // ====================================================================
        // 4. 重点：用户已登录！在此注入所有 JSP 页面（Header/Sidebar）需要的公共字段
        // 这样你的业务 Servlet (DashboardServlet等) 就不需要再重复写这些代码了！
        // ====================================================================

        // 4.1 组装前端需要的 userProfile 对象
        Map<String, Object> userProfile = new HashMap<>();

        String fullName = currentUser.getFullName() != null ? currentUser.getFullName() : "Student User";
        String[] nameParts = fullName.split(" ", 2);
        userProfile.put("firstName", nameParts[0]);
        userProfile.put("lastName", nameParts.length > 1 ? nameParts[1] : "");
        userProfile.put("fullName", fullName);
        userProfile.put("email", currentUser.getEmail());
        userProfile.put("phone", currentUser.getPhone());

        // 直接从 User 对象读取持久化字段（已存入 users.json）
        String dept = currentUser.getDepartment();
        userProfile.put("department", (dept == null || dept.isBlank()) ? "None" : dept);
        userProfile.put("studentId", currentUser.getStudentId());
        userProfile.put("bio", currentUser.getBio());
        userProfile.put("notificationsEnabled", currentUser.isNotificationsEnabled());

        req.setAttribute("userName", fullName);
        req.setAttribute("userProfile", userProfile);
        req.setAttribute("userRole", currentUser.getRole().name());

        // 4.2 动态计算并注入 sidebar 需要的 profileCompletionPercentage
        int completion = calculateProfileCompletion(currentUser);
        req.setAttribute("profileCompletionPercentage", completion);

        // 5. 放行请求，继续走到对应的 Servlet
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 销毁逻辑（留空即可）
    }

    /**
     * 根据 User 对象的字段是否完善，简单计算资料完整度
     */
    private int calculateProfileCompletion(User user) {
        int score = 20; // 基础分（邮箱必填）
        if (user.getFullName() != null && !user.getFullName().isBlank()) score += 20;
        if (user.getPhone()    != null && !user.getPhone().isBlank())    score += 15;
        if (user.getDepartment() != null && !user.getDepartment().isBlank()) score += 15;
        if (user.getStudentId()  != null && !user.getStudentId().isBlank())  score += 15;
        if (user.getBio()        != null && !user.getBio().isBlank())        score += 15;
        return Math.min(score, 100);
    }
}