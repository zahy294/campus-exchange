package com.campusexchange.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** An electronic device listed by a student. Shared listing fields live in {@link Listing}. */
@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_devices_status", columnList = "status"),
    @Index(name = "idx_devices_category", columnList = "category")
})
public class Device extends Listing {

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    /** One of {@link DeviceCategory}'s names; stored as text to keep the REST contract a plain string. */
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String category;

    @Size(max = 100)
    @Column(length = 100)
    private String brand;

    public Device() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
