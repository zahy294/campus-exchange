package com.campusexchange.controller.api;

import com.campusexchange.dto.DeviceRequest;
import com.campusexchange.dto.DeviceResponse;
import com.campusexchange.dto.DeviceStatusRequest;
import com.campusexchange.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

/**
 * JSON REST API for device listings. Errors are rendered by
 * {@link com.campusexchange.exception.ApiExceptionHandler}.
 * Seller email addresses are only included for authenticated callers (see SellerCard).
 */
@RestController
@RequestMapping("/api/devices")
public class DeviceApiController {

    private final DeviceService deviceService;

    public DeviceApiController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * @param sort newest (default), oldest, price_asc or price_desc
     */
    @GetMapping
    public ResponseEntity<Page<DeviceResponse>> listDevices(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "newest") String sort,
            Principal principal) {

        return ResponseEntity.ok(deviceService.getAvailableDevices(category, q, page, size, sort, viewer(principal)));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable("id") Long id, Principal principal) {
        return ResponseEntity.ok(deviceService.getDeviceById(id, viewer(principal)));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<DeviceResponse>> getMyDevices(Principal principal) {
        return ResponseEntity.ok(deviceService.getMyDevices(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request, Principal principal) {
        DeviceResponse created = deviceService.createDevice(request, principal.getName());
        return ResponseEntity.created(URI.create("/api/devices/" + created.getId())).body(created);
    }

    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable("id") Long id,
            @Valid @RequestBody DeviceRequest request,
            Principal principal) {
        return ResponseEntity.ok(deviceService.updateDevice(id, request, principal.getName()));
    }

    @PatchMapping("/{id:[0-9]+}/status")
    public ResponseEntity<DeviceResponse> updateDeviceStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody DeviceStatusRequest body,
            Principal principal) {
        return ResponseEntity.ok(deviceService.updateDeviceStatus(id, body.getStatus(), principal.getName()));
    }

    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> deleteDevice(@PathVariable("id") Long id, Principal principal) {
        deviceService.deleteDevice(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    /** Seller contact in responses follows SellerCard: anonymous callers don't get the email. */
    private static String viewer(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
