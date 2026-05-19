package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.db.store.JsonTableStore;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * JSON-backed application repository with resume ownership-aware queries.
 */
public class JsonApplicationRepository extends BaseJsonRepository<Application> implements ApplicationRepository {
    private final JsonTableStore<Resume> resumeStore;

    public JsonApplicationRepository(JsonTableStore<Application> store, JsonTableStore<Resume> resumeStore) {
        super(store);
        this.resumeStore = resumeStore;
    }

    @Override
    public List<Application> findAll() {
        return super.findAll().stream()
                .sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<Application> listByJobId(UUID jobId) {
        return findAll().stream().filter(application -> jobId.equals(application.getJobId())).toList();
    }

    @Override
    public List<Application> listByResumeId(UUID resumeId) {
        return findAll().stream().filter(application -> resumeId.equals(application.getResumeId())).toList();
    }

    @Override
    public List<Application> listByStatuses(UUID jobId, List<ApplicationStatus> statuses) {
        Set<ApplicationStatus> statusSet = Set.copyOf(statuses);
        return findAll().stream()
                .filter(application -> jobId.equals(application.getJobId()))
                .filter(application -> statusSet.contains(application.getStatus()))
                .toList();
    }

    @Override
    public boolean existsByTaAndJob(UUID taUserId, UUID jobId) {
        return store.query(app -> jobId.equals(app.getJobId())).stream().anyMatch(application ->
                application.getStatus() != ApplicationStatus.WITHDRAWN
                        &&
                resumeStore.findById(application.getResumeId())
                        .map(Resume::getUserId)
                        .filter(taUserId::equals)
                        .isPresent());
    }

    @Override
    public Application save(Application entity) {
        if (entity.getResumeId() == null) {
            throw new ConstraintViolationException("Application resumeId must not be null");
        }
        if (entity.getJobId() == null) {
            throw new ConstraintViolationException("Application jobId must not be null");
        }
        if (entity.getStatus() == null) {
            throw new ConstraintViolationException("Application status must not be null");
        }
        return super.save(entity);
    }
}
