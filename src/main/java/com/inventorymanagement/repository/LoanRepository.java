package com.inventorymanagement.repository;

import com.inventorymanagement.entity.Loan;
import com.inventorymanagement.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findByLoanCode(String loanCode);

    @Query("SELECT l FROM Loan l LEFT JOIN FETCH l.borrower LEFT JOIN FETCH l.approvedBy WHERE l.id = :id")
    Optional<Loan> findByIdWithDetails(@Param("id") Long id);

    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
    Page<Loan> findByBorrowerId(Long borrowerId, Pageable pageable);

    @Query("SELECT l FROM Loan l LEFT JOIN FETCH l.borrower WHERE l.status = :status ORDER BY l.createdAt DESC")
    List<Loan> findByStatusWithBorrower(@Param("status") LoanStatus status);

    @Query("SELECT l FROM Loan l LEFT JOIN FETCH l.borrower ORDER BY l.createdAt DESC")
    List<Loan> findRecentLoans(Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.status = 'BORROWED' AND l.dueDate < :today")
    List<Loan> findOverdueLoans(@Param("today") LocalDate today);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = :status")
    long countByStatus(@Param("status") LoanStatus status);

    @Query("SELECT l FROM Loan l LEFT JOIN FETCH l.borrower WHERE " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:borrowerId IS NULL OR l.borrower.id = :borrowerId) AND " +
           "(:startDate IS NULL OR l.loanDate >= :startDate) AND " +
           "(:endDate IS NULL OR l.loanDate <= :endDate)")
    Page<Loan> findByFilters(@Param("status") LoanStatus status,
                             @Param("borrowerId") Long borrowerId,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             Pageable pageable);

    @Query("SELECT FUNCTION('TO_CHAR', l.createdAt, 'YYYY-MM') as month, COUNT(l) FROM Loan l WHERE l.createdAt >= :since GROUP BY FUNCTION('TO_CHAR', l.createdAt, 'YYYY-MM') ORDER BY month")
    List<Object[]> countLoansByMonth(@Param("since") java.time.LocalDateTime since);
}
