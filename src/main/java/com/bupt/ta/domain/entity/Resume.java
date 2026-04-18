package com.bupt.ta.domain.entity;

import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.value.AvailabilitySlot;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Resume extends AbstractEntity {
    private UUID userId;
    private String title;
    private String department;
    private DegreeLevel degreeLevel;
    private BigDecimal gpa;
    private String bio;
    private int maxWeeklyHours = 20;
    private List<AvailabilitySlot> availabilitySlots = new ArrayList<>();
    private String uploadedFilePath;
    private String originalFileName;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public DegreeLevel getDegreeLevel() {
        return degreeLevel;
    }

    public void setDegreeLevel(DegreeLevel degreeLevel) {
        this.degreeLevel = degreeLevel;
    }

    public BigDecimal getGpa() {
        return gpa;
    }

    public void setGpa(BigDecimal gpa) {
        this.gpa = gpa;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public void setMaxWeeklyHours(int maxWeeklyHours) {
        this.maxWeeklyHours = maxWeeklyHours;
    }

    public List<AvailabilitySlot> getAvailabilitySlots() {
        return availabilitySlots == null ? new ArrayList<>() : new ArrayList<>(availabilitySlots);
    }

    public void setAvailabilitySlots(List<AvailabilitySlot> availabilitySlots) {
        this.availabilitySlots = availabilitySlots == null ? new ArrayList<>() : new ArrayList<>(availabilitySlots);
    }

    public String getUploadedFilePath() {
        return uploadedFilePath;
    }

    public void setUploadedFilePath(String uploadedFilePath) {
        this.uploadedFilePath = uploadedFilePath;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    @JsonIgnore
    public String getUpdatedAtDisplay() {
        if (getUpdatedAt() == null) {
            return "N/A";
        }
        return DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(getUpdatedAt());
    }
}
