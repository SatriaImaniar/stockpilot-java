package com.inventorymanagement.controller;

import com.inventorymanagement.dto.DashboardDTO;
import com.inventorymanagement.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardDTO dashboard = dashboardService.getDashboardData();
        model.addAttribute("dashboard", dashboard);
        return "dashboard";
    }
}

