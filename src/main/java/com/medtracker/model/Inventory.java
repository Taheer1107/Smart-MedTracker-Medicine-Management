package com.medtracker.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventories")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    @Column(nullable = false)
    private Integer currentQuantity = 0;

    @Column(nullable = false)
    private Integer minQuantity = 5;

    @Column(nullable = false)
    private Integer maxQuantity = 100;

    @Column
    private LocalDateTime lastUpdated;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    @JsonIgnore
    private Household household;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Medicine> medicines = new ArrayList<>();

    public Inventory() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Inventory(Household household) {
        this();
        this.household = household;
    }

    // Getters and Setters
    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Integer getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(Integer currentQuantity) {
        this.currentQuantity = currentQuantity;
        this.lastUpdated = LocalDateTime.now();
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public List<Medicine> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
    }

    // Business Methods
    public boolean isLowStock() {
        return currentQuantity <= minQuantity;
    }

    public boolean isOverStock() {
        return currentQuantity >= maxQuantity;
    }

    public void addMedicine(Medicine medicine) {
        medicines.add(medicine);
        medicine.setInventory(this);
        updateQuantity();
    }

    public void removeMedicine(Medicine medicine) {
        medicines.remove(medicine);
        medicine.setInventory(null);
        updateQuantity();
    }

    public void reduceQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currentQuantity < amount) {
            throw new IllegalStateException("Insufficient quantity");
        }
        this.currentQuantity -= amount;
        this.lastUpdated = LocalDateTime.now();
    }

    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.currentQuantity += amount;
        this.lastUpdated = LocalDateTime.now();
    }

    private void updateQuantity() {
        this.currentQuantity = medicines.size();
        this.lastUpdated = LocalDateTime.now();
    }

    public List<Medicine> getExpiredMedicines() {
        List<Medicine> expired = new ArrayList<>();
        for (Medicine medicine : medicines) {
            if (medicine.isExpired()) {
                expired.add(medicine);
            }
        }
        return expired;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryId=" + inventoryId +
                ", currentQuantity=" + currentQuantity +
                ", minQuantity=" + minQuantity +
                ", medicineCount=" + medicines.size() +
                '}';
    }
}