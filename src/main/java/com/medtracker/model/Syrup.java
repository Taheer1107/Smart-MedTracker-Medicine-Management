package com.medtracker.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("SYRUP")
public class Syrup extends Medicine {

    @Column
    private Integer volumeML;

    @Column
    private String concentration;

    @Column
    private String flavor;

    @Column
    private boolean requiresRefrigeration = false;

    public Syrup() {
        super();
        this.setType("SYRUP");
    }

    public Syrup(String name, String brand) {
        super(name, brand, "SYRUP");
    }

    public Syrup(String name, String brand, Integer volumeML, String concentration) {
        this(name, brand);
        this.volumeML = volumeML;
        this.concentration = concentration;
    }

    public Integer getVolumeML() {
        return volumeML;
    }

    public void setVolumeML(Integer volumeML) {
        this.volumeML = volumeML;
    }

    public String getConcentration() {
        return concentration;
    }

    public void setConcentration(String concentration) {
        this.concentration = concentration;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public boolean isRequiresRefrigeration() {
        return requiresRefrigeration;
    }

    public void setRequiresRefrigeration(boolean requiresRefrigeration) {
        this.requiresRefrigeration = requiresRefrigeration;
    }

    @Override
    public String getDosageInstructions() {
        return "Take " + (concentration != null ? concentration : "as prescribed") + " using measuring cup";
    }

    @Override
    public String getAdministrationMethod() {
        String method = "Oral - Shake well before use";
        if (requiresRefrigeration) {
            method += ". Store in refrigerator";
        }
        return method;
    }

    @Override
    public String toString() {
        return "Syrup{" +
                "name='" + getName() + '\'' +
                ", brand='" + getBrand() + '\'' +
                ", volumeML=" + volumeML +
                ", concentration='" + concentration + '\'' +
                '}';
    }
}