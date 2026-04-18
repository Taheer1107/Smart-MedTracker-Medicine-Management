package com.medtracker.factory;

import com.medtracker.model.Notification;

public class NotificationFactory {

    public static Notification createNotification(String type, String message) {

        Notification notification = new Notification();

        switch (type.toLowerCase()) {
            case "email":
                notification.setType("EMAIL");
                break;
            case "sms":
                notification.setType("SMS");
                break;
            default:
                notification.setType("GENERAL");
        }

        notification.setMessage(message);
        return notification;
    }
}