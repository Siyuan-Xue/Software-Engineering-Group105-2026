package com.bupt.ta.domain.value;

import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.EntityType;

import java.time.Instant;
import java.util.UUID;

/**
 * Search criteria for administrator audit-log review and export.
 */
public class AuditLogQuery {
    private UUID operatorId;
    private AuditAction action;
    private EntityType entityType;
    private Instant from;
    private Instant to;
    private int page;
    private int size = 50;

    public UUID getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(UUID operatorId) {
        this.operatorId = operatorId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(page, 0);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size <= 0 ? 50 : size;
    }
}
