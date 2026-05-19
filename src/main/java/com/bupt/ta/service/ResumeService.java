package com.bupt.ta.service;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.ResumeSkill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Business service for TA resume versions and resume skill bindings.
 */
public class ResumeService {
    private static final Set<ApplicationStatus> ACTIVE_APPLICATION_STATUSES = Set.of(
            ApplicationStatus.PENDING,
            ApplicationStatus.REVIEWING,
            ApplicationStatus.OFFER_PENDING,
            ApplicationStatus.ACCEPTED
    );

    private final TaDatabase db;

    public ResumeService(TaDatabase db) {
        this.db = db;
    }

    public List<Resume> listByUserId(UUID userId) {
        return db.resumes().listByUserId(userId);
    }

    public Resume create(Resume resume) {
        return save(resume);
    }

    public Resume update(Resume resume) {
        return save(resume);
    }

    public Resume save(Resume resume) {
        validateResume(resume);
        return db.resumes().save(resume);
    }

    public Resume duplicate(UUID resumeId) {
        return db.resumes().duplicate(resumeId);
    }

    public void delete(UUID resumeId) {
        delete(null, resumeId);
    }

    /**
     * Deletes a resume only when it is not referenced by active applications.
     */
    public void delete(UUID operatorId, UUID resumeId) {
        Resume existing = db.resumes().findById(resumeId)
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + resumeId));
        if (operatorId != null && !operatorId.equals(existing.getUserId())) {
            throw new ConstraintViolationException("You can only delete your own resumes");
        }
        boolean referenced = db.applications().listByResumeId(resumeId).stream()
                .map(Application::getStatus)
                .anyMatch(ACTIVE_APPLICATION_STATUSES::contains);
        if (referenced) {
            throw new ConstraintViolationException("Cannot delete a resume that is already referenced by active applications");
        }

        db.executeAtomically(() -> {
            db.resumeSkills().deleteByResumeId(existing.getId());
            db.resumes().delete(existing.getId());
        });
    }

    /**
     * Replaces all skills bound to a resume after verifying TA ownership.
     */
    public void replaceSkills(UUID operatorId, UUID resumeId, List<ResumeSkill> skills) {
        Resume resume = db.resumes().findById(resumeId)
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + resumeId));
        if (operatorId != null && !operatorId.equals(resume.getUserId())) {
            throw new ConstraintViolationException("You can only update skills on your own resumes");
        }
        db.executeAtomically(() -> {
            db.resumeSkills().deleteByResumeId(resumeId);
            for (ResumeSkill skill : skills) {
                skill.setResumeId(resumeId);
                db.resumeSkills().save(skill);
            }
        });
    }

    private void validateResume(Resume resume) {
        if (resume.getUserId() == null) {
            throw new ConstraintViolationException("Resume userId must not be null");
        }
        if (resume.getTitle() == null || resume.getTitle().isBlank()) {
            throw new ConstraintViolationException("Resume title must not be blank");
        }
        User owner = db.users().findById(resume.getUserId())
                .orElseThrow(() -> new ConstraintViolationException("Resume owner does not exist: " + resume.getUserId()));
        if (!owner.isActive()) {
            throw new ConstraintViolationException("Resume owner is inactive");
        }
        resume.setTitle(resume.getTitle().trim());
    }
}
