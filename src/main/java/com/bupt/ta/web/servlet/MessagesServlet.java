package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.dto.ConversationDTO;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.service.MessageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Servlet handling the Messages page.
 * <p>
 * GET /messages                    — render conversation list (no active conversation)
 * GET /messages?conversationId=... — render conversation list + active conversation thread
 * POST /messages                   — send a message then redirect back
 */
@WebServlet("/messages")
public class MessagesServlet extends HttpServlet {
    private static final String VIEW_PATH = "/portal/messages.jsp";

    private TaDatabase database;
    private MessageService messageService;

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.messageService = new MessageService(database);
    }

    // ── GET ──────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "auth.loginRequired"), StandardCharsets.UTF_8));
            return;
        }

        String language = I18n.resolveLanguage(req);
        boolean zh = I18n.isChinese(language);

        try {
            List<ConversationDTO> conversations = messageService.listConversations(currentUser.getId(), zh);
            req.setAttribute("conversations", conversations);

            String conversationId = normalize(req.getParameter("conversationId"));
            if (conversationId != null) {
                ConversationDTO activeConversation = messageService.getConversation(currentUser.getId(), conversationId, zh);
                if (activeConversation != null) {
                    req.setAttribute("activeConversation", activeConversation);
                    conversations = messageService.listConversations(currentUser.getId(), zh);
                    req.setAttribute("conversations", conversations);
                    req.setAttribute("pageState", "normal");
                } else {
                    req.setAttribute("pageState", "noActiveConversation");
                }
            } else if (conversations.isEmpty()) {
                req.setAttribute("pageState", "empty");
            } else {
                req.setAttribute("pageState", "noActiveConversation");
            }
            req.setAttribute("successMessage", normalize(req.getParameter("successMessage")));
            req.setAttribute("errorMessage", normalize(req.getParameter("errorMessage")));
        } catch (Exception e) {
            getServletContext().log("Failed to load messages", e);
            applyLoadError(req);
        }

        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    private void applyLoadError(HttpServletRequest req) {
        req.setAttribute("pageState", "loadError");
        req.setAttribute("conversations", List.of());
        req.removeAttribute("activeConversation");
        req.setAttribute("successMessage", null);
        req.setAttribute("errorMessage", I18n.message(req, "msg.messagesLoadFailed"));
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = requireCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "auth.loginRequired"), StandardCharsets.UTF_8));
            return;
        }

        String language = I18n.resolveLanguage(req);
        boolean zh = I18n.isChinese(language);

        String action = normalize(req.getParameter("action"));
        if ("markAllRead".equals(action)) {
            database.notifications().markAllRead(currentUser.getId());
            String successMsg = zh ? "所有通知已标记为已读。" : "All notifications marked as read.";
            resp.sendRedirect(req.getContextPath() + "/messages?conversationId=system"
                    + "&successMessage=" + URLEncoder.encode(successMsg, StandardCharsets.UTF_8));
            return;
        }

        String conversationId = normalize(req.getParameter("conversationId"));
        String messageContent = normalize(req.getParameter("messageContent"));

        if (conversationId == null || messageContent == null) {
            String errorMsg = zh ? "会话或消息内容不能为空。" : "Conversation or message content cannot be empty.";
            resp.sendRedirect(req.getContextPath() + "/messages"
                    + (conversationId != null ? "?conversationId=" + URLEncoder.encode(conversationId, StandardCharsets.UTF_8) + "&" : "?")
                    + "errorMessage=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
            return;
        }

        try {
            messageService.sendMessage(currentUser.getId(), conversationId, messageContent);
            String successMsg = zh ? "消息已发送。" : "Message sent.";
            resp.sendRedirect(req.getContextPath() + "/messages?conversationId="
                    + URLEncoder.encode(conversationId, StandardCharsets.UTF_8)
                    + "&successMessage=" + URLEncoder.encode(successMsg, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : (zh ? "发送消息失败。" : "Failed to send message.");
            resp.sendRedirect(req.getContextPath() + "/messages?conversationId="
                    + URLEncoder.encode(conversationId, StandardCharsets.UTF_8)
                    + "&errorMessage=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
        } catch (Exception e) {
            String errorMsg = zh ? "发送消息失败，请重试。" : "Failed to send message. Please try again.";
            resp.sendRedirect(req.getContextPath() + "/messages?conversationId="
                    + URLEncoder.encode(conversationId, StandardCharsets.UTF_8)
                    + "&errorMessage=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User requireCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute("currentUser");
        return value instanceof User u ? u : null;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
