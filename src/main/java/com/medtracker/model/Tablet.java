package com.medtracker.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("TABLET")
public class Tablet extends Medicine {

    @Column
    private String strength;

    @Column
    private String coatingType;

    @Column
    private boolean scored = false;

    @Column
    private Integer tabletsPerStrip;

    public Tablet() {
        super();
        this.setType("TABLET");
    }

    public Tablet(String name, String brand) {
        super(name, brand, "TABLET");
    }

    public Tablet(String name, String brand, String strength) {
        this(name, brand);
        this.strength = strength;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getCoatingType() {
        return coatingType;
    }

    public void setCoatingType(String coatingType) {
        this.coatingType = coatingType;
    }

    public boolean isScored() {
        return scored;
    }

    public void setScored(boolean scored) {
        this.scored = scored;
    }

    public Integer getTabletsPerStrip() {
        return tabletsPerStrip;
    }

    public void setTabletsPerStrip(Integer tabletsPerStrip) {
        this.tabletsPerStrip = tabletsPerStrip;
    }

    @Override
    public String getDosageInstructions() {
        return "Take " + (strength != null ? strength : "as prescribed") + " tablet orally with water";
    }

    @Override
    public String getAdministrationMethod() {
        return "Oral - Swallow whole" + (scored ? " (can be split)" : " (do not crush)");
    }

    @Override
    public String toString() {
        return "Tablet{" +
                "name='" + getName() + '\'' +
                ", brand='" + getBrand() + '\'' +
                ", strength='" + strength + '\'' +
                '}';
    }
}