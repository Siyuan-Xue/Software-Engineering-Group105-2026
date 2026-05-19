package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.value.AuditLogQuery;
import com.bupt.ta.db.store.JsonTableStore;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * JSON-backed audit repository with filtered chronological search.
 */
public class JsonAuditLogRepository extends BaseJsonRepository<AuditLog> implements AuditLogRepository {
    public JsonAuditLogRepository(JsonTableStore<AuditLog> store) {
        super(store);
    }

    @Override
    public List<AuditLog> search(AuditLogQuery query) {
        return findAll().stream()
                .filter(log -> query.getOperatorId() == null || query.getOperatorId().equals(log.getOperatorId()))
                .filter(log -> query.getAction() == null || query.getAction() == log.getAction())
                .filter(log -> query.getEntityType() == null || query.getEntityType() == log.getEntityType())
                .filter(log -> query.getFrom() == null || !safeOperatedAt(log).isBefore(query.getFrom()))
                .filter(log -> query.getTo() == null || !safeOperatedAt(log).isAfter(query.getTo()))
                .sorted(Comparator.comparing(this::safeOperatedAt).reversed())
                .skip((long) query.getPage() * query.getSize())
                .limit(query.getSize())
                .toList();
    }

    @Override
    public void append(AuditLog log) {
        save(log);
    }

    @Override
    public AuditLog save(AuditLog entity) {
        if (entity.getOperatedAt() == null) {
            entity.setOperatedAt(Instant.now());
        }
        return super.save(entity);
    }

    private Instant safeOperatedAt(AuditLog log) {
        return log.getOperatedAt() == null ? Instant.EPOCH : log.getOperatedAt();
    }
}
