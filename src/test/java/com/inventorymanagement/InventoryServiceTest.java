package com.inventorymanagement;

import com.inventorymanagement.dto.CategoryDTO;
import com.inventorymanagement.dto.ItemDTO;
import com.inventorymanagement.dto.LoanDTO;
import com.inventorymanagement.dto.LoanItemDTO;
import com.inventorymanagement.entity.LoanStatus;
import com.inventorymanagement.entity.User;
import com.inventorymanagement.exception.InsufficientStockException;
import com.inventorymanagement.repository.UserRepository;
import com.inventorymanagement.service.CategoryService;
import com.inventorymanagement.service.ItemService;
import com.inventorymanagement.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InventoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Uji CRUD Kategori dan Barang")
    void testCategoryAndItemCrud() {
        CategoryDTO cat = new CategoryDTO();
        cat.setName("Peralatan Komputer");
        cat.setDescription("Komputer dan aksesoris");
        CategoryDTO savedCat = categoryService.createCategory(cat);
        assertNotNull(savedCat.getId());

        ItemDTO item = new ItemDTO();
        item.setItemCode("KOMP-001");
        item.setName("Monitor 24 Inch");
        item.setCategoryId(savedCat.getId());
        item.setQuantity(10);
        item.setStorageLocation("Gudang 1");
        ItemDTO savedItem = itemService.createItem(item);

        assertNotNull(savedItem.getId());
        assertEquals("KOMP-001", savedItem.getItemCode());
        assertEquals(10, savedItem.getAvailableQuantity());
    }

    @Test
    @DisplayName("Uji Validasi Stok: Meminjam melebihi stok yang tersedia harus melempar InsufficientStockException")
    void testInsufficientStockValidation() {
        CategoryDTO cat = new CategoryDTO();
        cat.setName("Elektronik");
        CategoryDTO savedCat = categoryService.createCategory(cat);

        ItemDTO item = new ItemDTO();
        item.setItemCode("HP-001");
        item.setName("Smartphone Test");
        item.setCategoryId(savedCat.getId());
        item.setQuantity(2);
        ItemDTO savedItem = itemService.createItem(item);

        User borrower = userRepository.findByUsername("testadmin").orElseThrow();

        LoanDTO loan = new LoanDTO();
        loan.setBorrowerId(borrower.getId());
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(5));

        LoanItemDTO loanItem = new LoanItemDTO();
        loanItem.setItemId(savedItem.getId());
        loanItem.setQuantity(5); // lebih besar dari stok (2)
        loan.setLoanItems(Collections.singletonList(loanItem));

        assertThrows(InsufficientStockException.class, () -> {
            loanService.createLoan(loan, borrower);
        });
    }

    @Test
    @DisplayName("Uji Siklus Peminjaman: Approve kurangi stok dan Return mengembalikan stok")
    void testLoanWorkflowAndStockRestoration() {
        CategoryDTO cat = new CategoryDTO();
        cat.setName("Alat Kantor");
        CategoryDTO savedCat = categoryService.createCategory(cat);

        ItemDTO item = new ItemDTO();
        item.setItemCode("PRN-001");
        item.setName("Printer Laser");
        item.setCategoryId(savedCat.getId());
        item.setQuantity(5);
        ItemDTO savedItem = itemService.createItem(item);

        User borrower = userRepository.findByUsername("testadmin").orElseThrow();

        LoanDTO loan = new LoanDTO();
        loan.setBorrowerId(borrower.getId());
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(3));

        LoanItemDTO loanItem = new LoanItemDTO();
        loanItem.setItemId(savedItem.getId());
        loanItem.setQuantity(2);
        loan.setLoanItems(Collections.singletonList(loanItem));

        LoanDTO createdLoan = loanService.createLoan(loan, borrower);
        assertEquals(LoanStatus.PENDING, createdLoan.getStatus());

        // Approve loan -> stok tersedia berkurang 5 - 2 = 3
        LoanDTO approvedLoan = loanService.approveLoan(createdLoan.getId(), borrower);
        assertEquals(LoanStatus.BORROWED, approvedLoan.getStatus());
        ItemDTO itemAfterApprove = itemService.getItemById(savedItem.getId());
        assertEquals(3, itemAfterApprove.getAvailableQuantity());

        // Return loan -> stok kembali menjadi 5
        LoanDTO returnedLoan = loanService.returnLoan(createdLoan.getId(), borrower);
        assertEquals(LoanStatus.RETURNED, returnedLoan.getStatus());
        ItemDTO itemAfterReturn = itemService.getItemById(savedItem.getId());
        assertEquals(5, itemAfterReturn.getAvailableQuantity());
    }
}

