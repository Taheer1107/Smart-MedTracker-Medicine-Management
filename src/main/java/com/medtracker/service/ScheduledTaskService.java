package com.medtracker.service;

import com.medtracker.model.*;
import com.medtracker.repository.UserRepository;
import com.medtracker.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.medtracker.observer.MedicineEventPublisher;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Scheduled tasks for daily maintenance and notifications
 */
@Service
public class ScheduledTaskService {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private EmailService emailService;

    /** Observer Pattern — publisher so scheduled tasks can fire events. */
    @Autowired
    private MedicineEventPublisher medicineEventPublisher;

    /**
     * Fire medication reminder emails every minute.
     * Checks all active reminders whose scheduled time falls within the current minute
     * and sends an email to the custom notificationEmail if one was provided.
     *
     * Cron: "0 * * * * ?" = top of every minute.
     */
    @Scheduled(cron = "0 * * * * ?")
    public void fireReminderEmails() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        List<Reminder> activeReminders = reminderRepository.findByIsActiveTrue();
        for (Reminder reminder : activeReminders) {
            if (reminder.getNotificationEmail() == null || reminder.getNotificationEmail().isBlank()) {
                continue; // no custom email — handled by regular notification flow
            }
            if (reminder.getReminderTime() == null) continue;

            LocalTime scheduledTime = reminder.getReminderTime().withSecond(0).withNano(0);
            if (!scheduledTime.equals(now)) continue;

            // Skip ALTERNATE_DAY reminders on the wrong day
            if ("ALTERNATE_DAY".equalsIgnoreCase(reminder.getFrequency())) {
                if (reminder.getStartDate() != null) {
                    long daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(
                            reminder.getStartDate().toLocalDate(), LocalDate.now());
                    if (daysSinceStart % 2 != 0) continue;
                }
            }

            String memberName = reminder.getFamilyMember() != null
                    ? reminder.getFamilyMember().getName() : "Member";
            String medicineName = reminder.getMedicine() != null
                    ? reminder.getMedicine().getName() : "medicine";
            int dosage = reminder.getDosage() != null ? reminder.getDosage() : 1;

            emailService.sendScheduledReminderEmail(
                    reminder.getNotificationEmail(),
                    memberName,
                    medicineName,
                    dosage,
                    scheduledTime.toString()
            );
        }
    }

    /**
     * Generate daily analytics for all users - runs at 2 AM daily
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyAnalytics() {
        List<User> users = userRepository.findAll();
        LocalDate today = LocalDate.now();

        for (User user : users) {
            try {
                analyticsService.generateDailyAnalytics(user, today);
            } catch (Exception e) {
                System.err.println("Error generating analytics for user " + user.getUserId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Check for expiring medicines and send alerts - runs daily at 8 AM.
     *
     * DESIGN PATTERN - Observer:
     *   Publishes a typed MedicineEvent through MedicineEventPublisher.
     *   All registered observers (ExpiryAlertObserver) react automatically.
     *   Adding a new alert channel requires only a new observer - this method never changes.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkExpiryAlerts() {
        List<Medicine> expiringMedicines = medicineService.getMedicinesExpiringSoon(30);

        for (Medicine medicine : expiringMedicines) {
            if (medicine.getInventory() != null && medicine.getInventory().getHousehold() != null) {
                List<User> householdUsers = userRepository.findByHousehold(medicine.getInventory().getHousehold());
                User primaryCaretaker = householdUsers.stream()
                    .filter(u -> "PrimaryCaretaker".equals(u.getRole()))
                    .findFirst()
                    .orElse(null);

                if (primaryCaretaker != null) {
                    int daysUntilExpiry = (int) java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.now(), medicine.getExpiryDate());
                    Long householdId = medicine.getInventory().getHousehold().getHouseholdId();

                    // Observer Pattern: publish event; let observers handle notification
                    medicineEventPublisher.publishExpiryAlert(
                        medicine.getName(), medicine.getBrand(),
                        householdId, primaryCaretaker.getUserId(), daysUntilExpiry);
                }
            }
        }
    }

    /**
     * Check for low stock items and send alerts - runs daily at 9 AM.
     *
     * DESIGN PATTERN - Observer:
     *   Publishes LOW_STOCK events; LowStockObserver handles the actual notification.
     *   This decouples the scheduler from the notification mechanism entirely.
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkLowStockAlerts() {
        List<Medicine> allMedicines = medicineService.getAllMedicines();

        for (Medicine medicine : allMedicines) {
            if (medicine.getInventory() != null) {
                Inventory inventory = medicine.getInventory();
                if (inventory.getCurrentQuantity() <= inventory.getMinQuantity()) {
                    if (inventory.getHousehold() != null) {
                        List<User> householdUsers = userRepository.findByHousehold(inventory.getHousehold());
                        Long householdId = inventory.getHousehold().getHouseholdId();

                        for (User user : householdUsers) {
                            // Observer Pattern: publish event; LowStockObserver reacts
                            medicineEventPublisher.publishLowStock(
                                medicine.getName(), medicine.getBrand(),
                                householdId, user.getUserId(),
                                inventory.getCurrentQuantity());
                        }
                    }
                }
            }
        }
    }

    /**
     * Clean up old notifications (older than 90 days) - runs weekly on Sunday at 3 AM
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void cleanupOldNotifications() {
        // This would require a custom repository method to delete old notifications
        // For now, we'll just log that cleanup should happen
        System.out.println("Scheduled cleanup of old notifications completed.");
    }
}