package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.enums.WorkloadStatus;
import com.bupt.ta.domain.value.AuditLogQuery;
import com.bupt.ta.domain.value.NotificationQuery;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonRepositoryDetailedTest {
    @TempDir
    Path tempDir;

    @Test
    void notificationRepositoryShouldFilterSortLimitAndMarkRead() {
        TaDatabase db = TestDatabases.open(tempDir);
        User user = db.users().save(TestData.user(TestData.uniqueEmail("repo-notify"), UserRole.TA, "Repo Notify"));
        User other = db.users().save(TestData.user(TestData.uniqueEmail("repo-notify-other"), UserRole.TA, "Repo Notify Other"));
        Notification old = notification(user.getId(), NotificationType.SYSTEM, "Old", Instant.parse("2026-01-01T00:00:00Z"), false);
        Notification newest = notification(user.getId(), NotificationType.SYSTEM, "Newest", Instant.parse("2026-01-03T00:00:00Z"), false);
        Notification read = notification(user.getId(), NotificationType.SYSTEM, "Read", Instant.parse("2026-01-04T00:00:00Z"), true);
        Notification wrongType = notification(user.getId(), NotificationType.MESSAGE, "Message", Instant.parse("2026-01-05T00:00:00Z"), false);
        Notification otherUser = notification(other.getId(), NotificationType.SYSTEM, "Other", Instant.parse("2026-01-06T00:00:00Z"), false);
        old = db.notifications().save(old);
        newest = db.notifications().save(newest);
        db.notifications().save(read);
        db.notifications().save(wrongType);
        otherUser = db.notifications().save(otherUser);

        NotificationQuery query = new NotificationQuery();
        query.setUserId(user.getId());
        query.setUnreadOnly(true);
        query.setType(NotificationType.SYSTEM);
        query.setLimit(2);

        List<Notification> rows = db.notifications().listByUser(query);
        assertEquals(List.of(newest.getId(), old.getId()), rows.stream().map(Notification::getId).toList());

        db.notifications().markRead(newest.getId());
        assertTrue(db.notifications().findById(newest.getId()).orElseThrow().isRead());
        assertFalse(db.notifications().findById(old.getId()).orElseThrow().isRead());
        db.notifications().markAllRead(user.getId());
        assertTrue(db.notifications().findById(old.getId()).orElseThrow().isRead());
        assertFalse(db.notifications().findById(otherUser.getId()).orElseThrow().isRead());
    }

    @Test
    void auditRepositoryShouldFilterByFieldsDatesAndPageInReverseChronologicalOrder() {
        TaDatabase db = TestDatabases.open(tempDir);
        User operator = db.users().save(TestData.user(TestData.uniqueEmail("repo-audit"), UserRole.ADMIN, "Audit Operator"));
        User other = db.users().save(TestData.user(TestData.uniqueEmail("repo-audit-other"), UserRole.ADMIN, "Other Operator"));
        AuditLog old = audit(operator.getId(), AuditAction.CREATE, EntityType.USER, Instant.parse("2026-01-01T00:00:00Z"));
        AuditLog middle = audit(operator.getId(), AuditAction.UPDATE, EntityType.USER, Instant.parse("2026-01-02T00:00:00Z"));
        AuditLog newest = audit(operator.getId(), AuditAction.UPDATE, EntityType.USER, Instant.parse("2026-01-03T00:00:00Z"));
        AuditLog wrongOperator = audit(other.getId(), AuditAction.UPDATE, EntityType.USER, Instant.parse("2026-01-04T00:00:00Z"));
        db.auditLogs().append(old);
        middle = db.auditLogs().save(middle);
        newest = db.auditLogs().save(newest);
        db.auditLogs().append(wrongOperator);

        AuditLogQuery query = new AuditLogQuery();
        query.setOperatorId(operator.getId());
        query.setAction(AuditAction.UPDATE);
        query.setEntityType(EntityType.USER);
        query.setFrom(Instant.parse("2026-01-02T00:00:00Z"));
        query.setTo(Instant.parse("2026-01-04T00:00:00Z"));
        query.setSize(1);

        assertEquals(newest.getId(), db.auditLogs().search(query).get(0).getId());
        query.setPage(1);
        assertEquals(middle.getId(), db.auditLogs().search(query).get(0).getId());
    }

    @Test
    void resumeDuplicateUniquenessAndApplicationExistenceShouldFollowDomainRules() {
        TaDatabase db = TestDatabases.open(tempDir);
        User ta = db.users().save(TestData.user(TestData.uniqueEmail("repo-resume"), UserRole.TA, "Repo Resume TA"));
        User mo = db.users().save(TestData.user(TestData.uniqueEmail("repo-resume-mo"), UserRole.MO, "Repo Resume MO"));
        Resume resume = db.resumes().save(TestData.resume(ta.getId(), "Main Resume"));
        Resume duplicate = db.resumes().duplicate(resume.getId());

        assertEquals("Main Resume Copy", duplicate.getTitle());
        assertThrows(ConstraintViolationException.class,
                () -> db.resumes().save(TestData.resume(ta.getId(), "main resume")));

        Job job = db.jobs().save(TestData.openJob(mo.getId(), "Repository Application Job"));
        Application withdrawn = TestData.application(resume.getId(), job.getId(), ApplicationStatus.WITHDRAWN);
        db.applications().save(withdrawn);
        assertFalse(db.applications().existsByTaAndJob(ta.getId(), job.getId()));

        Application active = TestData.application(duplicate.getId(), job.getId(), ApplicationStatus.PENDING);
        db.applications().save(active);
        assertTrue(db.applications().existsByTaAndJob(ta.getId(), job.getId()));
    }

    @Test
    void workloadAggregateShouldIgnoreCancelledRecordsAndUseResumeCapacity() {
        TaDatabase db = TestDatabases.open(tempDir);
        User ta = db.users().save(TestData.user(TestData.uniqueEmail("repo-workload"), UserRole.TA, "Repo Workload TA"));
        User mo = db.users().save(TestData.user(TestData.uniqueEmail("repo-workload-mo"), UserRole.MO, "Repo Workload MO"));
        Resume resume = TestData.resume(ta.getId(), "Capacity Resume");
        resume.setMaxWeeklyHours(12);
        resume = db.resumes().save(resume);
        Job job = db.jobs().save(TestData.openJob(mo.getId(), "Repository Workload Job"));
        Application application = db.applications().save(TestData.application(resume.getId(), job.getId(), ApplicationStatus.ACCEPTED));
        db.workloadRecords().save(workload(ta.getId(), job.getId(), application.getId(), "Spring 2026", 8, WorkloadStatus.ACTIVE));
        db.workloadRecords().save(workload(ta.getId(), job.getId(), application.getId(), "Spring 2026", 99, WorkloadStatus.CANCELLED));

        var aggregate = db.workloadRecords().aggregateBySemester("Spring 2026").stream()
                .filter(item -> ta.getId().equals(item.getTaId()))
                .findFirst()
                .orElseThrow();

        assertEquals(8, aggregate.getAssignedHours());
        assertEquals(12, aggregate.getCapacityHours());
        assertEquals(4, aggregate.getRemainingHours());
        assertEquals(8.0 / 12.0, aggregate.getUtilizationRatio(), 0.0001);
    }

    @Test
    void repositoriesShouldValidateRequiredFields() {
        TaDatabase db = TestDatabases.open(tempDir);

        assertThrows(ConstraintViolationException.class, () -> db.users().save(new User()));
        assertThrows(ConstraintViolationException.class, () -> db.jobs().save(new Job()));
        assertThrows(ConstraintViolationException.class, () -> db.applications().save(new Application()));
        assertThrows(ConstraintViolationException.class, () -> db.skills().save(new Skill()));
        assertThrows(ConstraintViolationException.class, () -> db.workloadRecords().save(new WorkloadRecord()));
    }

    private Notification notification(UUID userId, NotificationType type, String title, Instant createdAt, boolean read) {
        Notification notification = TestData.notification(userId, type, title);
        notification.setCreatedAt(createdAt);
        notification.setRead(read);
        return notification;
    }

    private AuditLog audit(UUID operatorId, AuditAction action, EntityType entityType, Instant operatedAt) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(UUID.randomUUID());
        log.setOperatedAt(operatedAt);
        return log;
    }

    private WorkloadRecord workload(UUID taId, UUID jobId, UUID applicationId, String semester, int hours,
                                    WorkloadStatus status) {
        WorkloadRecord record = TestData.workload(taId, jobId, applicationId, semester, hours);
        record.setStatus(status);
        return record;
    }
}
