package com.campusexchange.dto;

import com.campusexchange.model.Device;
import com.campusexchange.model.DeviceCategory;
import com.campusexchange.model.ItemCondition;
import com.campusexchange.model.ListingStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Public JSON shape of a device. Never exposes the seller entity or password hash.
 * Carries display labels and the wear grade so clients don't need their own copies of the enums.
 */
public class DeviceResponse {

    private Long id;
    private String name;
    private String category;
    private String brand;
    private ItemCondition condition;
    private BigDecimal price;
    private String description;
    private ListingStatus status;
    private SellerSummary seller;
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SellerSummary {
        private Long id;
        private String fullName;
        private String email;

        public SellerSummary() {
        }

        public SellerSummary(Long id, String fullName, String email) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public DeviceResponse() {
    }

    /**
     * Maps an entity whose seller is already initialised (call inside a transaction).
     * Seller contact follows {@link SellerCard}: the email is present only for signed-in viewers.
     *
     * @param viewerEmail the signed-in viewer, or null for an anonymous caller
     */
    public static DeviceResponse from(Device device, String viewerEmail) {
        DeviceResponse response = new DeviceResponse();
        response.setId(device.getId());
        response.setName(device.getName());
        response.setCategory(device.getCategory());
        response.setBrand(device.getBrand());
        response.setCondition(device.getCondition());
        response.setPrice(device.getPrice());
        response.setDescription(device.getDescription());
        response.setStatus(device.getStatus());
        response.setCreatedAt(device.getCreatedAt());
        if (device.getSeller() != null) {
            SellerCard card = SellerCard.forViewer(device.getSeller(), viewerEmail);
            response.setSeller(new SellerSummary(card.getId(), card.getFullName(), card.getEmail()));
        }
        return response;
    }

    /** Display label for the category, from {@link DeviceCategory}. */
    public String getCategoryLabel() {
        return DeviceCategory.labelOf(category);
    }

    /** Display label for the condition, from {@link ItemCondition}. */
    public String getConditionLabel() {
        return condition == null ? null : condition.getLabel();
    }

    /** Wear grade 1-4, from {@link ItemCondition}. */
    public Integer getConditionGrade() {
        return condition == null ? null : condition.getGrade();
    }

    @JsonIgnore
    public boolean isAvailable() {
        return status == ListingStatus.AVAILABLE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public SellerSummary getSeller() {
        return seller;
    }

    public void setSeller(SellerSummary seller) {
        this.seller = seller;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
