package com.bupt.ta.persistence.json;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.enums.JobStatus;
import com.bupt.ta.persistence.DataAccessException;
import com.bupt.ta.persistence.JsonTableStore;
import com.bupt.ta.repository.JobRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JsonJobRepository implements JobRepository {
    private static final String FILE_NAME = "jobs.json";

    private final JsonTableStore<Job> store;

    public JsonJobRepository(DatabaseConfig config) {
        this.store = new JsonTableStore<>(config, FILE_NAME, Job.class);
        this.store.initialize();
    }

    @Override
    public Optional<Job> findById(UUID id) {
        return store.findById(id);
    }

    @Override
    public List<Job> listOpen(Instant now) {
        return store.findAll().stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN)
                .filter(job -> job.getDeadline() != null && job.getDeadline().isAfter(now))
                .sorted(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<Job> listByPoster(UUID posterId) {
        return store.findAll().stream()
                .filter(job -> posterId.equals(job.getPostedBy()))
                .sorted(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public Job save(Job job) {
        if (job.getPostedBy() == null) {
            throw new DataAccessException("Job postedBy must not be null");
        }
        if (job.getTitle() == null || job.getTitle().isBlank()) {
            throw new DataAccessException("Job title must not be blank");
        }
        if (job.getDeadline() == null) {
            throw new DataAccessException("Job deadline must not be null");
        }
        return store.save(job);
    }
}
