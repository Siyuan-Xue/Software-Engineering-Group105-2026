package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.ResumeSkill;

import java.util.List;
import java.util.UUID;

public interface ResumeSkillRepository extends CrudRepository<ResumeSkill> {
    List<ResumeSkill> listByResumeId(UUID resumeId);

    void deleteByResumeId(UUID resumeId);
}
