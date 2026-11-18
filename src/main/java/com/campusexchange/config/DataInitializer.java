package com.campusexchange.config;

import com.campusexchange.model.*;
import com.campusexchange.repository.BookRepository;
import com.campusexchange.repository.DeviceRepository;
import com.campusexchange.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            BookRepository bookRepository,
            DeviceRepository deviceRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.deviceRepository = deviceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // Demo Users (password: password123)
        User user1 = new User("Aarav Patel", "student1@campus.edu", passwordEncoder.encode("password123"), "9876543210", "Computer Science");
        User user2 = new User("Priya Nair", "student2@campus.edu", passwordEncoder.encode("password123"), "9123456789", "Electronics & Comm");
        userRepository.save(user1);
        userRepository.save(user2);

        // Demo Books (~10 items)
        bookRepository.save(createBook("Introduction to Algorithms (CLRS)", "Cormen, Leiserson, Rivest, Stein", "978-0262033848", "Data Structures & Algorithms", 3, ItemCondition.GOOD, new BigDecimal("650.00"), "Standard textbook for CSE, some pencil markings.", user1));
        bookRepository.save(createBook("Operating System Concepts (Silberschatz)", "Silberschatz, Galvin, Gagne", "978-1118063330", "Operating Systems", 4, ItemCondition.LIKE_NEW, new BigDecimal("500.00"), "10th edition paperback. Excellent condition.", user1));
        bookRepository.save(createBook("Database System Concepts", "Silberschatz, Korth, Sudarshan", "978-0078022159", "Database Systems", 4, ItemCondition.GOOD, new BigDecimal("450.00"), "Comprehensive book for DBMS lab and theory.", user2));
        bookRepository.save(createBook("Computer Networks", "Andrew S. Tanenbaum, David J. Wetherall", "978-0132126953", "Networking", 5, ItemCondition.FAIR, new BigDecimal("350.00"), "5th Edition. Minor cover wear, complete pages.", user2));
        bookRepository.save(createBook("Digital Design and Computer Architecture", "David Harris, Sarah Harris", "978-0123944245", "Computer Architecture", 3, ItemCondition.NEW, new BigDecimal("700.00"), "Brand new copy, untouched condition.", user1));
        bookRepository.save(createBook("Artificial Intelligence: A Modern Approach", "Stuart Russell, Peter Norvig", "978-0136042594", "Artificial Intelligence", 6, ItemCondition.LIKE_NEW, new BigDecimal("750.00"), "Hardcover 3rd edition, no highlights.", user2));
        bookRepository.save(createBook("Software Engineering: A Practitioner's Approach", "Roger S. Pressman", "978-0078022128", "Software Engineering", 5, ItemCondition.GOOD, new BigDecimal("400.00"), "Clean interior, perfect for semester exams.", user1));
        bookRepository.save(createBook("Discrete Mathematics and Its Applications", "Kenneth H. Rosen", "978-0073383095", "Mathematics", 3, ItemCondition.LIKE_NEW, new BigDecimal("550.00"), "7th edition. Used for one semester only.", user2));
        bookRepository.save(createBook("Compilers: Principles, Techniques, and Tools", "Aho, Lam, Sethi, Ullman", "978-0321486813", "Compiler Design", 6, ItemCondition.GOOD, new BigDecimal("500.00"), "The classic Dragon Book for compiler design.", user1));
        bookRepository.save(createBook("Data Structures and Algorithm Analysis in C++", "Mark Allen Weiss", "978-0132847377", "Data Structures", 3, ItemCondition.GOOD, new BigDecimal("420.00"), "4th Edition, essential for CS3603 lab.", user2));

        // Demo Devices (~8 items)
        deviceRepository.save(createDevice("Dell Inspiron 15 3520", "LAPTOP", "Dell", ItemCondition.GOOD, new BigDecimal("28000.00"), "Intel Core i5, 16GB RAM, 512GB SSD. Charger and laptop bag included.", user1));
        deviceRepository.save(createDevice("Texas Instruments TI-84 Plus CE", "CALCULATOR", "Texas Instruments", ItemCondition.LIKE_NEW, new BigDecimal("8500.00"), "Color graphing calculator with rechargeable battery and USB cable.", user2));
        deviceRepository.save(createDevice("Apple iPad Air 4th Generation 64GB", "TABLET", "Apple", ItemCondition.GOOD, new BigDecimal("29000.00"), "Space Gray, 10.9-inch liquid retina display. Includes protective magnetic case.", user1));
        deviceRepository.save(createDevice("Logitech MX Master 3S Wireless Mouse", "ACCESSORY", "Logitech", ItemCondition.LIKE_NEW, new BigDecimal("4500.00"), "Quiet clicks, 8K DPI sensor, Bluetooth + Bolt receiver.", user2));
        deviceRepository.save(createDevice("Raspberry Pi 4 Model B (4GB RAM)", "OTHER", "Raspberry Pi", ItemCondition.LIKE_NEW, new BigDecimal("4200.00"), "Includes official power supply, micro HDMI cable, and aluminum heatsink case.", user1));
        deviceRepository.save(createDevice("Casio fx-991EX ClassWiz Scientific Calculator", "CALCULATOR", "Casio", ItemCondition.GOOD, new BigDecimal("950.00"), "Engineering scientific calculator with 552 functions.", user2));
        deviceRepository.save(createDevice("Samsung Galaxy Tab S7 FE with S-Pen", "TABLET", "Samsung", ItemCondition.GOOD, new BigDecimal("24000.00"), "12.4-inch display, great for college note-taking and PDF reading.", user1));
        deviceRepository.save(createDevice("Sony WH-1000XM4 Wireless Headphones", "ACCESSORY", "Sony", ItemCondition.FAIR, new BigDecimal("11000.00"), "Industry leading noise cancelling, 30h battery, original carrying case.", user2));
    }

    private Book createBook(String title, String author, String isbn, String subject, Integer sem, ItemCondition cond, BigDecimal price, String desc, User seller) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setIsbn(isbn);
        b.setSubject(subject);
        b.setSemester(sem);
        b.setCondition(cond);
        b.setPrice(price);
        b.setDescription(desc);
        b.setStatus(ListingStatus.AVAILABLE);
        b.setSeller(seller);
        return b;
    }

    private Device createDevice(String name, String cat, String brand, ItemCondition cond, BigDecimal price, String desc, User seller) {
        Device d = new Device();
        d.setName(name);
        d.setCategory(cat);
        d.setBrand(brand);
        d.setCondition(cond);
        d.setPrice(price);
        d.setDescription(desc);
        d.setStatus(ListingStatus.AVAILABLE);
        d.setSeller(seller);
        return d;
    }
}
