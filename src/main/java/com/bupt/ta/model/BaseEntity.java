package com.bupt.ta.model;

import com.bupt.ta.persistence.PersistableEntity;

import java.time.Instant;
import java.util.UUID;

public abstract class BaseEntity implements PersistableEntity {
    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
