package com.inventorymanagement.repository;

import com.inventorymanagement.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.items")
    List<Category> findAllWithItemCount();

    @Query("SELECT COUNT(i) FROM Item i WHERE i.category.id = :categoryId")
    long countItemsByCategoryId(Long categoryId);
}
