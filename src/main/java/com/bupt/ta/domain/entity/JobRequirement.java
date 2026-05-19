package com.bupt.ta.domain.entity;

import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Skill requirement attached to a vacancy, including required/preferred status.
 */
public class JobRequirement extends AbstractEntity {
    private UUID jobId;
    private UUID skillId;
    private boolean required;
    private ProficiencyLevel minProficiency;

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getSkillId() {
        return skillId;
    }

    public void setSkillId(UUID skillId) {
        this.skillId = skillId;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public ProficiencyLevel getMinProficiency() {
        return minProficiency;
    }

    public void setMinProficiency(ProficiencyLevel minProficiency) {
        this.minProficiency = minProficiency;
    }
}
