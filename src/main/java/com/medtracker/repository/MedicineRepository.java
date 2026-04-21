package com.medtracker.repository;

import com.medtracker.model.Medicine;
import com.medtracker.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // Find by name (case-insensitive search)
    List<Medicine> findByNameContainingIgnoreCase(String name);

    // Find by brand
    List<Medicine> findByBrand(String brand);

    // Find by type
    List<Medicine> findByType(String type);

    // Find by inventory
    List<Medicine> findByInventory(Inventory inventory);

    // Find by inventory ID
    @Query("SELECT m FROM Medicine m WHERE m.inventory.inventoryId = :inventoryId")
    List<Medicine> findByInventoryId(@Param("inventoryId") Long inventoryId);

    // Find expired medicines
    @Query("SELECT m FROM Medicine m WHERE m.expiryDate < :currentDate")
    List<Medicine> findExpiredMedicines(@Param("currentDate") LocalDate currentDate);

    // Find medicines expiring soon (within specified days)
    @Query("SELECT m FROM Medicine m WHERE m.expiryDate BETWEEN :startDate AND :endDate")
    List<Medicine> findMedicinesExpiringSoon(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    // Find by name and brand
    Optional<Medicine> findByNameAndBrand(String name, String brand);

    // Find medicines requiring prescription
    List<Medicine> findByRequiresPrescriptionTrue();

    // Search medicines by name or brand
    @Query("SELECT m FROM Medicine m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(m.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Medicine> searchByNameOrBrand(@Param("searchTerm") String searchTerm);

    // Count medicines by type
    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.type = :type")
    Long countByType(@Param("type") String type);

    // Find medicines by batch number
    List<Medicine> findByBatchNumber(String batchNumber);

    // Get all medicine types
    @Query("SELECT DISTINCT m.type FROM Medicine m")
    List<String> findAllMedicineTypes();

    // Get all medicines with inventory eagerly loaded
    @Query("SELECT m FROM Medicine m LEFT JOIN FETCH m.inventory")
    List<Medicine> findAllWithInventory();
}