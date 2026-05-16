package com.bupt.ta.web.servlet;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.service.ApplicationService;
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

@WebServlet("/application/decision")
public class ApplicationDecisionServlet extends HttpServlet {
    private ApplicationService applicationService;

    @Override
    public void init() throws ServletException {
        TaDatabase database = DatabaseProvider.get(getServletContext());
        this.applicationService = new ApplicationService(database);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "auth.loginRequired"), StandardCharsets.UTF_8));
            return;
        }

        String applicationIdStr = param(req, "applicationId");
        String action = param(req, "action");
        if (applicationIdStr == null || action == null) {
            resp.sendRedirect(req.getContextPath() + "/applications?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.appDecisionMissing"), StandardCharsets.UTF_8));
            return;
        }

        UUID applicationId;
        try {
            applicationId = UUID.fromString(applicationIdStr);
        } catch (IllegalArgumentException ex) {
            resp.sendRedirect(req.getContextPath() + "/applications?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.appInvalidIds"), StandardCharsets.UTF_8));
            return;
        }

        try {
            String successKey = switch (action) {
                case "review" -> {
                    requireRole(currentUser, UserRole.MO);
                    applicationService.startReview(currentUser.getId(), applicationId);
                    yield "msg.appReviewStarted";
                }
                case "offer" -> {
                    requireRole(currentUser, UserRole.MO);
                    applicationService.sendOffer(currentUser.getId(), applicationId);
                    yield "msg.appOfferSent";
                }
                case "reject" -> {
                    requireRole(currentUser, UserRole.MO);
                    applicationService.reject(currentUser.getId(), applicationId, req.getParameter("rejectionNote"));
                    yield "msg.appRejected";
                }
                case "accept" -> {
                    requireRole(currentUser, UserRole.TA);
                    applicationService.acceptOffer(currentUser.getId(), applicationId);
                    yield "msg.appOfferAccepted";
                }
                case "decline" -> {
                    requireRole(currentUser, UserRole.TA);
                    applicationService.declineOffer(currentUser.getId(), applicationId);
                    yield "msg.appOfferDeclined";
                }
                case "withdraw" -> {
                    requireRole(currentUser, UserRole.TA);
                    applicationService.withdraw(currentUser.getId(), applicationId);
                    yield "msg.appWithdrawn";
                }
                default -> throw new ConstraintViolationException("Unsupported decision action: " + action);
            };
            redirectAfterDecision(req, resp, applicationId, param(req, "returnTo"),
                    I18n.message(req, successKey), false);
        } catch (ConstraintViolationException ex) {
            redirectAfterDecision(req, resp, applicationId, param(req, "returnTo"),
                    localizeDecisionError(req, ex.getMessage()), true);
        } catch (RuntimeException ex) {
            redirectAfterDecision(req, resp, applicationId, param(req, "returnTo"),
                    localizeDecisionError(req, ex.getMessage()), true);
        }
    }

    private static String localizeDecisionError(HttpServletRequest req, String message) {
        if (message == null || message.isBlank()) {
            return I18n.message(req, "msg.appDecisionFailed");
        }
        return switch (message) {
            case "Only submitted applications can be moved to review" ->
                    I18n.message(req, "msg.appReviewOnlySubmitted");
            case "Only submitted or reviewing applications can receive an offer" ->
                    I18n.message(req, "msg.appOfferOnlyReviewing");
            case "This vacancy has no remaining slots for new offers" ->
                    I18n.message(req, "msg.appNoSlots");
            case "This application can no longer be withdrawn" ->
                    I18n.message(req, "msg.appCannotWithdraw");
            default -> message;
        };
    }

    private void redirectAfterDecision(HttpServletRequest req, HttpServletResponse resp, UUID applicationId,
                                       String returnTo, String message, boolean isError) throws IOException {
        String base = "detail".equalsIgnoreCase(returnTo)
                ? req.getContextPath() + "/application/detail?applicationId=" + applicationId
                : req.getContextPath() + "/applications";
        String paramName = isError ? "errorMessage" : "successMessage";
        resp.sendRedirect(RedirectUrls.withQueryParam(base, paramName, message));
    }

    private static void requireRole(User user, UserRole role) {
        if (user.getRole() != role) {
            throw new ConstraintViolationException("You are not allowed to perform this action");
        }
    }

    private static String param(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
