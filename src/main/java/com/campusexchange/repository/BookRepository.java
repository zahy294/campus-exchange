package com.campusexchange.repository;

import com.campusexchange.model.Book;
import com.campusexchange.model.ListingStatus;
import com.campusexchange.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b JOIN FETCH b.seller WHERE b.id = :id")
    Optional<Book> findByIdWithSeller(@Param("id") Long id);

    @Query(value = "SELECT b FROM Book b JOIN FETCH b.seller WHERE b.status = :status AND " +
           "(:q IS NULL OR :q = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(b.subject) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:semester IS NULL OR b.semester = :semester)",
           countQuery = "SELECT count(b) FROM Book b WHERE b.status = :status AND " +
           "(:q IS NULL OR :q = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(b.subject) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:semester IS NULL OR b.semester = :semester)")
    Page<Book> searchAvailableBooks(@Param("status") ListingStatus status,
                                    @Param("q") String q,
                                    @Param("semester") Integer semester,
                                    Pageable pageable);

    @Query("SELECT b FROM Book b JOIN FETCH b.seller WHERE b.seller = :seller ORDER BY b.createdAt DESC")
    List<Book> findBySellerOrderByCreatedAtDesc(@Param("seller") User seller);

    @Query("SELECT b FROM Book b JOIN FETCH b.seller WHERE b.status = :status ORDER BY b.createdAt DESC")
    List<Book> findTop6ByStatusOrderByCreatedAtDesc(@Param("status") ListingStatus status, Pageable pageable);

    long countByStatus(ListingStatus status);

    /** Rows of [semester, count] for available books that have a semester. */
    @Query("SELECT b.semester, COUNT(b) FROM Book b WHERE b.status = :status AND b.semester IS NOT NULL GROUP BY b.semester")
    List<Object[]> countBySemester(@Param("status") ListingStatus status);
}
