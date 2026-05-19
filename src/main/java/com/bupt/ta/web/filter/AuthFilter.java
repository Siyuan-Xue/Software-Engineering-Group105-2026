package com.bupt.ta.web.filter;

import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.value.NotificationQuery;
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
 * Global authentication filter and request-context injector.
 *
 * <p>The filter redirects unauthenticated users to the login page and enriches
 * authenticated requests with shared profile, role, theme, language, and
 * notification attributes used by portal JSP fragments. Servlet-level
 * authorization remains responsible for sensitive role-specific operations.</p>
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        String language = resolveLanguage(session, currentUser);
        String appearance = resolveAppearance(session, currentUser);
        req.setAttribute("language", language);
        req.setAttribute("langTag", I18n.langTag(language));
        req.setAttribute("i18n", I18n.messagesFor(language));
        req.setAttribute("appearance", appearance);

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        String route = path.substring(contextPath.length());
        boolean dbDemoRoute = route.equals("/db-demo");

        if (route.equals("/") ||
            route.equals("/login") ||
            route.equals("/register") ||
            route.equals("/forgot-password") ||
            route.equals("/logout") ||
            dbDemoRoute ||
            route.startsWith("/css/") || 
            route.startsWith("/js/") || 
            route.startsWith("/images/") ||
            route.startsWith("/assets/")) {
            if (dbDemoRoute && currentUser == null) {
                applyGuestProfile(req, language, appearance);
            }
            chain.doFilter(request, response);
            return;
        }

        if (currentUser == null) {
            String errorMsg = URLEncoder.encode(I18n.message(language, "auth.loginRequired"), StandardCharsets.UTF_8);
            resp.sendRedirect(contextPath + "/login?errorMessage=" + errorMsg);
            return;
        }

        if (session != null) {
            session.setAttribute(I18n.SESSION_LANGUAGE_ATTR, language);
            session.setAttribute(I18n.SESSION_APPEARANCE_ATTR, appearance);
        }

        // Shared profile data consumed by the header and sidebar fragments.
        Map<String, Object> userProfile = new HashMap<>();

        String fullName = currentUser.getFullName() != null ? currentUser.getFullName() : I18n.message(language, "common.studentUser");
        String[] nameParts = fullName.split(" ", 2);
        userProfile.put("firstName", nameParts[0]);
        userProfile.put("lastName", nameParts.length > 1 ? nameParts[1] : "");
        userProfile.put("fullName", fullName);
        userProfile.put("email", currentUser.getEmail());
        userProfile.put("phone", currentUser.getPhone());

        String dept = currentUser.getDepartment();
        userProfile.put("department", (dept == null || dept.isBlank()) ? "None" : dept);
        userProfile.put("studentId", currentUser.getStudentId());
        userProfile.put("bio", currentUser.getBio());
        userProfile.put("notificationsEnabled", currentUser.isNotificationsEnabled());
        userProfile.put("preferredLanguage", language);
        userProfile.put("preferredAppearance", appearance);

        req.setAttribute("userName", fullName);
        req.setAttribute("userProfile", userProfile);
        req.setAttribute("userRole", currentUser.getRole().name());

        int completion = calculateProfileCompletion(currentUser);
        req.setAttribute("profileCompletionPercentage", completion);
        req.setAttribute("unreadNotificationCount", unreadNotificationCount(req, currentUser));

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Calculates the profile-completion percentage displayed in the sidebar.
     */
    private int calculateProfileCompletion(User user) {
        int score = 20;
        if (user.getFullName() != null && !user.getFullName().isBlank()) score += 20;
        if (user.getPhone()    != null && !user.getPhone().isBlank())    score += 15;
        if (user.getDepartment() != null && !user.getDepartment().isBlank()) score += 15;
        if (user.getStudentId()  != null && !user.getStudentId().isBlank())  score += 15;
        if (user.getBio()        != null && !user.getBio().isBlank())        score += 15;
        return Math.min(score, 100);
    }

    private String resolveLanguage(HttpSession session, User currentUser) {
        if (currentUser != null && currentUser.getPreferredLanguage() != null) {
            return I18n.normalizeLanguage(currentUser.getPreferredLanguage());
        }
        if (session != null) {
            Object language = session.getAttribute(I18n.SESSION_LANGUAGE_ATTR);
            if (language instanceof String value) {
                return I18n.normalizeLanguage(value);
            }
        }
        return I18n.DEFAULT_LANGUAGE;
    }

    private String resolveAppearance(HttpSession session, User currentUser) {
        if (currentUser != null && currentUser.getPreferredAppearance() != null) {
            return I18n.normalizeAppearance(currentUser.getPreferredAppearance());
        }
        if (session != null) {
            Object appearance = session.getAttribute(I18n.SESSION_APPEARANCE_ATTR);
            if (appearance instanceof String value) {
                return I18n.normalizeAppearance(value);
            }
        }
        return I18n.DEFAULT_APPEARANCE;
    }

    private void applyGuestProfile(HttpServletRequest req, String language, String appearance) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("firstName", "DB");
        userProfile.put("lastName", "Demo");
        userProfile.put("fullName", "DB Demo");
        userProfile.put("email", "demo@local");
        userProfile.put("phone", "");
        userProfile.put("department", "System Demo");
        userProfile.put("studentId", "");
        userProfile.put("bio", "Public demo mode");
        userProfile.put("notificationsEnabled", false);
        userProfile.put("preferredLanguage", language);
        userProfile.put("preferredAppearance", appearance);

        req.setAttribute("userName", "DB Demo");
        req.setAttribute("userProfile", userProfile);
        req.setAttribute("userRole", "DEMO");
        req.setAttribute("profileCompletionPercentage", 100);
        req.setAttribute("userRoleLabel", "Public Demo");
    }

    private int unreadNotificationCount(HttpServletRequest req, User currentUser) {
        try {
            TaDatabase database = DatabaseProvider.get(req.getServletContext());
            NotificationQuery query = new NotificationQuery();
            query.setUserId(currentUser.getId());
            query.setUnreadOnly(true);
            query.setLimit(1000);
            return database.notifications().listByUser(query).size();
        } catch (RuntimeException ex) {
            req.getServletContext().log("Unable to load unread notification count", ex);
            return 0;
        }
    }
}
