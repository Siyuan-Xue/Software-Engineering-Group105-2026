package com.bupt.ta.dto;

public class MessageView {
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

    public boolean isMine() {
        return isMine;
    }

    public boolean getIsMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public boolean isSystemMessage() {
        return isSystemMessage;
    }

    public boolean getIsSystemMessage() {
        return isSystemMessage;
    }

    public void setSystemMessage(boolean systemMessage) {
        isSystemMessage = systemMessage;
    }
}
