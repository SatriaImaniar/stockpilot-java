package com.inventorymanagement.service;

import com.inventorymanagement.dto.LoanDTO;
import com.inventorymanagement.entity.LoanStatus;
import org.springframework.stereotype.Service;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final LoanService loanService;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReportService(LoanService loanService) {
        this.loanService = loanService;
    }

    public void exportLoansToCsv(PrintWriter writer, LoanStatus status, Long borrowerId,
                                 LocalDate startDate, LocalDate endDate) {
        List<LoanDTO> loans = loanService.getAllLoansForExport(status, borrowerId, startDate, endDate);

        writer.println("Kode Peminjaman,Peminjam,Status,Tanggal Pinjam,Batas Kembali,Tanggal Kembali,Catatan");

        for (LoanDTO loan : loans) {
            writer.println(String.join(",",
                    escapeCsv(loan.getLoanCode()),
                    escapeCsv(loan.getBorrowerName()),
                    escapeCsv(loan.getStatus() != null ? loan.getStatus().name() : ""),
                    escapeCsv(loan.getLoanDate() != null ? loan.getLoanDate().format(DATE_FORMAT) : ""),
                    escapeCsv(loan.getDueDate() != null ? loan.getDueDate().format(DATE_FORMAT) : ""),
                    escapeCsv(loan.getReturnDate() != null ? loan.getReturnDate().format(DATE_FORMAT) : ""),
                    escapeCsv(loan.getNotes() != null ? loan.getNotes() : "")
            ));
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

