package com.bupt.ta.service;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.db.facade.FileTaDatabase;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.bupt.ta.domain.enums.SkillCategory;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.enums.WorkloadStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void cancellingJobShouldWithdrawInProgressApplicationsAndCreateSideEffects() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        JobService service = new JobService(db);
        int notificationCount = db.notifications().findAll().size();
        int auditLogCount = db.auditLogs().findAll().size();

        User ta = db.users().save(user("ta@example.com", UserRole.TA, "TA User"));
        User mo = db.users().save(user("job-service-mo@example.com", UserRole.MO, "MO User"));
        Resume resume = new Resume();
        resume.setUserId(ta.getId());
        resume.setTitle("Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume = db.resumes().save(resume);

        Job job = new Job();
        job.setPostedBy(mo.getId());
        job.setTitle("Data Structures");
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(8);
        job.setDeadline(Instant.now().plusSeconds(3600));
        job = db.jobs().save(job);

        Application pending = new Application();
        pending.setResumeId(resume.getId());
        pending.setJobId(job.getId());
        pending.setStatus(ApplicationStatus.PENDING);
        pending = db.applications().save(pending);

        Application reviewing = new Application();
        reviewing.setResumeId(resume.getId());
        reviewing.setJobId(job.getId());
        reviewing.setStatus(ApplicationStatus.REVIEWING);
        reviewing = db.applications().save(reviewing);

        service.changeStatus(mo.getId(), job.getId(), JobStatus.CANCELLED);

        assertEquals(JobStatus.CANCELLED, db.jobs().findById(job.getId()).orElseThrow().getStatus());
        assertEquals(ApplicationStatus.WITHDRAWN, db.applications().findById(pending.getId()).orElseThrow().getStatus());
        assertEquals(ApplicationStatus.WITHDRAWN, db.applications().findById(reviewing.getId()).orElseThrow().getStatus());
        assertEquals(notificationCount + 2, db.notifications().findAll().size());
        assertEquals(auditLogCount + 1, db.auditLogs().findAll().size());
    }

    @Test
    void cancellingAcceptedJobShouldCancelLinkedWorkload() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        JobService service = new JobService(db);

        User ta = db.users().save(user("accepted-ta@example.com", UserRole.TA, "Accepted TA"));
        User mo = db.users().save(user("accepted-mo@example.com", UserRole.MO, "Accepted MO"));
        Resume resume = new Resume();
        resume.setUserId(ta.getId());
        resume.setTitle("Accepted Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume = db.resumes().save(resume);

        Job job = new Job();
        job.setPostedBy(mo.getId());
        job.setTitle("Accepted Data Structures");
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(8);
        job.setDeadline(Instant.now().plusSeconds(3600));
        job = db.jobs().save(job);

        Application accepted = new Application();
        accepted.setResumeId(resume.getId());
        accepted.setJobId(job.getId());
        accepted.setStatus(ApplicationStatus.ACCEPTED);
        accepted = db.applications().save(accepted);

        WorkloadRecord record = new WorkloadRecord();
        record.setApplicationId(accepted.getId());
        record.setTaId(ta.getId());
        record.setJobId(job.getId());
        record.setSemester("Spring 2026");
        record.setAssignedHours(8);
        record.setStatus(WorkloadStatus.ACTIVE);
        record = db.workloadRecords().save(record);

        service.changeStatus(mo.getId(), job.getId(), JobStatus.CANCELLED);

        assertEquals(ApplicationStatus.WITHDRAWN, db.applications().findById(accepted.getId()).orElseThrow().getStatus());
        assertEquals(WorkloadStatus.CANCELLED, db.workloadRecords().findById(record.getId()).orElseThrow().getStatus());
        assertTrue(db.workloadRecords().aggregateBySemester("Spring 2026").stream()
                .noneMatch(item -> ta.getId().equals(item.getTaId())));
    }

    @Test
    void listByPosterShouldReturnOnlyVacanciesPostedByThatModuleOrganiser() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        JobService service = new JobService(db);

        User owner = db.users().save(user("owner-mo@example.com", UserRole.MO, "Owner MO"));
        User otherMo = db.users().save(user("other-mo@example.com", UserRole.MO, "Other MO"));
        Job ownJob = db.jobs().save(job(owner.getId(), "Owner Vacancy"));
        db.jobs().save(job(otherMo.getId(), "Other Vacancy"));

        var visibleJobs = service.listByPoster(owner.getId());

        assertEquals(1, visibleJobs.size());
        assertEquals(ownJob.getId(), visibleJobs.get(0).getId());
    }

    @Test
    void createDraftShouldDefaultStatusValidatePosterAndNotifyActiveTasWhenOpen() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        JobService service = new JobService(db);
        User mo = db.users().save(user("create-mo-" + UUID.randomUUID() + "@example.com", UserRole.MO, "Create MO"));
        User activeTa = db.users().save(user("active-ta-" + UUID.randomUUID() + "@example.com", UserRole.TA, "Active TA"));
        User inactiveTa = user("inactive-ta-" + UUID.randomUUID() + "@example.com", UserRole.TA, "Inactive TA");
        inactiveTa.setActive(false);
        inactiveTa = db.users().save(inactiveTa);

        Job draft = job(mo.getId(), "  Drafted Vacancy  ");
        draft.setStatus(null);
        Job savedDraft = service.createDraft(draft);
        assertEquals(JobStatus.DRAFT, savedDraft.getStatus());
        assertEquals("Drafted Vacancy", savedDraft.getTitle());

        Job open = job(mo.getId(), "Open Vacancy Notification");
        open.setStatus(JobStatus.OPEN);
        Job savedOpen = service.createDraft(open);

        assertTrue(db.notifications().findAll().stream()
                .anyMatch(notification -> activeTa.getId().equals(notification.getUserId())
                        && notification.getNotifType() == NotificationType.NEW_JOB
                        && savedOpen.getId().equals(notification.getEntityId())));
        User finalInactiveTa = inactiveTa;
        assertTrue(db.notifications().findAll().stream()
                .noneMatch(notification -> finalInactiveTa.getId().equals(notification.getUserId())
                        && savedOpen.getId().equals(notification.getEntityId())));

        User taPoster = db.users().save(user("bad-poster-" + UUID.randomUUID() + "@example.com", UserRole.TA, "Bad Poster"));
        assertThrows(ConstraintViolationException.class, () -> service.createDraft(job(taPoster.getId(), "Bad Poster Job")));
    }

    @Test
    void updateShouldRejectInvalidDatesAndMissingOwnership() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        JobService service = new JobService(db);
        User mo = db.users().save(user("update-mo-" + UUID.randomUUID() + "@example.com", UserRole.MO, "Update MO"));
        Job job = db.jobs().save(job(mo.getId(), "Update Job"));

        Job invalidDates = job(mo.getId(), "Invalid Dates");
        invalidDates.setId(job.getId());
        invalidDates.setStartDate(java.time.LocalDate.of(2026, 5, 1));
        invalidDates.setEndDate(java.time.LocalDate.of(2026, 4, 1));
        assertThrows(ConstraintViolationException.class, () -> service.update(mo.getId(), invalidDates));

        Job missing = job(mo.getId(), "Missing Job");
        missing.setId(UUID.randomUUID());
        assertThrows(ConstraintViolationException.class, () -> service.update(mo.getId(), missing));
    }

    @Test
    void replaceRequirementsShouldAtomicallyReplaceExistingRowsAndBindJobId() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        JobService service = new JobService(db);
        User mo = db.users().save(user("requirements-mo-" + UUID.randomUUID() + "@example.com", UserRole.MO, "Requirements MO"));
        Job job = db.jobs().save(job(mo.getId(), "Requirements Job"));
        Skill oldSkill = db.skills().save(skill("Old Requirement " + UUID.randomUUID()));
        Skill newSkill = db.skills().save(skill("New Requirement " + UUID.randomUUID()));
        db.jobRequirements().save(requirement(job.getId(), oldSkill.getId()));

        JobRequirement replacement = requirement(UUID.randomUUID(), newSkill.getId());
        service.replaceRequirements(mo.getId(), job.getId(), java.util.List.of(replacement));

        var rows = db.jobRequirements().listByJobId(job.getId());
        assertEquals(1, rows.size());
        assertEquals(newSkill.getId(), rows.get(0).getSkillId());
        assertEquals(job.getId(), rows.get(0).getJobId());
    }

    private User user(String email, UserRole role, String name) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(name);
        return user;
    }

    private Job job(UUID posterId, String title) {
        Job job = new Job();
        job.setPostedBy(posterId);
        job.setTitle(title);
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(8);
        job.setDeadline(Instant.now().plusSeconds(3600));
        return job;
    }

    private Skill skill(String name) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setCategory(SkillCategory.PROGRAMMING);
        return skill;
    }

    private JobRequirement requirement(UUID jobId, UUID skillId) {
        JobRequirement requirement = new JobRequirement();
        requirement.setJobId(jobId);
        requirement.setSkillId(skillId);
        requirement.setRequired(true);
        requirement.setMinProficiency(ProficiencyLevel.INTERMEDIATE);
        return requirement;
    }
}
