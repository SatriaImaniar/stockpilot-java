package com.inventorymanagement.service;

import com.inventorymanagement.dto.CategoryDTO;
import com.inventorymanagement.entity.Category;
import com.inventorymanagement.exception.DuplicateResourceException;
import com.inventorymanagement.exception.InvalidOperationException;
import com.inventorymanagement.exception.ResourceNotFoundException;
import com.inventorymanagement.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return toDTO(category);
    }

    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Kategori dengan nama '" + dto.getName() + "' sudah ada");
        }
        Category category = new Category(dto.getName(), dto.getDescription());
        category = categoryRepository.save(category);
        return toDTO(category);
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        categoryRepository.findByName(dto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("Kategori dengan nama '" + dto.getName() + "' sudah ada");
            }
        });

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category = categoryRepository.save(category);
        return toDTO(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        long itemCount = categoryRepository.countItemsByCategoryId(id);
        if (itemCount > 0) {
            throw new InvalidOperationException("Kategori '" + category.getName() + "' tidak dapat dihapus karena masih digunakan oleh " + itemCount + " barang");
        }

        categoryRepository.delete(category);
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setItemCount((int) categoryRepository.countItemsByCategoryId(category.getId()));
        return dto;
    }
}

