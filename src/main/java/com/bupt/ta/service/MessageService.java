package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.dto.ConversationDTO;
import com.bupt.ta.dto.MessageDTO;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that provides a messaging/conversation layer on top of the Notification table.
 * <p>
 * Messages are stored as {@link Notification} records with {@code notifType = MESSAGE}.
 * The {@code userId} field stores the <b>recipient</b>, while {@code entityId} stores the <b>sender</b>.
 * The {@code message} field stores the message content, and {@code title} holds a conversation context hint.
 * <p>
 * A "conversation" is the set of all MESSAGE notifications exchanged between two users
 * (i.e. where {userId, entityId} matches {userA, userB} in either direction).
 * The conversationId is a deterministic string derived from the two user IDs.
 */
public class MessageService {
    private static final String SYSTEM_CONVERSATION_ID = "system";
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, HH:mm").withZone(ZoneId.systemDefault());

    private final TaDatabase db;

    public MessageService(TaDatabase db) {
        this.db = db;
    }

    /**
     * Build the list of conversations for the given user (without loading full message lists).
     */
    public List<ConversationDTO> listConversations(UUID currentUserId, boolean zh) {
        List<Notification> allMessages = getAllMessageNotifications();

        // Find all messages where the current user is either sender or recipient
        List<Notification> myMessages = allMessages.stream()
                .filter(n -> currentUserId.equals(n.getUserId()) || currentUserId.equals(n.getEntityId()))
                .toList();

        // Group by the "other" user
        Map<UUID, List<Notification>> byOtherUser = new LinkedHashMap<>();
        for (Notification n : myMessages) {
            UUID otherUserId = currentUserId.equals(n.getUserId()) ? n.getEntityId() : n.getUserId();
            if (otherUserId == null) continue;
            byOtherUser.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(n);
        }

        List<ConversationDTO> conversations = new ArrayList<>();
        for (Map.Entry<UUID, List<Notification>> entry : byOtherUser.entrySet()) {
            UUID otherUserId = entry.getKey();
            List<Notification> messages = entry.getValue();
            messages.sort(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));

            User otherUser = db.users().findById(otherUserId).orElse(null);

            ConversationDTO conv = new ConversationDTO();
            conv.setConversationId(buildConversationId(currentUserId, otherUserId));
            conv.setContactName(otherUser != null ? safe(otherUser.getFullName(), zh ? "用户" : "User") : (zh ? "未知用户" : "Unknown User"));
            conv.setContactRole(otherUser != null ? formatRole(otherUser.getRole().name(), zh) : (zh ? "用户" : "User"));
            conv.setContactAvatar(null);

            // Last message
            Notification lastMsg = messages.get(messages.size() - 1);
            conv.setLastMessage(truncate(lastMsg.getMessage(), 50));
            conv.setLastMessageTime(lastMsg.getCreatedAt() != null ? formatTimeAgo(lastMsg.getCreatedAt(), zh) : "");

            // Unread count: messages where current user is recipient and not read
            long unread = messages.stream()
                    .filter(n -> currentUserId.equals(n.getUserId()) && !n.isRead())
                    .count();
            conv.setUnreadCount((int) unread);

            conversations.add(conv);
        }

        // Sort by last message time (most recent first)
        conversations.sort((a, b) -> {
            // We need to compare by the actual last message time, not the formatted string
            // Re-derive from the notification data
            return 0; // Will be sorted by insertion order which is already by last message
        });

        Map<String, Instant> lastTimestamps = new HashMap<>();
        for (Map.Entry<UUID, List<Notification>> entry : byOtherUser.entrySet()) {
            List<Notification> msgs = entry.getValue();
            Notification last = msgs.get(msgs.size() - 1);
            String convId = buildConversationId(currentUserId, entry.getKey());
            lastTimestamps.put(convId, last.getCreatedAt() != null ? last.getCreatedAt() : Instant.EPOCH);
        }

