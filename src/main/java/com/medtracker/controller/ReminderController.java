package com.medtracker.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medtracker.model.Reminder;
import com.medtracker.dto.ReminderDTO;
import com.medtracker.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for Reminder Management
 * Handles all reminder-related HTTP requests
 * Uses Strategy Pattern for reminder scheduling
 */
@RestController
@RequestMapping("/api/reminders")
@CrossOrigin(origins = "*")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    // Create reminder from web form (familyMemberId, medicineId, reminderTime, frequency, dosage, notes)
    @PostMapping("/create")
    public ResponseEntity<?> createReminderFromForm(@RequestBody ReminderRequest request) {
        try {
            // Debug: Log incoming request
            System.out.println("=== REMINDER REQUEST DEBUG ===");
            System.out.println("Request object: " + request);
            System.out.println("familyMemberId: " + request.getFamilyMemberId());
            System.out.println("medicineId: " + request.getMedicineId());
            System.out.println("reminderTime: " + request.getReminderTime());
            System.out.println("frequency: " + request.getFrequency());
            System.out.println("dosage: " + request.getDosage());
            System.out.println("=== END DEBUG ===");
            
            // Explicit null check
            if (request.getFamilyMemberId() == null) {
                System.out.println("ERROR: familyMemberId is null!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("familyMemberId cannot be null. Please select a family member.");
            }
            if (request.getMedicineId() == null) {
                System.out.println("ERROR: medicineId is null!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("medicineId cannot be null. Please select a medicine.");
            }
            
            LocalTime time = request.getReminderTime() != null 
                    ? LocalTime.parse(request.getReminderTime()) 
                    : LocalTime.of(8, 0);
            Reminder created = reminderService.createReminderFromIds(
                    request.getFamilyMemberId(),
                    request.getMedicineId(),
                    time,
                    request.getFrequency() != null ? request.getFrequency() : "DAILY",
                    request.getDosage() != null ? request.getDosage() : 1,
                    request.getNotes(),
                    request.getNotificationEmail()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating reminder: " + e.getMessage());
        }
    }

    // Create new reminder
    @PostMapping
    public ResponseEntity<?> createReminder(@RequestBody Reminder reminder) {
        try {
            Reminder createdReminder = reminderService.createReminder(reminder);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReminder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating reminder: " + e.getMessage());
        }
    }

    // Get reminder by ID
    @GetMapping("/{reminderId}")
    public ResponseEntity<?> getReminderById(@PathVariable Long reminderId) {
        try {
            Optional<Reminder> reminder = reminderService.getReminderById(reminderId);
            if (reminder.isPresent()) {
                return ResponseEntity.ok(reminder.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Reminder not found with ID: " + reminderId);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching reminder: " + e.getMessage());
        }
    }

    // Get all reminders
    @GetMapping
    public ResponseEntity<?> getAllReminders() {
        try {
            List<Reminder> reminders = reminderService.getAllReminders();
            List<ReminderDTO> reminderDTOs = reminders.stream()
                    .map(ReminderDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reminderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching reminders: " + e.getMessage());
        }
    }

    // Get reminders by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRemindersByUserId(@PathVariable Long userId) {
        try {
            List<Reminder> reminders = reminderService.getRemindersByUserId(userId);
            List<ReminderDTO> reminderDTOs = reminders.stream()
                    .map(ReminderDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reminderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching user reminders: " + e.getMessage());
        }
    }

    // Get reminders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getRemindersByStatus(@PathVariable String status) {
        try {
            List<Reminder> reminders = reminderService.getRemindersByStatus(status);
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching reminders by status: " + e.getMessage());
        }
    }

    // Get active reminders
    @GetMapping("/active")
    public ResponseEntity<?> getActiveReminders() {
        try {
            List<Reminder> reminders = reminderService.getActiveReminders();
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching active reminders: " + e.getMessage());
        }
    }

    // Get pending reminders
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingReminders() {
        try {
            List<Reminder> reminders = reminderService.getPendingReminders();
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching pending reminders: " + e.getMessage());
        }
    }

    // Get due reminders
    @GetMapping("/due")
    public ResponseEntity<?> getDueReminders() {
        try {
            List<Reminder> reminders = reminderService.getDueReminders();
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching due reminders: " + e.getMessage());
        }
    }

    // Get today's reminders for user
    @GetMapping("/user/{userId}/today")
    public ResponseEntity<?> getTodaysRemindersForUser(@PathVariable Long userId) {
        try {
            List<Reminder> reminders = reminderService.getTodaysRemindersForUser(userId);
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching today's reminders: " + e.getMessage());
        }
    }

    // Get missed reminders for user
    @GetMapping("/user/{userId}/missed")
    public ResponseEntity<?> getMissedRemindersForUser(@PathVariable Long userId) {
        try {
            List<Reminder> reminders = reminderService.getMissedRemindersForUser(userId);
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching missed reminders: " + e.getMessage());
        }
    }

    // Get reminders by frequency
    @GetMapping("/frequency/{frequency}")
    public ResponseEntity<?> getRemindersByFrequency(@PathVariable String frequency) {
        try {
            List<Reminder> reminders = reminderService.getRemindersByFrequency(frequency);
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching reminders by frequency: " + e.getMessage());
        }
    }

    // Update reminder
    @PutMapping("/{reminderId}")
    public ResponseEntity<?> updateReminder(@PathVariable Long reminderId, @RequestBody Reminder reminder) {
        try {
            reminder.setReminderId(reminderId);
            Reminder updatedReminder = reminderService.updateReminder(reminder);
            return ResponseEntity.ok(updatedReminder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating reminder: " + e.getMessage());
        }
    }

    // Delete reminder
    @DeleteMapping("/{reminderId}")
    public ResponseEntity<?> deleteReminder(@PathVariable Long reminderId) {
        try {
            reminderService.deleteReminder(reminderId);
            return ResponseEntity.ok("Reminder deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting reminder: " + e.getMessage());
        }
    }

    // Activate reminder
    @PutMapping("/{reminderId}/activate")
    public ResponseEntity<?> activateReminder(@PathVariable Long reminderId) {
        try {
            Reminder reminder = reminderService.activateReminder(reminderId);
            return ResponseEntity.ok(reminder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating reminder: " + e.getMessage());
        }
    }

    // Mark reminder as completed
    @PutMapping("/{reminderId}/complete")
    public ResponseEntity<?> markReminderCompleted(@PathVariable Long reminderId) {
        try {
            Reminder reminder = reminderService.markReminderCompleted(reminderId);
            return ResponseEntity.ok(reminder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error completing reminder: " + e.getMessage());
        }
    }

    // Mark reminder as missed
    @PutMapping("/{reminderId}/missed")
    public ResponseEntity<?> markReminderMissed(@PathVariable Long reminderId) {
        try {
            Reminder reminder = reminderService.markReminderMissed(reminderId);
            return ResponseEntity.ok(reminder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking reminder as missed: " + e.getMessage());
        }
    }

    // Cancel reminder
    @PutMapping("/{reminderId}/cancel")
    public ResponseEntity<?> cancelReminder(@PathVariable Long reminderId) {
        try {
            Reminder reminder = reminderService.cancelReminder(reminderId);
            return ResponseEntity.ok(reminder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling reminder: " + e.getMessage());
        }
    }

    // Process due reminders (batch operation)
    @PostMapping("/process-due")
    public ResponseEntity<?> processDueReminders() {
        try {
            reminderService.processDueReminders();
            return ResponseEntity.ok("Due reminders processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing reminders: " + e.getMessage());
        }
    }

    // Get reminder statistics for user
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<?> getReminderStatistics(@PathVariable Long userId) {
        try {
            ReminderService.ReminderStatistics stats = reminderService.getReminderStatistics(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching statistics: " + e.getMessage());
        }
    }

    // Count reminders by status
    @GetMapping("/count/status/{status}")
    public ResponseEntity<?> countRemindersByStatus(@PathVariable String status) {
        try {
            Long count = reminderService.countRemindersByStatus(status);
            return ResponseEntity.ok(new CountResponse(count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error counting reminders: " + e.getMessage());
        }
    }

    // Inner class for request
    public static class ReminderRequest {
        @JsonProperty("familyMemberId")
        private Long familyMemberId;
        @JsonProperty("medicineId")
        private Long medicineId;
        @JsonProperty("reminderTime")
        private String reminderTime;  // HH:mm format
        @JsonProperty("frequency")
        private String frequency;
        @JsonProperty("dosage")
        private Integer dosage;
        @JsonProperty("notes")
        private String notes;
        @JsonProperty("notificationEmail")
        private String notificationEmail;
        
        public Long getFamilyMemberId() { return familyMemberId; }
        public void setFamilyMemberId(Long familyMemberId) { this.familyMemberId = familyMemberId; }
        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public String getReminderTime() { return reminderTime; }
        public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public Integer getDosage() { return dosage; }
        public void setDosage(Integer dosage) { this.dosage = dosage; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getNotificationEmail() { return notificationEmail; }
        public void setNotificationEmail(String notificationEmail) { this.notificationEmail = notificationEmail; }
    }

    // Inner class for response
    public static class CountResponse {
        private Long count;

        public CountResponse(Long count) { this.count = count; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}