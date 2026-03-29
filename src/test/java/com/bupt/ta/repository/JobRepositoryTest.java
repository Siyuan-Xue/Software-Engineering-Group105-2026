package com.bupt.ta.repository;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.enums.JobStatus;
import com.bupt.ta.model.enums.JobType;
import com.bupt.ta.persistence.DataAccessException;
import com.bupt.ta.persistence.json.JsonJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void listOpenShouldOnlyReturnOpenJobsWithFutureDeadline() {
        JobRepository repository = new JsonJobRepository(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));
        UUID posterId = UUID.randomUUID();

        repository.save(job(posterId, "Open", JobStatus.OPEN, Instant.now().plusSeconds(3600)));
        repository.save(job(posterId, "Closed", JobStatus.CLOSED, Instant.now().plusSeconds(3600)));
        repository.save(job(posterId, "Expired", JobStatus.OPEN, Instant.now().minusSeconds(3600)));

        List<Job> jobs = repository.listOpen(Instant.now());
        assertEquals(1, jobs.size());
        assertEquals("Open", jobs.get(0).getTitle());
    }

    @Test
    void saveShouldRejectMissingMandatoryFields() {
        JobRepository repository = new JsonJobRepository(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));
        Job invalid = new Job();

        assertThrows(DataAccessException.class, () -> repository.save(invalid));
    }

    private Job job(UUID posterId, String title, JobStatus status, Instant deadline) {
        Job job = new Job();
        job.setPostedBy(posterId);
        job.setTitle(title);
        job.setType(JobType.MODULE_SUPPORT);
        job.setDeadline(deadline);
        job.setStatus(status);
        job.setRequiredHours(8);
        return job;
    }
}
