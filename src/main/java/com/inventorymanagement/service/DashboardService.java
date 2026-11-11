package com.inventorymanagement.service;

import com.inventorymanagement.dto.DashboardDTO;
import com.inventorymanagement.repository.CategoryRepository;
import com.inventorymanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ItemService itemService;
    private final LoanService loanService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public DashboardService(ItemService itemService, LoanService loanService,
                            CategoryRepository categoryRepository, UserRepository userRepository) {
        this.itemService = itemService;
        this.loanService = loanService;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public DashboardDTO getDashboardData() {
        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setTotalItems(itemService.countAllItems());
        dashboard.setTotalCategories(categoryRepository.count());
        dashboard.setTotalAvailableItems(itemService.countAvailableItems());
        dashboard.setTotalBorrowedItems(itemService.countTotalBorrowed());
        dashboard.setTotalUsers(userRepository.countByActiveTrue());
        dashboard.setTotalLoans(loanService.countAllLoans());
        dashboard.setLowStockItems(itemService.countLowStockItems());
        dashboard.setRecentLoans(loanService.getRecentLoans(5));
        dashboard.setMonthlyActivity(loanService.getMonthlyActivity());
        return dashboard;
    }
}

