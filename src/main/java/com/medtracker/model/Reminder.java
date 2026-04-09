package com.medtracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reminderId;

    @NotNull(message = "Reminder time is required")
    @Column(nullable = false)
    private LocalTime reminderTime;

    @Column(nullable = false)
    private String frequency;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Column
    private LocalDateTime nextScheduledTime;

    @Min(value = 1, message = "Dosage must be at least 1")
    @Column(nullable = false)
    private Integer dosage = 1;

    @Column(length = 500)
    private String notes;

    /**
     * Optional email address supplied by the user when creating the reminder.
     * If provided, scheduled reminder emails are sent to this address instead of
     * (or in addition to) the family-member's registered email.
     */
    @Column(length = 200)
    private String notificationEmail;

    @Column
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id", nullable = false)
    @JsonIgnore
    private FamilyMember familyMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    @JsonIgnore
    private Medicine medicine;

    public Reminder() {
        this.startDate = LocalDateTime.now();
    }

    public Reminder(LocalTime reminderTime, String frequency, 
                    FamilyMember familyMember, Medicine medicine) {
        this();
        this.reminderTime = reminderTime;
        this.frequency = frequency;
        this.familyMember = familyMember;
        this.medicine = medicine;
        calculateNextScheduledTime();
    }

    // Getters and Setters
    public Long getReminderId() {
        return reminderId;
    }

    public void setReminderId(Long reminderId) {
        this.reminderId = reminderId;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
        calculateNextScheduledTime();
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
        calculateNextScheduledTime();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getNextScheduledTime() {
        return nextScheduledTime;
    }

    public void setNextScheduledTime(LocalDateTime nextScheduledTime) {
        this.nextScheduledTime = nextScheduledTime;
    }

    public Integer getDosage() {
        return dosage;
    }

    public void setDosage(Integer dosage) {
        this.dosage = dosage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public FamilyMember getFamilyMember() {
        return familyMember;
    }

    public void setFamilyMember(FamilyMember familyMember) {
        this.familyMember = familyMember;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    // Business Methods - State Transitions
    public void activate() {
        this.status = "ACTIVE";
    }

    public void markCompleted() {
        this.status = "COMPLETED";
        calculateNextScheduledTime();
    }

    public void markMissed() {
        this.status = "MISSED";
        calculateNextScheduledTime();
    }

    public void cancel() {
        this.status = "CANCELLED";
        this.isActive = false;
    }

    public boolean shouldTrigger() {
        if (!isActive || "CANCELLED".equals(status)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return nextScheduledTime != null && !now.isBefore(nextScheduledTime);
    }

    private void calculateNextScheduledTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.toLocalDate().atTime(reminderTime);
        if (next.isBefore(now)) {
            next = next.plusDays(1);
        }
        this.nextScheduledTime = next;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "reminderId=" + reminderId +
                ", medicine=" + (medicine != null ? medicine.getName() : "null") +
                ", time=" + reminderTime +
                ", frequency='" + frequency + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}