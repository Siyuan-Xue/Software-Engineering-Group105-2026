package com.bupt.ta.persistence;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.db.core.JsonStoreConfig;

/**
 * Narrow delegation layer over {@link com.bupt.ta.db.facade.TaDatabase}.
 *
 * <p>New code should depend directly on {@code com.bupt.ta.db.facade} types; this class exists solely
 * to avoid breaking stale imports inside legacy modules.</p>
 *
 * @deprecated Call {@linkplain com.bupt.ta.db.facade.DatabaseProvider servlet-context bootstrap} instead.
 */
@Deprecated
public final class TaDatabase {

    private final com.bupt.ta.db.facade.TaDatabase delegate;

    TaDatabase(com.bupt.ta.db.facade.TaDatabase delegate) {
        this.delegate = delegate;
    }

    /**
     * Opens JSON tables using an explicit legacy {@linkplain DatabaseConfig} description.
     *
     * @param config directory + mapper factory inputs
     * @return facade wrapper suitable for callers still bound to persistence package APIs
     */
    public static TaDatabase open(DatabaseConfig config) {
        return new TaDatabase(com.bupt.ta.db.facade.FileTaDatabase.open(
                JsonStoreConfig.of(config.getDataDirectory(), config.getObjectMapper())));
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#users() */
    public com.bupt.ta.db.repository.UserRepository users() {
        return delegate.users();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#resumes() */
    public com.bupt.ta.db.repository.ResumeRepository resumes() {
        return delegate.resumes();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#jobs() */
    public com.bupt.ta.db.repository.JobRepository jobs() {
        return delegate.jobs();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#applications() */
    public com.bupt.ta.db.repository.ApplicationRepository applications() {
        return delegate.applications();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#skills() */
    public com.bupt.ta.db.repository.SkillRepository skills() {
        return delegate.skills();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#resumeSkills() */
    public com.bupt.ta.db.repository.ResumeSkillRepository resumeSkills() {
        return delegate.resumeSkills();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#jobRequirements() */
    public com.bupt.ta.db.repository.JobRequirementRepository jobRequirements() {
        return delegate.jobRequirements();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#workloadRecords() */
    public com.bupt.ta.db.repository.WorkloadRecordRepository workloadRecords() {
        return delegate.workloadRecords();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#matchScores() */
    public com.bupt.ta.db.repository.MatchScoreRepository matchScores() {
        return delegate.matchScores();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#notifications() */
    public com.bupt.ta.db.repository.NotificationRepository notifications() {
        return delegate.notifications();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#auditLogs() */
    public com.bupt.ta.db.repository.AuditLogRepository auditLogs() {
        return delegate.auditLogs();
    }

    /** @see com.bupt.ta.db.facade.TaDatabase#executeAtomically(java.lang.Runnable) */
    public void executeAtomically(Runnable action) {
        delegate.executeAtomically(action);
    }

    /** Exposes the delegate for bridging into non-legacy packages. */
    com.bupt.ta.db.facade.TaDatabase unwrap() {
        return delegate;
    }
}
