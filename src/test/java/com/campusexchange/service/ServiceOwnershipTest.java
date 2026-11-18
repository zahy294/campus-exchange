package com.campusexchange.service;

import com.campusexchange.dto.BookForm;
import com.campusexchange.dto.DeviceRequest;
import com.campusexchange.dto.DeviceResponse;
import com.campusexchange.dto.RegisterForm;
import com.campusexchange.exception.ForbiddenOperationException;
import com.campusexchange.exception.ResourceNotFoundException;
import com.campusexchange.model.*;
import com.campusexchange.repository.BookRepository;
import com.campusexchange.repository.DeviceRepository;
import com.campusexchange.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOwnershipTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    private BookService bookService;
    private DeviceService deviceService;
    private ListingLifecycle lifecycle;

    private User owner;
    private User otherUser;
    private Book testBook;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
        lifecycle = new ListingLifecycle(bookRepository, deviceRepository);
        bookService = new BookService(bookRepository, userService, lifecycle);
        deviceService = new DeviceService(deviceRepository, userService, lifecycle);

        owner = new User("Owner User", "owner@campus.edu", "passHash", "111", "CSE");
        owner.setId(1L);

        otherUser = new User("Other User", "other@campus.edu", "passHash", "222", "ECE");
        otherUser.setId(2L);

        testBook = new Book();
        testBook.setId(10L);
        testBook.setTitle("Algorithms");
        testBook.setAuthor("CLRS");
        testBook.setPrice(new BigDecimal("500.00"));
        testBook.setCondition(ItemCondition.GOOD);
        testBook.setStatus(ListingStatus.AVAILABLE);
        testBook.setSeller(owner);

        testDevice = new Device();
        testDevice.setId(20L);
        testDevice.setName("Laptop");
        testDevice.setCategory("LAPTOP");
        testDevice.setPrice(new BigDecimal("25000.00"));
        testDevice.setCondition(ItemCondition.GOOD);
        testDevice.setStatus(ListingStatus.AVAILABLE);
        testDevice.setSeller(owner);
    }

    @Test
    @DisplayName("Duplicate email registration throws IllegalArgumentException")
    void testDuplicateEmailRegistration() {
        when(userRepository.existsByEmail("owner@campus.edu")).thenReturn(true);

        RegisterForm form = new RegisterForm();
        form.setFullName("Duplicate");
        form.setEmail("owner@campus.edu");
        form.setPassword("password123");

        assertThatThrownBy(() -> userService.registerUser(form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Book owner can update book listing")
    void testOwnerCanUpdateBook() {
        when(bookRepository.findByIdWithSeller(10L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookForm form = new BookForm();
        form.setTitle("Algorithms Updated");
        form.setAuthor("CLRS");
        form.setCondition(ItemCondition.LIKE_NEW);
        form.setPrice(new BigDecimal("550.00"));

        Book updated = bookService.updateBook(10L, form, "owner@campus.edu");
        assertThat(updated.getTitle()).isEqualTo("Algorithms Updated");
        assertThat(updated.getPrice()).isEqualTo(new BigDecimal("550.00"));
    }

    @Test
    @DisplayName("Non-owner updating book throws ForbiddenOperationException")
    void testNonOwnerCannotUpdateBook() {
        when(bookRepository.findByIdWithSeller(10L)).thenReturn(Optional.of(testBook));

        BookForm form = new BookForm();
        form.setTitle("Hacked Title");
        form.setAuthor("CLRS");
        form.setCondition(ItemCondition.FAIR);
        form.setPrice(new BigDecimal("10.00"));

        assertThatThrownBy(() -> bookService.updateBook(10L, form, "other@campus.edu"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("Non-owner deleting book throws ForbiddenOperationException")
    void testNonOwnerCannotDeleteBook() {
        when(bookRepository.findByIdWithSeller(10L)).thenReturn(Optional.of(testBook));

        assertThatThrownBy(() -> lifecycle.withdraw(ListingKind.BOOK, 10L, "other@campus.edu"))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Device owner can update device listing")
    void testOwnerCanUpdateDevice() {
        when(deviceRepository.findByIdWithSeller(20L)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeviceRequest req = new DeviceRequest();
        req.setName("Laptop Pro");
        req.setCategory("LAPTOP");
        req.setCondition(ItemCondition.LIKE_NEW);
        req.setPrice(new BigDecimal("26000.00"));

        DeviceResponse res = deviceService.updateDevice(20L, req, "owner@campus.edu");
        assertThat(res.getName()).isEqualTo("Laptop Pro");
        assertThat(res.getPrice()).isEqualTo(new BigDecimal("26000.00"));
    }

    @Test
    @DisplayName("Non-owner updating device throws ForbiddenOperationException")
    void testNonOwnerCannotUpdateDevice() {
        when(deviceRepository.findByIdWithSeller(20L)).thenReturn(Optional.of(testDevice));

        DeviceRequest req = new DeviceRequest();
        req.setName("Stolen Laptop");
        req.setCategory("LAPTOP");
        req.setCondition(ItemCondition.FAIR);
        req.setPrice(new BigDecimal("100.00"));

        assertThatThrownBy(() -> deviceService.updateDevice(20L, req, "other@campus.edu"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("Non-owner deleting device throws ForbiddenOperationException")
    void testNonOwnerCannotDeleteDevice() {
        when(deviceRepository.findByIdWithSeller(20L)).thenReturn(Optional.of(testDevice));

        assertThatThrownBy(() -> lifecycle.withdraw(ListingKind.DEVICE, 20L, "other@campus.edu"))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(deviceRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Owner can sell and relist through the lifecycle; non-owner cannot")
    void lifecycleStatusChanges() {
        when(bookRepository.findByIdWithSeller(10L)).thenReturn(Optional.of(testBook));

        lifecycle.changeStatus(ListingKind.BOOK, 10L, ListingStatus.SOLD, "OWNER@campus.edu");
        assertThat(testBook.getStatus()).isEqualTo(ListingStatus.SOLD);
        lifecycle.changeStatus(ListingKind.BOOK, 10L, ListingStatus.AVAILABLE, "owner@campus.edu");
        assertThat(testBook.getStatus()).isEqualTo(ListingStatus.AVAILABLE);

        assertThatThrownBy(() -> lifecycle.changeStatus(ListingKind.BOOK, 10L, ListingStatus.SOLD, "other@campus.edu"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("book listing");
        assertThat(testBook.getStatus()).isEqualTo(ListingStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Lifecycle rejects a missing status and reports unknown listings as not found")
    void lifecycleRejectsBadInput() {
        assertThatThrownBy(() -> lifecycle.changeStatus(ListingKind.DEVICE, 20L, null, "owner@campus.edu"))
                .isInstanceOf(IllegalArgumentException.class);
        when(deviceRepository.findByIdWithSeller(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> lifecycle.requireOwned(ListingKind.DEVICE, 99L, "owner@campus.edu"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Device not found");
    }

    @Test
    @DisplayName("Owner withdraw deletes the listing")
    void ownerWithdrawDeletes() {
        when(deviceRepository.findByIdWithSeller(20L)).thenReturn(Optional.of(testDevice));
        lifecycle.withdraw(ListingKind.DEVICE, 20L, "owner@campus.edu");
        verify(deviceRepository).delete(testDevice);
    }
}
