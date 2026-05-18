package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.value.WorkloadAggregate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkloadRecordRepository extends CrudRepository<WorkloadRecord> {
    Optional<WorkloadRecord> findByApplicationId(UUID applicationId);

    List<WorkloadRecord> listByTaAndSemester(UUID taId, String semester);

    List<WorkloadAggregate> aggregateBySemester(String semester);
}
