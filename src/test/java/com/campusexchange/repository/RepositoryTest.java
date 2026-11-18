package com.campusexchange.repository;

import com.campusexchange.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Alice Smith", "alice@campus.edu", "hashed_pw", "9876543210", "CSE");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Verify User creation and email lookup")
    void testUserSaveAndFind() {
        Optional<User> found = userRepository.findByEmail("alice@campus.edu");
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Alice Smith");
    }

    @Test
    @DisplayName("Verify Book persistence with item_condition column mapping and JOIN FETCH seller query")
    void testBookSaveAndFetchWithSeller() {
        Book book = new Book();
        book.setTitle("Database Systems Concepts");
        book.setAuthor("Silberschatz");
        book.setSubject("DBMS");
        book.setSemester(4);
        book.setCondition(ItemCondition.LIKE_NEW);
        book.setPrice(new BigDecimal("450.00"));
        book.setDescription("Hardcover 7th edition, perfect condition.");
        book.setStatus(ListingStatus.AVAILABLE);
        book.setSeller(testUser);

        Book saved = bookRepository.save(book);
        assertThat(saved.getId()).isNotNull();

        Optional<Book> fetched = bookRepository.findByIdWithSeller(saved.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getCondition()).isEqualTo(ItemCondition.LIKE_NEW);
        assertThat(fetched.get().getSeller().getEmail()).isEqualTo("alice@campus.edu");

        Page<Book> searchResult = bookRepository.searchAvailableBooks(
                ListingStatus.AVAILABLE, "database", 4, PageRequest.of(0, 10));
        assertThat(searchResult.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Verify Device persistence with item_condition column mapping and search query")
    void testDeviceSaveAndFetchWithSeller() {
        Device device = new Device();
        device.setName("iPad Air 4th Gen");
        device.setCategory("TABLET");
        device.setBrand("Apple");
        device.setCondition(ItemCondition.GOOD);
        device.setPrice(new BigDecimal("28000.00"));
        device.setDescription("Includes Apple Pencil and magnetic cover.");
        device.setStatus(ListingStatus.AVAILABLE);
        device.setSeller(testUser);

        Device saved = deviceRepository.save(device);
        assertThat(saved.getId()).isNotNull();

        Optional<Device> fetched = deviceRepository.findByIdWithSeller(saved.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getCondition()).isEqualTo(ItemCondition.GOOD);
        assertThat(fetched.get().getSeller().getEmail()).isEqualTo("alice@campus.edu");

        Page<Device> searchResult = deviceRepository.searchAvailableDevices(
                ListingStatus.AVAILABLE, "TABLET", "iPad", PageRequest.of(0, 10));
        assertThat(searchResult.getTotalElements()).isEqualTo(1);
    }
}
