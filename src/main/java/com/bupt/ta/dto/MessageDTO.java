package com.bupt.ta.dto;

/**
 * View object representing a single message inside a conversation.
 * Assembled by MessageService from Notification records.
 */
public class MessageDTO {
    private String messageId;
    private String content;
    private String timestamp;
    private boolean isMine;
    private boolean isSystemMessage;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getIsMine() {
        return isMine;
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }

    public boolean getIsSystemMessage() {
        return isSystemMessage;
    }

    public void setIsSystemMessage(boolean isSystemMessage) {
        this.isSystemMessage = isSystemMessage;
    }
}