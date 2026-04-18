package com.bupt.ta.service;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.domain.enums.WorkloadStatus;
import com.bupt.ta.db.core.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ApplicationService {
    private final TaDatabase db;
    private final ObjectMapper mapper = JsonMapperFactory.create();

    public ApplicationService(TaDatabase db) {
        this.db = db;
    }

    public List<Application> listByJobId(UUID jobId) {
        return db.applications().listByJobId(jobId);
    }

    public List<Application> listByResumeId(UUID resumeId) {
        return db.applications().listByResumeId(resumeId);
    }

    public Application save(Application application) {
        validateBaseApplication(application);
        return db.applications().save(application);
    }

    public Application submit(UUID taUserId, UUID resumeId, UUID jobId, String coverLetter) {
        Resume resume = db.resumes().findById(resumeId)
                .orElseThrow(() -> new ConstraintViolationException("Application resume does not exist: " + resumeId));
        if (!taUserId.equals(resume.getUserId())) {
            throw new ConstraintViolationException("Resume does not belong to the applicant");
        }
        Job job = db.jobs().findById(jobId)
                .orElseThrow(() -> new ConstraintViolationException("Application job does not exist: " + jobId));
        if (job.getStatus() != JobStatus.OPEN || job.getDeadline() == null || !job.getDeadline().isAfter(Instant.now())) {
            throw new ConstraintViolationException("Job is not open for applications");
        }
        if (db.applications().existsByTaAndJob(taUserId, jobId)) {
            throw new ConstraintViolationException("The same TA cannot apply to the same job twice");
        }

        Application application = new Application();
        application.setResumeId(resumeId);
        application.setJobId(jobId);
        application.setStatus(ApplicationStatus.PENDING);
        application.setCoverLetter(coverLetter);

        Application saved = db.applications().save(application);
        appendAudit(taUserId, AuditAction.CREATE, saved.getId(), null, saved);
        notifyUser(job.getPostedBy(), NotificationType.NEW_APPLICANT,
                "New applicant",
                "A TA has submitted an application to your vacancy.",
                EntityType.APPLICATION, saved.getId());
        return saved;
    }

    public Application startReview(UUID moUserId, UUID applicationId) {
        return transition(moUserId, applicationId, ApplicationStatus.REVIEWING, null);
    }

    public Application sendOffer(UUID moUserId, UUID applicationId) {
        return transition(moUserId, applicationId, ApplicationStatus.OFFER_PENDING, null);
    }

    public Application reject(UUID moUserId, UUID applicationId, String notes) {
        return transition(moUserId, applicationId, ApplicationStatus.REJECTED, notes);
    }

    public Application acceptOffer(UUID taUserId, UUID applicationId) {
        Application current = requireApplication(applicationId);
        if (current.getStatus() != ApplicationStatus.OFFER_PENDING) {
            throw new ConstraintViolationException("Only offered applications can be accepted");
        }
        Resume resume = requireResume(current.getResumeId());
        if (!taUserId.equals(resume.getUserId())) {
            throw new ConstraintViolationException("Only the resume owner can accept an offer");
        }
        Job job = requireJob(current.getJobId());

        Application updated = mapper.convertValue(current, Application.class);
        updated.setStatus(ApplicationStatus.ACCEPTED);
        updated.setTaRespondedAt(Instant.now());

        WorkloadRecord record = db.workloadRecords().findByApplicationId(applicationId)
                .orElseGet(WorkloadRecord::new);
        record.setApplicationId(applicationId);
        record.setTaId(taUserId);
        record.setJobId(job.getId());
        record.setSemester(resolveSemester(job));
        record.setAssignedHours(job.getRequiredHours());
        record.setStatus(WorkloadStatus.ACTIVE);

        db.executeAtomically(() -> {
            db.applications().save(updated);
            db.workloadRecords().save(record);
            notifyUser(taUserId, NotificationType.OFFER_RECEIVED,
                    "Offer accepted",
                    "You accepted the offer and your workload was updated.",
                    EntityType.APPLICATION, applicationId);
            appendAudit(taUserId, AuditAction.STATUS_CHANGE, applicationId, current, updated);
        });
        return db.applications().findById(applicationId).orElseThrow();
    }

    public Application declineOffer(UUID taUserId, UUID applicationId) {
        return respondToOffer(taUserId, applicationId, ApplicationStatus.DECLINED);
    }

    public Application withdraw(UUID taUserId, UUID applicationId) {
        Application current = requireApplication(applicationId);
        Resume resume = requireResume(current.getResumeId());
        if (!taUserId.equals(resume.getUserId())) {
            throw new ConstraintViolationException("Only the resume owner can withdraw the application");
        }
        return transition(taUserId, applicationId, ApplicationStatus.WITHDRAWN, current.getMoNotes());
    }

    private Application respondToOffer(UUID taUserId, UUID applicationId, ApplicationStatus targetStatus) {
        Application current = requireApplication(applicationId);
        Resume resume = requireResume(current.getResumeId());
        if (!taUserId.equals(resume.getUserId())) {
            throw new ConstraintViolationException("Only the resume owner can respond to the offer");
        }
        if (current.getStatus() != ApplicationStatus.OFFER_PENDING) {
            throw new ConstraintViolationException("Only offered applications can be updated");
        }
        return transition(taUserId, applicationId, targetStatus, current.getMoNotes());
    }

    private Application transition(UUID operatorId, UUID applicationId, ApplicationStatus targetStatus, String notes) {
        Application current = requireApplication(applicationId);
        Application updated = mapper.convertValue(current, Application.class);
        updated.setStatus(targetStatus);
        if (notes != null) {
            updated.setMoNotes(notes);
        }
        if (targetStatus == ApplicationStatus.REVIEWING || targetStatus == ApplicationStatus.REJECTED || targetStatus == ApplicationStatus.OFFER_PENDING) {
            updated.setReviewedBy(operatorId);
            updated.setReviewedAt(Instant.now());
        }
        if (targetStatus == ApplicationStatus.ACCEPTED || targetStatus == ApplicationStatus.DECLINED || targetStatus == ApplicationStatus.WITHDRAWN) {
            updated.setTaRespondedAt(Instant.now());
        }
        Application saved = db.applications().save(updated);
        Resume resume = requireResume(saved.getResumeId());
        notifyUser(resume.getUserId(), NotificationType.APPLICATION_STATUS,
                "Application status updated",
                "Your application status changed to " + saved.getStatus() + ".",
                EntityType.APPLICATION, saved.getId());
        appendAudit(operatorId, AuditAction.STATUS_CHANGE, saved.getId(), current, saved);
        return saved;
    }

    private void validateBaseApplication(Application application) {
        if (application.getResumeId() == null) {
            throw new ConstraintViolationException("Application resumeId must not be null");
        }
        if (application.getJobId() == null) {
            throw new ConstraintViolationException("Application jobId must not be null");
        }
        requireResume(application.getResumeId());
        requireJob(application.getJobId());
    }

    private Application requireApplication(UUID applicationId) {
        return db.applications().findById(applicationId)
                .orElseThrow(() -> new ConstraintViolationException("Application not found: " + applicationId));
    }

    private Resume requireResume(UUID resumeId) {
        return db.resumes().findById(resumeId)
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + resumeId));
    }

    private Job requireJob(UUID jobId) {
        return db.jobs().findById(jobId)
                .orElseThrow(() -> new ConstraintViolationException("Job not found: " + jobId));
    }

    private void notifyUser(UUID userId, NotificationType type, String title, String message, EntityType entityType, UUID entityId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setNotifType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        db.notifications().save(notification);
    }

    private void appendAudit(UUID operatorId, AuditAction action, UUID entityId, Object oldValue, Object newValue) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setEntityType(EntityType.APPLICATION);
        log.setEntityId(entityId);
        if (oldValue != null) {
            log.setOldValue(mapper.valueToTree(oldValue));
        }
        if (newValue != null) {
            log.setNewValue(mapper.valueToTree(newValue));
        }
        log.setOperatedAt(Instant.now());
        db.auditLogs().append(log);
    }

    private String resolveSemester(Job job) {
        int year;
        int month;
        if (job.getStartDate() != null) {
            year = job.getStartDate().getYear();
            month = job.getStartDate().getMonthValue();
        } else if (job.getDeadline() != null) {
            year = job.getDeadline().atZone(java.time.ZoneId.systemDefault()).getYear();
            month = job.getDeadline().atZone(java.time.ZoneId.systemDefault()).getMonthValue();
        } else {
            java.time.LocalDate today = java.time.LocalDate.now();
            year = today.getYear();
            month = today.getMonthValue();
        }
        return (month >= 8 ? "Fall " : "Spring ") + year;
    }
}
