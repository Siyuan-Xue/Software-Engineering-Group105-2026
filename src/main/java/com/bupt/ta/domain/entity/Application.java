package com.bupt.ta.domain.entity;

import com.bupt.ta.domain.enums.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Application submitted by a TA for a specific vacancy using a selected resume.
 */
public class Application extends AbstractEntity {
    private UUID resumeId;
    private UUID jobId;
    private ApplicationStatus status = ApplicationStatus.PENDING;
    private String coverLetter;
    private UUID reviewedBy;
    private Instant reviewedAt;
    private String moNotes;
    private Instant taRespondedAt;
    /** Snapshot of the resume file submitted with this application (for MO screening). */
    private String submittedFilePath;
    private String submittedFileName;

    public UUID getResumeId() {
        return resumeId;
    }

    public void setResumeId(UUID resumeId) {
        this.resumeId = resumeId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(UUID reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getMoNotes() {
        return moNotes;
    }

    public void setMoNotes(String moNotes) {
        this.moNotes = moNotes;
    }

    public Instant getTaRespondedAt() {
        return taRespondedAt;
    }

    public void setTaRespondedAt(Instant taRespondedAt) {
        this.taRespondedAt = taRespondedAt;
    }

    public String getSubmittedFilePath() {
        return submittedFilePath;
    }

    public void setSubmittedFilePath(String submittedFilePath) {
        this.submittedFilePath = submittedFilePath;
    }

    public String getSubmittedFileName() {
        return submittedFileName;
    }

    public void setSubmittedFileName(String submittedFileName) {
        this.submittedFileName = submittedFileName;
    }
}
