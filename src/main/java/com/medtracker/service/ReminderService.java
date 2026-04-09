package com.medtracker.service;

import com.medtracker.model.Reminder;
import com.medtracker.model.FamilyMember;
import com.medtracker.model.Medicine;
import com.medtracker.model.User;
import com.medtracker.repository.ReminderRepository;
import com.medtracker.repository.UserRepository;
import com.medtracker.repository.MedicineRepository;
import com.medtracker.strategy.ReminderStrategy;
import com.medtracker.strategy.DailyReminderStrategy;
import com.medtracker.strategy.AlternateDayReminderStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    private ReminderStrategy reminderStrategy;

    /**
     * Create reminder from web form.
     * If notificationEmail is supplied it is persisted on the reminder and
     * used for all future scheduled reminder emails.
     * A confirmation email is sent to that address immediately on creation.
     */
    public Reminder createReminderFromIds(Long familyMemberId, Long medicineId, LocalTime reminderTime,
                                          String frequency, Integer dosage, String notes,
                                          String notificationEmail) {
        User user = userRepository.findById(familyMemberId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + familyMemberId));
        if (!(user instanceof FamilyMember)) {
            throw new IllegalArgumentException("Selected user is not a Family Member or Primary Caretaker");
        }
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found: " + medicineId));

        Reminder reminder = new Reminder();
        reminder.setFamilyMember((FamilyMember) user);
        reminder.setMedicine(medicine);
        reminder.setReminderTime(reminderTime);
        reminder.setFrequency(frequency != null ? frequency : "DAILY");
        reminder.setDosage(dosage != null ? dosage : 1);
        reminder.setNotes(notes);

        // Store the custom notification email if provided
        if (notificationEmail != null && !notificationEmail.isBlank()) {
            reminder.setNotificationEmail(notificationEmail.trim());
        }

        Reminder saved = createReminder(reminder);

        // Send a confirmation / "reminder set" email to the custom address immediately
        String targetEmail = saved.getNotificationEmail();
        if (targetEmail != null && !targetEmail.isBlank()) {
            String timeStr = reminderTime != null ? reminderTime.toString() : "08:00";
            emailService.sendReminderConfirmationEmail(
                    targetEmail,
                    user.getName(),
                    medicine.getName(),
                    timeStr,
                    saved.getFrequency()
            );
        }

        return saved;
    }

    // Create reminder
    public Reminder createReminder(Reminder reminder) {
        // Apply strategy based on frequency
        setStrategyBasedOnFrequency(reminder.getFrequency());
        
        // Calculate next scheduled time using strategy
        if (reminderStrategy != null) {
            LocalDateTime nextTime = reminderStrategy.calculateNextReminderTime(reminder);
            reminder.setNextScheduledTime(nextTime);
        }
        
        Reminder saved = reminderRepository.save(reminder);
        
        // Create a notification for the reminder
        try {
            if (reminder.getFamilyMember() != null && reminder.getMedicine() != null) {
                String reminderTimeStr = reminder.getReminderTime() != null 
                    ? reminder.getReminderTime().toString() 
                    : "08:00";
                notificationService.createReminderNotification(
                    reminder.getFamilyMember(),
                    reminder.getMedicine().getName(),
                    reminderTimeStr
                );
            }
        } catch (Exception e) {
            // Log but don't fail the reminder creation if notification fails
            System.err.println("Warning: Could not create notification for reminder: " + e.getMessage());
        }
        
        return saved;
    }

    // Get reminder by ID
    public Optional<Reminder> getReminderById(Long reminderId) {
        return reminderRepository.findById(reminderId);
    }

    // Get all reminders
    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }

    // Get reminders by family member
    public List<Reminder> getRemindersByFamilyMember(FamilyMember familyMember) {
        return reminderRepository.findByFamilyMember(familyMember);
    }

    // Get reminders by user ID
    public List<Reminder> getRemindersByUserId(Long userId) {
        return reminderRepository.findByFamilyMemberId(userId);
    }

    // Get reminders by medicine
    public List<Reminder> getRemindersByMedicine(Medicine medicine) {
        return reminderRepository.findByMedicine(medicine);
    }

    // Get reminders by status
    public List<Reminder> getRemindersByStatus(String status) {
        return reminderRepository.findByStatus(status);
    }

    // Get active reminders
    public List<Reminder> getActiveReminders() {
        return reminderRepository.findByIsActiveTrue();
    }

    // Get pending reminders
    public List<Reminder> getPendingReminders() {
        return reminderRepository.findPendingReminders();
    }

    // Get due reminders
    public List<Reminder> getDueReminders() {
        return reminderRepository.findDueReminders(LocalDateTime.now());
    }

    // Get today's reminders for user
    public List<Reminder> getTodaysRemindersForUser(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return reminderRepository.findTodaysRemindersByUserId(userId, startOfDay, endOfDay);
    }

    // Get missed reminders for user
    public List<Reminder> getMissedRemindersForUser(Long userId) {
        return reminderRepository.findMissedRemindersByUserId(userId);
    }

    // Update reminder
    public Reminder updateReminder(Reminder reminder) {
        if (!reminderRepository.existsById(reminder.getReminderId())) {
            throw new IllegalArgumentException("Reminder not found");
        }
        return reminderRepository.save(reminder);
    }

    // Delete reminder
    public void deleteReminder(Long reminderId) {
        reminderRepository.deleteById(reminderId);
    }

    // Activate reminder
    public Reminder activateReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        reminder.activate();
        return reminderRepository.save(reminder);
    }

    // Mark reminder as completed
    public Reminder markReminderCompleted(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        reminder.markCompleted();
        
        // Calculate next reminder using strategy
        setStrategyBasedOnFrequency(reminder.getFrequency());
        if (reminderStrategy != null) {
            LocalDateTime nextTime = reminderStrategy.calculateNextReminderTime(reminder);
            reminder.setNextScheduledTime(nextTime);

            
        }
        
        return reminderRepository.save(reminder);
    }

    // Mark reminder as missed
    public Reminder markReminderMissed(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        reminder.markMissed();
        return reminderRepository.save(reminder);
    }

    // Cancel reminder
    public Reminder cancelReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        reminder.cancel();
        return reminderRepository.save(reminder);
    }

    // Get reminders by frequency
    public List<Reminder> getRemindersByFrequency(String frequency) {
        return reminderRepository.findByFrequency(frequency);
    }

    // Get reminders in time range
    public List<Reminder> getRemindersInTimeRange(LocalTime startTime, LocalTime endTime) {
        return reminderRepository.findByReminderTimeBetween(startTime, endTime);
    }

    // Get reminders in date range
    public List<Reminder> getRemindersInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return reminderRepository.findRemindersInDateRange(startDate, endDate);
    }

    // Count reminders by status
    public Long countRemindersByStatus(String status) {
        return reminderRepository.countByStatus(status);
    }

    // Process due reminders (batch operation)
    public void processDueReminders() {
        List<Reminder> dueReminders = getDueReminders();
        for (Reminder reminder : dueReminders) {
            if (reminder.shouldTrigger()) {
                reminder.activate();
                reminderRepository.save(reminder);
            }
        }
    }

    // Set reminder strategy based on frequency
    private void setStrategyBasedOnFrequency(String frequency) {
        switch (frequency.toUpperCase()) {
            case "DAILY":
            case "ONCE_DAILY":
                this.reminderStrategy = new DailyReminderStrategy();
                break;
            case "ALTERNATE_DAY":
                this.reminderStrategy = new AlternateDayReminderStrategy();
                break;
            default:
                this.reminderStrategy = new DailyReminderStrategy();
                break;
        }
    }

    // Get reminder statistics
    public ReminderStatistics getReminderStatistics(Long userId) {
        long totalReminders = reminderRepository.findByFamilyMemberId(userId).size();
        long activeReminders = reminderRepository.findActiveRemindersByUserId(userId).size();
        long completedReminders = reminderRepository.findByStatusAndUserId("COMPLETED", userId).size();
        long missedReminders = reminderRepository.findMissedRemindersByUserId(userId).size();
        
        return new ReminderStatistics(totalReminders, activeReminders, completedReminders, missedReminders);
    }

    // Inner class for statistics
    public static class ReminderStatistics {
        private long totalReminders;
        private long activeReminders;
        private long completedReminders;
        private long missedReminders;

        public ReminderStatistics(long totalReminders, long activeReminders, 
                                  long completedReminders, long missedReminders) {
            this.totalReminders = totalReminders;
            this.activeReminders = activeReminders;
            this.completedReminders = completedReminders;
            this.missedReminders = missedReminders;
        }

        public long getTotalReminders() { return totalReminders; }
        public long getActiveReminders() { return activeReminders; }
        public long getCompletedReminders() { return completedReminders; }
        public long getMissedReminders() { return missedReminders; }
    }
}