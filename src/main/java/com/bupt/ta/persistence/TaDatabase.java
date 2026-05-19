package com.bupt.ta.persistence;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.db.core.JsonStoreConfig;

@Deprecated
/**
 * Backward-compatible wrapper around the primary database facade.
 */
public final class TaDatabase {
    private final com.bupt.ta.db.facade.TaDatabase delegate;

    TaDatabase(com.bupt.ta.db.facade.TaDatabase delegate) {
        this.delegate = delegate;
    }

    public static TaDatabase open(DatabaseConfig config) {
        return new TaDatabase(com.bupt.ta.db.facade.FileTaDatabase.open(
                JsonStoreConfig.of(config.getDataDirectory(), config.getObjectMapper())));
    }

    public com.bupt.ta.db.repository.UserRepository users() {
        return delegate.users();
    }

    public com.bupt.ta.db.repository.ResumeRepository resumes() {
        return delegate.resumes();
    }

    public com.bupt.ta.db.repository.JobRepository jobs() {
        return delegate.jobs();
    }

    public com.bupt.ta.db.repository.ApplicationRepository applications() {
        return delegate.applications();
    }

    public com.bupt.ta.db.repository.SkillRepository skills() {
        return delegate.skills();
    }

    public com.bupt.ta.db.repository.ResumeSkillRepository resumeSkills() {
        return delegate.resumeSkills();
    }

    public com.bupt.ta.db.repository.JobRequirementRepository jobRequirements() {
        return delegate.jobRequirements();
    }

    public com.bupt.ta.db.repository.WorkloadRecordRepository workloadRecords() {
        return delegate.workloadRecords();
    }

    public com.bupt.ta.db.repository.MatchScoreRepository matchScores() {
        return delegate.matchScores();
    }

    public com.bupt.ta.db.repository.NotificationRepository notifications() {
        return delegate.notifications();
    }

    public com.bupt.ta.db.repository.AuditLogRepository auditLogs() {
        return delegate.auditLogs();
    }

    public void executeAtomically(Runnable action) {
        delegate.executeAtomically(action);
    }

    com.bupt.ta.db.facade.TaDatabase unwrap() {
        return delegate;
    }
}
