package com.medtracker.model;

public class Analytics {

    private int totalReminders;
    private int completedReminders;

    public int getTotalReminders() {
        return totalReminders;
    }

    public void setTotalReminders(int totalReminders) {
        this.totalReminders = totalReminders;
    }

    public int getCompletedReminders() {
        return completedReminders;
    }

    public void setCompletedReminders(int completedReminders) {
        this.completedReminders = completedReminders;
    }
}