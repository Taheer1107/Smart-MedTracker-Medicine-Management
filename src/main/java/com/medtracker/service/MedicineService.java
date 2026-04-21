package com.medtracker.service;

import com.medtracker.model.Medicine;
import com.medtracker.model.Inventory;
import com.medtracker.model.User;
import com.medtracker.model.UsageHistory;
import com.medtracker.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private UsageHistoryService usageHistoryService;

    @Autowired
    private InventoryService inventoryService;

    // Create medicine
    public Medicine addMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    // Get medicine by ID
    public Optional<Medicine> getMedicineById(Long medicineId) {
        return medicineRepository.findById(medicineId);
    }

    // Get all medicines
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAllWithInventory();
    }

    // Search medicines by name
    public List<Medicine> searchMedicinesByName(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name);
    }

    // Get medicines by brand
    public List<Medicine> getMedicinesByBrand(String brand) {
        return medicineRepository.findByBrand(brand);
    }

    // Get medicines by type
    public List<Medicine> getMedicinesByType(String type) {
        return medicineRepository.findByType(type);
    }

    // Get medicines by inventory
    public List<Medicine> getMedicinesByInventory(Inventory inventory) {
        return medicineRepository.findByInventory(inventory);
    }

    // Get medicines by inventory ID
    public List<Medicine> getMedicinesByInventoryId(Long inventoryId) {
        return medicineRepository.findByInventoryId(inventoryId);
    }

    // Update medicine
    public Medicine updateMedicine(Medicine medicine) {
        if (!medicineRepository.existsById(medicine.getMedicineId())) {
            throw new IllegalArgumentException("Medicine not found");
        }
        return medicineRepository.save(medicine);
    }

    // Delete medicine
    public void deleteMedicine(Long medicineId) {
        medicineRepository.deleteById(medicineId);
    }

    // Get expired medicines
    public List<Medicine> getExpiredMedicines() {
        return medicineRepository.findExpiredMedicines(LocalDate.now());
    }

    // Get medicines expiring soon (within 30 days)
    public List<Medicine> getMedicinesExpiringSoon(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return medicineRepository.findMedicinesExpiringSoon(startDate, endDate);
    }

    // Search medicines by name or brand
    public List<Medicine> searchMedicines(String searchTerm) {
        return medicineRepository.searchByNameOrBrand(searchTerm);
    }

    // Get medicines requiring prescription
    public List<Medicine> getMedicinesRequiringPrescription() {
        return medicineRepository.findByRequiresPrescriptionTrue();
    }

    // Find medicine by name and brand
    public Optional<Medicine> findMedicineByNameAndBrand(String name, String brand) {
        return medicineRepository.findByNameAndBrand(name, brand);
    }

    // Count medicines by type
    public Long countMedicinesByType(String type) {
        return medicineRepository.countByType(type);
    }

    // Get all medicine types
    public List<String> getAllMedicineTypes() {
        return medicineRepository.findAllMedicineTypes();
    }

    // Get medicines by batch number
    public List<Medicine> getMedicinesByBatchNumber(String batchNumber) {
        return medicineRepository.findByBatchNumber(batchNumber);
    }

    // Check if medicine is expired
    public boolean isMedicineExpired(Long medicineId) {
        Optional<Medicine> medicine = medicineRepository.findById(medicineId);
        return medicine.isPresent() && medicine.get().isExpired();
    }

    // Get medicine statistics
    public MedicineStatistics getMedicineStatistics() {
        List<Medicine> allMedicines = medicineRepository.findAll();
        long totalCount = allMedicines.size();
        long expiredCount = getExpiredMedicines().size();
        long prescriptionCount = getMedicinesRequiringPrescription().size();

        return new MedicineStatistics(totalCount, expiredCount, prescriptionCount);
    }

    // Record medicine usage and update inventory
    @Transactional
    public UsageHistory recordMedicineUsage(Long medicineId, User user, Double quantityUsed, String notes) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        // Determine unit based on medicine type
        String unit = getUnitForMedicine(medicine);

        // Record usage history
        UsageHistory usage = usageHistoryService.recordUsage(medicine, user, quantityUsed, unit, notes);

        // Update inventory quantity
        if (medicine.getInventory() != null) {
            inventoryService.reduceQuantity(medicine.getInventory().getInventoryId(), quantityUsed.intValue());
        }

        return usage;
    }

    private String getUnitForMedicine(Medicine medicine) {
        if (medicine instanceof com.medtracker.model.Tablet) {
            return "tablets";
        } else if (medicine instanceof com.medtracker.model.Syrup) {
            return "ml";
        }
        return "units";
    }

    // Inner class for statistics
    public static class MedicineStatistics {
        private long totalMedicines;
        private long expiredMedicines;
        private long prescriptionMedicines;

        public MedicineStatistics(long totalMedicines, long expiredMedicines, long prescriptionMedicines) {
            this.totalMedicines = totalMedicines;
            this.expiredMedicines = expiredMedicines;
            this.prescriptionMedicines = prescriptionMedicines;
        }

        public long getTotalMedicines() { return totalMedicines; }
        public long getExpiredMedicines() { return expiredMedicines; }
        public long getPrescriptionMedicines() { return prescriptionMedicines; }
    }
}