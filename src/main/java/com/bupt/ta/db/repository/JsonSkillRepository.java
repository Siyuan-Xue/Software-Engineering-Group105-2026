package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.enums.SkillCategory;
import com.bupt.ta.db.store.JsonTableStore;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * JSON-backed repository for administrator-managed skill library entries.
 */
public class JsonSkillRepository extends BaseJsonRepository<Skill> implements SkillRepository {
    public JsonSkillRepository(JsonTableStore<Skill> store) {
        super(store);
    }

    @Override
    public Optional<Skill> findByNameIgnoreCase(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return store.query(skill -> normalized.equals(normalize(skill.getName()))).stream().findFirst();
    }

    @Override
    public List<Skill> listByCategory(SkillCategory category) {
        return findAll().stream().filter(skill -> skill.getCategory() == category).toList();
    }

    @Override
    public Skill save(Skill entity) {
        if (entity.getName() == null || entity.getName().isBlank()) {
            throw new ConstraintViolationException("Skill name must not be blank");
        }
        Optional<Skill> duplicate = findByNameIgnoreCase(entity.getName());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(entity.getId())) {
            throw new ConstraintViolationException("Duplicate skill name: " + entity.getName());
        }
        entity.setName(entity.getName().trim());
        return super.save(entity);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
