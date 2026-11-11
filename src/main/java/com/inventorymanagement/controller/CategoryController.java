package com.inventorymanagement.controller;

import com.inventorymanagement.dto.CategoryDTO;
import com.inventorymanagement.security.CustomUserDetails;
import com.inventorymanagement.service.AuditLogService;
import com.inventorymanagement.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final AuditLogService auditLogService;

    public CategoryController(CategoryService categoryService, AuditLogService auditLogService) {
        this.categoryService = categoryService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("newCategory", new CategoryDTO());
        return "categories/list";
    }

    @PostMapping("/add")
    public String addCategory(@Valid @ModelAttribute("newCategory") CategoryDTO categoryDTO,
                              BindingResult result,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "categories/list";
        }
        try {
            CategoryDTO created = categoryService.createCategory(categoryDTO);
            auditLogService.log(userDetails.getUser(), "CREATE", "CATEGORY", created.getId(),
                    "Membuat kategori: " + created.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil ditambahkan");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/{id}/edit")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute CategoryDTO categoryDTO,
                                 BindingResult result,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Silakan periksa input Anda");
            return "redirect:/categories";
        }
        try {
            categoryService.updateCategory(id, categoryDTO);
            auditLogService.log(userDetails.getUser(), "UPDATE", "CATEGORY", id,
                    "Mengubah kategori: " + categoryDTO.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil diperbarui");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            CategoryDTO cat = categoryService.getCategoryById(id);
            categoryService.deleteCategory(id);
            auditLogService.log(userDetails.getUser(), "DELETE", "CATEGORY", id,
                    "Menghapus kategori: " + cat.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil dihapus");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }
}

