package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.value.AuditLogQuery;
import com.bupt.ta.domain.value.WorkloadAggregate;

import java.util.List;
import java.util.UUID;

public class AdminService {
    private final TaDatabase db;

    public AdminService(TaDatabase db) {
        this.db = db;
    }

    public List<WorkloadAggregate> workloadDashboard(String semester) {
        return db.workloadRecords().aggregateBySemester(semester);
    }

    public List<AuditLog> searchAuditLogs(AuditLogQuery query) {
        return db.auditLogs().search(query);
    }

    public User updateUser(UUID operatorId, User user) {
        return db.users().save(user);
    }
}
