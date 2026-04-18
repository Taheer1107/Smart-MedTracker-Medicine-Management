package com.medtracker.model;

public class UsageHistory {

    private Long id;
    private String action;

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAction(String action) {
        this.action = action;
    }
}