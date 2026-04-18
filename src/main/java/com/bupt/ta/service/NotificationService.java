package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.value.NotificationQuery;

import java.util.List;
import java.util.UUID;

public class NotificationService {
    private final TaDatabase db;

    public NotificationService(TaDatabase db) {
        this.db = db;
    }

    public Notification create(Notification notification) {
        return db.notifications().save(notification);
    }

    public List<Notification> inbox(UUID userId, NotificationQuery query) {
        query.setUserId(userId);
        return db.notifications().listByUser(query);
    }

    public void markAllRead(UUID userId) {
        db.notifications().markAllRead(userId);
    }
}
