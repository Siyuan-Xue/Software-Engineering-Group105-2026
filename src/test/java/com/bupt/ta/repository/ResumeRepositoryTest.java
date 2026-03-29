package com.bupt.ta.repository;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Resume;
import com.bupt.ta.model.enums.DegreeLevel;
import com.bupt.ta.persistence.DataAccessException;
import com.bupt.ta.persistence.json.JsonResumeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void listByUserIdShouldReturnUpdatedDesc() throws InterruptedException {
        ResumeRepository repository = new JsonResumeRepository(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));
        UUID userId = UUID.randomUUID();

        Resume older = new Resume();
        older.setUserId(userId);
        older.setTitle("Older");
        older.setDegreeLevel(DegreeLevel.BACHELOR);
        older.setGpa(BigDecimal.valueOf(3.7));
        repository.save(older);

        Thread.sleep(5);

        Resume newer = new Resume();
        newer.setUserId(userId);
        newer.setTitle("Newer");
        newer.setDegreeLevel(DegreeLevel.MASTER);
        newer.setGpa(BigDecimal.valueOf(3.9));
        repository.save(newer);

        List<Resume> resumes = repository.listByUserId(userId);
        assertEquals(2, resumes.size());
        assertEquals("Newer", resumes.get(0).getTitle());
        assertEquals("Older", resumes.get(1).getTitle());
    }

    @Test
    void saveShouldRejectMissingUserIdOrTitle() {
        ResumeRepository repository = new JsonResumeRepository(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));

        Resume invalid = new Resume();
        invalid.setTitle("No owner");

        assertThrows(DataAccessException.class, () -> repository.save(invalid));

        invalid.setUserId(UUID.randomUUID());
        invalid.setTitle("   ");

        assertThrows(DataAccessException.class, () -> repository.save(invalid));
    }

    @Test
    void deleteShouldRemoveResume() {
        ResumeRepository repository = new JsonResumeRepository(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));

        Resume resume = new Resume();
        resume.setUserId(UUID.randomUUID());
        resume.setTitle("Python TA");
        Resume saved = repository.save(resume);

        assertTrue(repository.delete(saved.getId()));
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
