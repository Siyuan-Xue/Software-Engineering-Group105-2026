package com.bupt.ta.domain.entity;

import com.bupt.ta.domain.enums.WorkloadStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Workload assignment created when a TA accepts an offer.
 */
public class WorkloadRecord extends AbstractEntity {
    private UUID taId;
    private UUID jobId;
    private UUID applicationId;
    private String semester;
    private int assignedHours;
    private int actualHours;
    private WorkloadStatus status = WorkloadStatus.ACTIVE;

    public UUID getTaId() {
        return taId;
    }

    public void setTaId(UUID taId) {
        this.taId = taId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getAssignedHours() {
        return assignedHours;
    }

    public void setAssignedHours(int assignedHours) {
        this.assignedHours = assignedHours;
    }

    public int getActualHours() {
        return actualHours;
    }

    public void setActualHours(int actualHours) {
        this.actualHours = actualHours;
    }

    public WorkloadStatus getStatus() {
        return status;
    }

    public void setStatus(WorkloadStatus status) {
        this.status = status;
    }
}
