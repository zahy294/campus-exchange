package com.campusexchange.repository;

import com.campusexchange.model.Device;
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
public interface DeviceRepository extends JpaRepository<Device, Long> {

    @Query("SELECT d FROM Device d JOIN FETCH d.seller WHERE d.id = :id")
    Optional<Device> findByIdWithSeller(@Param("id") Long id);

    @Query(value = "SELECT d FROM Device d JOIN FETCH d.seller WHERE d.status = :status AND " +
           "(:category IS NULL OR :category = '' OR d.category = :category) AND " +
           "(:q IS NULL OR :q = '' OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.brand) LIKE LOWER(CONCAT('%', :q, '%')))",
           countQuery = "SELECT count(d) FROM Device d WHERE d.status = :status AND " +
           "(:category IS NULL OR :category = '' OR d.category = :category) AND " +
           "(:q IS NULL OR :q = '' OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.brand) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Device> searchAvailableDevices(@Param("status") ListingStatus status,
                                        @Param("category") String category,
                                        @Param("q") String q,
                                        Pageable pageable);

    @Query("SELECT d FROM Device d JOIN FETCH d.seller WHERE d.seller = :seller ORDER BY d.createdAt DESC")
    List<Device> findBySellerOrderByCreatedAtDesc(@Param("seller") User seller);

    @Query("SELECT d FROM Device d JOIN FETCH d.seller WHERE d.status = :status ORDER BY d.createdAt DESC")
    List<Device> findTop6ByStatusOrderByCreatedAtDesc(@Param("status") ListingStatus status, Pageable pageable);

    long countByStatus(ListingStatus status);
}
