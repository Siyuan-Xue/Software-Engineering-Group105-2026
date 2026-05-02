package com.bupt.ta.db.facade;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaDatabaseIntegrationTest {
    @TempDir
    Path tempDir;

    @Test
    void openShouldCreateAllElevenTableFilesAndSeedDefaults() {
        TaDatabase database = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));

        List<String> expected = List.of(
                "users.json", "resumes.json", "jobs.json", "applications.json", "skills.json",
                "resume_skills.json", "job_requirements.json", "workload_records.json",
                "match_scores.json", "notifications.json", "audit_logs.json"
        );
        expected.forEach(file -> assertTrue(Files.exists(tempDir.resolve(file))));
        assertTrue(database.users().findByEmail("test@example.com").isPresent());
        assertTrue(database.users().findByEmail("mo@example.com").isPresent());
        assertTrue(database.users().findByEmail("admin@example.com").isPresent());
        assertTrue(database.skills().findByNameIgnoreCase("Java").isPresent());
        assertTrue(database.skills().findAll().size() >= 5);
    }

    @Test
    void executeAtomicallyShouldPersistCrossTableWrites() {
        TaDatabase database = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));

        User user = new User();
        user.setEmail("ta@example.com");
        user.setPasswordHash("hash");
        user.setRole(UserRole.TA);
        user.setFullName("TA User");
        user = database.users().save(user);

        Resume resume = new Resume();
        resume.setUserId(user.getId());
        resume.setTitle("Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume = database.resumes().save(resume);

        User mo = new User();
        mo.setEmail("atomic-mo@example.com");
        mo.setPasswordHash("hash");
        mo.setRole(UserRole.MO);
        mo.setFullName("MO User");
        mo = database.users().save(mo);

        Job job = new Job();
        job.setPostedBy(mo.getId());
        job.setTitle("Java TA");
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(10);
        job.setDeadline(Instant.now().plusSeconds(3600));
        job = database.jobs().save(job);

        Application application = new Application();
        application.setResumeId(resume.getId());
        application.setJobId(job.getId());
        application.setStatus(ApplicationStatus.ACCEPTED);
        WorkloadRecord record = new WorkloadRecord();
        record.setTaId(user.getId());
        record.setJobId(job.getId());
        record.setSemester("Spring 2026");
        record.setAssignedHours(10);

        Application[] savedApplication = new Application[1];
        database.executeAtomically(() -> {
            savedApplication[0] = database.applications().save(application);
            record.setApplicationId(savedApplication[0].getId());
            database.workloadRecords().save(record);
        });

        assertEquals(1, database.applications().findAll().size());
        assertEquals(1, database.workloadRecords().findAll().size());
    }
}
