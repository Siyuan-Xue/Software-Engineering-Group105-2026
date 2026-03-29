package com.bupt.ta.repository;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.enums.ApplicationStatus;
import com.bupt.ta.persistence.DataAccessException;
import com.bupt.ta.persistence.json.JsonApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void listByJobIdAndResumeIdShouldFilterCorrectly() {
        ApplicationRepository repository = new JsonApplicationRepository(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));
        UUID jobId = UUID.randomUUID();
        UUID otherJobId = UUID.randomUUID();
        UUID resumeId = UUID.randomUUID();
        UUID otherResumeId = UUID.randomUUID();

        repository.save(application(jobId, resumeId, ApplicationStatus.PENDING));
        repository.save(application(otherJobId, resumeId, ApplicationStatus.REVIEWING));
        repository.save(application(jobId, otherResumeId, ApplicationStatus.ACCEPTED));

        List<Application> byJob = repository.listByJobId(jobId);
        List<Application> byResume = repository.listByResumeId(resumeId);

        assertEquals(2, byJob.size());
        assertEquals(2, byResume.size());
    }

    @Test
    void saveShouldRejectMissingForeignKeys() {
        ApplicationRepository repository = new JsonApplicationRepository(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));

        Application invalid = new Application();
        invalid.setJobId(UUID.randomUUID());

        assertThrows(DataAccessException.class, () -> repository.save(invalid));
    }

    private Application application(UUID jobId, UUID resumeId, ApplicationStatus status) {
        Application application = new Application();
        application.setJobId(jobId);
        application.setResumeId(resumeId);
        application.setStatus(status);
        return application;
    }
}
