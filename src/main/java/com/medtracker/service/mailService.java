package com.medtracker.service;

import com.medtracker.model.Notification;
import com.medtracker.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendNotificationEmail(User user, Notification notification) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("MedTracker Alert: " + notification.getTitle());
        message.setText(buildEmailContent(notification));

        try {
            mailSender.send(message);
            System.out.println("Email sent successfully to " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends an email confirmation when a new reminder is assigned to a user.
     * Called by NotificationService -> createReminderNotification.
     */
    public void sendReminderAssignedEmail(User user, String medicineName, String reminderTime, String frequency) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            System.out.println("No email for user " + user.getName() + " – skipping reminder email.");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("✅ Medication Reminder Set – " + medicineName);

        String body = "Dear " + user.getName() + ",\n\n"
                + "A new medication reminder has been successfully assigned to you.\n\n"
                + "📋 Reminder Details:\n"
                + "  Medicine  : " + medicineName + "\n"
                + "  Time      : " + reminderTime + "\n"
                + "  Frequency : " + frequency + "\n\n"
                + "You will receive daily reminders at the scheduled time.\n"
                + "Please mark the reminder as 'Complete' in the app after taking your medication.\n\n"
                + "Stay healthy! 💊\n\n"
                + "Best regards,\n"
                + "Smart MedTracker Team\n\n"
                + "---\nThis is an automated message. Please do not reply.";

        message.setText(body);
        try {
            mailSender.send(message);
            System.out.println("Reminder assignment email sent to " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send reminder email to " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildEmailContent(Notification notification) {
        StringBuilder content = new StringBuilder();
        content.append("Dear MedTracker User,\n\n");
        content.append(notification.getTitle()).append("\n\n");
        content.append(notification.getMessage()).append("\n\n");

        switch (notification.getPriority()) {
            case "HIGH":
                content.append("⚠️ This is a HIGH PRIORITY notification. Please take immediate action.\n\n");
                break;
            case "MEDIUM":
                content.append("ℹ️ This is a MEDIUM priority notification.\n\n");
                break;
            case "LOW":
                content.append("📝 This is a LOW priority notification.\n\n");
                break;
        }

        content.append("Best regards,\n");
        content.append("Smart MedTracker Team\n\n");
        content.append("---\n");
        content.append("This is an automated message. Please do not reply.");

        return content.toString();
    }

    public void sendExpiryAlert(User user, String medicineName, int daysUntilExpiry, int remainingQuantity) {
        Notification notification = new Notification();
        notification.setTitle("Medicine Expiry Alert");
        notification.setMessage(String.format(
            "Your %s will expire in %d days. You have %d units remaining. Please use it soon or dispose safely.",
            medicineName, daysUntilExpiry, remainingQuantity
        ));
        notification.setType("EXPIRY_ALERT");
        notification.setPriority(daysUntilExpiry <= 7 ? "HIGH" : daysUntilExpiry <= 30 ? "MEDIUM" : "LOW");
        notification.setUser(user);

        sendNotificationEmail(user, notification);
    }

    public void sendLowStockAlert(User user, String medicineName, int currentQuantity, int minQuantity) {
        Notification notification = new Notification();
        notification.setTitle("Low Stock Alert");
        notification.setMessage(String.format(
            "Your %s stock is low (%d remaining, minimum: %d). Consider adding to shopping list.",
            medicineName, currentQuantity, minQuantity
        ));
        notification.setType("LOW_STOCK_ALERT");
        notification.setPriority("MEDIUM");
        notification.setUser(user);

        sendNotificationEmail(user, notification);
    }

    public void sendReminderNotification(User user, String medicineName, String reminderTime) {
        Notification notification = new Notification();
        notification.setTitle("Medication Reminder");
        notification.setMessage(String.format(
            "It's time to take your %s medication (%s). Please mark as taken after use.",
            medicineName, reminderTime
        ));
        notification.setType("REMINDER");
        notification.setPriority("HIGH");
        notification.setUser(user);

        sendNotificationEmail(user, notification);
    }

    /**
     * Sends a confirmation email to the custom address the user entered when
     * creating the reminder.  Called immediately after the reminder is saved.
     *
     * @param toEmail      The email address provided in the reminder form
     * @param memberName   Display name of the family member
     * @param medicineName Name of the medicine
     * @param reminderTime HH:mm scheduled time
     * @param frequency    e.g. DAILY, ALTERNATE_DAY
     */
    public void sendReminderConfirmationEmail(String toEmail, String memberName,
                                              String medicineName, String reminderTime,
                                              String frequency) {
        if (toEmail == null || toEmail.isBlank()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("✅ Medication Reminder Confirmed – " + medicineName);

        String freqLabel = frequency != null ? frequency.replace("_", " ") : "Daily";
        String body = "Dear " + memberName + ",\n\n"
                + "Your medication reminder has been successfully set up.\n\n"
                + "📋 Reminder Details:\n"
                + "  Medicine   : " + medicineName + "\n"
                + "  Time       : " + reminderTime + "\n"
                + "  Frequency  : " + freqLabel + "\n"
                + "  Notify to  : " + toEmail + "\n\n"
                + "You will receive email reminders at the scheduled time.\n"
                + "Please mark the reminder as 'Complete' in the app after taking your medication.\n\n"
                + "Stay healthy! 💊\n\n"
                + "Best regards,\n"
                + "Smart MedTracker Team\n\n"
                + "---\nThis is an automated message. Please do not reply.";

        message.setText(body);
        try {
            mailSender.send(message);
            System.out.println("Reminder confirmation email sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends a scheduled "time-to-take-your-medicine" email to the custom address
     * stored on the reminder.  Called by ScheduledTaskService at the reminder's time.
     *
     * @param toEmail      Custom notification email from the reminder
     * @param memberName   Family member's display name
     * @param medicineName Name of the medicine
     * @param dosage       Dosage count
     * @param reminderTime HH:mm scheduled time
     */
    public void sendScheduledReminderEmail(String toEmail, String memberName,
                                           String medicineName, int dosage,
                                           String reminderTime) {
        if (toEmail == null || toEmail.isBlank()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("⏰ Medication Reminder: Time to take " + medicineName);

        String body = "Dear " + memberName + ",\n\n"
                + "⏰ It's time to take your medication!\n\n"
                + "  Medicine : " + medicineName + "\n"
                + "  Dosage   : " + dosage + " unit(s)\n"
                + "  Time     : " + reminderTime + "\n\n"
                + "Please take your medicine and mark the reminder as 'Complete' in the app.\n\n"
                + "Stay healthy! 💊\n\n"
                + "Best regards,\n"
                + "Smart MedTracker Team\n\n"
                + "---\nThis is an automated message. Please do not reply.";

        message.setText(body);
        try {
            mailSender.send(message);
            System.out.println("Scheduled reminder email sent to " + toEmail + " for " + medicineName);
        } catch (Exception e) {
            System.err.println("Failed to send scheduled reminder email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}