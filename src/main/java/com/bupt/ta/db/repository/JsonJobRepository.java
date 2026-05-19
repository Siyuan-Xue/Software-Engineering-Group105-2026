package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.value.JobQuery;
import com.bupt.ta.db.store.JsonTableStore;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * JSON-backed vacancy repository with open-job filtering.
 */
public class JsonJobRepository extends BaseJsonRepository<Job> implements JobRepository {
    private final JsonTableStore<Application> applicationStore;

    public JsonJobRepository(JsonTableStore<Job> store, JsonTableStore<Application> applicationStore) {
        super(store);
        this.applicationStore = applicationStore;
    }

    @Override
    public List<Job> findAll() {
        return super.findAll().stream()
                .sorted(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<Job> listOpen(JobQuery query) {
        Instant now = query == null ? null : query.getNow();
        return findAll().stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN)
                .filter(job -> query == null || query.getType() == null || job.getType() == query.getType())
                .filter(job -> query == null || query.getStatus() == null || job.getStatus() == query.getStatus())
                .filter(job -> query == null || query.getModuleCodeKeyword() == null
                        || (job.getModuleCode() != null
                        && job.getModuleCode().toLowerCase().contains(query.getModuleCodeKeyword().toLowerCase())))
                .filter(job -> query == null || query.getMinRequiredHours() == null
                        || job.getRequiredHours() >= query.getMinRequiredHours())
                .filter(job -> query == null || query.getMaxRequiredHours() == null
                        || job.getRequiredHours() <= query.getMaxRequiredHours())
                .filter(job -> now == null || (job.getDeadline() != null && job.getDeadline().isAfter(now)))
                .toList();
    }

    @Override
    public List<Job> listByPoster(UUID posterId) {
        return findAll().stream().filter(job -> posterId.equals(job.getPostedBy())).toList();
    }

    @Override
    public long countAccepted(UUID jobId) {
        return applicationStore.query(app -> jobId.equals(app.getJobId()) && app.getStatus() == ApplicationStatus.ACCEPTED).size();
    }

    @Override
    public Job save(Job entity) {
        if (entity.getPostedBy() == null) {
            throw new ConstraintViolationException("Job postedBy must not be null");
        }
        if (entity.getTitle() == null || entity.getTitle().isBlank()) {
            throw new ConstraintViolationException("Job title must not be blank");
        }
        if (entity.getDeadline() == null) {
            throw new ConstraintViolationException("Job deadline must not be null");
        }
        return super.save(entity);
    }
}
