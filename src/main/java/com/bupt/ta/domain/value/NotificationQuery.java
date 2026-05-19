package com.bupt.ta.domain.value;

import com.bupt.ta.domain.enums.NotificationType;

import java.util.UUID;

/**
 * Filter and paging criteria for notification inbox queries.
 */
public class NotificationQuery {
    private UUID userId;
    private Boolean unreadOnly;
    private NotificationType type;
    private int limit = 50;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Boolean getUnreadOnly() {
        return unreadOnly;
    }

    public void setUnreadOnly(Boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
