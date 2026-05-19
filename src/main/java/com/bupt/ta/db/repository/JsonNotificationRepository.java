package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.value.NotificationQuery;
import com.bupt.ta.db.store.JsonTableStore;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JSON-backed notification repository for inbox and unread-count operations.
 */
public class JsonNotificationRepository extends BaseJsonRepository<Notification> implements NotificationRepository {
    public JsonNotificationRepository(JsonTableStore<Notification> store) {
        super(store);
    }

    @Override
    public List<Notification> listByUser(NotificationQuery query) {
        if (query == null || query.getUserId() == null) {
            return List.of();
        }
        return findAll().stream()
                .filter(notification -> query.getUserId().equals(notification.getUserId()))
                .filter(notification -> query.getUnreadOnly() == null || !query.getUnreadOnly() || !notification.isRead())
                .filter(notification -> query.getType() == null || notification.getNotifType() == query.getType())
                .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(query.getLimit() <= 0 ? Long.MAX_VALUE : query.getLimit())
                .toList();
    }

    @Override
    public void markRead(UUID notificationId) {
        Optional<Notification> existing = findById(notificationId);
        existing.ifPresent(notification -> {
            notification.setRead(true);
            save(notification);
        });
    }

    @Override
    public void markAllRead(UUID userId) {
        store.replaceAll(findAll().stream().peek(notification -> {
            if (userId.equals(notification.getUserId())) {
                notification.setRead(true);
            }
        }).toList());
    }

    @Override
    public Notification save(Notification entity) {
        if (entity.getUserId() == null) {
            throw new ConstraintViolationException("Notification userId must not be null");
        }
        if (entity.getTitle() == null || entity.getTitle().isBlank()) {
            throw new ConstraintViolationException("Notification title must not be blank");
        }
        return super.save(entity);
    }
}
