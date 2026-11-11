package com.inventorymanagement.service;

import com.inventorymanagement.dto.LoanDTO;
import com.inventorymanagement.dto.LoanItemDTO;
import com.inventorymanagement.entity.*;
import com.inventorymanagement.exception.InsufficientStockException;
import com.inventorymanagement.exception.InvalidOperationException;
import com.inventorymanagement.exception.ResourceNotFoundException;
import com.inventorymanagement.repository.ItemRepository;
import com.inventorymanagement.repository.LoanItemRepository;
import com.inventorymanagement.repository.LoanRepository;
import com.inventorymanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final LoanItemRepository loanItemRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public LoanService(LoanRepository loanRepository, LoanItemRepository loanItemRepository,
                       ItemRepository itemRepository, UserRepository userRepository,
                       AuditLogService auditLogService) {
        this.loanRepository = loanRepository;
        this.loanItemRepository = loanItemRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<LoanDTO> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<LoanDTO> getLoansByFilters(LoanStatus status, Long borrowerId,
                                           LocalDate startDate, LocalDate endDate,
                                           Pageable pageable) {
        return loanRepository.findByFilters(status, borrowerId, startDate, endDate, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public LoanDTO getLoanById(Long id) {
        Loan loan = loanRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));
        LoanDTO dto = toDTO(loan);
        List<LoanItem> items = loanItemRepository.findByLoanIdWithItems(id);
        dto.setLoanItems(items.stream().map(this::toLoanItemDTO).collect(Collectors.toList()));
        return dto;
    }

    public LoanDTO createLoan(LoanDTO dto, User currentUser) {
        User borrower = userRepository.findById(dto.getBorrowerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getBorrowerId()));

        if (dto.getDueDate().isBefore(dto.getLoanDate())) {
            throw new InvalidOperationException("Tanggal pengembalian harus setelah tanggal peminjaman");
        }

        Loan loan = new Loan();
        loan.setLoanCode(generateLoanCode());
        loan.setBorrower(borrower);
        loan.setLoanDate(dto.getLoanDate());
        loan.setDueDate(dto.getDueDate());
        loan.setNotes(dto.getNotes());
        loan.setStatus(LoanStatus.PENDING);

        // Process loan items
        for (LoanItemDTO itemDTO : dto.getLoanItems()) {
            Item item = itemRepository.findById(itemDTO.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemDTO.getItemId()));

            if (itemDTO.getQuantity() > item.getAvailableQuantity()) {
                throw new InsufficientStockException("Stok tidak mencukupi untuk barang '" + item.getName() +
                        "'. Tersedia: " + item.getAvailableQuantity() + ", Diminta: " + itemDTO.getQuantity());
            }

            LoanItem loanItem = new LoanItem(item, itemDTO.getQuantity());
            loan.addLoanItem(loanItem);
        }

        loan = loanRepository.save(loan);

        auditLogService.log(currentUser, "CREATE", "LOAN", loan.getId(),
                "Membuat peminjaman " + loan.getLoanCode() + " untuk " + borrower.getFullName());

        return toDTO(loan);
    }

    public LoanDTO approveLoan(Long id, User approver) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new InvalidOperationException("Hanya peminjaman dengan status PENDING yang dapat disetujui");
        }

        // Deduct stock for each item
        List<LoanItem> loanItems = loanItemRepository.findByLoanIdWithItems(id);
        for (LoanItem loanItem : loanItems) {
            Item item = loanItem.getItem();
            if (loanItem.getQuantity() > item.getAvailableQuantity()) {
                throw new InsufficientStockException("Stok tidak mencukupi untuk barang '" + item.getName() +
                        "'. Tersedia: " + item.getAvailableQuantity() + ", Diminta: " + loanItem.getQuantity());
            }
            item.setAvailableQuantity(item.getAvailableQuantity() - loanItem.getQuantity());
            itemRepository.save(item);
        }

        loan.setStatus(LoanStatus.BORROWED);
        loan.setApprovedBy(approver);
        loan = loanRepository.save(loan);

        auditLogService.log(approver, "APPROVE", "LOAN", loan.getId(),
                "Menyetujui peminjaman " + loan.getLoanCode());

        return toDTO(loan);
    }

    public LoanDTO rejectLoan(Long id, User rejector) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new InvalidOperationException("Hanya peminjaman dengan status PENDING yang dapat ditolak");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setApprovedBy(rejector);
        loan = loanRepository.save(loan);

        auditLogService.log(rejector, "REJECT", "LOAN", loan.getId(),
                "Menolak peminjaman " + loan.getLoanCode());

        return toDTO(loan);
    }

    public LoanDTO returnLoan(Long id, User currentUser) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));

        if (loan.getStatus() != LoanStatus.BORROWED && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new InvalidOperationException("Hanya peminjaman berstatus BORROWED atau OVERDUE yang dapat dikembalikan");
        }

        // Return stock for each item
        List<LoanItem> loanItems = loanItemRepository.findByLoanIdWithItems(id);
        for (LoanItem loanItem : loanItems) {
            if (!loanItem.isFullyReturned()) {
                Item item = loanItem.getItem();
                int toReturn = loanItem.getQuantity() - loanItem.getReturnedQuantity();
                item.setAvailableQuantity(item.getAvailableQuantity() + toReturn);
                loanItem.setReturnedQuantity(loanItem.getQuantity());
                itemRepository.save(item);
                loanItemRepository.save(loanItem);
            }
        }

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());
        loan = loanRepository.save(loan);

        auditLogService.log(currentUser, "RETURN", "LOAN", loan.getId(),
                "Pengembalian peminjaman " + loan.getLoanCode());

        return toDTO(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> getRecentLoans(int limit) {
        return loanRepository.findRecentLoans(PageRequest.of(0, limit)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countAllLoans() {
        return loanRepository.count();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getMonthlyActivity() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Object[]> results = loanRepository.countLoansByMonth(sixMonthsAgo);
        Map<String, Long> activity = new LinkedHashMap<>();
        for (Object[] result : results) {
            activity.put((String) result[0], (Long) result[1]);
        }
        return activity;
    }

    public void checkOverdueLoans() {
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDate.now());
        for (Loan loan : overdueLoans) {
            loan.setStatus(LoanStatus.OVERDUE);
            loanRepository.save(loan);
            logger.info("Peminjaman {} ditandai sebagai OVERDUE", loan.getLoanCode());
        }
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> getAllLoansForExport(LoanStatus status, Long borrowerId,
                                             LocalDate startDate, LocalDate endDate) {
        Page<LoanDTO> page = getLoansByFilters(status, borrowerId, startDate, endDate,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdAt")));
        return page.getContent();
    }

    private String generateLoanCode() {
        String prefix = "LN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String suffix = String.format("%04d", new Random().nextInt(10000));
        return prefix + suffix;
    }

    private LoanDTO toDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setLoanCode(loan.getLoanCode());
        dto.setStatus(loan.getStatus());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnDate(loan.getReturnDate());
        dto.setNotes(loan.getNotes());
        dto.setCreatedAt(loan.getCreatedAt());

        if (loan.getBorrower() != null) {
            dto.setBorrowerId(loan.getBorrower().getId());
            dto.setBorrowerName(loan.getBorrower().getFullName());
        }
        if (loan.getApprovedBy() != null) {
            dto.setApprovedById(loan.getApprovedBy().getId());
            dto.setApprovedByName(loan.getApprovedBy().getFullName());
        }
        return dto;
    }

    private LoanItemDTO toLoanItemDTO(LoanItem loanItem) {
        LoanItemDTO dto = new LoanItemDTO();
        dto.setId(loanItem.getId());
        dto.setQuantity(loanItem.getQuantity());
        dto.setReturnedQuantity(loanItem.getReturnedQuantity());
        if (loanItem.getItem() != null) {
            dto.setItemId(loanItem.getItem().getId());
            dto.setItemName(loanItem.getItem().getName());
            dto.setItemCode(loanItem.getItem().getItemCode());
            dto.setAvailableStock(loanItem.getItem().getAvailableQuantity());
        }
        return dto;
    }
}

