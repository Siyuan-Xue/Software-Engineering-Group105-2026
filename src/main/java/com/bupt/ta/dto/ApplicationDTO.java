package com.bupt.ta.dto;

import java.time.Instant;
import java.util.UUID;

// 这是专门传给前端展示用的对象 (Data Transfer Object)
public class ApplicationDTO {
    private UUID applicationId;
    private String vacancyTitle;
    private String courseCode;
    private String department;  // 【新增】解决 Checklist 7.1 提到的 Department 字段空缺
    private String status;
    private Instant appliedDate;
    private String resumeName;

    // Getters and Setters
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public String getVacancyTitle() { return vacancyTitle; }
    public void setVacancyTitle(String vacancyTitle) { this.vacancyTitle = vacancyTitle; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getAppliedDate() { return appliedDate; }
    public void setAppliedDate(Instant appliedDate) { this.appliedDate = appliedDate; }

    public String getResumeName() { return resumeName; }
    public void setResumeName(String resumeName) { this.resumeName = resumeName; }
}