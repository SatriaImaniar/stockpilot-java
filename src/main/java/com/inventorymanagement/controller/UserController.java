package com.inventorymanagement.controller;

import com.inventorymanagement.dto.UserDTO;
import com.inventorymanagement.entity.Role;
import com.inventorymanagement.security.CustomUserDetails;
import com.inventorymanagement.service.AuditLogService;
import com.inventorymanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public UserController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String search,
                            Model model) {
        Page<UserDTO> users = userService.searchUsers(search, PageRequest.of(page, size));
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        return "users/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new UserDTO());
        model.addAttribute("roles", Role.values());
        return "users/form";
    }

    @PostMapping("/add")
    public String addUser(@Valid @ModelAttribute("user") UserDTO userDTO,
                          BindingResult result,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", "Password wajib diisi untuk pengguna baru");
            return "users/form";
        }
        try {
            UserDTO created = userService.createUser(userDTO);
            auditLogService.log(userDetails.getUser(), "CREATE", "USER", created.getId(),
                    "Membuat pengguna: " + created.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Pengguna berhasil ditambahkan");
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", Role.values());
        return "users/form";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("user") UserDTO userDTO,
                             BindingResult result,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
        try {
            userService.updateUser(id, userDTO);
            auditLogService.log(userDetails.getUser(), "UPDATE", "USER", id,
                    "Mengubah data pengguna: " + userDTO.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Pengguna berhasil diperbarui");
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggleUser(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserActive(id);
            auditLogService.log(userDetails.getUser(), "TOGGLE", "USER", id, "Mengubah status aktif pengguna");
            redirectAttributes.addFlashAttribute("successMessage", "Status pengguna berhasil diubah");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String newPassword,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(id, newPassword);
            auditLogService.log(userDetails.getUser(), "RESET_PASSWORD", "USER", id, "Reset password pengguna");
            redirectAttributes.addFlashAttribute("successMessage", "Password berhasil di-reset");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }
}

