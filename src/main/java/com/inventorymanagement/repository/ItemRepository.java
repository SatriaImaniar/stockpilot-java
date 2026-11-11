package com.inventorymanagement.repository;

import com.inventorymanagement.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByItemCode(String itemCode);
    boolean existsByItemCode(String itemCode);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.category WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Item> searchItems(@Param("search") String search, Pageable pageable);

    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.availableQuantity <= 5 AND i.quantity > 0")
    List<Item> findLowStockItems();

    @Query("SELECT COUNT(i) FROM Item i WHERE i.availableQuantity <= 5 AND i.quantity > 0")
    long countLowStockItems();

    @Query("SELECT i FROM Item i WHERE i.availableQuantity > 0")
    List<Item> findAvailableItems();

    @Query("SELECT COUNT(i) FROM Item i WHERE i.availableQuantity > 0")
    long countAvailableItems();

    @Query("SELECT SUM(i.quantity - i.availableQuantity) FROM Item i")
    Long countTotalBorrowedQuantity();

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.category")
    Page<Item> findAllWithCategory(Pageable pageable);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.category WHERE i.category.id = :categoryId")
    Page<Item> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.category WHERE i.availableQuantity <= 5 AND i.quantity > 0")
    Page<Item> findLowStockItemsPaged(Pageable pageable);
}
