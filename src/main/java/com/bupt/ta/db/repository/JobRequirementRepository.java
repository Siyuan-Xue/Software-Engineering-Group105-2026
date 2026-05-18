package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.JobRequirement;

import java.util.List;
import java.util.UUID;

public interface JobRequirementRepository extends CrudRepository<JobRequirement> {
    List<JobRequirement> listByJobId(UUID jobId);

    List<JobRequirement> listRequiredByJobId(UUID jobId);
}
