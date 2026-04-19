package com.bupt.ta.service;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.db.facade.FileTaDatabase;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbDemoServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void demoFlowShouldWriteAcrossRepositoriesUsingUnifiedDatabaseEntry() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        DbDemoService service = new DbDemoService(db);

        User ta = new User();
        ta.setEmail("db-demo-ta@example.com");
        ta.setFullName("DB Demo TA");
        ta.setRole(UserRole.TA);
        ta.setDepartment("CS");
        ta = service.saveUser(ta, "secret123");

        User mo = new User();
        mo.setEmail("db-demo-mo@example.com");
        mo.setFullName("DB Demo MO");
        mo.setRole(UserRole.MO);
        mo = service.saveUser(mo, "secret123");

        assertTrue(PasswordUtil.checkPassword("secret123", db.users().findById(ta.getId()).orElseThrow().getPasswordHash()));

        Resume resume = new Resume();
        resume.setUserId(ta.getId());
        resume.setTitle("Database Demo Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume.setGpa(new BigDecimal("3.80"));
        resume.setMaxWeeklyHours(16);
        resume = service.saveResume(resume, """
                [
                  {"dayOfWeek":"MONDAY","startTime":"09:00","endTime":"11:00"}
                ]
                """);

        Job job = new Job();
        job.setPostedBy(mo.getId());
        job.setTitle("Database Demo Job");
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(8);
        job.setSlots(1);
        job.setDeadline(Instant.now().plusSeconds(7200));
        job = service.saveJob(job);

        Application application = service.submitApplication(resume.getId(), job.getId(), "Please consider me.");
        service.startReview(application.getId());
        service.sendOffer(application.getId());
        service.acceptOffer(application.getId());

        assertEquals(1, db.workloadRecords().findAll().size());
        assertTrue(db.notifications().findAll().size() >= 3);
        assertTrue(db.auditLogs().findAll().size() >= 3);
        assertEquals(ApplicationStatus.ACCEPTED, db.applications().findById(application.getId()).orElseThrow().getStatus());
        assertEquals(1, db.resumes().findById(resume.getId()).orElseThrow().getAvailabilitySlots().size());
        assertEquals(2, service.listTableCounts().stream()
                .filter(table -> table.getTableName().equals("users"))
                .findFirst()
                .orElseThrow()
                .getRowCount());
    }
}
