package com.campusexchange.dto;

import com.campusexchange.model.ListingStatus;
import jakarta.validation.constraints.NotNull;

/** Body of {@code PATCH /api/devices/{id}/status}, e.g. {"status":"SOLD"}. */
public class DeviceStatusRequest {

    @NotNull(message = "Status is required: AVAILABLE or SOLD")
    private ListingStatus status;

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }
}
