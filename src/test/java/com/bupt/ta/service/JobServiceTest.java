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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void cancellingJobShouldWithdrawInProgressApplicationsAndCreateSideEffects() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        JobService service = new JobService(db);

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
        assertEquals(2, db.notifications().findAll().size());
        assertEquals(1, db.auditLogs().findAll().size());
    }

    private User user(String email, UserRole role, String name) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(name);
        return user;
    }
}
