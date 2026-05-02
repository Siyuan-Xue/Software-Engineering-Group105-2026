package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.domain.entity.MatchScore;
import com.bupt.ta.db.store.JsonTableStore;

import java.util.Optional;
import java.util.UUID;

public class JsonMatchScoreRepository extends BaseJsonRepository<MatchScore> implements MatchScoreRepository {
    public JsonMatchScoreRepository(JsonTableStore<MatchScore> store) {
        super(store);
    }

    @Override
    public Optional<MatchScore> findByApplicationId(UUID applicationId) {
        return store.query(score -> applicationId.equals(score.getApplicationId())).stream().findFirst();
    }

    @Override
    public MatchScore save(MatchScore entity) {
        if (entity.getApplicationId() == null) {
            throw new ConstraintViolationException("MatchScore applicationId must not be null");
        }
        return super.save(entity);
    }
}
