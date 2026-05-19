package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.JobRequirement;

import java.util.List;
import java.util.UUID;

/**
 * Repository contract for skill requirements attached to vacancies.
 */
public interface JobRequirementRepository extends CrudRepository<JobRequirement> {
    List<JobRequirement> listByJobId(UUID jobId);

    List<JobRequirement> listRequiredByJobId(UUID jobId);
}
