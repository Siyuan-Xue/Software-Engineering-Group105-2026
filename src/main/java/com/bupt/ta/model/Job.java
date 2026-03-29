package com.bupt.ta.model;

import com.bupt.ta.model.enums.JobStatus;
import com.bupt.ta.model.enums.JobType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Job extends BaseEntity {
    private UUID postedBy;
    private String title;
    private JobType type;
    private String moduleCode;
    private String description;
    private int requiredHours;
    private int slots = 1;
    private JobStatus status = JobStatus.DRAFT;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant deadline;

    public UUID getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(UUID postedBy) {
        this.postedBy = postedBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public JobType getType() {
        return type;
    }

    public void setType(JobType type) {
        this.type = type;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRequiredHours() {
        return requiredHours;
    }

    public void setRequiredHours(int requiredHours) {
        this.requiredHours = requiredHours;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }
}
