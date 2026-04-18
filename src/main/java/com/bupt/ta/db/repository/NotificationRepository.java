package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.value.NotificationQuery;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends CrudRepository<Notification> {
    List<Notification> listByUser(NotificationQuery query);

    void markRead(UUID notificationId);

    void markAllRead(UUID userId);
}
