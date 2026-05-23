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
 * Coordinates TA resume drafts and resume-skill linkage with transactional guardrails.
 *
 * <p>Deletion and skill replacement enforce ownership checks when {@code operatorId} is supplied,
 * preserving demo flows that invoke service methods without an authenticated caller.</p>
 */
public class ResumeService {
    private static final Set<ApplicationStatus> ACTIVE_APPLICATION_STATUSES = Set.of(
            ApplicationStatus.PENDING,
            ApplicationStatus.REVIEWING,
            ApplicationStatus.OFFER_PENDING,
            ApplicationStatus.ACCEPTED
    );

    private final TaDatabase db;

    /** @param db persistence facade consulted for resumes, resume skills, and application lookups */
    public ResumeService(TaDatabase db) {
        this.db = db;
    }

    /** @return resumes owned by {@code userId}, ordered by persistence layer defaults */
    public List<Resume> listByUserId(UUID userId) {
        return db.resumes().listByUserId(userId);
    }

    /** @see #save(Resume) */
    public Resume create(Resume resume) {
        return save(resume);
    }

    /** @see #save(Resume) */
    public Resume update(Resume resume) {
        return save(resume);
    }

    /**
     * Validates invariants before persisting a resume snapshot.
     *
     * @throws ConstraintViolationException when the resume title is blank, the owner ID is absent,
     *         or the owning user cannot be activated for edits
     */
    public Resume save(Resume resume) {
        validateResume(resume);
        return db.resumes().save(resume);
    }

    /** @param resumeId resume to clone verbatim through the persistence layer */
    public Resume duplicate(UUID resumeId) {
        return db.resumes().duplicate(resumeId);
    }

    /** @see #delete(UUID, UUID) */
    public void delete(UUID resumeId) {
        delete(null, resumeId);
    }

    /**
     * Deletes a resume only when no non-terminal applications reference it.
     *
     * @param operatorId when non-null, must match {@link Resume#getUserId()} or the removal is rejected
     * @throws ConstraintViolationException when the resume does not exist, ownership fails, or active applications bind it
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
     * Replaces all skills attached to {@code resumeId} within one atomic persistence operation.
     *
     * @param operatorId when non-null, must own the resume; each {@link ResumeSkill} gets {@link ResumeSkill#setResumeId(UUID)}
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
