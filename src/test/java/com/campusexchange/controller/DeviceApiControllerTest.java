package com.campusexchange.controller;

import com.campusexchange.dto.DeviceRequest;
import com.campusexchange.dto.DeviceResponse;
import com.campusexchange.exception.ForbiddenOperationException;
import com.campusexchange.model.ItemCondition;
import com.campusexchange.model.ListingStatus;
import com.campusexchange.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DeviceApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceService deviceService;

    @Test
    @DisplayName("Public GET /api/devices returns 200 OK without authentication")
    void testPublicGetDevices() throws Exception {
        when(deviceService.getAvailableDevices(any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unauthenticated POST /api/devices returns 401 JSON error payload")
    void testUnauthenticatedPostReturns401Json() throws Exception {
        DeviceRequest request = new DeviceRequest();
        request.setName("iPad Pro");
        request.setCategory("TABLET");
        request.setCondition(ItemCondition.GOOD);
        request.setPrice(new BigDecimal("35000.00"));

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "student1@campus.edu", roles = {"USER"})
    @DisplayName("Authenticated POST /api/devices with invalid body returns 400 Bad Request with fieldErrors")
    void testInvalidBodyReturns400WithFieldErrors() throws Exception {
        DeviceRequest request = new DeviceRequest();
        request.setName(""); // Blank name
        request.setCategory("LAPTOP");
        request.setCondition(ItemCondition.GOOD);
        request.setPrice(new BigDecimal("-100.00")); // Negative price

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists());
    }

    @Test
    @WithMockUser(username = "student1@campus.edu", roles = {"USER"})
    @DisplayName("Authenticated POST /api/devices with valid body returns 201 Created with Location header")
    void testValidPostReturns201Created() throws Exception {
        DeviceRequest request = new DeviceRequest();
        request.setName("Logitech Mouse");
        request.setCategory("ACCESSORY");
        request.setCondition(ItemCondition.LIKE_NEW);
        request.setPrice(new BigDecimal("1500.00"));

        DeviceResponse response = new DeviceResponse();
        response.setId(5L);
        response.setName("Logitech Mouse");
        response.setCategory("ACCESSORY");
        response.setCondition(ItemCondition.LIKE_NEW);
        response.setPrice(new BigDecimal("1500.00"));
        response.setStatus(ListingStatus.AVAILABLE);

        when(deviceService.createDevice(any(DeviceRequest.class), eq("student1@campus.edu")))
                .thenReturn(response);

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/devices/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Logitech Mouse"));
    }

    @Test
    @WithMockUser(username = "student2@campus.edu", roles = {"USER"})
    @DisplayName("Non-owner PUT /api/devices/{id} returns 403 Forbidden")
    void testNonOwnerPutReturns403() throws Exception {
        DeviceRequest request = new DeviceRequest();
        request.setName("Hacked Laptop");
        request.setCategory("LAPTOP");
        request.setCondition(ItemCondition.GOOD);
        request.setPrice(new BigDecimal("100.00"));

        when(deviceService.updateDevice(eq(5L), any(DeviceRequest.class), eq("student2@campus.edu")))
                .thenThrow(new ForbiddenOperationException("You do not have permission to modify this device listing."));

        mockMvc.perform(put("/api/devices/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
