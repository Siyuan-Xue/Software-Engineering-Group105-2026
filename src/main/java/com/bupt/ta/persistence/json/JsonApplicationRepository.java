package com.bupt.ta.persistence.json;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Application;
import com.bupt.ta.persistence.DataAccessException;
import com.bupt.ta.persistence.JsonTableStore;
import com.bupt.ta.repository.ApplicationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JsonApplicationRepository implements ApplicationRepository {
    private static final String FILE_NAME = "applications.json";

    private final JsonTableStore<Application> store;

    public JsonApplicationRepository(DatabaseConfig config) {
        this.store = new JsonTableStore<>(config, FILE_NAME, Application.class);
        this.store.initialize();
    }

    @Override
    public Optional<Application> findById(UUID id) {
        return store.findById(id);
    }

    @Override
    public List<Application> listByJobId(UUID jobId) {
        return store.findAll().stream()
                .filter(application -> jobId.equals(application.getJobId()))
                .sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<Application> listByResumeId(UUID resumeId) {
        return store.findAll().stream()
                .filter(application -> resumeId.equals(application.getResumeId()))
                .sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public Application save(Application application) {
        if (application.getResumeId() == null) {
            throw new DataAccessException("Application resumeId must not be null");
        }
        if (application.getJobId() == null) {
            throw new DataAccessException("Application jobId must not be null");
        }
        return store.save(application);
    }
}
