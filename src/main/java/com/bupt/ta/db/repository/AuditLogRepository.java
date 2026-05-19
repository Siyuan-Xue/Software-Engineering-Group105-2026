package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.value.AuditLogQuery;

import java.util.List;

/**
 * Repository contract for append-only audit log search and export.
 */
public interface AuditLogRepository extends CrudRepository<AuditLog> {
    List<AuditLog> search(AuditLogQuery query);

    void append(AuditLog log);
}
