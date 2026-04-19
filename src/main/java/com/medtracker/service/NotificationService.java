package com.medtracker.service;

import com.medtracker.model.Notification;
import com.medtracker.model.User;
import com.medtracker.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    public Notification createNotification(User user, String title, String message, String type, String priority) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPriority(priority);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);

        // Send email notification
        emailService.sendNotificationEmail(user, saved);

        return saved;
    }

    public Notification createExpiryAlert(User user, String medicineName, int daysUntilExpiry, int remainingQuantity) {
        String priority = daysUntilExpiry <= 7 ? "HIGH" : daysUntilExpiry <= 30 ? "MEDIUM" : "LOW";
        String title = "Medicine Expiry Alert";
        String message = String.format(
            "Your %s will expire in %d days. You have %d units remaining. Please use it soon or dispose safely.",
            medicineName, daysUntilExpiry, remainingQuantity
        );
        // createNotification already sends email – do NOT call emailService again here
        return createNotification(user, title, message, "EXPIRY_ALERT", priority);
    }

    public Notification createLowStockAlert(User user, String medicineName, int currentQuantity, int minQuantity) {
        String title = "Low Stock Alert";
        String message = String.format(
            "Your %s stock is low (%d remaining, minimum: %d). Consider adding to shopping list.",
            medicineName, currentQuantity, minQuantity
        );
        // createNotification already sends email – do NOT call emailService again here
        return createNotification(user, title, message, "LOW_STOCK_ALERT", "MEDIUM");
    }

    public Notification createReminderNotification(User user, String medicineName, String reminderTime) {
        String title = "Medication Reminder - " + medicineName;
        String message = String.format(
            "Reminder set: %s must take %s at %s. Please mark as taken after use.",
            user.getName(), medicineName, reminderTime
        );
        // Save to DB and send standard notification email
        Notification saved = createNotification(user, title, message, "REMINDER", "HIGH");
        // Also send a clean "reminder assigned" confirmation email
        emailService.sendReminderAssignedEmail(user, medicineName, reminderTime, "DAILY");
        return saved;
    }

    public List<Notification> getNotificationsByUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotificationsByUser(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(User user) {
        List<Notification> unread = getUnreadNotificationsByUser(user);
        for (Notification notification : unread) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepository.saveAll(unread);
    }

    public Long countUnreadNotifications(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
}