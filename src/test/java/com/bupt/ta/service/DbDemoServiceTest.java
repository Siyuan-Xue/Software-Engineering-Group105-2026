package com.bupt.ta.service;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.Resume;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.ApplicationStatus;
import com.bupt.ta.model.enums.DegreeLevel;
import com.bupt.ta.model.enums.JobStatus;
import com.bupt.ta.model.enums.JobType;
import com.bupt.ta.model.enums.UserRole;
import com.bupt.ta.persistence.TaDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbDemoServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void deactivateUserShouldPersistInactiveFlag() {
        DbDemoService service = createService();
        User user = service.saveUser(user("ta@example.com", UserRole.TA, "TA User"));

        assertTrue(service.deactivateUser(user.getId()));
        assertFalse(service.findUser(user.getId()).orElseThrow().isActive());
    }

    @Test
    void saveJobShouldRejectPosterWithoutRecruiterRole() {
        DbDemoService service = createService();
        User ta = service.saveUser(user("ta@example.com", UserRole.TA, "TA User"));
        Job job = job(ta.getId());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.saveJob(job));
        assertEquals("Job poster must be an MO or ADMIN user", error.getMessage());
    }

    @Test
    void saveApplicationShouldRejectUnknownForeignKeys() {
        DbDemoService service = createService();
        Application application = new Application();
        application.setResumeId(UUID.randomUUID());
        application.setJobId(UUID.randomUUID());
        application.setStatus(ApplicationStatus.PENDING);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.saveApplication(application));
        assertTrue(error.getMessage().startsWith("Application resume does not exist"));
    }

    @Test
    void deleteResumeAndJobShouldRejectReferencedRecords() {
        DbDemoService service = createService();

        User ta = service.saveUser(user("ta@example.com", UserRole.TA, "TA User"));
        User mo = service.saveUser(user("mo@example.com", UserRole.MO, "MO User"));
        Resume resume = service.saveResume(resume(ta.getId()));
        Job job = service.saveJob(job(mo.getId()));

        Application application = new Application();
        application.setResumeId(resume.getId());
        application.setJobId(job.getId());
        application.setStatus(ApplicationStatus.PENDING);
        service.saveApplication(application);

        assertThrows(IllegalArgumentException.class, () -> service.deleteResume(resume.getId()));
        assertThrows(IllegalArgumentException.class, () -> service.deleteJob(job.getId()));
    }

    private DbDemoService createService() {
        TaDatabase database = TaDatabase.open(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));
        return DbDemoService.from(database);
    }

    private User user(String email, UserRole role, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(fullName);
        return user;
    }

    private Resume resume(UUID userId) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle("Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume.setGpa(BigDecimal.valueOf(3.9));
        resume.setMaxWeeklyHours(10);
        return resume;
    }

    private Job job(UUID posterId) {
        Job job = new Job();
        job.setPostedBy(posterId);
        job.setTitle("Job");
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(8);
        job.setSlots(1);
        job.setDeadline(Instant.now().plusSeconds(3600));
        return job;
    }
}
