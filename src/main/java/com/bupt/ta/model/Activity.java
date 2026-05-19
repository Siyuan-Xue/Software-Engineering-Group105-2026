package com.bupt.ta.model;

/**
 * Dashboard activity item rendered in recent-activity feeds.
 */
public class Activity {
    private String title;
    private String description;
    private String timeAgo;
    private String icon;
    private String iconColorClass;
    private String iconBgClass;
    private String statusBadge;
    private String statusBadgeClass;

    public Activity(String title, String description, String timeAgo, String icon, 
                    String iconColorClass, String iconBgClass, String statusBadge, String statusBadgeClass) {
        this.title = title;
        this.description = description;
        this.timeAgo = timeAgo;
        this.icon = icon;
        this.iconColorClass = iconColorClass;
        this.iconBgClass = iconBgClass;
        this.statusBadge = statusBadge;
        this.statusBadgeClass = statusBadgeClass;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTimeAgo() { return timeAgo; }
    public String getIcon() { return icon; }
    public String getIconColorClass() { return iconColorClass; }
    public String getIconBgClass() { return iconBgClass; }
    public String getStatusBadge() { return statusBadge; }
    public String getStatusBadgeClass() { return statusBadgeClass; }
}
