package com.inventorymanagement.config;

import com.inventorymanagement.entity.*;
import com.inventorymanagement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DataSeeder ekuivalen dengan DatabaseSeeder di Laravel.
 * Berjalan otomatis saat aplikasi pertama kali dijalankan (CommandLineRunner)
 * untuk mengisi data awal (Admin, Staff, Kategori, Barang, dan Transaksi Sampel).
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      ItemRepository itemRepository,
                      LoanRepository loanRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.loanRepository = loanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Staff User jika belum ada
        User staffUser = userRepository.findByUsername("staff").orElseGet(() -> {
            User staff = new User();
            staff.setUsername("staff");
            staff.setEmail("staff@inventory.local");
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setFullName("Budi Santoso (Staff)");
            staff.setRole(Role.STAFF);
            staff.setActive(true);
            logger.info("Seeder: Akun demo Staff berhasil dibuat (username: staff, password: staff123)");
            return userRepository.save(staff);
        });

        User adminUser = userRepository.findByUsername("admin").orElse(null);

        // 2. Seed Kategori jika masih kosong
        if (categoryRepository.count() == 0) {
            logger.info("Seeder: Mengisi data kategori sampel...");
            Category catKomp = categoryRepository.save(new Category("Peralatan Komputer", "Komputer, laptop, monitor, dan periferal"));
            Category catElek = categoryRepository.save(new Category("Elektronik & Jaringan", "Switch, router, printer, dan server"));
            Category catAv = categoryRepository.save(new Category("Audio Visual", "Proyektor, mic, kabel HDMI, dan speaker"));
            Category catAtk = categoryRepository.save(new Category("Alat Tulis Kantor", "Kertas, pulpen, map, dan perlengkapan dokumen"));

            // 3. Seed Barang jika masih kosong
            if (itemRepository.count() == 0) {
                logger.info("Seeder: Mengisi data barang inventaris sampel...");
                Item item1 = itemRepository.save(new Item("ITM-LAPTOP-01", "Laptop Lenovo ThinkPad T14", "Intel Core i7, 16GB RAM, 512GB SSD", catKomp, 12, "Gudang Utama Rak A-1"));
                Item item2 = itemRepository.save(new Item("ITM-MONITOR-02", "Monitor LG UltraGear 24 Inch", "Full HD IPS 144Hz", catKomp, 10, "Gudang Utama Rak A-2"));
                
                // Barang dengan stok rendah (<= 5) untuk menguji alert dashboard
                Item item3 = itemRepository.save(new Item("ITM-PROJ-03", "Proyektor Epson EB-E500", "3LCD 3300 Lumens HDMI", catAv, 4, "Lemari AV Lantai 2"));
                Item item4 = itemRepository.save(new Item("ITM-PRN-04", "Printer Canon PIXMA G3010", "Ink Tank Multifungsi", catElek, 3, "Ruang IT Support"));
                Item item5 = itemRepository.save(new Item("ITM-SWT-05", "Switch Cisco Gigabit 24-Port", "Managed Switch Rackmount", catElek, 8, "Server Room"));

                // 4. Seed Contoh Transaksi Peminjaman
                if (loanRepository.count() == 0 && adminUser != null) {
                    logger.info("Seeder: Mengisi contoh transaksi peminjaman...");

                    // Transaksi 1: Sedang Dipinjam (BORROWED)
                    Loan loan1 = new Loan();
                    loan1.setLoanCode("LN-20261112-0001");
                    loan1.setBorrower(staffUser);
                    loan1.setApprovedBy(adminUser);
                    loan1.setStatus(LoanStatus.BORROWED);
                    loan1.setLoanDate(LocalDate.now().minusDays(2));
                    loan1.setDueDate(LocalDate.now().plusDays(5));
                    loan1.setNotes("Peminjaman laptop untuk tugas luar kantor");

                    LoanItem li1 = new LoanItem(item1, 2);
                    loan1.addLoanItem(li1);
                    item1.setAvailableQuantity(item1.getQuantity() - 2); // 10 available
                    itemRepository.save(item1);
                    loanRepository.save(loan1);

                    // Transaksi 2: Menunggu Persetujuan (PENDING)
                    Loan loan2 = new Loan();
                    loan2.setLoanCode("LN-20261112-0002");
                    loan2.setBorrower(staffUser);
                    loan2.setStatus(LoanStatus.PENDING);
                    loan2.setLoanDate(LocalDate.now());
                    loan2.setDueDate(LocalDate.now().plusDays(3));
                    loan2.setNotes("Peminjaman proyektor untuk presentasi klien");

                    LoanItem li2 = new LoanItem(item3, 1);
                    loan2.addLoanItem(li2);
                    loanRepository.save(loan2);

                    logger.info("Seeder: Selesai mengisi database sampel.");
                }
            }
        }
    }
}
