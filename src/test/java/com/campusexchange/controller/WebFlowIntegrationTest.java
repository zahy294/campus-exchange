package com.campusexchange.controller;

import com.campusexchange.model.*;
import com.campusexchange.repository.BookRepository;
import com.campusexchange.repository.DeviceRepository;
import com.campusexchange.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end MVC + REST checks against the real services and an in-memory database:
 * HTML error pages, device web CRUD, ownership, validation and privacy rules.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WebFlowIntegrationTest {

    private static final String OWNER = "owner@campus.edu";
    private static final String OTHER = "other@campus.edu";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Book book;
    private Device device;

    @BeforeEach
    void seed() {
        User owner = userRepository.save(new User("Owner User", OWNER, passwordEncoder.encode("password123"), "9876543210", "CSE"));
        userRepository.save(new User("Other User", OTHER, passwordEncoder.encode("password123"), null, "ECE"));

        book = new Book();
        book.setTitle("Operating System Concepts");
        book.setAuthor("Silberschatz");
        book.setSemester(4);
        book.setCondition(ItemCondition.GOOD);
        book.setPrice(new BigDecimal("500.00"));
        book.setSeller(owner);
        book = bookRepository.save(book);

        device = new Device();
        device.setName("Casio fx-991EX");
        device.setCategory("CALCULATOR");
        device.setCondition(ItemCondition.LIKE_NEW);
        device.setPrice(new BigDecimal("950.00"));
        device.setSeller(owner);
        device = deviceRepository.save(device);
    }

    @Test
    @DisplayName("Missing book renders the HTML 404 page, not JSON")
    void missingBookRendersHtml404() throws Exception {
        mockMvc.perform(get("/books/999999").accept(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    @WithMockUser(username = OTHER)
    @DisplayName("Non-owner opening the edit form gets the HTML 403 page")
    void nonOwnerEditFormIs403() throws Exception {
        mockMvc.perform(get("/books/{id}/edit", book.getId()))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));
        mockMvc.perform(get("/devices/{id}/edit", device.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = OTHER)
    @DisplayName("Non-owner cannot mark someone else's book as sold")
    void nonOwnerCannotMarkSold() throws Exception {
        mockMvc.perform(post("/books/{id}/sold", book.getId()).with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getStatus()).isEqualTo(ListingStatus.AVAILABLE);
    }

    @Test
    @WithMockUser(username = OWNER)
    @DisplayName("Owner can mark sold and put back on sale")
    void ownerCanSellAndRelist() throws Exception {
        mockMvc.perform(post("/books/{id}/sold", book.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("justSold"));
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getStatus()).isEqualTo(ListingStatus.SOLD);

        mockMvc.perform(post("/books/{id}/available", book.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getStatus()).isEqualTo(ListingStatus.AVAILABLE);
    }

    @Test
    @WithMockUser(username = OWNER)
    @DisplayName("Device can be created, edited and deleted from the web UI")
    void deviceWebCrud() throws Exception {
        String location = mockMvc.perform(post("/devices").with(csrf())
                        .param("name", "Dell Latitude 5420")
                        .param("category", "LAPTOP")
                        .param("brand", "Dell")
                        .param("condition", "GOOD")
                        .param("price", "32000"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();
        assertThat(location).startsWith("/devices/");
        long id = Long.parseLong(location.substring("/devices/".length()));

        mockMvc.perform(get("/devices/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("devices/detail"))
                .andExpect(model().attribute("seller", hasProperty("viewerIsOwner", is(true))));

        mockMvc.perform(post("/devices/{id}", id).with(csrf())
                        .param("name", "Dell Latitude 5420 (16GB)")
                        .param("category", "LAPTOP")
                        .param("condition", "GOOD")
                        .param("price", "30000"))
                .andExpect(status().is3xxRedirection());
        assertThat(deviceRepository.findById(id).orElseThrow().getName()).isEqualTo("Dell Latitude 5420 (16GB)");

        mockMvc.perform(post("/devices/{id}/delete", id).with(csrf()))
                .andExpect(redirectedUrl("/my/listings"));
        assertThat(deviceRepository.findById(id)).isEmpty();
    }

    @Test
    @WithMockUser(username = OWNER)
    @DisplayName("Device form rejects unknown category and bad price with field errors")
    void deviceFormValidation() throws Exception {
        mockMvc.perform(post("/devices").with(csrf())
                        .param("name", "Toaster")
                        .param("category", "TOASTER")
                        .param("condition", "GOOD")
                        .param("price", "600000"))
                .andExpect(status().isOk())
                .andExpect(view().name("devices/form"))
                .andExpect(model().attributeHasFieldErrors("deviceForm", "category", "price"));
    }

    @Test
    @WithMockUser(username = OWNER)
    @DisplayName("Book form rejects an invalid ISBN")
    void bookFormRejectsBadIsbn() throws Exception {
        mockMvc.perform(post("/books").with(csrf())
                        .param("title", "Some Book")
                        .param("author", "Someone")
                        .param("isbn", "12345")
                        .param("condition", "GOOD")
                        .param("price", "100"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("bookForm", "isbn"));
    }

    @Test
    @DisplayName("Registration validates fields, and duplicate emails are case-insensitive")
    void registrationValidation() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "R2D2")
                        .param("email", "not-an-email")
                        .param("password", "short")
                        .param("confirmPassword", "different")
                        .param("phone", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerForm",
                        "fullName", "email", "password", "confirmPasswordValid", "phone"));

        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Owner Again")
                        .param("email", "OWNER@Campus.edu")
                        .param("password", "campus2026")
                        .param("confirmPassword", "campus2026"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("registerForm", "email", "duplicate"));
    }

    @Test
    @DisplayName("Valid registration stores a lower-cased email and redirects to sign in")
    void registrationSuccess() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Riya Sen")
                        .param("email", "Riya.Sen@Campus.edu")
                        .param("password", "campus2026")
                        .param("confirmPassword", "campus2026")
                        .param("phone", "9988776655"))
                .andExpect(redirectedUrl("/login"));
        assertThat(userRepository.findByEmail("riya.sen@campus.edu")).isPresent();
    }

    @Test
    @DisplayName("Public device API hides seller email from anonymous callers")
    void apiHidesEmailForAnonymous() throws Exception {
        mockMvc.perform(get("/api/devices/{id}", device.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seller.fullName").value("Owner User"))
                .andExpect(jsonPath("$.seller.email").doesNotExist());
    }

    @Test
    @WithMockUser(username = OTHER)
    @DisplayName("Authenticated API callers see the seller email")
    void apiShowsEmailWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/devices/{id}", device.getId()))
                .andExpect(jsonPath("$.seller.email").value(OWNER));
    }

    @Test
    @WithMockUser(username = OWNER)
    @DisplayName("API returns 400 JSON for malformed JSON, unknown enum and missing status")
    void apiBadRequests() throws Exception {
        mockMvc.perform(post("/api/devices").contentType(MediaType.APPLICATION_JSON).content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mockMvc.perform(patch("/api/devices/{id}/status", device.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"GONE\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/devices/{id}/status", device.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    @Test
    @WithMockUser(username = OTHER)
    @DisplayName("API returns 403 JSON when a non-owner deletes")
    void apiNonOwnerDelete() throws Exception {
        mockMvc.perform(delete("/api/devices/{id}", device.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Anonymous book detail hides seller contact and shows the sign-in gate")
    void anonymousBookDetailHidesContact() throws Exception {
        mockMvc.perform(get("/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(OWNER))))
                .andExpect(content().string(containsString("Sign in to contact")));
    }

    @Test
    @DisplayName("Home page renders listings, counts and semester chips")
    void homePageRenders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("semesterCounts", "bookCount", "deviceCount"))
                .andExpect(content().string(containsString("Operating System Concepts")))
                .andExpect(content().string(containsString("₹500")));
    }

    @Test
    @DisplayName("Out-of-range paging and unknown sort keys are clamped, not errors")
    void pagingIsClamped() throws Exception {
        mockMvc.perform(get("/books").param("page", "-5").param("size", "100000").param("sort", "nonsense"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/devices").param("page", "-1").param("size", "0"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = OWNER)
    @DisplayName("Seller dashboard and account pages render for a signed-in student")
    void dashboardAndAccount() throws Exception {
        mockMvc.perform(get("/my/listings"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeCount", 2L));
        mockMvc.perform(get("/account"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/profile"));
    }

    @Test
    @WithMockUser(username = OWNER)
    @DisplayName("Password change rejects a wrong current password")
    void passwordChangeWrongCurrent() throws Exception {
        mockMvc.perform(post("/account/password").with(csrf())
                        .param("currentPassword", "wrong-password1")
                        .param("newPassword", "newpass2026")
                        .param("confirmPassword", "newpass2026"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("passwordForm", "currentPassword", "mismatch"));
    }

    @Test
    @DisplayName("Anonymous device detail hides seller email and phone")
    void anonymousDeviceDetailHidesContact() throws Exception {
        mockMvc.perform(get("/devices/{id}", device.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("seller", hasProperty("contactVisible", is(false))))
                .andExpect(content().string(not(containsString(OWNER))))
                .andExpect(content().string(not(containsString("9876543210"))))
                .andExpect(content().string(containsString("Sign in to contact")));
    }

    @Test
    @WithMockUser(username = OTHER)
    @DisplayName("Signed-in student sees seller email and phone on device detail")
    void signedInDeviceDetailShowsContact() throws Exception {
        mockMvc.perform(get("/devices/{id}", device.getId()))
                .andExpect(content().string(containsString(OWNER)))
                .andExpect(content().string(containsString("9876543210")));
    }

    @Test
    @WithMockUser(username = OWNER)
    @DisplayName("Shared listing actions work for devices too")
    void listingActionsForDevices() throws Exception {
        mockMvc.perform(post("/devices/{id}/sold", device.getId()).with(csrf()))
                .andExpect(redirectedUrl("/devices/" + device.getId()));
        assertThat(deviceRepository.findById(device.getId()).orElseThrow().getStatus()).isEqualTo(ListingStatus.SOLD);
        mockMvc.perform(post("/devices/{id}/available", device.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/devices/{id}/delete", device.getId()).with(csrf()))
                .andExpect(redirectedUrl("/my/listings"));
        assertThat(deviceRepository.findById(device.getId())).isEmpty();
    }

    @Test
    @DisplayName("Device API carries labels and wear grade from the enums")
    void apiCarriesEnumLabels() throws Exception {
        mockMvc.perform(get("/api/devices/{id}", device.getId()))
                .andExpect(jsonPath("$.categoryLabel").value("Calculator"))
                .andExpect(jsonPath("$.conditionLabel").value("Like new"))
                .andExpect(jsonPath("$.conditionGrade").value(3));
    }

    @Test
    @WithMockUser(username = OWNER)
    @DisplayName("Category validation error lists the categories from the enum")
    void apiCategoryValidationMessage() throws Exception {
        mockMvc.perform(post("/api/devices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Toaster\",\"category\":\"TOASTER\",\"condition\":\"GOOD\",\"price\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.category").value(containsString("CALCULATOR")));
    }
}
