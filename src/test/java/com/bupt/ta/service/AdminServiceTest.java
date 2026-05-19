package com.bupt.ta.service;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.db.facade.FileTaDatabase;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.enums.WorkloadStatus;
import com.bupt.ta.domain.value.AuditLogQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void workloadFiltersShouldMatchTaNameAndDepartment() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        AdminService service = new AdminService(db);

        User ta = db.users().save(user("alice-ta@example.com", UserRole.TA, "Alice TA", "CS", "20210001"));
        User mo = db.users().save(user("alice-mo@example.com", UserRole.MO, "MO User", "MATH", null));
        Resume resume = db.resumes().save(resume(ta.getId()));
        Job job = db.jobs().save(job(mo.getId(), "Algorithms TA", "MATH101"));

        Application application = new Application();
        application.setResumeId(resume.getId());
        application.setJobId(job.getId());
        application.setStatus(ApplicationStatus.ACCEPTED);
        application = db.applications().save(application);

        WorkloadRecord record = new WorkloadRecord();
        record.setApplicationId(application.getId());
        record.setTaId(ta.getId());
        record.setJobId(job.getId());
        record.setSemester("Spring 2026");
        record.setAssignedHours(10);
        record.setStatus(WorkloadStatus.ACTIVE);
        db.workloadRecords().save(record);

        List<Map<String, Object>> all = service.calculateTAWorkloads();

        assertEquals(1, all.stream().filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(1, service.filterTAWorkloads(all, "Alice", null).stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(1, service.filterTAWorkloads(all, "20210001", null).stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(0, service.filterTAWorkloads(all, "zz-no-such-ta", null).stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(1, service.filterTAWorkloads(all, null, "CS").stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(1, service.filterTAWorkloads(all, null, "MATH").stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(0, service.filterTAWorkloads(all, null, "EE").stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());

        assertTrue(service.listWorkloadDepartmentOptions().contains("CS"));
        assertTrue(service.listWorkloadDepartmentOptions().contains("MATH"));
    }

    @Test
    void workloadViewsShouldComputeTotalsStatusAndSemesterOptionsFromActiveRecords() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        AdminService service = new AdminService(db);
        User ta = db.users().save(user("busy-ta-" + UUID.randomUUID() + "@example.com", UserRole.TA, "Busy TA", "CS", "S2"));
        User mo = db.users().save(user("busy-mo-" + UUID.randomUUID() + "@example.com", UserRole.MO, "Busy MO", "CS", null));
        Resume resume = resume(ta.getId());
        resume.setMaxWeeklyHours(10);
        resume = db.resumes().save(resume);
        Job job = db.jobs().save(job(mo.getId(), "Busy Job", "CS999"));
        Application app = TestApplication(resume.getId(), job.getId());
        app = db.applications().save(app);
        db.workloadRecords().save(workload(ta.getId(), job.getId(), app.getId(), "Fall 2026", 10, WorkloadStatus.ACTIVE));
        db.workloadRecords().save(workload(ta.getId(), job.getId(), app.getId(), "Spring 2027", 5, WorkloadStatus.CANCELLED));

        Map<String, Object> row = service.calculateTAWorkloads("Fall 2026", null, null).stream()
                .filter(item -> ta.getId().equals(item.get("taId")))
                .findFirst()
                .orElseThrow();

        assertEquals(10, row.get("totalWeeklyHours"));
        assertEquals(80, row.get("totalWorkloadHours"));
        assertEquals("Overloaded", row.get("workloadStatus"));
        assertTrue(service.listWorkloadSemesterOptions().contains("Fall 2026"));
        assertTrue(service.listWorkloadSemesterOptions().stream().noneMatch("Spring 2027"::equals));
    }

    @Test
    void searchAuditLogsShouldHonorQueryFiltersAndUpdateUserShouldPersist() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        AdminService service = new AdminService(db);
        User admin = db.users().save(user("audit-admin-" + UUID.randomUUID() + "@example.com", UserRole.ADMIN, "Audit Admin", "Ops", null));
        User target = db.users().save(user("audit-target-" + UUID.randomUUID() + "@example.com", UserRole.TA, "Audit Target", "CS", "S3"));
        AuditLog log = new AuditLog();
        log.setOperatorId(admin.getId());
        log.setAction(AuditAction.UPDATE);
        log.setEntityType(EntityType.USER);
        log.setEntityId(target.getId());
        log.setOperatedAt(Instant.now());
        db.auditLogs().append(log);

        AuditLogQuery query = new AuditLogQuery();
        query.setOperatorId(admin.getId());
        query.setAction(AuditAction.UPDATE);
        query.setEntityType(EntityType.USER);
        query.setSize(5);

        assertEquals(target.getId(), service.searchAuditLogs(query).stream()
                .max(Comparator.comparing(AuditLog::getOperatedAt))
                .orElseThrow()
                .getEntityId());

        target.setFullName("Updated Target");
        service.updateUser(admin.getId(), target);
        assertEquals("Updated Target", db.users().findById(target.getId()).orElseThrow().getFullName());
    }

    private User user(String email, UserRole role, String name, String department, String studentId) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(name);
        user.setDepartment(department);
        user.setStudentId(studentId);
        return user;
    }

    private Resume resume(java.util.UUID userId) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle("Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume.setMaxWeeklyHours(20);
        return resume;
    }

    private Job job(java.util.UUID posterId, String title, String moduleCode) {
        Job job = new Job();
        job.setPostedBy(posterId);
        job.setTitle(title);
        job.setModuleCode(moduleCode);
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(10);
        job.setHourlyRate(new BigDecimal("15.00"));
        job.setDeadline(Instant.now().plusSeconds(3600));
        return job;
    }

    private Application TestApplication(java.util.UUID resumeId, java.util.UUID jobId) {
        Application application = new Application();
        application.setResumeId(resumeId);
        application.setJobId(jobId);
        application.setStatus(ApplicationStatus.ACCEPTED);
        return application;
    }

    private WorkloadRecord workload(java.util.UUID taId, java.util.UUID jobId, java.util.UUID applicationId,
                                    String semester, int hours, WorkloadStatus status) {
        WorkloadRecord record = new WorkloadRecord();
        record.setTaId(taId);
        record.setJobId(jobId);
        record.setApplicationId(applicationId);
        record.setSemester(semester);
        record.setAssignedHours(hours);
        record.setStatus(status);
        return record;
    }
}
