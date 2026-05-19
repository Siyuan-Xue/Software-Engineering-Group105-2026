package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.domain.entity.ResumeSkill;
import com.bupt.ta.db.store.JsonTableStore;

import java.util.List;
import java.util.UUID;

/**
 * JSON-backed repository for resume skill tags.
 */
public class JsonResumeSkillRepository extends BaseJsonRepository<ResumeSkill> implements ResumeSkillRepository {
    public JsonResumeSkillRepository(JsonTableStore<ResumeSkill> store) {
        super(store);
    }

    @Override
    public List<ResumeSkill> listByResumeId(UUID resumeId) {
        return findAll().stream().filter(skill -> resumeId.equals(skill.getResumeId())).toList();
    }

    @Override
    public void deleteByResumeId(UUID resumeId) {
        store.replaceAll(findAll().stream().filter(skill -> !resumeId.equals(skill.getResumeId())).toList());
    }

    @Override
    public ResumeSkill save(ResumeSkill entity) {
        if (entity.getResumeId() == null || entity.getSkillId() == null) {
            throw new ConstraintViolationException("ResumeSkill resumeId and skillId must not be null");
        }
        return super.save(entity);
    }
}
