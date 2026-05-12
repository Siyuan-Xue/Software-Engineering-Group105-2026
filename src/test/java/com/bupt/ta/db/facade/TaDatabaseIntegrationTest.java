package com.bupt.ta.db.facade;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.db.store.TableEnvelope;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaDatabaseIntegrationTest {
    @TempDir
    Path tempDir;

    @Test
    void openShouldCreateAllElevenTableFilesAndSeedDemoData() {
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
        assertTrue(database.users().findAll().size() >= 10);
        assertTrue(database.resumes().findAll().size() >= 6);
        assertTrue(database.jobs().findAll().size() >= 8);
        assertTrue(database.applications().findAll().size() >= 9);
        assertTrue(database.skills().findAll().size() >= 12);
        assertTrue(database.resumeSkills().findAll().size() >= 24);
        assertTrue(database.jobRequirements().findAll().size() >= 24);
        assertTrue(database.workloadRecords().findAll().size() >= 3);
        assertTrue(database.matchScores().findAll().size() >= 9);
        assertTrue(database.notifications().findAll().size() >= 13);
        assertTrue(database.auditLogs().findAll().size() >= 6);

        assertTrue(database.jobs().findAll().stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN)
                .filter(job -> job.getDeadline() != null && job.getDeadline().isAfter(Instant.now()))
                .count() >= 7);

        Set<ApplicationStatus> statuses = database.applications().findAll().stream()
                .map(Application::getStatus)
                .collect(Collectors.toSet());
        assertTrue(statuses.containsAll(Set.of(ApplicationStatus.PENDING, ApplicationStatus.REVIEWING,
                ApplicationStatus.OFFER_PENDING, ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED,
                ApplicationStatus.DECLINED, ApplicationStatus.WITHDRAWN)));

        String currentSemester = (LocalDate.now().getMonthValue() >= 8 ? "Fall " : "Spring ") + LocalDate.now().getYear();
        assertFalse(database.workloadRecords().aggregateBySemester(currentSemester).isEmpty());
    }

    @Test
    void openShouldSkipDemoSeedingWhenAnyBusinessDataAlreadyExists() throws Exception {
        ObjectMapper mapper = AppConfig.createObjectMapper();
        User existing = new User();
        existing.setId(UUID.randomUUID());
        existing.setEmail("existing@example.com");
        existing.setPasswordHash("hash");
        existing.setRole(UserRole.TA);
        existing.setFullName("Existing User");

        mapper.writeValue(tempDir.resolve("users.json").toFile(), new TableEnvelope<>(1, List.of(existing)));
        for (String file : List.of("resumes.json", "jobs.json", "applications.json", "skills.json",
                "resume_skills.json", "job_requirements.json", "workload_records.json",
                "match_scores.json", "notifications.json", "audit_logs.json")) {
            writeEmptyTable(mapper, tempDir.resolve(file));
        }

        TaDatabase database = FileTaDatabase.open(JsonStoreConfig.of(tempDir, mapper));

        assertTrue(database.users().findByEmail("existing@example.com").isPresent());
        assertTrue(database.users().findByEmail("test@example.com").isEmpty());
        assertEquals(1, database.users().findAll().size());
        assertTrue(database.skills().findAll().isEmpty());
    }

    @Test
    void executeAtomicallyShouldPersistCrossTableWrites() {
        TaDatabase database = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        int applicationCount = database.applications().findAll().size();
        int workloadCount = database.workloadRecords().findAll().size();

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

        assertEquals(applicationCount + 1, database.applications().findAll().size());
        assertEquals(workloadCount + 1, database.workloadRecords().findAll().size());
    }

    private void writeEmptyTable(ObjectMapper mapper, Path file) throws IOException {
        mapper.writeValue(file.toFile(), Map.of("version", 1, "rows", List.of()));
    }
}
