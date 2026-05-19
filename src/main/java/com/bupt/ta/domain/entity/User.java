package com.bupt.ta.domain.entity;

import com.bupt.ta.domain.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Account record for Teaching Assistants, Module Organisers, and Administrators.
 */
public class User extends AbstractEntity {
    private String email;
    private String passwordHash;
    private UserRole role;
    private String fullName;
    private String phone;
    private boolean active = true;
    private String department;
    private String studentId;
    private String bio;
    private boolean notificationsEnabled = true;
    private String preferredLanguage = "en";
    private String preferredAppearance = "light";
    private Set<UUID> savedJobIds = new HashSet<>();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getPreferredAppearance() {
        return preferredAppearance;
    }

    public void setPreferredAppearance(String preferredAppearance) {
        this.preferredAppearance = preferredAppearance;
    }

    public Set<UUID> getSavedJobIds() {
        return savedJobIds == null ? new HashSet<>() : new HashSet<>(savedJobIds);
    }

    public void setSavedJobIds(Set<UUID> savedJobIds) {
        this.savedJobIds = savedJobIds == null ? new HashSet<>() : new HashSet<>(savedJobIds);
    }
}
