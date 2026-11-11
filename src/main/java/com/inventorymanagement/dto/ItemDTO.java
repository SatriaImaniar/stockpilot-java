package com.inventorymanagement.dto;

import jakarta.validation.constraints.*;

public class ItemDTO {
    private Long id;

    @NotBlank(message = "Item code is required")
    @Size(max = 50)
    private String itemCode;

    @NotBlank(message = "Item name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 1000)
    private String description;

    private Long categoryId;
    private String categoryName;

    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;

    private int availableQuantity;

    @Size(max = 200)
    private String storageLocation;

    public ItemDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
}
