package com.medtracker.controller;

import com.medtracker.model.Inventory;
import com.medtracker.model.Medicine;
import com.medtracker.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Inventory Management
 * Handles all inventory-related HTTP requests
 */
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // Create new inventory
    @PostMapping
    public ResponseEntity<?> createInventory(@RequestBody Inventory inventory) {
        try {
            Inventory createdInventory = inventoryService.createInventory(inventory);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInventory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating inventory: " + e.getMessage());
        }
    }

    // Get inventory by ID
    @GetMapping("/{inventoryId}")
    public ResponseEntity<?> getInventoryById(@PathVariable Long inventoryId) {
        try {
            Optional<Inventory> inventory = inventoryService.getInventoryById(inventoryId);
            if (inventory.isPresent()) {
                return ResponseEntity.ok(inventory.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Inventory not found with ID: " + inventoryId);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching inventory: " + e.getMessage());
        }
    }

    // Get all inventories
    @GetMapping
    public ResponseEntity<?> getAllInventories() {
        try {
            List<Inventory> inventories = inventoryService.getAllInventories();
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching inventories: " + e.getMessage());
        }
    }

    // Get inventory by household ID
    @GetMapping("/household/{householdId}")
    public ResponseEntity<?> getInventoryByHouseholdId(@PathVariable Long householdId) {
        try {
            Optional<Inventory> inventory = inventoryService.getInventoryByHouseholdId(householdId);
            if (inventory.isPresent()) {
                return ResponseEntity.ok(inventory.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Inventory not found for household ID: " + householdId);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching inventory: " + e.getMessage());
        }
    }

    // Get low stock inventories
    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockInventories() {
        try {
            List<Inventory> inventories = inventoryService.getLowStockInventories();
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching low stock inventories: " + e.getMessage());
        }
    }

    // Get overstock inventories
    @GetMapping("/overstock")
    public ResponseEntity<?> getOverStockInventories() {
        try {
            List<Inventory> inventories = inventoryService.getOverStockInventories();
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching overstock inventories: " + e.getMessage());
        }
    }

    // Get inventories needing restock
    @GetMapping("/needs-restock")
    public ResponseEntity<?> getInventoriesNeedingRestock() {
        try {
            List<Inventory> inventories = inventoryService.getInventoriesNeedingRestock();
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching inventories needing restock: " + e.getMessage());
        }
    }

    // Get inventories with medicines
    @GetMapping("/with-medicines")
    public ResponseEntity<?> getInventoriesWithMedicines() {
        try {
            List<Inventory> inventories = inventoryService.getInventoriesWithMedicines();
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching inventories: " + e.getMessage());
        }
    }

    // Get empty inventories
    @GetMapping("/empty")
    public ResponseEntity<?> getEmptyInventories() {
        try {
            List<Inventory> inventories = inventoryService.getEmptyInventories();
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching empty inventories: " + e.getMessage());
        }
    }

    // Update inventory
    @PutMapping("/{inventoryId}")
    public ResponseEntity<?> updateInventory(@PathVariable Long inventoryId, @RequestBody Inventory inventory) {
        try {
            inventory.setInventoryId(inventoryId);
            Inventory updatedInventory = inventoryService.updateInventory(inventory);
            return ResponseEntity.ok(updatedInventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating inventory: " + e.getMessage());
        }
    }

    // Delete inventory
    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<?> deleteInventory(@PathVariable Long inventoryId) {
        try {
            inventoryService.deleteInventory(inventoryId);
            return ResponseEntity.ok("Inventory deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting inventory: " + e.getMessage());
        }
    }

    // Add quantity to inventory
    @PutMapping("/{inventoryId}/add-quantity")
    public ResponseEntity<?> addQuantity(@PathVariable Long inventoryId, @RequestParam int amount) {
        try {
            Inventory inventory = inventoryService.addQuantity(inventoryId, amount);
            return ResponseEntity.ok(inventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding quantity: " + e.getMessage());
        }
    }

    // Reduce quantity from inventory
    @PutMapping("/{inventoryId}/reduce-quantity")
    public ResponseEntity<?> reduceQuantity(@PathVariable Long inventoryId, @RequestParam int amount) {
        try {
            Inventory inventory = inventoryService.reduceQuantity(inventoryId, amount);
            return ResponseEntity.ok(inventory);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reducing quantity: " + e.getMessage());
        }
    }

    // Update inventory thresholds
    @PutMapping("/{inventoryId}/thresholds")
    public ResponseEntity<?> updateInventoryThresholds(@PathVariable Long inventoryId, 
                                                       @RequestParam Integer minQuantity, 
                                                       @RequestParam Integer maxQuantity) {
        try {
            Inventory inventory = inventoryService.updateInventoryThresholds(inventoryId, minQuantity, maxQuantity);
            return ResponseEntity.ok(inventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating thresholds: " + e.getMessage());
        }
    }

    // Get expired medicines in inventory
    @GetMapping("/{inventoryId}/expired-medicines")
    public ResponseEntity<?> getExpiredMedicinesInInventory(@PathVariable Long inventoryId) {
        try {
            List<Medicine> medicines = inventoryService.getExpiredMedicinesInInventory(inventoryId);
            return ResponseEntity.ok(medicines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching expired medicines: " + e.getMessage());
        }
    }

    // Check if inventory is low stock
    @GetMapping("/{inventoryId}/is-low-stock")
    public ResponseEntity<?> isInventoryLowStock(@PathVariable Long inventoryId) {
        try {
            boolean isLowStock = inventoryService.isInventoryLowStock(inventoryId);
            return ResponseEntity.ok(new BooleanResponse(isLowStock));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking stock: " + e.getMessage());
        }
    }

    // Check if inventory is overstock
    @GetMapping("/{inventoryId}/is-overstock")
    public ResponseEntity<?> isInventoryOverStock(@PathVariable Long inventoryId) {
        try {
            boolean isOverStock = inventoryService.isInventoryOverStock(inventoryId);
            return ResponseEntity.ok(new BooleanResponse(isOverStock));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking stock: " + e.getMessage());
        }
    }

    // Count medicines in inventory
    @GetMapping("/{inventoryId}/medicine-count")
    public ResponseEntity<?> countMedicinesInInventory(@PathVariable Long inventoryId) {
        try {
            Long count = inventoryService.countMedicinesInInventory(inventoryId);
            return ResponseEntity.ok(new CountResponse(count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error counting medicines: " + e.getMessage());
        }
    }

    // Get total inventory value
    @GetMapping("/{inventoryId}/total-value")
    public ResponseEntity<?> getTotalInventoryValue(@PathVariable Long inventoryId) {
        try {
            Double value = inventoryService.getTotalInventoryValue(inventoryId);
            return ResponseEntity.ok(new ValueResponse(value != null ? value : 0.0));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calculating value: " + e.getMessage());
        }
    }

    // Get average inventory quantity
    @GetMapping("/average-quantity")
    public ResponseEntity<?> getAverageInventoryQuantity() {
        try {
            Double average = inventoryService.getAverageInventoryQuantity();
            return ResponseEntity.ok(new ValueResponse(average != null ? average : 0.0));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calculating average: " + e.getMessage());
        }
    }

    // Inner classes for responses
    public static class BooleanResponse {
        private boolean value;

        public BooleanResponse(boolean value) { this.value = value; }
        public boolean isValue() { return value; }
        public void setValue(boolean value) { this.value = value; }
    }

    public static class CountResponse {
        private Long count;

        public CountResponse(Long count) { this.count = count; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }

    public static class ValueResponse {
        private Double value;

        public ValueResponse(Double value) { this.value = value; }
        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
    }
}