        // Add system conversation for non-MESSAGE notifications
        List<Notification> systemNotifications = getSystemNotifications(currentUserId);
        if (!systemNotifications.isEmpty()) {
            ConversationDTO systemConv = new ConversationDTO();
            systemConv.setConversationId(SYSTEM_CONVERSATION_ID);
            systemConv.setContactName(zh ? "系统通知" : "System");
            systemConv.setContactRole(zh ? "应用通知" : "App Notifications");
            systemConv.setContactAvatar(null);

            Notification lastSys = systemNotifications.get(systemNotifications.size() - 1);
            systemConv.setLastMessage(truncate(lastSys.getMessage(), 50));
            systemConv.setLastMessageTime(lastSys.getCreatedAt() != null ? formatTimeAgo(lastSys.getCreatedAt(), zh) : "");

            long sysUnread = systemNotifications.stream().filter(n -> !n.isRead()).count();
            systemConv.setUnreadCount((int) sysUnread);

            conversations.add(systemConv);
            lastTimestamps.put(SYSTEM_CONVERSATION_ID, lastSys.getCreatedAt() != null ? lastSys.getCreatedAt() : Instant.EPOCH);
        }

        conversations.sort(Comparator.comparing(
                (ConversationDTO c) -> lastTimestamps.getOrDefault(c.getConversationId(), Instant.EPOCH)
        ).reversed());

