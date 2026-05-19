package com.bupt.ta.domain.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.DayOfWeek;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Weekly availability interval stored on a TA resume.
 */
public class AvailabilitySlot {
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
