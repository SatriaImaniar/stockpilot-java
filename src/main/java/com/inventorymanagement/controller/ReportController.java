package com.inventorymanagement.controller;

import com.inventorymanagement.dto.LoanDTO;
import com.inventorymanagement.entity.LoanStatus;
import com.inventorymanagement.service.LoanService;
import com.inventorymanagement.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final LoanService loanService;
    private final UserService userService;

    public ReportController(LoanService loanService, UserService userService) {
        this.loanService = loanService;
        this.userService = userService;
    }

    @GetMapping
    public String reportPage(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
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
        return "reports/list";
    }
}

