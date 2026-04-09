package com.medtracker.repository;

import com.medtracker.model.Reminder;
import com.medtracker.model.FamilyMember;
import com.medtracker.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    // Find by family member
    List<Reminder> findByFamilyMember(FamilyMember familyMember);

    // Find by family member ID
    @Query("SELECT r FROM Reminder r WHERE r.familyMember.userId = :userId")
    List<Reminder> findByFamilyMemberId(@Param("userId") Long userId);

    // Find by medicine
    List<Reminder> findByMedicine(Medicine medicine);

    // Find by status
    List<Reminder> findByStatus(String status);

    // Find active reminders
    List<Reminder> findByIsActiveTrue();

    // Find by frequency
    List<Reminder> findByFrequency(String frequency);

    // Find reminders due now or past due
    @Query("SELECT r FROM Reminder r WHERE r.nextScheduledTime <= :currentTime AND r.isActive = true")
    List<Reminder> findDueReminders(@Param("currentTime") LocalDateTime currentTime);

    // Find reminders by time range
    @Query("SELECT r FROM Reminder r WHERE r.reminderTime BETWEEN :startTime AND :endTime")
    List<Reminder> findByReminderTimeBetween(@Param("startTime") LocalTime startTime, 
                                              @Param("endTime") LocalTime endTime);

    // Find active reminders for a family member
    @Query("SELECT r FROM Reminder r WHERE r.familyMember.userId = :userId AND r.isActive = true")
    List<Reminder> findActiveRemindersByUserId(@Param("userId") Long userId);

    // Find reminders by status and family member
    @Query("SELECT r FROM Reminder r WHERE r.status = :status AND r.familyMember.userId = :userId")
    List<Reminder> findByStatusAndUserId(@Param("status") String status, @Param("userId") Long userId);

    // Find pending reminders
    @Query("SELECT r FROM Reminder r WHERE r.status = 'PENDING' AND r.isActive = true")
    List<Reminder> findPendingReminders();

    // Count reminders by status
    @Query("SELECT COUNT(r) FROM Reminder r WHERE r.status = :status")
    Long countByStatus(@Param("status") String status);

    // Find reminders scheduled between dates
    @Query("SELECT r FROM Reminder r WHERE r.nextScheduledTime BETWEEN :startDate AND :endDate")
    List<Reminder> findRemindersInDateRange(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    // Find today's reminders for a user
    @Query("SELECT r FROM Reminder r WHERE r.familyMember.userId = :userId " +
           "AND r.nextScheduledTime >= :startOfDay AND r.nextScheduledTime < :endOfDay AND r.isActive = true")
    List<Reminder> findTodaysRemindersByUserId(@Param("userId") Long userId, 
                                                @Param("startOfDay") LocalDateTime startOfDay,
                                                @Param("endOfDay") LocalDateTime endOfDay);

    // Find missed reminders
    @Query("SELECT r FROM Reminder r WHERE r.status = 'MISSED' AND r.familyMember.userId = :userId")
    List<Reminder> findMissedRemindersByUserId(@Param("userId") Long userId);

    // Find reminders by medicine ID
    @Query("SELECT r FROM Reminder r WHERE r.medicine.medicineId = :medicineId")
    List<Reminder> findByMedicineId(@Param("medicineId") Long medicineId);
}