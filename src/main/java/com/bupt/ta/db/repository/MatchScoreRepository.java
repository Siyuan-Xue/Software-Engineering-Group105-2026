package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.MatchScore;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository contract for one persisted match score per application.
 */
public interface MatchScoreRepository extends CrudRepository<MatchScore> {
    Optional<MatchScore> findByApplicationId(UUID applicationId);
}
