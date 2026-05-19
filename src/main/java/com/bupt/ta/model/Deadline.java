package com.bupt.ta.model;

/**
 * Dashboard deadline item with precomputed display classes.
 */
public class Deadline {
    private String title;
    private String timeRemaining;
    private String colorClass;
    private String textColorClass;

    public Deadline(String title, String timeRemaining, String colorClass, String textColorClass) {
        this.title = title;
        this.timeRemaining = timeRemaining;
        this.colorClass = colorClass;
        this.textColorClass = textColorClass;
    }

    public String getTitle() { return title; }
    public String getTimeRemaining() { return timeRemaining; }
    public String getColorClass() { return colorClass; }
    public String getTextColorClass() { return textColorClass; }
}
