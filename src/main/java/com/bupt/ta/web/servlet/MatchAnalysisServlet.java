package com.bupt.ta.web.servlet;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.MatchingService;
import com.bupt.ta.web.util.RedirectUrls;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Handles persisted match-analysis refreshes for Module Organisers.
 *
 * <p>Limited strictly to vacancy-owned applications currently in {@link ApplicationStatus#REVIEWING}, the servlet mirrors the backlog
 * contract: Teaching Assistants submit, Module Organisers start review, then this endpoint persists refreshed rule-driven analytics (with AI placeholders).</p>
 *
 * @see MatchingService#runAnalysis(java.util.UUID, java.util.UUID)
 */
@WebServlet("/match-analysis")
public class MatchAnalysisServlet extends HttpServlet {
    private TaDatabase database;
    private ApplicationService applicationService;
    private MatchingService matchingService;

    /** Provisions cooperating services referencing the servlet {@link TaDatabase}. */
    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.applicationService = new ApplicationService(database);
        this.matchingService = new MatchingService(database);
    }

    /**
     * Recomputes stored analytics exclusively for applications tied to the logged-in recruiter once {@link ApplicationStatus#REVIEWING} is reached,
     * then redirects back to {@code application/detail} carrying flash messaging.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = requireCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "auth.loginRequired"), StandardCharsets.UTF_8));
            return;
        }
        if (currentUser.getRole() != UserRole.MO) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only module organisers can run match analysis");
            return;
        }

        UUID applicationId;
        try {
            applicationId = UUID.fromString(param(req, "applicationId"));
            Application application = database.applications().findById(applicationId)
                    .orElseThrow(() -> new ConstraintViolationException("Application not found"));
            applicationService.assertMoOwnsApplication(currentUser.getId(), application);
            if (application.getStatus() != ApplicationStatus.REVIEWING) {
                throw new ConstraintViolationException("Match analysis can only be refreshed after review starts");
            }
            matchingService.runAnalysis(currentUser.getId(), applicationId);
            redirect(req, resp, applicationId, I18n.isChinese(I18n.resolveLanguage(req))
                    ? "匹配分析已刷新。"
                    : "Match analysis refreshed.", false);
        } catch (RuntimeException ex) {
            String rawId = req.getParameter("applicationId");
            UUID fallbackId = parseUuid(rawId);
            if (fallbackId == null) {
                resp.sendRedirect(req.getContextPath() + "/applications?errorMessage="
                        + URLEncoder.encode(errorMessage(req, ex),
                        StandardCharsets.UTF_8));
            } else {
                redirect(req, resp, fallbackId, errorMessage(req, ex), true);
            }
        }
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, UUID applicationId,
                          String message, boolean error) throws IOException {
        String base = req.getContextPath() + "/application/detail?applicationId=" + applicationId;
        resp.sendRedirect(RedirectUrls.withQueryParam(base, error ? "errorMessage" : "successMessage", message));
    }

    private User requireCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("currentUser");
        return value instanceof User user ? user : null;
    }

    private String param(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }

    private UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String errorMessage(HttpServletRequest req, RuntimeException ex) {
        String message = ex.getMessage();
        if ("Match analysis can only be refreshed after review starts".equals(message)) {
            return I18n.isChinese(I18n.resolveLanguage(req))
                    ? "请先将申请标记为审核中，再刷新匹配分析。"
                    : "Start review before refreshing match analysis.";
        }
        return message == null || message.isBlank() ? "Unable to run match analysis." : message;
    }
}
