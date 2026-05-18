package com.bupt.ta.domain.value;

import java.util.UUID;

public class WorkloadAggregate {
    private UUID taId;
    private String semester;
    private int assignedHours;
    private int capacityHours;
    private int remainingHours;
    private double utilizationRatio;

    public UUID getTaId() {
        return taId;
    }

    public void setTaId(UUID taId) {
        this.taId = taId;
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

    public int getCapacityHours() {
        return capacityHours;
    }

    public void setCapacityHours(int capacityHours) {
        this.capacityHours = capacityHours;
    }

    public int getRemainingHours() {
        return remainingHours;
    }

    public void setRemainingHours(int remainingHours) {
        this.remainingHours = remainingHours;
    }

    public double getUtilizationRatio() {
        return utilizationRatio;
    }

    public void setUtilizationRatio(double utilizationRatio) {
        this.utilizationRatio = utilizationRatio;
    }
}
