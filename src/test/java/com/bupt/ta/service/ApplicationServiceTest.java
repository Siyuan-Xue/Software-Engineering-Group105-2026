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

        service.sendOffer(mo.getId(), application.getId());
        assertEquals(workloadCount, db.workloadRecords().findAll().size());
        service.acceptOffer(ta.getId(), application.getId());

        assertEquals(workloadCount + 1, db.workloadRecords().findAll().size());
        assertEquals(notificationCount + 5, db.notifications().findAll().size());
        assertEquals(auditLogCount + 3, db.auditLogs().findAll().size());
        assertEquals(ApplicationStatus.ACCEPTED, db.applications().findById(application.getId()).orElseThrow().getStatus());
    }

    @Test
    void decliningOfferShouldNotCreateWorkload() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        ApplicationService service = new ApplicationService(db);
        int workloadCount = db.workloadRecords().findAll().size();

        User ta = db.users().save(user("decline-ta@example.com", UserRole.TA, "Decline TA"));
        User mo = db.users().save(user("decline-mo@example.com", UserRole.MO, "Decline MO"));
        Resume resume = db.resumes().save(resume(ta.getId()));
        Job job = db.jobs().save(job(mo.getId(), "Offer Decline TA"));

        Application application = service.submit(ta.getId(), resume.getId(), job.getId(), "Cover letter");
        service.sendOffer(mo.getId(), application.getId());
        service.declineOffer(ta.getId(), application.getId());

        assertEquals(workloadCount, db.workloadRecords().findAll().size());
        assertEquals(ApplicationStatus.DECLINED, db.applications().findById(application.getId()).orElseThrow().getStatus());
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

    @Test
    void submitShouldRejectWrongResumeOwnerClosedAndExpiredJobs() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        ApplicationService service = new ApplicationService(db);

        User ta = db.users().save(user("submit-ta-" + java.util.UUID.randomUUID() + "@example.com", UserRole.TA, "Submit TA"));
        User otherTa = db.users().save(user("submit-other-" + java.util.UUID.randomUUID() + "@example.com", UserRole.TA, "Other TA"));
        User mo = db.users().save(user("submit-mo-" + java.util.UUID.randomUUID() + "@example.com", UserRole.MO, "Submit MO"));
        Resume otherResume = db.resumes().save(resume(otherTa.getId()));
        Job openJob = db.jobs().save(job(mo.getId(), "Open Submit Job"));

        assertThrows(ConstraintViolationException.class,
                () -> service.submit(ta.getId(), otherResume.getId(), openJob.getId(), "Wrong owner"));
        assertThrows(ConstraintViolationException.class,
                () -> service.submit(ta.getId(), null, openJob.getId(), "No resume"));

        Resume ownResume = db.resumes().save(resume(ta.getId()));
        Job expired = job(mo.getId(), "Expired Submit Job");
        expired.setDeadline(Instant.now().minusSeconds(1));
        expired = db.jobs().save(expired);
        Job closed = job(mo.getId(), "Closed Submit Job");
        closed.setStatus(JobStatus.CLOSED);
        closed = db.jobs().save(closed);

        Job finalExpired = expired;
        assertThrows(ConstraintViolationException.class,
                () -> service.submit(ta.getId(), ownResume.getId(), finalExpired.getId(), "Expired"));
        Job finalClosed = closed;
        assertThrows(ConstraintViolationException.class,
                () -> service.submit(ta.getId(), ownResume.getId(), finalClosed.getId(), "Closed"));
    }

    @Test
    void reviewRejectAndWithdrawShouldEnforceRolesOwnershipAndStates() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        ApplicationService service = new ApplicationService(db);

        User ta = db.users().save(user("review-ta-" + java.util.UUID.randomUUID() + "@example.com", UserRole.TA, "Review TA"));
        User ownerMo = db.users().save(user("review-mo-" + java.util.UUID.randomUUID() + "@example.com", UserRole.MO, "Owner MO"));
        User otherMo = db.users().save(user("review-other-mo-" + java.util.UUID.randomUUID() + "@example.com", UserRole.MO, "Other MO"));
        Resume resume = db.resumes().save(resume(ta.getId()));
        Job job = db.jobs().save(job(ownerMo.getId(), "Review Job"));
        Application application = service.submit(ta.getId(), resume.getId(), job.getId(), "Review me");

        assertThrows(ConstraintViolationException.class,
                () -> service.startReview(otherMo.getId(), application.getId()));

        Application reviewing = service.startReview(ownerMo.getId(), application.getId());
        assertEquals(ApplicationStatus.REVIEWING, reviewing.getStatus());
        assertEquals(ownerMo.getId(), reviewing.getReviewedBy());

        Application rejected = service.reject(ownerMo.getId(), application.getId(), "Missing skill");
        assertEquals(ApplicationStatus.REJECTED, rejected.getStatus());
        assertEquals("Missing skill", rejected.getMoNotes());
        assertThrows(ConstraintViolationException.class,
                () -> service.withdraw(ta.getId(), application.getId()));
    }

    @Test
    void sendOfferShouldRespectVacancyCapacityIncludingPendingOffers() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        ApplicationService service = new ApplicationService(db);

        User ta1 = db.users().save(user("offer-ta1-" + java.util.UUID.randomUUID() + "@example.com", UserRole.TA, "Offer TA 1"));
        User ta2 = db.users().save(user("offer-ta2-" + java.util.UUID.randomUUID() + "@example.com", UserRole.TA, "Offer TA 2"));
        User mo = db.users().save(user("offer-mo-" + java.util.UUID.randomUUID() + "@example.com", UserRole.MO, "Offer MO"));
        Resume resume1 = db.resumes().save(resume(ta1.getId()));
        Resume resume2 = db.resumes().save(resume(ta2.getId()));
        Job job = job(mo.getId(), "Single Slot Job");
        job.setSlots(1);
        job = db.jobs().save(job);

        Application app1 = service.submit(ta1.getId(), resume1.getId(), job.getId(), null);
        Application app2 = service.submit(ta2.getId(), resume2.getId(), job.getId(), null);

        service.sendOffer(mo.getId(), app1.getId());
        assertThrows(ConstraintViolationException.class, () -> service.sendOffer(mo.getId(), app2.getId()));
    }

    @Test
    void acceptingOfferShouldCloseCompetingApplicationsWhenVacancyIsFull() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        ApplicationService service = new ApplicationService(db);

        User ta1 = db.users().save(user("accept-ta1-" + java.util.UUID.randomUUID() + "@example.com", UserRole.TA, "Accept TA 1"));
        User ta2 = db.users().save(user("accept-ta2-" + java.util.UUID.randomUUID() + "@example.com", UserRole.TA, "Accept TA 2"));
        User mo = db.users().save(user("accept-mo-" + java.util.UUID.randomUUID() + "@example.com", UserRole.MO, "Accept MO"));
        Resume resume1 = db.resumes().save(resume(ta1.getId()));
        Resume resume2 = db.resumes().save(resume(ta2.getId()));
        Job job = job(mo.getId(), "Filled Job");
        job.setSlots(1);
        job = db.jobs().save(job);

        Application winner = service.submit(ta1.getId(), resume1.getId(), job.getId(), null);
        Application competitor = service.submit(ta2.getId(), resume2.getId(), job.getId(), null);

        service.sendOffer(mo.getId(), winner.getId());
        service.acceptOffer(ta1.getId(), winner.getId());

        Application closedCompetitor = db.applications().findById(competitor.getId()).orElseThrow();
        assertEquals(ApplicationStatus.REJECTED, closedCompetitor.getStatus());
        assertEquals("Vacancy filled", closedCompetitor.getMoNotes());
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
