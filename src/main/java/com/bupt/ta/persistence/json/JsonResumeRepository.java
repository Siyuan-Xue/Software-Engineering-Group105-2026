package com.bupt.ta.persistence.json;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Resume;
import com.bupt.ta.persistence.DataAccessException;
import com.bupt.ta.persistence.JsonTableStore;
import com.bupt.ta.repository.ResumeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JsonResumeRepository implements ResumeRepository {
    private static final String FILE_NAME = "resumes.json";

    private final JsonTableStore<Resume> store;

    public JsonResumeRepository(DatabaseConfig config) {
        this.store = new JsonTableStore<>(config, FILE_NAME, Resume.class);
        this.store.initialize();
    }

    @Override
    public Optional<Resume> findById(UUID id) {
        return store.findById(id);
    }

    @Override
    public List<Resume> listByUserId(UUID userId) {
        return store.findAll().stream()
                .filter(resume -> userId.equals(resume.getUserId()))
                .sorted(Comparator.comparing(Resume::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public Resume save(Resume resume) {
        if (resume.getUserId() == null) {
            throw new DataAccessException("Resume userId must not be null");
        }
        if (resume.getTitle() == null || resume.getTitle().isBlank()) {
            throw new DataAccessException("Resume title must not be blank");
        }
        return store.save(resume);
    }

    @Override
    public boolean delete(UUID id) {
        return store.delete(id);
    }
}
