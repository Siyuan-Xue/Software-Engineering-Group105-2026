package com.bupt.ta.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * View object representing a conversation between the current user and another user.
 * This is not a persisted entity; it is assembled by MessageService from Notification records.
 */
public class ConversationDTO {
    private String conversationId;
    private String contactName;
    private String contactRole;
    private String contactAvatar;
    private String lastMessage;
    private String lastMessageTime;
    private int unreadCount;
    private List<MessageDTO> messages = new ArrayList<>();

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactRole() {
        return contactRole;
    }

    public void setContactRole(String contactRole) {
        this.contactRole = contactRole;
    }

    public String getContactAvatar() {
        return contactAvatar;
    }

    public void setContactAvatar(String contactAvatar) {
        this.contactAvatar = contactAvatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<MessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDTO> messages) {
        this.messages = messages == null ? new ArrayList<>() : messages;
    }
}
