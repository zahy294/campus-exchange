package com.campusexchange.service;

import com.campusexchange.dto.DeviceRequest;
import com.campusexchange.dto.DeviceResponse;
import com.campusexchange.dto.ListingDetail;
import com.campusexchange.dto.SellerCard;
import com.campusexchange.exception.ResourceNotFoundException;
import com.campusexchange.model.Device;
import com.campusexchange.model.ListingKind;
import com.campusexchange.model.ListingStatus;
import com.campusexchange.model.User;
import com.campusexchange.repository.DeviceRepository;
import com.campusexchange.util.Text;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Device search and field editing. Results are {@link DeviceResponse} DTOs mapped
 * inside the transaction, so no lazy entity reaches a controller or view
 * (open-in-view is disabled). Ownership and status changes belong to
 * {@link ListingLifecycle}; seller contact visibility belongs to {@link SellerCard}.
 *
 * <p>Read methods take the viewer's email (null = anonymous) because what a viewer
 * sees of the seller depends on who they are.
 */
@Service
public class DeviceService {

    private static final int MAX_QUERY_LENGTH = 100;

    private final DeviceRepository deviceRepository;
    private final UserService userService;
    private final ListingLifecycle lifecycle;

    public DeviceService(DeviceRepository deviceRepository, UserService userService, ListingLifecycle lifecycle) {
        this.deviceRepository = deviceRepository;
        this.userService = userService;
        this.lifecycle = lifecycle;
    }

    /**
     * @param sort a {@link ListingSort} key; unknown keys mean newest first
     */
    @Transactional(readOnly = true)
    public Page<DeviceResponse> getAvailableDevices(String category, String q, int page, int size, String sort,
                                                    String viewerEmail) {
        String cat = Text.trimToNull(category);
        String query = Text.trimToNull(q);
        if (query != null && query.length() > MAX_QUERY_LENGTH) {
            query = query.substring(0, MAX_QUERY_LENGTH);
        }
        return deviceRepository.searchAvailableDevices(ListingStatus.AVAILABLE,
                        cat == null ? null : cat.toUpperCase(Locale.ROOT), query, Paging.of(page, size, sort))
                .map(device -> DeviceResponse.from(device, viewerEmail));
    }

    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(Long id, String viewerEmail) {
        return DeviceResponse.from(findDevice(id), viewerEmail);
    }

    /** The device plus what this viewer may see of its seller (viewer null = anonymous). */
    @Transactional(readOnly = true)
    public ListingDetail<DeviceResponse> getDeviceDetail(Long id, String viewerEmail) {
        Device device = findDevice(id);
        return new ListingDetail<>(DeviceResponse.from(device, viewerEmail),
                SellerCard.forViewer(device.getSeller(), viewerEmail));
    }

    /** Edit-form data for the owner; 404 if missing, 403 if someone else owns it. */
    @Transactional(readOnly = true)
    public DeviceRequest getDeviceForEdit(Long id, String userEmail) {
        return DeviceRequest.from(ownedDevice(id, userEmail));
    }

    @Transactional
    public DeviceResponse createDevice(DeviceRequest request, String userEmail) {
        User seller = userService.getUserByEmail(userEmail);
        Device device = new Device();
        request.applyTo(device);
        device.setStatus(ListingStatus.AVAILABLE);
        device.setSeller(seller);
        return DeviceResponse.from(deviceRepository.save(device), userEmail);
    }

    @Transactional
    public DeviceResponse updateDevice(Long id, DeviceRequest request, String userEmail) {
        Device device = ownedDevice(id, userEmail);
        request.applyTo(device);
        return DeviceResponse.from(deviceRepository.save(device), userEmail);
    }

    /** Sell or relist; delegates the rules to {@link ListingLifecycle}. */
    @Transactional
    public DeviceResponse updateDeviceStatus(Long id, ListingStatus status, String userEmail) {
        Device device = (Device) lifecycle.changeStatus(ListingKind.DEVICE, id, status, userEmail);
        return DeviceResponse.from(device, userEmail);
    }

    @Transactional
    public void deleteDevice(Long id, String userEmail) {
        lifecycle.withdraw(ListingKind.DEVICE, id, userEmail);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getMyDevices(String userEmail) {
        User seller = userService.getUserByEmail(userEmail);
        return deviceRepository.findBySellerOrderByCreatedAtDesc(seller)
                .stream()
                .map(device -> DeviceResponse.from(device, userEmail))
                .toList();
    }

    /** Newest available devices for the home page; seller contact is never included. */
    @Transactional(readOnly = true)
    public List<DeviceResponse> getFeaturedDevices() {
        return deviceRepository.findTop6ByStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE, PageRequest.of(0, 6))
                .stream()
                .map(device -> DeviceResponse.from(device, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public long countAvailable() {
        return deviceRepository.countByStatus(ListingStatus.AVAILABLE);
    }

    private Device findDevice(Long id) {
        return deviceRepository.findByIdWithSeller(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
    }

    private Device ownedDevice(Long id, String userEmail) {
        return (Device) lifecycle.requireOwned(ListingKind.DEVICE, id, userEmail);
    }
}
