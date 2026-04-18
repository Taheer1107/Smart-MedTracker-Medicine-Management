package com.medtracker.decorator;

public abstract class NotificationDecorator {

    protected String message;

    public NotificationDecorator(String message) {
        this.message = message;
    }

    public abstract String getMessage();
}