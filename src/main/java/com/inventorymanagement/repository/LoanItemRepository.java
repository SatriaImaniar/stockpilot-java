package com.inventorymanagement.repository;

import com.inventorymanagement.entity.LoanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanItemRepository extends JpaRepository<LoanItem, Long> {
    List<LoanItem> findByLoanId(Long loanId);

    @Query("SELECT li FROM LoanItem li LEFT JOIN FETCH li.item WHERE li.loan.id = :loanId")
    List<LoanItem> findByLoanIdWithItems(@Param("loanId") Long loanId);
}
