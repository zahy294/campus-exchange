package com.campusexchange.controller.web;

import com.campusexchange.service.BookService;
import com.campusexchange.service.DeviceService;
import com.campusexchange.service.MyListingsSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    private final BookService bookService;
    private final DeviceService deviceService;

    public HomeController(BookService bookService, DeviceService deviceService) {
        this.bookService = bookService;
        this.deviceService = deviceService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("featuredBooks", bookService.getFeaturedBooks());
        model.addAttribute("featuredDevices", deviceService.getFeaturedDevices());
        model.addAttribute("bookCount", bookService.countAvailable());
        model.addAttribute("deviceCount", deviceService.countAvailable());
        model.addAttribute("semesterCounts", bookService.countAvailableBySemester());
        return "index";
    }

    /** Seller dashboard: everything the signed-in student has listed, with quick actions. */
    @GetMapping("/my/listings")
    public String myListings(Model model, Principal principal) {
        MyListingsSummary summary = MyListingsSummary.of(
                bookService.getMyBooks(principal.getName()),
                deviceService.getMyDevices(principal.getName()));
        model.addAttribute("myBooks", summary.getBooks());
        model.addAttribute("myDevices", summary.getDevices());
        model.addAttribute("activeCount", summary.getActiveCount());
        model.addAttribute("soldCount", summary.getSoldCount());
        model.addAttribute("soldValue", summary.getSoldValue());
        return "books/my-listings";
    }
}