        return conversations;
    }

    /**
     * Build a full conversation (including messages) between the current user and the other party
     * identified by conversationId.
     */
    public ConversationDTO getConversation(UUID currentUserId, String conversationId, boolean zh) {
        if (SYSTEM_CONVERSATION_ID.equals(conversationId)) {
            return getSystemConversation(currentUserId, zh);
        }

        UUID otherUserId = extractOtherUserId(currentUserId, conversationId);
        if (otherUserId == null) {
            return null;
        }

        List<Notification> allMessages = getAllMessageNotifications();
        List<Notification> conversationMessages = allMessages.stream()
                .filter(n -> isConversationMessage(n, currentUserId, otherUserId))
                .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        User otherUser = db.users().findById(otherUserId).orElse(null);

        ConversationDTO conv = new ConversationDTO();
        conv.setConversationId(conversationId);
        conv.setContactName(otherUser != null ? safe(otherUser.getFullName(), zh ? "用户" : "User") : (zh ? "未知用户" : "Unknown User"));
        conv.setContactRole(otherUser != null ? formatRole(otherUser.getRole().name(), zh) : (zh ? "用户" : "User"));
        conv.setContactAvatar(null);

        List<MessageDTO> messageDTOs = new ArrayList<>();
        for (Notification n : conversationMessages) {
            MessageDTO msg = new MessageDTO();
            msg.setMessageId(n.getId() != null ? n.getId().toString() : UUID.randomUUID().toString());
            msg.setContent(n.getMessage());
            msg.setTimestamp(n.getCreatedAt() != null ? TIME_FORMATTER.format(n.getCreatedAt()) : "");
            // isMine: the current user is the sender (entityId is sender)
            msg.setIsMine(currentUserId.equals(n.getEntityId()));
            msg.setIsSystemMessage(false);
            messageDTOs.add(msg);
        }
        conv.setMessages(messageDTOs);

        // Last message info
        if (!conversationMessages.isEmpty()) {
            Notification lastMsg = conversationMessages.get(conversationMessages.size() - 1);
            conv.setLastMessage(truncate(lastMsg.getMessage(), 50));
            conv.setLastMessageTime(lastMsg.getCreatedAt() != null ? formatTimeAgo(lastMsg.getCreatedAt(), zh) : "");
        }

        // Unread count
        long unread = conversationMessages.stream()
                .filter(n -> currentUserId.equals(n.getUserId()) && !n.isRead())
                .count();
        conv.setUnreadCount((int) unread);

        // Mark messages as read for the current user
        markConversationRead(currentUserId, otherUserId);

        return conv;
    }

    /**
     * Send a message from the current user to the other party in the given conversation.
     */
    public void sendMessage(UUID senderUserId, String conversationId, String content) {
        UUID recipientUserId = extractOtherUserId(senderUserId, conversationId);
        if (recipientUserId == null) {
            throw new IllegalArgumentException("Invalid conversation ID");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        // Verify both users exist
        if (!db.users().exists(senderUserId)) {
            throw new IllegalArgumentException("Sender user not found");
        }
        if (!db.users().exists(recipientUserId)) {
            throw new IllegalArgumentException("Recipient user not found");
        }

        Notification notification = new Notification();
        notification.setUserId(recipientUserId);        // recipient
        notification.setEntityId(senderUserId);          // sender
        notification.setEntityType(EntityType.USER);     // conversation context
        notification.setNotifType(NotificationType.MESSAGE);
        notification.setTitle("Message");                // generic title for message type
        notification.setMessage(content.trim());
        notification.setRead(false);

        db.notifications().save(notification);
    }

    /**
     * Start or retrieve a conversation with a specific user.
     * Returns the conversation ID.
     */
    public String getOrCreateConversationId(UUID currentUserId, UUID otherUserId) {
        return buildConversationId(currentUserId, otherUserId);
    }


    /**
     * Return all non-MESSAGE notifications addressed to the given user, sorted oldest-first.
     */
    private List<Notification> getSystemNotifications(UUID userId) {
        return db.notifications().findAll().stream()
                .filter(n -> userId.equals(n.getUserId()))
                .filter(n -> n.getNotifType() != NotificationType.MESSAGE)
                .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /**
     * Build the synthetic "System" conversation containing all non-MESSAGE notifications.
     */
    private ConversationDTO getSystemConversation(UUID currentUserId, boolean zh) {
        List<Notification> systemNotifications = getSystemNotifications(currentUserId);

        ConversationDTO conv = new ConversationDTO();
        conv.setConversationId(SYSTEM_CONVERSATION_ID);
        conv.setContactName(zh ? "系统通知" : "System");
        conv.setContactRole(zh ? "应用通知" : "App Notifications");
        conv.setContactAvatar(null);

        List<MessageDTO> messageDTOs = new ArrayList<>();
        for (Notification n : systemNotifications) {
            MessageDTO msg = new MessageDTO();
            msg.setMessageId(n.getId() != null ? n.getId().toString() : UUID.randomUUID().toString());
            String title = n.getTitle() == null || n.getTitle().isBlank() ? typeLabel(n.getNotifType(), zh) : n.getTitle();
            msg.setContent(title + " - " + n.getMessage());
            msg.setTimestamp(n.getCreatedAt() != null ? TIME_FORMATTER.format(n.getCreatedAt()) : "");
            msg.setTypeLabel(typeLabel(n.getNotifType(), zh));
            msg.setActionHref(actionHref(n));
            msg.setIsMine(false);
            msg.setIsSystemMessage(true);
            messageDTOs.add(msg);
        }
        conv.setMessages(messageDTOs);

        if (!systemNotifications.isEmpty()) {
            Notification lastMsg = systemNotifications.get(systemNotifications.size() - 1);
            conv.setLastMessage(truncate(lastMsg.getMessage(), 50));
            conv.setLastMessageTime(lastMsg.getCreatedAt() != null ? formatTimeAgo(lastMsg.getCreatedAt(), zh) : "");
        }

        long unread = systemNotifications.stream().filter(n -> !n.isRead()).count();
        conv.setUnreadCount((int) unread);

        markSystemNotificationsRead(currentUserId, systemNotifications);

        return conv;
    }

    private String typeLabel(NotificationType type, boolean zh) {
        if (type == null) {
            return zh ? "通知" : "Notification";
        }
        return switch (type) {
            case APPLICATION_STATUS -> zh ? "申请" : "Application";
            case NEW_JOB -> zh ? "新岗位" : "New job";
            case NEW_APPLICANT -> zh ? "新申请人" : "Applicant";
            case OFFER_RECEIVED -> zh ? "Offer" : "Offer";
            case WORKLOAD_ALERT -> zh ? "工作量" : "Workload";
            case SYSTEM -> zh ? "系统" : "System";
            case MESSAGE -> zh ? "消息" : "Message";
        };
    }

    private String actionHref(Notification notification) {
        EntityType entityType = notification.getEntityType();
        UUID entityId = notification.getEntityId();
        if (entityType == null || entityId == null) {
            return null;
        }
        return switch (entityType) {
            case APPLICATION -> "/application/detail?applicationId=" + entityId;
            case JOB -> "/vacancy?vacancyId=" + entityId;
            case WORKLOAD_RECORD -> "/workloads";
            case MATCH_SCORE -> "/applications";
            default -> null;
        };
    }

    private void markSystemNotificationsRead(UUID userId, List<Notification> systemNotifications) {
        for (Notification n : systemNotifications) {
            if (!n.isRead()) {
                n.setRead(true);
                db.notifications().save(n);
            }
        }
    }


    private List<Notification> getAllMessageNotifications() {
        return db.notifications().findAll().stream()
                .filter(n -> n.getNotifType() == NotificationType.MESSAGE)
                .toList();
    }

    private boolean isConversationMessage(Notification n, UUID userA, UUID userB) {
        // A message belongs to a conversation if it was sent between userA and userB
        return (userA.equals(n.getUserId()) && userB.equals(n.getEntityId()))
                || (userB.equals(n.getUserId()) && userA.equals(n.getEntityId()));
    }

    private void markConversationRead(UUID currentUserId, UUID otherUserId) {
        List<Notification> allMessages = getAllMessageNotifications();
        List<Notification> unread = allMessages.stream()
                .filter(n -> currentUserId.equals(n.getUserId()) && otherUserId.equals(n.getEntityId()))
                .filter(n -> !n.isRead())
                .toList();

        for (Notification n : unread) {
            n.setRead(true);
            db.notifications().save(n);
        }
    }

    /**
     * Build a deterministic conversation ID from two user IDs.
     * Format: "conv-{smallerUUID}-{largerUUID}" to ensure the same pair always yields the same ID.
     */
    static String buildConversationId(UUID userA, UUID userB) {
        if (userA.compareTo(userB) <= 0) {
            return "conv-" + userA + "-" + userB;
        } else {
            return "conv-" + userB + "-" + userA;
        }
    }

    /**
     * Extract the other user's UUID from a conversation ID, given the current user.
     */
    UUID extractOtherUserId(UUID currentUserId, String conversationId) {
        if (conversationId == null || !conversationId.startsWith("conv-")) {
            return null;
        }
        String payload = conversationId.substring(5); // remove "conv-"
        // The payload is "{uuid1}-{uuid2}" but UUIDs themselves contain hyphens
        // UUID format: 8-4-4-4-12 = 36 chars
        // So payload is 36 + 1 + 36 = 73 chars
        if (payload.length() != 73) {
            return null;
        }
        try {
            String first = payload.substring(0, 36);
            String second = payload.substring(37, 73);
            UUID uuid1 = UUID.fromString(first);
            UUID uuid2 = UUID.fromString(second);
            if (currentUserId.equals(uuid1)) {
                return uuid2;
            } else if (currentUserId.equals(uuid2)) {
                return uuid1;
            } else {
                return null; // Current user not part of this conversation
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String formatRole(String role, boolean zh) {
        if (role == null) return zh ? "用户" : "User";
        return switch (role.toUpperCase()) {
            case "TA" -> zh ? "助教" : "Teaching Assistant";
            case "MO" -> zh ? "模块负责人" : "Module Organiser";
            case "ADMIN" -> zh ? "管理员" : "Administrator";
            default -> zh ? "用户" : "User";
        };
    }

    private String formatTimeAgo(Instant instant, boolean zh) {
        if (instant == null) return "";
        long minutes = Duration.between(instant, Instant.now()).toMinutes();
        if (minutes < 1) return zh ? "刚刚" : "Just now";
        if (minutes < 60) return zh ? minutes + " 分钟前" : minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return zh ? hours + " 小时前" : hours + "h ago";
        long days = hours / 24;
        if (days < 7) return zh ? days + " 天前" : days + "d ago";
        return TIME_FORMATTER.format(instant);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
