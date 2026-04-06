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
    public List<Application> listAll() {
        return store.findAll().stream()
                .sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<Application> listByJobId(UUID jobId) {
        return listAll().stream()
                .filter(application -> jobId.equals(application.getJobId()))
                .toList();
    }

    @Override
    public List<Application> listByResumeId(UUID resumeId) {
        return listAll().stream()
                .filter(application -> resumeId.equals(application.getResumeId()))
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

    @Override
    public boolean delete(UUID id) {
        return store.delete(id);
    }
}
