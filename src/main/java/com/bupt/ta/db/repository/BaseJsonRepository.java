package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.AbstractEntity;
import com.bupt.ta.db.store.JsonTableStore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base implementation that delegates common CRUD operations to a JSON table store.
 */
public abstract class BaseJsonRepository<T extends AbstractEntity> implements CrudRepository<T> {
    protected final JsonTableStore<T> store;

    protected BaseJsonRepository(JsonTableStore<T> store) {
        this.store = store;
    }

    @Override
    public Optional<T> findById(UUID id) {
        return store.findById(id);
    }

    @Override
    public List<T> findAll() {
        return store.list();
    }

    @Override
    public T save(T entity) {
        return store.save(entity);
    }

    @Override
    public void delete(UUID id) {
        store.delete(id);
    }

    @Override
    public boolean exists(UUID id) {
        return store.findById(id).isPresent();
    }
}
