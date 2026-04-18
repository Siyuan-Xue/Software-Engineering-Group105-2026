package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.enums.SkillCategory;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends CrudRepository<Skill> {
    Optional<Skill> findByNameIgnoreCase(String name);

    List<Skill> listByCategory(SkillCategory category);
}
