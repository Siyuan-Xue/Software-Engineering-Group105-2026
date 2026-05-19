package com.bupt.ta.domain.value;

import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;

import java.time.Instant;

/**
 * Filter criteria for vacancy listing and search.
 */
public class JobQuery {
    private JobType type;
    private String moduleCodeKeyword;
    private Integer minRequiredHours;
    private Integer maxRequiredHours;
    private JobStatus status;
    private Instant now;

    public JobType getType() {
        return type;
    }

    public void setType(JobType type) {
        this.type = type;
    }

    public String getModuleCodeKeyword() {
        return moduleCodeKeyword;
    }

    public void setModuleCodeKeyword(String moduleCodeKeyword) {
        this.moduleCodeKeyword = moduleCodeKeyword;
    }

    public Integer getMinRequiredHours() {
        return minRequiredHours;
    }

    public void setMinRequiredHours(Integer minRequiredHours) {
        this.minRequiredHours = minRequiredHours;
    }

    public Integer getMaxRequiredHours() {
        return maxRequiredHours;
    }

    public void setMaxRequiredHours(Integer maxRequiredHours) {
        this.maxRequiredHours = maxRequiredHours;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Instant getNow() {
        return now;
    }

    public void setNow(Instant now) {
        this.now = now;
    }
}
