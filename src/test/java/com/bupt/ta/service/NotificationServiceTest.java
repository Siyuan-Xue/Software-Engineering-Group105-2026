package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.value.NotificationQuery;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void inboxShouldScopeQueryToRequestedUserAndHonorFilters() {
        TaDatabase db = TestDatabases.open(tempDir);
        NotificationService service = new NotificationService(db);
        User user = db.users().save(TestData.user(TestData.uniqueEmail("notify"), UserRole.TA, "Notify User"));
        User other = db.users().save(TestData.user(TestData.uniqueEmail("notify-other"), UserRole.TA, "Notify Other"));

        Notification unread = db.notifications().save(TestData.notification(user.getId(), NotificationType.SYSTEM, "Unread"));
        Notification read = TestData.notification(user.getId(), NotificationType.SYSTEM, "Read");
        read.setRead(true);
        db.notifications().save(read);
        db.notifications().save(TestData.notification(other.getId(), NotificationType.SYSTEM, "Other"));

        NotificationQuery query = new NotificationQuery();
        query.setUserId(other.getId());
        query.setUnreadOnly(true);
        query.setType(NotificationType.SYSTEM);
        query.setLimit(10);

        assertEquals(1, service.inbox(user.getId(), query).size());
        assertEquals(user.getId(), query.getUserId());
        assertEquals(unread.getId(), service.inbox(user.getId(), query).get(0).getId());
    }

    @Test
    void markAllReadShouldOnlyMutateRequestedUsersNotifications() {
        TaDatabase db = TestDatabases.open(tempDir);
        NotificationService service = new NotificationService(db);
        User user = db.users().save(TestData.user(TestData.uniqueEmail("mark-read"), UserRole.TA, "Mark User"));
        User other = db.users().save(TestData.user(TestData.uniqueEmail("mark-other"), UserRole.TA, "Other User"));
        Notification own = db.notifications().save(TestData.notification(user.getId(), NotificationType.SYSTEM, "Own"));
        Notification others = db.notifications().save(TestData.notification(other.getId(), NotificationType.SYSTEM, "Others"));

        service.markAllRead(user.getId());

        assertTrue(db.notifications().findById(own.getId()).orElseThrow().isRead());
        assertEquals(false, db.notifications().findById(others.getId()).orElseThrow().isRead());
    }
}
