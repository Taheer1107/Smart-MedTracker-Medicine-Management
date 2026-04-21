package com.medtracker.repository;

import com.medtracker.model.Inventory;
import com.medtracker.model.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Find by household
    Optional<Inventory> findByHousehold(Household household);

    // Find by household ID
    @Query("SELECT i FROM Inventory i WHERE i.household.householdId = :householdId")
    Optional<Inventory> findByHouseholdId(@Param("householdId") Long householdId);

    // Find low stock inventories
    @Query("SELECT i FROM Inventory i WHERE i.currentQuantity <= i.minQuantity")
    List<Inventory> findLowStockInventories();

    // Find overstock inventories
    @Query("SELECT i FROM Inventory i WHERE i.currentQuantity >= i.maxQuantity")
    List<Inventory> findOverStockInventories();

    // Find inventories by quantity range
    @Query("SELECT i FROM Inventory i WHERE i.currentQuantity BETWEEN :minQty AND :maxQty")
    List<Inventory> findByQuantityRange(@Param("minQty") Integer minQty, @Param("maxQty") Integer maxQty);

    // Count total medicines in inventory
    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.inventory.inventoryId = :inventoryId")
    Long countMedicinesInInventory(@Param("inventoryId") Long inventoryId);

    // Find inventories with medicines count
    @Query("SELECT i FROM Inventory i WHERE SIZE(i.medicines) > 0")
    List<Inventory> findInventoriesWithMedicines();

    // Find empty inventories
    @Query("SELECT i FROM Inventory i WHERE SIZE(i.medicines) = 0")
    List<Inventory> findEmptyInventories();

    // Get average inventory quantity
    @Query("SELECT AVG(i.currentQuantity) FROM Inventory i")
    Double getAverageInventoryQuantity();

    // Check if inventory exists for household
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inventory i WHERE i.household.householdId = :householdId")
    boolean existsByHouseholdId(@Param("householdId") Long householdId);

    // Find inventories needing restock
    @Query("SELECT i FROM Inventory i WHERE i.currentQuantity < i.minQuantity + 5")
    List<Inventory> findInventoriesNeedingRestock();

    // Get total inventory value (if medicines have prices)
    @Query("SELECT SUM(m.pricePerUnit) FROM Medicine m WHERE m.inventory.inventoryId = :inventoryId")
    Double getTotalInventoryValue(@Param("inventoryId") Long inventoryId);
}