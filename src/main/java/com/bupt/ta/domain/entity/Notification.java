package com.bupt.ta.domain.entity;

import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * In-app notification or message addressed to a single user.
 */
public class Notification extends AbstractEntity {
    private UUID userId;
    private NotificationType notifType;
    private String title;
    private String message;
    private boolean read;
    private EntityType entityType;
    private UUID entityId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public NotificationType getNotifType() {
        return notifType;
    }

    public void setNotifType(NotificationType notifType) {
        this.notifType = notifType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }
}
