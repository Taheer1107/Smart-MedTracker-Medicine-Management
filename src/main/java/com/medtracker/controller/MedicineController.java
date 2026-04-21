package com.medtracker.controller;

import com.medtracker.model.Medicine;
import com.medtracker.model.Inventory;
import com.medtracker.model.UsageHistory;
import com.medtracker.model.User;
import com.medtracker.factory.MedicineFactory;
import com.medtracker.decorator.MedicineDisplayFactory;
import com.medtracker.decorator.MedicineDisplayInfo;
import com.medtracker.service.MedicineService;
import com.medtracker.service.InventoryService;
import com.medtracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Medicine Management
 * Handles all medicine-related HTTP requests
 * Uses MedicineFactory for creating medicines
 */
@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = "*")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserService userService;

    // Add new medicine (using Factory Pattern)
    @PostMapping
    public ResponseEntity<?> addMedicine(@RequestBody MedicineRequest request) {
        try {
            Medicine medicine = MedicineFactory.createMedicine(
                request.getType(), 
                request.getName(), 
                request.getBrand()
            );
            
            // Set additional properties
            if (request.getDescription() != null) {
                medicine.setDescription(request.getDescription());
            }
            if (request.getExpiryDate() != null) {
                medicine.setExpiryDate(request.getExpiryDate());
            }
            if (request.getBatchNumber() != null) {
                medicine.setBatchNumber(request.getBatchNumber());
            }
            if (request.getPricePerUnit() != null) {
                medicine.setPricePerUnit(request.getPricePerUnit());
            }
            medicine.setRequiresPrescription(request.isRequiresPrescription());
            
            // Handle inventory if provided
            if (request.getInventory() != null) {
                Inventory inventory = new Inventory();
                if (request.getInventory().getCurrentQuantity() != null) {
                    inventory.setCurrentQuantity(request.getInventory().getCurrentQuantity());
                }
                if (request.getInventory().getMinQuantity() != null) {
                    inventory.setMinQuantity(request.getInventory().getMinQuantity());
                }
                if (request.getInventory().getMaxQuantity() != null) {
                    inventory.setMaxQuantity(request.getInventory().getMaxQuantity());
                }
                Inventory savedInventory = inventoryService.createInventory(inventory);
                medicine.setInventory(savedInventory);
            }
            
            Medicine savedMedicine = medicineService.addMedicine(medicine);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMedicine);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding medicine: " + e.getMessage());
        }
    }

    // Get medicine by ID
    @GetMapping("/{medicineId}")
    public ResponseEntity<?> getMedicineById(@PathVariable Long medicineId) {
        try {
            Optional<Medicine> medicine = medicineService.getMedicineById(medicineId);
            if (medicine.isPresent()) {
                return ResponseEntity.ok(medicine.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Medicine not found with ID: " + medicineId);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching medicine: " + e.getMessage());
        }
    }

    // Get all medicines
    @GetMapping
    public ResponseEntity<?> getAllMedicines() {
        try {
            List<Medicine> medicines = medicineService.getAllMedicines();
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            System.err.println("Error fetching medicines: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching medicines: " + e.getMessage());
        }
    }

    // Search medicines by name
    @GetMapping("/search")
    public ResponseEntity<?> searchMedicines(@RequestParam String searchTerm) {
        try {
            List<Medicine> medicines = medicineService.searchMedicines(searchTerm);
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching medicines: " + e.getMessage());
        }
    }

    // Get medicines by type
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getMedicinesByType(@PathVariable String type) {
        try {
            List<Medicine> medicines = medicineService.getMedicinesByType(type);
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching medicines by type: " + e.getMessage());
        }
    }

    // Get medicines by brand
    @GetMapping("/brand/{brand}")
    public ResponseEntity<?> getMedicinesByBrand(@PathVariable String brand) {
        try {
            List<Medicine> medicines = medicineService.getMedicinesByBrand(brand);
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching medicines by brand: " + e.getMessage());
        }
    }

    // Get medicines by inventory ID
    @GetMapping("/inventory/{inventoryId}")
    public ResponseEntity<?> getMedicinesByInventoryId(@PathVariable Long inventoryId) {
        try {
            List<Medicine> medicines = medicineService.getMedicinesByInventoryId(inventoryId);
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching medicines by inventory: " + e.getMessage());
        }
    }

    // Get expired medicines
    @GetMapping("/expired")
    public ResponseEntity<?> getExpiredMedicines() {
        try {
            List<Medicine> medicines = medicineService.getExpiredMedicines();
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching expired medicines: " + e.getMessage());
        }
    }

    // Get medicines expiring soon
    @GetMapping("/expiring-soon")
    public ResponseEntity<?> getMedicinesExpiringSoon(@RequestParam(defaultValue = "30") int days) {
        try {
            List<Medicine> medicines = medicineService.getMedicinesExpiringSoon(days);
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching medicines expiring soon: " + e.getMessage());
        }
    }

    // Get medicines requiring prescription
    @GetMapping("/prescription-required")
    public ResponseEntity<?> getMedicinesRequiringPrescription() {
        try {
            List<Medicine> medicines = medicineService.getMedicinesRequiringPrescription();
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching prescription medicines: " + e.getMessage());
        }
    }

    // Update medicine
    @PutMapping("/{medicineId}")
    public ResponseEntity<?> updateMedicine(@PathVariable Long medicineId, @RequestBody Medicine medicine) {
        try {
            medicine.setMedicineId(medicineId);
            Medicine updatedMedicine = medicineService.updateMedicine(medicine);
            return ResponseEntity.ok(updatedMedicine);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating medicine: " + e.getMessage());
        }
    }

    // Delete medicine
    @DeleteMapping("/{medicineId}")
    public ResponseEntity<?> deleteMedicine(@PathVariable Long medicineId) {
        try {
            medicineService.deleteMedicine(medicineId);
            return ResponseEntity.ok("Medicine deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting medicine: " + e.getMessage());
        }
    }

    // Get all medicine types
    @GetMapping("/types")
    public ResponseEntity<?> getAllMedicineTypes() {
        try {
            List<String> types = medicineService.getAllMedicineTypes();
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching medicine types: " + e.getMessage());
        }
    }

    // Get medicine statistics
    @GetMapping("/statistics")
    public ResponseEntity<?> getMedicineStatistics() {
        try {
            MedicineService.MedicineStatistics stats = medicineService.getMedicineStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching statistics: " + e.getMessage());
        }
    }

    // Check if medicine is expired
    @GetMapping("/{medicineId}/is-expired")
    public ResponseEntity<?> isMedicineExpired(@PathVariable Long medicineId) {
        try {
            boolean expired = medicineService.isMedicineExpired(medicineId);
            return ResponseEntity.ok(new BooleanResponse(expired));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking expiry: " + e.getMessage());
        }
    }

    // Record medicine usage
    @PostMapping("/{medicineId}/record-usage")
    public ResponseEntity<?> recordMedicineUsage(
            @PathVariable Long medicineId,
            @RequestParam Long userId,
            @RequestParam Double quantityUsed,
            @RequestParam(required = false) String notes) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            UsageHistory usage = medicineService.recordMedicineUsage(medicineId, user, quantityUsed, notes);
            return ResponseEntity.status(HttpStatus.CREATED).body(usage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error recording usage: " + e.getMessage());
        }
    }


    // ── Decorator Pattern endpoint ────────────────────────────────────────────
    /**
     * Returns a display-enriched view of a single medicine.
     *
     * DESIGN PATTERN — Decorator:
     *   Uses MedicineDisplayFactory.buildDisplay() to wrap the medicine in
     *   stacked decorators (ExpiryWarningDecorator, PrescriptionBadgeDecorator).
     *   The controller never knows which decorators are applied — it just asks
     *   for a "display" and returns the enriched result to the UI.
     *
     * GRASP — Controller:
     *   This method is a GRASP Controller: it receives the HTTP system event,
     *   coordinates with the service and decorator factory, and returns the
     *   result — containing zero business logic itself.
     */
    @GetMapping("/{medicineId}/display")
    public ResponseEntity<?> getMedicineDisplay(@PathVariable Long medicineId) {
        try {
            Optional<Medicine> medicineOpt = medicineService.getMedicineById(medicineId);
            if (medicineOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Medicine not found with ID: " + medicineId);
            }

            // Decorator Pattern: build the decorator chain for this medicine
            MedicineDisplayInfo display = MedicineDisplayFactory.buildDisplay(medicineOpt.get());

            MedicineDisplayResponse response = new MedicineDisplayResponse(
                display.getDisplayName(),
                display.getStatusBadge(),
                display.getAlertMessage(),
                display.getBadgeCssClass(),
                display.getSummary()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error building medicine display: " + e.getMessage());
        }
    }

    // DTO for Decorator pattern response
    public static class MedicineDisplayResponse {
        private String displayName;
        private String statusBadge;
        private String alertMessage;
        private String badgeCssClass;
        private String summary;

        public MedicineDisplayResponse(String displayName, String statusBadge,
                                       String alertMessage, String badgeCssClass,
                                       String summary) {
            this.displayName  = displayName;
            this.statusBadge  = statusBadge;
            this.alertMessage = alertMessage;
            this.badgeCssClass = badgeCssClass;
            this.summary      = summary;
        }

        public String getDisplayName()   { return displayName; }
        public String getStatusBadge()   { return statusBadge; }
        public String getAlertMessage()  { return alertMessage; }
        public String getBadgeCssClass() { return badgeCssClass; }
        public String getSummary()       { return summary; }
    }

    // Inner class for medicine request
    public static class MedicineRequest {
        private String type;
        private String name;
        private String brand;
        private String description;
        private java.time.LocalDate expiryDate;
        private String batchNumber;
        private Double pricePerUnit;
        private boolean requiresPrescription;
        private InventoryRequest inventory;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public java.time.LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(java.time.LocalDate expiryDate) { this.expiryDate = expiryDate; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        public Double getPricePerUnit() { return pricePerUnit; }
        public void setPricePerUnit(Double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
        public boolean isRequiresPrescription() { return requiresPrescription; }
        public void setRequiresPrescription(boolean requiresPrescription) { 
            this.requiresPrescription = requiresPrescription; 
        }
        public InventoryRequest getInventory() { return inventory; }
        public void setInventory(InventoryRequest inventory) { this.inventory = inventory; }
    }

    public static class InventoryRequest {
        private Integer currentQuantity;
        private Integer minQuantity;
        private Integer maxQuantity;

        // Getters and Setters
        public Integer getCurrentQuantity() { return currentQuantity; }
        public void setCurrentQuantity(Integer currentQuantity) { this.currentQuantity = currentQuantity; }
        public Integer getMinQuantity() { return minQuantity; }
        public void setMinQuantity(Integer minQuantity) { this.minQuantity = minQuantity; }
        public Integer getMaxQuantity() { return maxQuantity; }
        public void setMaxQuantity(Integer maxQuantity) { this.maxQuantity = maxQuantity; }
    }

    public static class BooleanResponse {
        private boolean value;

        public BooleanResponse(boolean value) { this.value = value; }
        public boolean isValue() { return value; }
        public void setValue(boolean value) { this.value = value; }
    }
}