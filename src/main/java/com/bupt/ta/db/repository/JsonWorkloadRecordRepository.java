package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.WorkloadStatus;
import com.bupt.ta.domain.value.WorkloadAggregate;
import com.bupt.ta.db.store.JsonTableStore;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonWorkloadRecordRepository extends BaseJsonRepository<WorkloadRecord> implements WorkloadRecordRepository {
    private final JsonTableStore<Resume> resumeStore;

    public JsonWorkloadRecordRepository(JsonTableStore<WorkloadRecord> store, JsonTableStore<Resume> resumeStore) {
        super(store);
        this.resumeStore = resumeStore;
    }

    @Override
    public Optional<WorkloadRecord> findByApplicationId(UUID applicationId) {
        return store.query(record -> applicationId.equals(record.getApplicationId())).stream().findFirst();
    }

    @Override
    public List<WorkloadRecord> listByTaAndSemester(UUID taId, String semester) {
        return findAll().stream()
                .filter(record -> taId.equals(record.getTaId()))
                .filter(record -> semester.equals(record.getSemester()))
                .sorted(Comparator.comparing(WorkloadRecord::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<WorkloadAggregate> aggregateBySemester(String semester) {
        Map<UUID, List<WorkloadRecord>> grouped = findAll().stream()
                .filter(record -> semester.equals(record.getSemester()))
                .filter(record -> record.getStatus() == WorkloadStatus.ACTIVE)
                .collect(Collectors.groupingBy(WorkloadRecord::getTaId));
        return grouped.entrySet().stream()
                .map(entry -> aggregate(entry.getKey(), semester, entry.getValue()))
                .sorted(Comparator.comparing(WorkloadAggregate::getAssignedHours).reversed())
                .toList();
    }

    @Override
    public WorkloadRecord save(WorkloadRecord entity) {
        if (entity.getTaId() == null || entity.getJobId() == null || entity.getApplicationId() == null) {
            throw new ConstraintViolationException("WorkloadRecord taId, jobId and applicationId must not be null");
        }
        if (entity.getSemester() == null || entity.getSemester().isBlank()) {
            throw new ConstraintViolationException("WorkloadRecord semester must not be blank");
        }
        return super.save(entity);
    }

    private WorkloadAggregate aggregate(UUID taId, String semester, List<WorkloadRecord> records) {
        int assigned = records.stream().mapToInt(WorkloadRecord::getAssignedHours).sum();
        int capacity = resumeStore.query(resume -> taId.equals(resume.getUserId())).stream()
                .sorted(Comparator.comparing(Resume::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst()
                .map(Resume::getMaxWeeklyHours)
                .orElse(20);
        WorkloadAggregate aggregate = new WorkloadAggregate();
        aggregate.setTaId(taId);
        aggregate.setSemester(semester);
        aggregate.setAssignedHours(assigned);
        aggregate.setCapacityHours(capacity);
        aggregate.setRemainingHours(capacity - assigned);
        aggregate.setUtilizationRatio(capacity == 0 ? 0.0 : (double) assigned / capacity);
        return aggregate;
    }
}
