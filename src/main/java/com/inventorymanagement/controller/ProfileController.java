package com.inventorymanagement.controller;

import com.inventorymanagement.dto.PasswordChangeDTO;
import com.inventorymanagement.dto.UserDTO;
import com.inventorymanagement.security.CustomUserDetails;
import com.inventorymanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("user", userService.getUserById(userDetails.getUserId()));
        model.addAttribute("passwordChange", new PasswordChangeDTO());
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("user") UserDTO userDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("passwordChange", new PasswordChangeDTO());
            return "profile";
        }
        try {
            userDTO.setRole(userDetails.getUser().getRole());
            userDTO.setActive(userDetails.getUser().isActive());
            userService.updateUser(userDetails.getUserId(), userDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Profil berhasil diperbarui");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("passwordChange") PasswordChangeDTO passwordDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Silakan periksa input password Anda");
            return "redirect:/profile";
        }
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password baru dan konfirmasi tidak cocok");
            return "redirect:/profile";
        }
        try {
            userService.changePassword(userDetails.getUserId(), passwordDTO.getCurrentPassword(), passwordDTO.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password berhasil diubah");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }
}

