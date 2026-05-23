package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.value.NotificationQuery;

import java.util.List;
import java.util.UUID;

/**
 * Coordinates inbox reads and acknowledgement state on top of the JSON notification repository.
 */
public class NotificationService {
    private final TaDatabase db;

    /**
     * @param db shared facade opened during servlet context bootstrap
     */
    public NotificationService(TaDatabase db) {
        this.db = db;
    }

    /**
     * Persists a new inbox row exactly as constructed by callers (validation remains caller responsibility).
     *
     * @param notification hydrated entity ({@link Notification#getUserId()}, textual payload, entity pointers, timestamps, ...)
     * @return saved instance containing generated identifiers where applicable
     */
    public Notification create(Notification notification) {
        return db.notifications().save(notification);
    }

    /**
     * Lists notifications for {@code userId} using the paging/filter flags carried by {@code query}.
     *
     * @param userId authenticated recipient
     * @param query template whose {@code userId} field will be overwritten; must not be {@code null}
     * @return ordered rows ready for servlet/JSP projection
     */
    public List<Notification> inbox(UUID userId, NotificationQuery query) {
        query.setUserId(userId);
        return db.notifications().listByUser(query);
    }

    /**
     * Marks every unread notification for {@code userId} as read.
     *
     * @param userId notification owner id
     */
    public void markAllRead(UUID userId) {
        db.notifications().markAllRead(userId);
    }
}
