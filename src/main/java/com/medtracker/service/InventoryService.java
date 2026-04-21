package com.medtracker.service;

import com.medtracker.model.Inventory;
import com.medtracker.model.Medicine;
import com.medtracker.model.Household;
import com.medtracker.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    // Create inventory
    public Inventory createInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    // Get inventory by ID
    public Optional<Inventory> getInventoryById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId);
    }

    // Get all inventories
    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    // Get inventory by household
    public Optional<Inventory> getInventoryByHousehold(Household household) {
        return inventoryRepository.findByHousehold(household);
    }

    // Get inventory by household ID
    public Optional<Inventory> getInventoryByHouseholdId(Long householdId) {
        return inventoryRepository.findByHouseholdId(householdId);
    }

    // Update inventory
    public Inventory updateInventory(Inventory inventory) {
        if (!inventoryRepository.existsById(inventory.getInventoryId())) {
            throw new IllegalArgumentException("Inventory not found");
        }
        return inventoryRepository.save(inventory);
    }

    // Delete inventory
    public void deleteInventory(Long inventoryId) {
        inventoryRepository.deleteById(inventoryId);
    }

    // Add medicine to inventory
    public Inventory addMedicineToInventory(Long inventoryId, Medicine medicine) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        inventory.addMedicine(medicine);
        return inventoryRepository.save(inventory);
    }

    // Remove medicine from inventory
    public Inventory removeMedicineFromInventory(Long inventoryId, Medicine medicine) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        inventory.removeMedicine(medicine);
        return inventoryRepository.save(inventory);
    }

    // Add quantity to inventory
    public Inventory addQuantity(Long inventoryId, int amount) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        inventory.addQuantity(amount);
        return inventoryRepository.save(inventory);
    }

    // Reduce quantity from inventory
    public Inventory reduceQuantity(Long inventoryId, int amount) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        inventory.reduceQuantity(amount);
        return inventoryRepository.save(inventory);
    }

    // Get low stock inventories
    public List<Inventory> getLowStockInventories() {
        return inventoryRepository.findLowStockInventories();
    }

    // Get overstock inventories
    public List<Inventory> getOverStockInventories() {
        return inventoryRepository.findOverStockInventories();
    }

    // Get inventories needing restock
    public List<Inventory> getInventoriesNeedingRestock() {
        return inventoryRepository.findInventoriesNeedingRestock();
    }

    // Get inventories with medicines
    public List<Inventory> getInventoriesWithMedicines() {
        return inventoryRepository.findInventoriesWithMedicines();
    }

    // Get empty inventories
    public List<Inventory> getEmptyInventories() {
        return inventoryRepository.findEmptyInventories();
    }

    // Get inventories by quantity range
    public List<Inventory> getInventoriesByQuantityRange(Integer minQty, Integer maxQty) {
        return inventoryRepository.findByQuantityRange(minQty, maxQty);
    }

    // Count medicines in inventory
    public Long countMedicinesInInventory(Long inventoryId) {
        return inventoryRepository.countMedicinesInInventory(inventoryId);
    }

    // Get total inventory value
    public Double getTotalInventoryValue(Long inventoryId) {
        return inventoryRepository.getTotalInventoryValue(inventoryId);
    }

    // Get average inventory quantity
    public Double getAverageInventoryQuantity() {
        return inventoryRepository.getAverageInventoryQuantity();
    }

    // Check if inventory exists for household
    public boolean inventoryExistsForHousehold(Long householdId) {
        return inventoryRepository.existsByHouseholdId(householdId);
    }

    // Get expired medicines in inventory
    public List<Medicine> getExpiredMedicinesInInventory(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        return inventory.getExpiredMedicines();
    }

    // Check if inventory is low stock
    public boolean isInventoryLowStock(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        return inventory.isLowStock();
    }

    // Check if inventory is overstock
    public boolean isInventoryOverStock(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        return inventory.isOverStock();
    }

    // Update inventory thresholds
    public Inventory updateInventoryThresholds(Long inventoryId, Integer minQuantity, Integer maxQuantity) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        inventory.setMinQuantity(minQuantity);
        inventory.setMaxQuantity(maxQuantity);
        return inventoryRepository.save(inventory);
    }
}