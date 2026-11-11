package com.inventorymanagement.dto;

import com.inventorymanagement.entity.LoanStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoanDTO {
    private Long id;
    private String loanCode;

    @NotNull(message = "Borrower is required")
    private Long borrowerId;
    private String borrowerName;

    private Long approvedById;
    private String approvedByName;

    private LoanStatus status;

    @NotNull(message = "Loan date is required")
    private LocalDate loanDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private LocalDate returnDate;
    private String notes;
    private LocalDateTime createdAt;

    private List<LoanItemDTO> loanItems = new ArrayList<>();

    public LoanDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLoanCode() { return loanCode; }
    public void setLoanCode(String loanCode) { this.loanCode = loanCode; }
    public Long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(Long borrowerId) { this.borrowerId = borrowerId; }
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }
    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<LoanItemDTO> getLoanItems() { return loanItems; }
    public void setLoanItems(List<LoanItemDTO> loanItems) { this.loanItems = loanItems; }
}
