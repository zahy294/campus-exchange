package com.campusexchange.controller.web;

import com.campusexchange.dto.DeviceRequest;
import com.campusexchange.dto.DeviceResponse;
import com.campusexchange.dto.ListingDetail;
import com.campusexchange.model.DeviceCategory;
import com.campusexchange.model.ItemCondition;
import com.campusexchange.service.DeviceService;
import com.campusexchange.service.ListingSort;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Server-rendered device pages. The catalogue itself (/devices) is rendered in
 * the browser from the REST API; detail and management pages are rendered here
 * so every API action is also available in the web UI. Sell / relist / delete live in
 * {@link ListingActionsController}.
 */
@Controller
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/devices")
    public String catalogue(Model model) {
        model.addAttribute("categories", DeviceCategory.values());
        model.addAttribute("sorts", ListingSort.values());
        return "devices/list";
    }

    @GetMapping("/devices/{id:[0-9]+}")
    public String viewDevice(@PathVariable("id") Long id, Model model, Principal principal) {
        ListingDetail<DeviceResponse> detail = deviceService.getDeviceDetail(id, principal == null ? null : principal.getName());
        model.addAttribute("device", detail.getListing());
        model.addAttribute("seller", detail.getSeller());
        return "devices/detail";
    }

    @GetMapping("/devices/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("deviceForm")) {
            model.addAttribute("deviceForm", new DeviceRequest());
        }
        return formView(model, null);
    }

    @PostMapping("/devices")
    public String createDevice(
            @Valid @ModelAttribute("deviceForm") DeviceRequest deviceForm,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return formView(model, null);
        }
        DeviceResponse created = deviceService.createDevice(deviceForm, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Your device is on the board. Classmates can find it now.");
        redirectAttributes.addFlashAttribute("justPublished", true);
        return "redirect:/devices/" + created.getId();
    }

    @GetMapping("/devices/{id:[0-9]+}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, Principal principal) {
        DeviceRequest form = deviceService.getDeviceForEdit(id, principal.getName());
        if (!model.containsAttribute("deviceForm")) {
            model.addAttribute("deviceForm", form);
        }
        return formView(model, id);
    }

    @PostMapping("/devices/{id:[0-9]+}")
    public String updateDevice(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("deviceForm") DeviceRequest deviceForm,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            deviceService.getDeviceForEdit(id, principal.getName()); // ownership check
            return formView(model, id);
        }
        deviceService.updateDevice(id, deviceForm, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Changes saved.");
        return "redirect:/devices/" + id;
    }

    private String formView(Model model, Long deviceId) {
        model.addAttribute("conditions", ItemCondition.values());
        model.addAttribute("categories", DeviceCategory.values());
        model.addAttribute("isEdit", deviceId != null);
        model.addAttribute("deviceId", deviceId);
        return "devices/form";
    }
}
