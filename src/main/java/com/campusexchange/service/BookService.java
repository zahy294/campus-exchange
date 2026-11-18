package com.campusexchange.service;

import com.campusexchange.dto.BookForm;
import com.campusexchange.dto.ListingDetail;
import com.campusexchange.dto.SellerCard;
import com.campusexchange.exception.ResourceNotFoundException;
import com.campusexchange.model.Book;
import com.campusexchange.model.ListingKind;
import com.campusexchange.model.ListingStatus;
import com.campusexchange.model.User;
import com.campusexchange.repository.BookRepository;
import com.campusexchange.util.Text;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Textbook search and field editing. Ownership, selling, relisting and deleting
 * belong to {@link ListingLifecycle}; this module only adds what is specific to books.
 */
@Service
public class BookService {

    private static final int MAX_QUERY_LENGTH = 100;

    private final BookRepository bookRepository;
    private final UserService userService;
    private final ListingLifecycle lifecycle;

    public BookService(BookRepository bookRepository, UserService userService, ListingLifecycle lifecycle) {
        this.bookRepository = bookRepository;
        this.userService = userService;
        this.lifecycle = lifecycle;
    }

    /**
     * Searches available books by keyword (title, author or subject) and semester.
     *
     * @param sort a {@link ListingSort} key; unknown keys mean newest first
     */
    @Transactional(readOnly = true)
    public Page<Book> searchAvailableBooks(String q, Integer semester, int page, int size, String sort) {
        String query = Text.trimToNull(q);
        if (query != null && query.length() > MAX_QUERY_LENGTH) {
            query = query.substring(0, MAX_QUERY_LENGTH);
        }
        Integer safeSemester = (semester != null && semester >= 1 && semester <= 8) ? semester : null;
        return bookRepository.searchAvailableBooks(ListingStatus.AVAILABLE, query, safeSemester,
                Paging.of(page, size, sort));
    }

    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookRepository.findByIdWithSeller(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    /** The book plus what this viewer may see of its seller (viewer null = anonymous). */
    @Transactional(readOnly = true)
    public ListingDetail<Book> getBookDetail(Long id, String viewerEmail) {
        Book book = getBookById(id);
        return new ListingDetail<>(book, SellerCard.forViewer(book.getSeller(), viewerEmail));
    }

    /** Loads a book for its owner; 404 if missing, 403 if someone else owns it. */
    @Transactional(readOnly = true)
    public Book getOwnedBook(Long id, String userEmail) {
        return (Book) lifecycle.requireOwned(ListingKind.BOOK, id, userEmail);
    }

    @Transactional
    public Book createBook(BookForm form, String userEmail) {
        User seller = userService.getUserByEmail(userEmail);
        Book book = new Book();
        form.applyTo(book);
        book.setStatus(ListingStatus.AVAILABLE);
        book.setSeller(seller);
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, BookForm form, String userEmail) {
        Book book = getOwnedBook(id, userEmail);
        form.applyTo(book);
        return bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<Book> getMyBooks(String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        return bookRepository.findBySellerOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Book> getFeaturedBooks() {
        return bookRepository.findTop6ByStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE, PageRequest.of(0, 6));
    }

    @Transactional(readOnly = true)
    public long countAvailable() {
        return bookRepository.countByStatus(ListingStatus.AVAILABLE);
    }

    /** Available-book count per semester (1-8), zero-filled, for the home page's semester chips. */
    @Transactional(readOnly = true)
    public Map<Integer, Long> countAvailableBySemester() {
        Map<Integer, Long> counts = new LinkedHashMap<>();
        for (int s = 1; s <= 8; s++) {
            counts.put(s, 0L);
        }
        for (Object[] row : bookRepository.countBySemester(ListingStatus.AVAILABLE)) {
            counts.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }
}
