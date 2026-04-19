package com.medtracker.controller;

import com.medtracker.model.Notification;
import com.medtracker.model.User;
import com.medtracker.service.NotificationService;
import com.medtracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Notification> createNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String type,
            @RequestParam(defaultValue = "MEDIUM") String priority) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Notification notification = notificationService.createNotification(user, title, message, type, priority);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/expiry-alert")
    public ResponseEntity<Notification> createExpiryAlert(
            @RequestParam Long userId,
            @RequestParam String medicineName,
            @RequestParam int daysUntilExpiry,
            @RequestParam int remainingQuantity) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Notification notification = notificationService.createExpiryAlert(user, medicineName, daysUntilExpiry, remainingQuantity);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/low-stock-alert")
    public ResponseEntity<Notification> createLowStockAlert(
            @RequestParam Long userId,
            @RequestParam String medicineName,
            @RequestParam int currentQuantity,
            @RequestParam int minQuantity) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Notification notification = notificationService.createLowStockAlert(user, medicineName, currentQuantity, minQuantity);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/reminder")
    public ResponseEntity<Notification> createReminderNotification(
            @RequestParam Long userId,
            @RequestParam String medicineName,
            @RequestParam String reminderTime) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Notification notification = notificationService.createReminderNotification(user, medicineName, reminderTime);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Notification> notifications = notificationService.getNotificationsByUser(user);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotificationsByUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Notification> notifications = notificationService.getUnreadNotificationsByUser(user);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId) {
        Notification notification = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long count = notificationService.countUnreadNotifications(user);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}