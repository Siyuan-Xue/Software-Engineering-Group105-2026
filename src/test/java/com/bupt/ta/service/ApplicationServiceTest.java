package com.bupt.ta.service;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.ConstraintViolationException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void duplicateSubmitShouldBeRejectedAndAcceptOfferShouldCreateLinkedRecords() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        ApplicationService service = new ApplicationService(db);

        User ta = db.users().save(user("ta@example.com", UserRole.TA, "TA User"));
        User mo = db.users().save(user("mo@example.com", UserRole.MO, "MO User"));
        Resume resume = db.resumes().save(resume(ta.getId()));
        Job job = db.jobs().save(job(mo.getId(), "Java TA"));

        Application application = service.submit(ta.getId(), resume.getId(), job.getId(), "Cover letter");
        assertThrows(ConstraintViolationException.class, () ->
                service.submit(ta.getId(), resume.getId(), job.getId(), "Again"));

        application.setStatus(ApplicationStatus.OFFER_PENDING);
        db.applications().save(application);
        service.acceptOffer(ta.getId(), application.getId());

        assertEquals(1, db.workloadRecords().findAll().size());
        assertEquals(2, db.notifications().findAll().size());
        assertEquals(2, db.auditLogs().findAll().size());
        assertEquals(ApplicationStatus.ACCEPTED, db.applications().findById(application.getId()).orElseThrow().getStatus());
    }

    @Test
    void deletingReferencedResumeShouldBeRejected() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        ApplicationService applicationService = new ApplicationService(db);
        ResumeService resumeService = new ResumeService(db);

        User ta = db.users().save(user("ta@example.com", UserRole.TA, "TA User"));
        User mo = db.users().save(user("mo@example.com", UserRole.MO, "MO User"));
        Resume resume = db.resumes().save(resume(ta.getId()));
        Job job = db.jobs().save(job(mo.getId(), "Algorithms TA"));

        applicationService.submit(ta.getId(), resume.getId(), job.getId(), null);

        assertThrows(ConstraintViolationException.class, () -> resumeService.delete(resume.getId()));
    }

    private User user(String email, UserRole role, String name) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(name);
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

    private Job job(java.util.UUID posterId, String title) {
        Job job = new Job();
        job.setPostedBy(posterId);
        job.setTitle(title);
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(10);
        job.setDeadline(Instant.now().plusSeconds(3600));
        return job;
    }
}
