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
import com.bupt.ta.util.ApplicationSubmissionFiles;
import com.bupt.ta.util.ResumeFileUpload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void duplicateSubmitShouldBeRejectedAndAcceptOfferShouldCreateLinkedRecords() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        ApplicationService service = new ApplicationService(db);
        int workloadCount = db.workloadRecords().findAll().size();
        int notificationCount = db.notifications().findAll().size();
        int auditLogCount = db.auditLogs().findAll().size();

        User ta = db.users().save(user("ta@example.com", UserRole.TA, "TA User"));
        User mo = db.users().save(user("application-mo@example.com", UserRole.MO, "MO User"));
        Resume resume = db.resumes().save(resume(ta.getId()));
        Job job = db.jobs().save(job(mo.getId(), "Java TA"));

        Application application = service.submit(ta.getId(), resume.getId(), job.getId(), "Cover letter");
        assertThrows(ConstraintViolationException.class, () ->
                service.submit(ta.getId(), resume.getId(), job.getId(), "Again"));

        application.setStatus(ApplicationStatus.OFFER_PENDING);
        db.applications().save(application);
        service.acceptOffer(ta.getId(), application.getId());

        assertEquals(workloadCount + 1, db.workloadRecords().findAll().size());
        assertEquals(notificationCount + 3, db.notifications().findAll().size());
        assertEquals(auditLogCount + 2, db.auditLogs().findAll().size());
        assertEquals(ApplicationStatus.ACCEPTED, db.applications().findById(application.getId()).orElseThrow().getStatus());
    }

    @Test
    void submitWithUploadedFileShouldStoreApplicationSnapshot() throws Exception {
        System.setProperty(AppConfig.DATA_DIR_PROPERTY, tempDir.toString());
        try {
            TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
            ApplicationService service = new ApplicationService(db);

            User ta = db.users().save(user("upload-ta@example.com", UserRole.TA, "Upload TA"));
            User mo = db.users().save(user("upload-mo@example.com", UserRole.MO, "Upload MO"));
            Job job = db.jobs().save(job(mo.getId(), "Upload Test Job"));

            Path uploadDir = tempDir.resolve("resumes/uploads");
            Files.createDirectories(uploadDir);
            Path source = uploadDir.resolve("sample.pdf");
            Files.writeString(source, "pdf-content");

            ResumeFileUpload.SavedResumeFile uploaded = new ResumeFileUpload.SavedResumeFile(source, "sample.pdf");
            Application application = service.submit(ta.getId(), null, job.getId(), "Please hire me", uploaded);

            Application saved = db.applications().findById(application.getId()).orElseThrow();
            assertTrue(ApplicationSubmissionFiles.isAvailable(saved));
            assertEquals("sample.pdf", saved.getSubmittedFileName());
        } finally {
            System.clearProperty(AppConfig.DATA_DIR_PROPERTY);
        }
    }

    @Test
    void deletingReferencedResumeShouldBeRejected() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        ApplicationService applicationService = new ApplicationService(db);
        ResumeService resumeService = new ResumeService(db);

        User ta = db.users().save(user("ta@example.com", UserRole.TA, "TA User"));
        User mo = db.users().save(user("application-delete-mo@example.com", UserRole.MO, "MO User"));
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
