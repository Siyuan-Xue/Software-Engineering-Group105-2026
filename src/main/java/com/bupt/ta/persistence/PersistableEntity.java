package com.bupt.ta.persistence;

import java.time.Instant;
import java.util.UUID;

public interface PersistableEntity {
    UUID getId();

    void setId(UUID id);

    Instant getCreatedAt();

    void setCreatedAt(Instant createdAt);

    Instant getUpdatedAt();

    void setUpdatedAt(Instant updatedAt);
}
