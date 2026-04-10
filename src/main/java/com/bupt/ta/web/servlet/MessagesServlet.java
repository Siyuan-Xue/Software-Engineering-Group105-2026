package com.bupt.ta.web.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.persistence.DatabaseProvider;
import com.bupt.ta.persistence.TaDatabase;
import com.bupt.ta.service.MessagesService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/messages")
public class MessagesServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/messages.jsp";

    private MessagesService messagesService;

    @Override
    public void init() throws ServletException {
        TaDatabase database = DatabaseProvider.get(getServletContext());
        this.messagesService = new MessagesService(database);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            String requestedConversationId = normalize(req.getParameter("conversationId"));
            MessagesService.InboxData inbox = messagesService.loadInbox(currentUser, requestedConversationId);

            req.setAttribute("conversations", inbox.conversations());
            if (inbox.activeConversation() != null) {
                req.setAttribute("activeConversation", inbox.activeConversation());
            }
            req.setAttribute("pageState", inbox.pageState());
            applyFeedback(req, inbox.derivedErrorMessage());
            req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
        } catch (Exception ex) {
            req.setAttribute("pageState", "loadError");
            req.setAttribute("errorMessage", "Failed to load messages. Please try again.");
            req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = requireCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String conversationId = normalize(req.getParameter("conversationId"));
        try {
            messagesService.appendMessage(currentUser, conversationId, req.getParameter("messageContent"));
            redirectToConversation(resp, req, conversationId, "successMessage", "Message sent successfully.");
        } catch (IllegalArgumentException ex) {
            redirectToConversation(
                    resp,
                    req,
                    conversationId,
                    "errorMessage",
                    ex.getMessage() != null ? ex.getMessage() : "Failed to send message."
            );
        } catch (Exception ex) {
            redirectToConversation(resp, req, conversationId, "errorMessage", "Failed to send message.");
        }
    }

    private void applyFeedback(HttpServletRequest req, String derivedErrorMessage) {
        String successMessage = normalize(req.getParameter("successMessage"));
        if (successMessage != null) {
            req.setAttribute("successMessage", successMessage);
        }

        String errorMessage = normalize(req.getParameter("errorMessage"));
        if (errorMessage != null) {
            req.setAttribute("errorMessage", errorMessage);
        } else if (derivedErrorMessage != null) {
            req.setAttribute("errorMessage", derivedErrorMessage);
        }
    }

    private void redirectToConversation(HttpServletResponse resp,
                                        HttpServletRequest req,
                                        String conversationId,
                                        String messageKey,
                                        String message) throws IOException {
        StringBuilder target = new StringBuilder(req.getContextPath()).append("/messages");
        boolean hasQuery = false;

        if (conversationId != null) {
            target.append("?conversationId=")
                    .append(URLEncoder.encode(conversationId, StandardCharsets.UTF_8));
            hasQuery = true;
        }

        target.append(hasQuery ? "&" : "?")
                .append(messageKey)
                .append("=")
                .append(URLEncoder.encode(message, StandardCharsets.UTF_8));
        resp.sendRedirect(target.toString());
    }

    private User requireCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("currentUser");
        return value instanceof User user ? user : null;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
