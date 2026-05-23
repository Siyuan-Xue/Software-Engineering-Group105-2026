package com.bupt.ta.service;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.enums.WorkloadStatus;
import com.bupt.ta.domain.value.JobQuery;
import com.bupt.ta.db.core.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Vacancy lifecycle orchestration spanning draft creation, publication, requirement editing, cancellation side effects,
 * and recruiter notifications shared by servlet handlers.
 *
 * <p>Status transitions funnel through {@link #changeStatus} so cascading application withdrawals stay atomic.</p>
 */
public class JobService {
    private static final Set<ApplicationStatus> IN_PROGRESS_APPLICATIONS = Set.of(
            ApplicationStatus.PENDING,
            ApplicationStatus.REVIEWING,
            ApplicationStatus.OFFER_PENDING,
            ApplicationStatus.ACCEPTED
    );

    private final TaDatabase db;
    private final ObjectMapper mapper = JsonMapperFactory.create();

    /**
     * @param db shared façade for JSON persistence accessed from MO servlets
     */
    public JobService(TaDatabase db) {
        this.db = db;
    }

    /** @param now comparator instant against vacancy deadlines supplied to repository filtering */
    public List<Job> listOpen(Instant now) {
        JobQuery query = new JobQuery();
        query.setNow(now);
        return db.jobs().listOpen(query);
    }

    /** @param query caller-populated predicates (titles, faculties, paging flags, etc.) */
    public List<Job> listOpen(JobQuery query) {
        return db.jobs().listOpen(query);
    }

    /** @param posterId MO or admin UUID who owns the authoring relationship */
    public List<Job> listByPoster(UUID posterId) {
        return db.jobs().listByPoster(posterId);
    }

    /**
     * Inserts drafts or updates persisted rows keyed by identifier presence on {@code job}.
     *
     * @param job hydrated entity referencing {@link Job#getPostedBy()} credentials
     * @return latest projection after persistence and notifications
     */
    public Job save(Job job) {
        UUID operatorId = job.getPostedBy();
        return job.getId() == null ? createDraft(job) : update(operatorId, job);
    }

    /**
     * Inserts either {@link JobStatus#DRAFT} or caller-provided statuses, auditing creation and optionally notifying TA users.
     */
    public Job createDraft(Job job) {
        validateJob(job);
        if (job.getStatus() == null) {
            job.setStatus(JobStatus.DRAFT);
        }
        Job saved = db.jobs().save(job);
        appendAudit(job.getPostedBy(), AuditAction.CREATE, EntityType.JOB, null, saved);
        if (saved.getStatus() == JobStatus.OPEN) {
            notifyActiveTasAboutNewJob(saved);
        }
        return saved;
    }

    /**
     * Promotes drafts to OPEN with identical validation as broader status mutations.
     */
    public Job publish(UUID operatorId, UUID jobId) {
        return changeStatus(operatorId, jobId, JobStatus.OPEN);
    }

    /**
     * Overwrites vacancy fields belonging to {@code operatorId}; poster identity must remain consistent with repository ACL.
     */
    public Job update(UUID operatorId, Job job) {
        Job existing = db.jobs().findById(job.getId())
                .orElseThrow(() -> new ConstraintViolationException("Job not found: " + job.getId()));
        validateJob(job);
        Job saved = db.jobs().save(job);
        appendAudit(operatorId, AuditAction.UPDATE, EntityType.JOB, existing, saved);
        return saved;
    }

    /**
     * Applies a status transition and performs cancellation side effects.
     *
     * <p>When a vacancy is cancelled, in-progress applications are withdrawn and
     * any linked workload records are cancelled in the same database operation.</p>
     */
    public Job changeStatus(UUID operatorId, UUID jobId, JobStatus status) {
        Job existing = db.jobs().findById(jobId)
                .orElseThrow(() -> new ConstraintViolationException("Job not found: " + jobId));
        Job updated = mapper.convertValue(existing, Job.class);
        updated.setStatus(status);

        if (status != JobStatus.CANCELLED) {
            Job saved = db.jobs().save(updated);
            appendAudit(operatorId, AuditAction.STATUS_CHANGE, EntityType.JOB, existing, saved);
            if (status == JobStatus.OPEN && existing.getStatus() != JobStatus.OPEN) {
                notifyActiveTasAboutNewJob(saved);
            }
            return saved;
        }

        db.executeAtomically(() -> {
            Job cancelled = db.jobs().save(updated);
            for (Application application : db.applications().listByStatuses(jobId, List.copyOf(IN_PROGRESS_APPLICATIONS))) {
                Application mutated = mapper.convertValue(application, Application.class);
                mutated.setStatus(ApplicationStatus.WITHDRAWN);
                db.applications().save(mutated);
                db.workloadRecords().findByApplicationId(mutated.getId()).ifPresent(record -> {
                    WorkloadRecord cancelledRecord = mapper.convertValue(record, WorkloadRecord.class);
                    cancelledRecord.setStatus(WorkloadStatus.CANCELLED);
                    db.workloadRecords().save(cancelledRecord);
                });
                notifyResumeOwner(mutated.getResumeId(),
                        NotificationType.APPLICATION_STATUS,
                        "Application withdrawn",
                        "Your application was withdrawn because the job was cancelled.",
                        EntityType.APPLICATION,
                        mutated.getId());
            }
            appendAudit(operatorId, AuditAction.STATUS_CHANGE, EntityType.JOB, existing, cancelled);
        });
        return db.jobs().findById(jobId).orElseThrow();
    }

    private void notifyActiveTasAboutNewJob(Job job) {
        for (User ta : db.users().listByRole(UserRole.TA)) {
            if (!ta.isActive()) {
                continue;
            }
            boolean alreadySent = db.notifications().findAll().stream()
                    .filter(notification -> ta.getId().equals(notification.getUserId()))
                    .filter(notification -> notification.getNotifType() == NotificationType.NEW_JOB)
                    .anyMatch(notification -> notification.getEntityType() == EntityType.JOB
                            && job.getId().equals(notification.getEntityId()));
            if (alreadySent) {
                continue;
            }
            Notification notification = new Notification();
            notification.setUserId(ta.getId());
            notification.setNotifType(NotificationType.NEW_JOB);
            notification.setTitle("New vacancy posted");
            notification.setMessage("A new TA vacancy is open: " + (job.getTitle() == null ? "Untitled vacancy" : job.getTitle()) + ".");
            notification.setEntityType(EntityType.JOB);
            notification.setEntityId(job.getId());
            db.notifications().save(notification);
        }
    }

    /**
     * Deletes prior rows for {@code jobId} and persists {@code requirements} atomically inside one transaction bracket.
     *
     * @param operatorId  actor recorded in supplemental audit hooks
     * @param jobId       target vacancy UUID
     * @param requirements full replacement snapshot (identifiers may be regenerated)
     */
    public void replaceRequirements(UUID operatorId, UUID jobId, List<JobRequirement> requirements) {
        db.jobs().findById(jobId).orElseThrow(() -> new ConstraintViolationException("Job not found: " + jobId));
        db.executeAtomically(() -> {
            List<JobRequirement> retained = db.jobRequirements().findAll().stream()
                    .filter(requirement -> !jobId.equals(requirement.getJobId()))
                    .toList();
            db.jobRequirements().findAll().forEach(requirement -> {
                if (jobId.equals(requirement.getJobId())) {
                    db.jobRequirements().delete(requirement.getId());
                }
            });
            for (JobRequirement requirement : requirements) {
                requirement.setJobId(jobId);
                db.jobRequirements().save(requirement);
            }
        });
        appendAudit(operatorId, AuditAction.UPDATE, EntityType.JOB_REQUIREMENT, null, null);
    }

    private void validateJob(Job job) {
        if (job.getPostedBy() == null) {
            throw new ConstraintViolationException("Job postedBy must not be null");
        }
        User poster = db.users().findById(job.getPostedBy())
                .orElseThrow(() -> new ConstraintViolationException("Job poster does not exist: " + job.getPostedBy()));
        if (poster.getRole() != UserRole.MO && poster.getRole() != UserRole.ADMIN) {
            throw new ConstraintViolationException("Job poster must be an MO or ADMIN user");
        }
        if (job.getTitle() == null || job.getTitle().isBlank()) {
            throw new ConstraintViolationException("Job title must not be blank");
        }
        if (job.getDeadline() == null) {
            throw new ConstraintViolationException("Job deadline must not be null");
        }
        LocalDate start = job.getStartDate();
        LocalDate end = job.getEndDate();
        if (start != null && end != null && end.isBefore(start)) {
            throw new ConstraintViolationException("Job endDate must not be before startDate");
        }
        job.setTitle(job.getTitle().trim());
    }

    private void notifyResumeOwner(UUID resumeId,
                                   NotificationType type,
                                   String title,
                                   String message,
                                   EntityType entityType,
                                   UUID entityId) {
        Resume resume = db.resumes().findById(resumeId).orElse(null);
        if (resume == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(resume.getUserId());
        notification.setNotifType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        db.notifications().save(notification);
    }

    private void appendAudit(UUID operatorId, AuditAction action, EntityType entityType, Object oldValue, Object newValue) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setEntityType(entityType);
        if (newValue instanceof Job job) {
            log.setEntityId(job.getId());
        }
        if (oldValue != null) {
            log.setOldValue(mapper.valueToTree(oldValue));
        }
        if (newValue != null) {
            log.setNewValue(mapper.valueToTree(newValue));
        }
        log.setOperatedAt(Instant.now());
        db.auditLogs().append(log);
    }
}
