package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.AbstractEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Common CRUD contract shared by JSON-backed repositories.
 */
public interface CrudRepository<T extends AbstractEntity> {
    Optional<T> findById(UUID id);

    List<T> findAll();

    T save(T entity);

    void delete(UUID id);

    boolean exists(UUID id);
}
