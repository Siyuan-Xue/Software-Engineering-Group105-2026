package com.bupt.ta.domain.entity;

import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Skill tag bound to a resume with proficiency and experience metadata.
 */
public class ResumeSkill extends AbstractEntity {
    private UUID resumeId;
    private UUID skillId;
    private ProficiencyLevel proficiency;
    private int yearsExp;

    public UUID getResumeId() {
        return resumeId;
    }

    public void setResumeId(UUID resumeId) {
        this.resumeId = resumeId;
    }

    public UUID getSkillId() {
        return skillId;
    }

    public void setSkillId(UUID skillId) {
        this.skillId = skillId;
    }

    public ProficiencyLevel getProficiency() {
        return proficiency;
    }

    public void setProficiency(ProficiencyLevel proficiency) {
        this.proficiency = proficiency;
    }

    public int getYearsExp() {
        return yearsExp;
    }

    public void setYearsExp(int yearsExp) {
        this.yearsExp = yearsExp;
    }
}
