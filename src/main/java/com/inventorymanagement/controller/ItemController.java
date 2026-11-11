package com.inventorymanagement.controller;

import com.inventorymanagement.dto.ItemDTO;
import com.inventorymanagement.security.CustomUserDetails;
import com.inventorymanagement.service.AuditLogService;
import com.inventorymanagement.service.CategoryService;
import com.inventorymanagement.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final CategoryService categoryService;
    private final AuditLogService auditLogService;

    public ItemController(ItemService itemService, CategoryService categoryService,
                          AuditLogService auditLogService) {
        this.itemService = itemService;
        this.categoryService = categoryService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String listItems(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) Long categoryId,
                            @RequestParam(required = false) String stockFilter,
                            Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name"));
        Page<ItemDTO> items;

        if ("low".equals(stockFilter)) {
            items = itemService.getLowStockItems(pageRequest);
        } else if (categoryId != null) {
            items = itemService.getItemsByCategory(categoryId, pageRequest);
        } else if (search != null && !search.trim().isEmpty()) {
            items = itemService.searchItems(search, pageRequest);
        } else {
            items = itemService.getAllItems(pageRequest);
        }

        model.addAttribute("items", items);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("stockFilter", stockFilter);
        model.addAttribute("currentPage", page);
        return "items/list";
    }

    @GetMapping("/{id}")
    public String viewItem(@PathVariable Long id, Model model) {
        model.addAttribute("item", itemService.getItemById(id));
        return "items/detail";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("item", new ItemDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "items/form";
    }

    @PostMapping("/add")
    public String addItem(@Valid @ModelAttribute("item") ItemDTO itemDTO,
                          BindingResult result,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "items/form";
        }
        try {
            ItemDTO created = itemService.createItem(itemDTO);
            auditLogService.log(userDetails.getUser(), "CREATE", "ITEM", created.getId(),
                    "Membuat barang: " + created.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Barang berhasil ditambahkan");
            return "redirect:/items";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "items/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("item", itemService.getItemById(id));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "items/form";
    }

    @PostMapping("/{id}/edit")
    public String updateItem(@PathVariable Long id,
                             @Valid @ModelAttribute("item") ItemDTO itemDTO,
                             BindingResult result,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "items/form";
        }
        try {
            itemService.updateItem(id, itemDTO);
            auditLogService.log(userDetails.getUser(), "UPDATE", "ITEM", id,
                    "Mengubah barang: " + itemDTO.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Barang berhasil diperbarui");
            return "redirect:/items";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "items/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteItem(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            ItemDTO item = itemService.getItemById(id);
            itemService.deleteItem(id);
            auditLogService.log(userDetails.getUser(), "DELETE", "ITEM", id,
                    "Menghapus barang: " + item.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Barang berhasil dihapus");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/items";
    }
}

