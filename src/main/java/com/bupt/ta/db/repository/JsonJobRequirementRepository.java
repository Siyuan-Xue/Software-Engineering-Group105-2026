package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.db.store.JsonTableStore;

import java.util.List;
import java.util.UUID;

public class JsonJobRequirementRepository extends BaseJsonRepository<JobRequirement> implements JobRequirementRepository {
    public JsonJobRequirementRepository(JsonTableStore<JobRequirement> store) {
        super(store);
    }

    @Override
    public List<JobRequirement> listByJobId(UUID jobId) {
        return findAll().stream().filter(requirement -> jobId.equals(requirement.getJobId())).toList();
    }

    @Override
    public List<JobRequirement> listRequiredByJobId(UUID jobId) {
        return findAll().stream()
                .filter(requirement -> jobId.equals(requirement.getJobId()))
                .filter(JobRequirement::isRequired)
                .toList();
    }

    @Override
    public JobRequirement save(JobRequirement entity) {
        if (entity.getJobId() == null || entity.getSkillId() == null) {
            throw new ConstraintViolationException("JobRequirement jobId and skillId must not be null");
        }
        return super.save(entity);
    }
}
