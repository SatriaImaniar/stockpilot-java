package com.inventorymanagement.scheduler;

import com.inventorymanagement.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OverdueScheduler.class);

    private final LoanService loanService;

    public OverdueScheduler(LoanService loanService) {
        this.loanService = loanService;
    }

    @Scheduled(cron = "0 0 1 * * *") // Run at 1 AM daily
    public void checkOverdueLoans() {
        logger.info("Menjalankan pengecekan peminjaman jatuh tempo...");
        loanService.checkOverdueLoans();
        logger.info("Pengecekan peminjaman jatuh tempo selesai");
    }
}

