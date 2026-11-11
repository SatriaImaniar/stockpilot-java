package com.inventorymanagement.service;

import com.inventorymanagement.dto.ItemDTO;
import com.inventorymanagement.entity.Category;
import com.inventorymanagement.entity.Item;
import com.inventorymanagement.exception.DuplicateResourceException;
import com.inventorymanagement.exception.ResourceNotFoundException;
import com.inventorymanagement.repository.CategoryRepository;
import com.inventorymanagement.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public ItemService(ItemRepository itemRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ItemDTO> getAllItems(Pageable pageable) {
        return itemRepository.findAllWithCategory(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ItemDTO> searchItems(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return getAllItems(pageable);
        }
        return itemRepository.searchItems(search.trim(), pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ItemDTO> getItemsByCategory(Long categoryId, Pageable pageable) {
        return itemRepository.findByCategoryIdWithCategory(categoryId, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ItemDTO> getLowStockItems(Pageable pageable) {
        return itemRepository.findLowStockItemsPaged(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ItemDTO getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        return toDTO(item);
    }

    @Transactional(readOnly = true)
    public Item getItemEntityById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
    }

    public ItemDTO createItem(ItemDTO dto) {
        if (itemRepository.existsByItemCode(dto.getItemCode())) {
            throw new DuplicateResourceException("Barang dengan kode '" + dto.getItemCode() + "' sudah ada");
        }

        Item item = new Item();
        item.setItemCode(dto.getItemCode());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setQuantity(dto.getQuantity());
        item.setAvailableQuantity(dto.getQuantity());
        item.setStorageLocation(dto.getStorageLocation());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            item.setCategory(category);
        }

        item = itemRepository.save(item);
        return toDTO(item);
    }

    public ItemDTO updateItem(Long id, ItemDTO dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        itemRepository.findByItemCode(dto.getItemCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("Barang dengan kode '" + dto.getItemCode() + "' sudah ada");
            }
        });

        int borrowedQty = item.getQuantity() - item.getAvailableQuantity();
        int newAvailable = dto.getQuantity() - borrowedQty;
        if (newAvailable < 0) {
            newAvailable = 0;
        }

        item.setItemCode(dto.getItemCode());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setQuantity(dto.getQuantity());
        item.setAvailableQuantity(newAvailable);
        item.setStorageLocation(dto.getStorageLocation());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            item.setCategory(category);
        } else {
            item.setCategory(null);
        }

        item = itemRepository.save(item);
        return toDTO(item);
    }

    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public long countAllItems() {
        return itemRepository.count();
    }

    @Transactional(readOnly = true)
    public long countAvailableItems() {
        return itemRepository.countAvailableItems();
    }

    @Transactional(readOnly = true)
    public long countLowStockItems() {
        return itemRepository.countLowStockItems();
    }

    @Transactional(readOnly = true)
    public Long countTotalBorrowed() {
        Long count = itemRepository.countTotalBorrowedQuantity();
        return count != null ? count : 0L;
    }

    @Transactional(readOnly = true)
    public List<Item> getAvailableItems() {
        return itemRepository.findAvailableItems();
    }

    private ItemDTO toDTO(Item item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setItemCode(item.getItemCode());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setQuantity(item.getQuantity());
        dto.setAvailableQuantity(item.getAvailableQuantity());
        dto.setStorageLocation(item.getStorageLocation());
        if (item.getCategory() != null) {
            dto.setCategoryId(item.getCategory().getId());
            dto.setCategoryName(item.getCategory().getName());
        }
        return dto;
    }
}

