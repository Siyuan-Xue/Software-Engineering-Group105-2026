package com.bupt.ta.db.repository;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.db.facade.FileTaDatabase;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.SkillCategory;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.enums.WorkloadStatus;
import com.bupt.ta.domain.value.JobQuery;
import com.bupt.ta.domain.value.WorkloadAggregate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonRepositoryIntegrationTest {
    @TempDir
    Path tempDir;

    @Test
    void userAndSkillRepositoriesShouldEnforceCaseInsensitiveUniqueness() {
        TaDatabase db = db();

        User user = user("User@Example.com", UserRole.TA, "TA User");
        db.users().save(user);

        User duplicate = user("user@example.com", UserRole.MO, "Other");
        assertThrows(ConstraintViolationException.class, () -> db.users().save(duplicate));

        Skill skill = new Skill();
        skill.setName("Java");
        skill.setCategory(SkillCategory.PROGRAMMING);
        db.skills().save(skill);

        Skill duplicateSkill = new Skill();
        duplicateSkill.setName("java");
        duplicateSkill.setCategory(SkillCategory.PROGRAMMING);
        assertThrows(ConstraintViolationException.class, () -> db.skills().save(duplicateSkill));
    }

    @Test
    void jobApplicationAndWorkloadQueriesShouldBehaveSemantically() {
        TaDatabase db = db();
        User ta = db.users().save(user("ta@example.com", UserRole.TA, "TA User"));
        User mo = db.users().save(user("mo@example.com", UserRole.MO, "MO User"));

        Resume resume = new Resume();
        resume.setUserId(ta.getId());
        resume.setTitle("Main Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume.setMaxWeeklyHours(15);
        resume = db.resumes().save(resume);

        Job openJob = job(mo.getId(), "Open", JobStatus.OPEN, Instant.now().plusSeconds(7200));
        openJob = db.jobs().save(openJob);
        db.jobs().save(job(mo.getId(), "Expired", JobStatus.OPEN, Instant.now().minusSeconds(3600)));

        Application application = new Application();
        application.setResumeId(resume.getId());
        application.setJobId(openJob.getId());
        application.setStatus(ApplicationStatus.ACCEPTED);
        application = db.applications().save(application);

        WorkloadRecord record = new WorkloadRecord();
        record.setTaId(ta.getId());
        record.setJobId(openJob.getId());
        record.setApplicationId(application.getId());
        record.setSemester("Spring 2026");
        record.setAssignedHours(10);
        record.setStatus(WorkloadStatus.ACTIVE);
        db.workloadRecords().save(record);

        JobQuery query = new JobQuery();
        query.setNow(Instant.now());
        assertEquals(1, db.jobs().listOpen(query).size());
        assertTrue(db.applications().existsByTaAndJob(ta.getId(), openJob.getId()));

        WorkloadAggregate aggregate = db.workloadRecords().aggregateBySemester("Spring 2026").get(0);
        assertEquals(10, aggregate.getAssignedHours());
        assertEquals(15, aggregate.getCapacityHours());
        assertEquals(5, aggregate.getRemainingHours());
    }

    private TaDatabase db() {
        return FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
    }

    private User user(String email, UserRole role, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(fullName);
        return user;
    }

    private Job job(java.util.UUID posterId, String title, JobStatus status, Instant deadline) {
        Job job = new Job();
        job.setPostedBy(posterId);
        job.setTitle(title);
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(status);
        job.setRequiredHours(10);
        job.setDeadline(deadline);
        return job;
    }
}
