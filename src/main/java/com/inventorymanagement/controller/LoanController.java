package com.inventorymanagement.controller;

import com.inventorymanagement.dto.LoanDTO;
import com.inventorymanagement.dto.LoanItemDTO;
import com.inventorymanagement.entity.LoanStatus;
import com.inventorymanagement.security.CustomUserDetails;
import com.inventorymanagement.service.ItemService;
import com.inventorymanagement.service.LoanService;
import com.inventorymanagement.service.ReportService;
import com.inventorymanagement.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

@Controller
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final ItemService itemService;
    private final UserService userService;
    private final ReportService reportService;

    public LoanController(LoanService loanService, ItemService itemService,
                          UserService userService, ReportService reportService) {
        this.loanService = loanService;
        this.itemService = itemService;
        this.userService = userService;
        this.reportService = reportService;
    }

    @GetMapping
    public String listLoans(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) LoanStatus status,
                            @RequestParam(required = false) Long borrowerId,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                            Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<LoanDTO> loans = loanService.getLoansByFilters(status, borrowerId, startDate, endDate, pageRequest);

        model.addAttribute("loans", loans);
        model.addAttribute("statuses", LoanStatus.values());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedBorrower", borrowerId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentPage", page);
        return "loans/list";
    }

    @GetMapping("/{id}")
    public String viewLoan(@PathVariable Long id, Model model) {
        model.addAttribute("loan", loanService.getLoanById(id));
        return "loans/detail";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        LoanDTO loanDTO = new LoanDTO();
        loanDTO.setLoanDate(LocalDate.now());
        loanDTO.setDueDate(LocalDate.now().plusDays(7));
        loanDTO.setLoanItems(new ArrayList<>());
        model.addAttribute("loan", loanDTO);
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("availableItems", itemService.getAvailableItems());
        return "loans/form";
    }

    @PostMapping("/add")
    public String createLoan(@ModelAttribute("loan") LoanDTO loanDTO,
                             @RequestParam(value = "itemIds", required = false) Long[] itemIds,
                             @RequestParam(value = "quantities", required = false) Integer[] quantities,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            if (itemIds == null || itemIds.length == 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Harap pilih minimal satu barang untuk dipinjam");
                return "redirect:/loans/add";
            }

            ArrayList<LoanItemDTO> loanItems = new ArrayList<>();
            for (int i = 0; i < itemIds.length; i++) {
                if (itemIds[i] != null && quantities[i] != null && quantities[i] > 0) {
                    LoanItemDTO itemDTO = new LoanItemDTO();
                    itemDTO.setItemId(itemIds[i]);
                    itemDTO.setQuantity(quantities[i]);
                    loanItems.add(itemDTO);
                }
            }
            loanDTO.setLoanItems(loanItems);

            loanService.createLoan(loanDTO, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Transaksi peminjaman berhasil dibuat");
            return "redirect:/loans";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/loans/add";
        }
    }

    @PostMapping("/{id}/approve")
    public String approveLoan(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            loanService.approveLoan(id, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Peminjaman berhasil disetujui");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/loans/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectLoan(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            loanService.rejectLoan(id, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Peminjaman berhasil ditolak");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/loans/" + id;
    }

    @PostMapping("/{id}/return")
    public String returnLoan(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            loanService.returnLoan(id, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Barang berhasil dikembalikan ke inventaris");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/loans/" + id;
    }

    @GetMapping("/export")
    public void exportLoans(@RequestParam(required = false) LoanStatus status,
                            @RequestParam(required = false) Long borrowerId,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                            HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=loans_report.csv");
        reportService.exportLoansToCsv(response.getWriter(), status, borrowerId, startDate, endDate);
    }
}

