package com.campusexchange.dto;

import com.campusexchange.model.Device;
import com.campusexchange.dto.validation.DeviceCategoryName;
import com.campusexchange.model.ItemCondition;
import com.campusexchange.util.Text;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Locale;

/** Input for creating or updating a device, used by both the REST API (JSON) and the web form. */
public class DeviceRequest {

    @NotBlank(message = "Enter the device name")
    @Size(max = 150, message = "Name must be 150 characters or fewer")
    private String name;

    @NotBlank(message = "Choose a category")
    @DeviceCategoryName
    private String category;

    @Size(max = 100, message = "Brand must be 100 characters or fewer")
    private String brand;

    @NotNull(message = "Choose the condition")
    private ItemCondition condition;

    @NotNull(message = "Enter a price")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @DecimalMax(value = ValidationRules.MAX_PRICE, message = "Price must be ₹5,00,000 or less")
    @Digits(integer = 8, fraction = 2, message = "Use at most 2 decimal places, e.g. 4500.00")
    private BigDecimal price;

    @Size(max = 1000, message = "Description must be 1000 characters or fewer")
    private String description;

    public DeviceRequest() {
    }

    /** Pre-fills the web edit form from an existing device. */
    public static DeviceRequest from(Device device) {
        DeviceRequest form = new DeviceRequest();
        form.setName(device.getName());
        form.setCategory(device.getCategory());
        form.setBrand(device.getBrand());
        form.setCondition(device.getCondition());
        form.setPrice(device.getPrice());
        form.setDescription(device.getDescription());
        return form;
    }

    /** Copies the (validated) values onto the entity, normalising whitespace and category case. */
    public void applyTo(Device device) {
        device.setName(Text.trimToNull(name));
        device.setCategory(category == null ? null : category.trim().toUpperCase(Locale.ROOT));
        device.setBrand(Text.trimToNull(brand));
        device.setCondition(condition);
        device.setPrice(price);
        device.setDescription(Text.trimToNull(description));
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

    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
