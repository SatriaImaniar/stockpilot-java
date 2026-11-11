package com.inventorymanagement.dto;

import java.util.List;
import java.util.Map;

public class DashboardDTO {
    private long totalItems;
    private long totalCategories;
    private long totalAvailableItems;
    private long totalBorrowedItems;
    private long totalUsers;
    private long totalLoans;
    private long lowStockItems;
    private List<LoanDTO> recentLoans;
    private Map<String, Long> monthlyActivity;

    public DashboardDTO() {}

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }
    public long getTotalCategories() { return totalCategories; }
    public void setTotalCategories(long totalCategories) { this.totalCategories = totalCategories; }
    public long getTotalAvailableItems() { return totalAvailableItems; }
    public void setTotalAvailableItems(long totalAvailableItems) { this.totalAvailableItems = totalAvailableItems; }
    public long getTotalBorrowedItems() { return totalBorrowedItems; }
    public void setTotalBorrowedItems(long totalBorrowedItems) { this.totalBorrowedItems = totalBorrowedItems; }
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalLoans() { return totalLoans; }
    public void setTotalLoans(long totalLoans) { this.totalLoans = totalLoans; }
    public long getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(long lowStockItems) { this.lowStockItems = lowStockItems; }
    public List<LoanDTO> getRecentLoans() { return recentLoans; }
    public void setRecentLoans(List<LoanDTO> recentLoans) { this.recentLoans = recentLoans; }
    public Map<String, Long> getMonthlyActivity() { return monthlyActivity; }
    public void setMonthlyActivity(Map<String, Long> monthlyActivity) { this.monthlyActivity = monthlyActivity; }
}
