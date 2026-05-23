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
 * Servlet filter mounted on {@code /*} enforcing login redirection and injecting portal request attributes.
 *
 * <p>Public routes (landing, credentials, logout, static assets) bypass enforcement. Authenticated traversals hydrate
 * internationalisation artefacts, flattened {@code userProfile} maps for header fragments, recruiter role strings,
 * profile-completion heuristics, and unread notification tallies sourced from {@link TaDatabase}.
 * Detailed resource authorisation stays within individual controllers.</p>
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    /** No-filter configuration hook; container lifecycle invokes this before first {@link #doFilter}. */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Enriches the request pipeline or terminates with a redirect to {@code /login}.
     *
     * @param request  incoming servlet request (cast to {@link HttpServletRequest})
     * @param response outgoing servlet response (cast to {@link HttpServletResponse})
     * @param chain    remainder of the filter/servlet invocation stack
     */
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

        if (route.equals("/") ||
            route.equals("/login") ||
            route.equals("/register") ||
            route.equals("/forgot-password") ||
            route.equals("/logout") ||
            route.startsWith("/css/") ||
            route.startsWith("/js/") ||
            route.startsWith("/images/") ||
            route.startsWith("/assets/")) {
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

        // Shared profile map consumed by header and sidebar fragments.
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

    /** Stateful resources are not tracked; hook retained for completeness. */
    @Override
    public void destroy() {
    }

    /**
     * Heuristic sidebar gauge derived from populated profile primitives.
     *
     * @return percentage capped at {@code 100}
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

    /**
     * @param session     optional servlet session transporting anonymous preference overrides
     * @param currentUser authoritative account row when authenticated
     * @return normalised locale token understood by {@link I18n}
     */
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

    /**
     * Mirrors {@link #resolveLanguage(HttpSession, User)} precedence for CSS theme presets.
     */
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

    /**
     * Counts unread {@link com.bupt.ta.domain.entity.Notification} envelopes for badges; silently degrades when persistence IO fails mid-request.
     */
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
