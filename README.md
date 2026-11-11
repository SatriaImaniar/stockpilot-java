# Inventory Management System (Full Java Web Application)

Aplikasi Web Manajemen Inventaris profesional berbasis **Java 21 LTS**, **Spring Boot 3.5.6**, **Spring Security**, **Spring Data JPA**, **Thymeleaf**, dan **PostgreSQL** yang siap di-deploy pada Home Server menggunakan **Docker Compose** dan **Ubuntu Server 26.04 LTS**.

---

## 1. Arsitektur & Teknologi

- **Backend:** Java 21 LTS, Spring Boot 3.5.6, Spring Security, Spring Data JPA, Hibernate ORM, Bean Validation.
- **Database:** PostgreSQL 16/17 dengan migrasi skema menggunakan Flyway.
- **Frontend:** Thymeleaf Template Engine, HTML5, CSS3 kustom responsif (Dark Navy, White, Blue, Light Gray), Vanilla JS.
- **Keamanan:** BCrypt password hashing, session-based authentication, Role-based Access Control (ADMIN & STAFF), CSRF protection dengan token repository, proteksi SQL Injection via JPA.
- **Deployment:** Multi-stage Dockerfile, Docker Compose (`compose.yaml`), Linux container non-root user, Application healthcheck (`/actuator/health`).

---

## 2. Struktur Proyek

```
b:\Infentori full java\
├── .mvn/wrapper/
├── src/
│   ├── main/
│   │   ├── java/com/inventorymanagement/
│   │   │   ├── config/          # SecurityConfig, AdminBootstrapConfig, WebConfig
│   │   │   ├── controller/      # AuthController, DashboardController, ItemController, ...
│   │   │   ├── dto/             # UserDTO, ItemDTO, CategoryDTO, LoanDTO, ...
│   │   │   ├── entity/          # User, Category, Item, Loan, LoanItem, AuditLog, Role, LoanStatus
│   │   │   ├── exception/       # GlobalExceptionHandler, Custom Exceptions
│   │   │   ├── repository/      # UserRepository, ItemRepository, LoanRepository, ...
│   │   │   ├── scheduler/       # OverdueScheduler
│   │   │   ├── security/        # CustomUserDetails, CustomUserDetailsService
│   │   │   └── service/         # UserService, ItemService, CategoryService, LoanService, ...
│   │   └── resources/
│   │       ├── db/migration/    # V1__init_schema.sql (Flyway)
│   │       ├── static/css/      # style.css (Desain modern responsif)
│   │       ├── templates/       # login.html, dashboard.html, items/, categories/, loans/, ...
│   │       └── application.properties
│   └── test/
│       ├── java/com/inventorymanagement/
│       │   ├── AuthenticationSecurityTest.java   # Pengujian otentikasi & otorisasi Spring Security
│       │   └── InventoryServiceTest.java         # Pengujian logika bisnis, stok & peminjaman
│       └── resources/
│           └── application-test.properties       # H2 in-memory DB test profile
├── Dockerfile                   # Multi-stage build (Temurin 21 JDK -> JRE)
├── compose.yaml                 # Docker Compose (app + db)
├── .env.example                 # Template variabel lingkungan
└── pom.xml
```

---

## 3. Konfigurasi Lingkungan (`.env`)

Salin file `.env.example` menjadi `.env`:

```bash
cp .env.example .env
```

Isi variabel sesuai konfigurasi Anda:
```properties
# Database Configuration
DB_NAME=inventory_db
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password_here

# Application Port
APP_PORT=8080

# Bootstrap Akun Administrator (hanya dieksekusi saat pertama kali database kosong)
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@inventory.local
ADMIN_PASSWORD=your_secure_admin_password
```

---

## 4. Menjalankan di Localhost

### Persyaratan:
- JDK 21 LTS
- Maven 3.9+
- PostgreSQL 16+ aktif di `localhost:5432` dengan database `inventory_db`

### Langkah Menjalankan:
```bash
# 1. Masuk ke direktori proyek
cd "b:\Infentori full java"

# 2. Jalankan automated test
mvn clean test

# 3. Build file executable JAR
mvn clean package -DskipTests

# 4. Jalankan aplikasi
java -jar target/inventory-management-1.0.0.jar
```
Akses aplikasi melalui browser di: `http://localhost:8080` (otomatis diarahkan ke `http://localhost:8080/login`).

Akun default bootstrap:
- **Username:** `admin`
- **Password:** `admin123` (atau sesuai nilai `app.admin.password` di konfigurasi)

---

## 5. Deployment Menggunakan Docker Compose (Ubuntu Server 26.04 LTS)

### Perintah Langsung di Ubuntu Server:
```bash
# 1. Pasang Docker & Docker Compose plugin jika belum ada
sudo apt update && sudo apt install -y docker.io docker-compose-v2

# 2. Masuk ke direktori aplikasi
cd /opt/inventory-management

# 3. Siapkan environment file
cp .env.example .env
nano .env

# 4. Jalankan aplikasi di latar belakang
docker compose up -d --build

# 5. Cek status container
docker compose ps

# 6. Cek logs aplikasi secara live
docker compose logs -f app
```

### Perintah Manajemen Operasional:
```bash
# Restart container
docker compose restart

# Mengganti Port Aplikasi (misal port 9000):
# Ubah APP_PORT=9000 di file .env, lalu jalankan:
docker compose up -d

# Backup Database PostgreSQL:
docker compose exec -T db pg_dump -U postgres inventory_db > backup_$(date +%F).sql

# Restore Database PostgreSQL:
docker compose exec -T db psql -U postgres -d inventory_db < backup_2026-11-12.sql

# Mengatasi Masalah Koneksi Database:
# Pastikan container db sudah dalam status (healthy):
docker compose ps
docker compose logs db
```

---

## 6. Fitur Aplikasi yang Telah Diverifikasi

- [x] **Authentication System**:
  - Halaman login modern responsif (`/login`).
  - Penanganan error kredensial salah dengan pesan Bahasa Indonesia.
  - Penanganan notifikasi logout berhasil.
  - Enkripsi password menggunakan BCrypt.
  - CSRF Token terintegrasi secara aman pada setiap form POST.
- [x] **Role-Based Authorization**:
  - Pengguna belum login dialihkan (302) ke `/login` saat mencoba mengakses `/dashboard` atau halaman lainnya.
  - Akun Administrator dapat mengakses menu Kelola Pengguna (`/users`).
  - Akun Staff dibatasi dari menu konfigurasi administrator.
- [x] **Dashboard Modern**:
  - Kartu statistik (Total Barang, Kategori, Stok Tersedia, Sedang Dipinjam, Total Pengguna, Total Transaksi).
  - Peringatan stok rendah (stok ≤ 5 unit).
  - Tabel transaksi peminjaman terbaru dengan status badge (PENDING, BORROWED, RETURNED, dll.).
- [x] **Manajemen Barang & Kategori**:
  - CRUD lengkap dengan validasi integritas (kategori tidak dapat dihapus jika masih digunakan oleh barang).
- [x] **Sistem Peminjaman & Validasi Stok**:
  - Validasi stok tidak mencukupi (`InsufficientStockException`).
  - Pemotongan stok otomatis saat peminjaman disetujui (APPROVE).
  - Pengembalian stok otomatis saat barang dikembalikan (RETURN).
- [x] **Automated Testing**:
  - 10 automated tests JUnit 5 + MockMvc mencakup otentikasi, otorisasi, penanganan error, validasi stok, dan siklus peminjaman (10/10 PASS).

