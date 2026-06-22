# BrewLedger Backend

BrewLedger Backend adalah REST API untuk aplikasi Point of Sale, inventori, gudang, pembelian, dapur, meja, user presence, approval, dan pelaporan operasional kedai kopi. Proyek ini memakai Spring Boot 3.5, Java 21, Spring Security, JWT, Spring Data JPA, H2 untuk development/test, dan PostgreSQL untuk production.

## Ringkasan Teknis

| Area | Detail |
| --- | --- |
| Bahasa | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Build | Maven Wrapper |
| Database dev/test | H2, mode kompatibilitas PostgreSQL |
| Database production | PostgreSQL atau Supabase PostgreSQL |
| Security | Spring Security, JWT, BCrypt, method-level RBAC |
| Validasi | Jakarta Bean Validation |
| Observability | `/api/health`, Spring Boot Actuator dependency |
| Test | JUnit 5, Spring Boot Test, Spring Security Test |

## Fitur Utama

- Autentikasi JWT dengan role `MANAGEMENT`, `GUDANG`, dan `KASIR`.
- Bootstrap role, admin awal, dan kategori produk default.
- POS checkout atomik dengan validasi shift kasir, validasi stok berbasis resep, idempotency key, kitchen order, update meja, stock movement, dan audit log.
- Approval terpusat untuk void transaksi, adjustment stok, dan pengajuan bahan baku baru.
- Approval purchase order terpisah untuk review PO oleh management.
- Inventori bahan baku, supplier, produk, kategori produk, resep produk, dan pergerakan stok.
- Manajemen cashier shift yang dikontrol management untuk kasir maupun gudang.
- Kitchen order dan restaurant table management.
- Dashboard eksekutif, laporan sales, purchase, inventory, dan ekspor CSV.
- User heartbeat dan daftar user online.

## Arsitektur

```text
Client
  -> Controller: kontrak HTTP, validasi request, RBAC endpoint
  -> Service: logika bisnis dan boundary transaksi
  -> Repository: akses data Spring Data JPA
  -> Entity: mapping tabel database
  -> Database: H2 saat dev/test, PostgreSQL saat production
```

Struktur paket utama:

```text
src/main/java/com/brewledger/brewledger/backend/
├── config/       Security, CORS, bean aplikasi
├── controller/   REST controller
├── dto/          request dan response contract
├── entity/       JPA entity
├── enums/        status dan enum domain
├── exception/    global error handling dan custom exception
├── repository/   Spring Data repository
├── security/     JWT filter, JWT service, user details
└── service/      business logic, seeder, workflow
```

## Prasyarat

- JDK 21 atau lebih baru.
- Maven tidak wajib karena repository menyertakan `./mvnw`.
- PostgreSQL hanya diperlukan untuk profile `prod`.

Validasi versi Java:

```bash
java -version
```

## Konfigurasi

Konfigurasi umum ada di `src/main/resources/application.properties`.

Profile default adalah `dev`. Profile ini otomatis memakai database H2 file lokal di `./data/brewledger-dev` sehingga backend bisa dijalankan tanpa PostgreSQL.

### Environment Development

Nilai default development:

```properties
SERVER_PORT=8081
DEV_DB_URL=jdbc:h2:file:./data/brewledger-dev;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH
DEV_DB_USERNAME=sa
DEV_DB_PASSWORD=
ADMIN_FULLNAME=Satriya Dwi Mahardhika
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin
JWT_SECRET=dev-secret-key-dev-secret-key-dev-secret-key-dev-secret-key
JWT_EXPIRATION=86400000
```

### Environment Production

Profile `prod` membaca variabel berikut:

```properties
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8081
DB_URL=jdbc:postgresql://localhost:5432/brewledger
DB_USERNAME=postgres
DB_PASSWORD=change-me
ADMIN_FULLNAME=Initial Management Admin
ADMIN_USERNAME=admin
ADMIN_PASSWORD=change-me-now
JWT_SECRET=change-me-with-at-least-32-bytes
JWT_EXPIRATION=86400000
TELEGRAM_BOT_TOKEN=
TELEGRAM_CHAT_ID=
```

Catatan penting:

- Jangan commit `.env`; file ini sudah masuk `.gitignore`.
- Profile `prod` memakai `spring.jpa.hibernate.ddl-auto=validate`, jadi tabel harus sudah tersedia sebelum aplikasi start.
- Ganti `JWT_SECRET`, `ADMIN_PASSWORD`, dan konfigurasi CORS sebelum production.

## Menjalankan Aplikasi

Development:

```bash
./mvnw spring-boot:run
```

Server berjalan di:

```text
http://localhost:8081
```

Mengganti port:

```bash
SERVER_PORT=18081 ./mvnw spring-boot:run
```

Menjalankan dengan PostgreSQL/profile production:

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

Build JAR:

```bash
./mvnw clean package
```

Run JAR:

```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/brewledger.backend-0.0.1-SNAPSHOT.jar
```

## Bootstrap Data

Saat aplikasi start, seeder membuat:

- Role bawaan: `MANAGEMENT`, `GUDANG`, `KASIR`.
- Admin awal dari `ADMIN_FULLNAME`, `ADMIN_USERNAME`, dan `ADMIN_PASSWORD` jika username belum ada.
- Kategori produk default: `Makanan Berat`, `Makanan Ringan`, `Kopi`, `Non Kopi`, `Teh`, `Dessert`, `Paket/Bundling`, dan `Merchandise`.

Seeder admin awal saat ini membuat akun aktif dengan `mustChangePassword=false`. Untuk production, gunakan password kuat lewat environment secret dan rotasi setelah bootstrap.

## Autentikasi

Login:

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

Gunakan token response pada request berikutnya:

```http
Authorization: Bearer <JWT_TOKEN>
```

Endpoint publik:

- `POST /api/auth/login`
- `GET /api/health`

Endpoint lain membutuhkan JWT. Beberapa endpoint dibatasi lagi dengan role via `@PreAuthorize`.

## Modul API

Dokumentasi kontrak request/response lengkap ada di `API_DOCUMENTATION.md`. Ringkasan endpoint utama:

| Modul | Endpoint |
| --- | --- |
| Auth | `POST /api/auth/login`, `POST /api/auth/change-password`, `GET /api/auth/me` |
| Health | `GET /api/health` |
| Users | `GET/POST/PUT/DELETE /api/users`, heartbeat, online users |
| POS | `GET /api/pos/catalog`, `POST /api/pos/checkout`, `GET /api/pos/summary` |
| Transactions | create, list, detail, void, receipt, my transactions |
| Products | CRUD product, search, activate/deactivate |
| Categories | CRUD category, activate/deactivate |
| Ingredients | CRUD ingredient, low stock, search, submit-new approval |
| Warehouse | workspace, update ingredient, stock adjustment request |
| Suppliers | CRUD supplier, activate/deactivate |
| Product Recipes | CRUD resep per produk |
| Purchase Orders | create, items, submit, receive, detail, list |
| Purchase Approvals | pending PO, approve PO, reject PO |
| Central Approvals | list, detail, approve, reject |
| Stock Requests | create, list, process, complete, reject |
| Stock Movements | list audit movement |
| Cashier Shifts | open, close, current, list, detail |
| Kitchen Orders | list, detail, update status |
| Tables | CRUD meja, update status |
| Dashboard | overview, best products |
| Reports | sales, purchases, inventory, CSV exports |
| Activity Logs | audit activity list |

## Alur Bisnis Kunci

### POS Checkout

1. Kasir atau management mengirim checkout.
2. Sistem memvalidasi request, payment method, item, discount, cash received, dan idempotency key.
3. Jika user role `KASIR`, sistem memastikan ada shift aktif.
4. Sistem menghitung subtotal, tax, discount, total, dan change amount.
5. Sistem memvalidasi stok ingredient berdasarkan product recipe.
6. Dalam satu transaksi database, sistem membuat transaksi, item transaksi, kitchen order, stock movements, update stok, update meja, dan activity log.
7. Jika stok kurang atau validasi gagal, seluruh perubahan rollback.

### Purchase Order

1. `GUDANG` atau `MANAGEMENT` membuat PO draft.
2. Item PO ditambahkan berdasarkan ingredient dan supplier.
3. PO disubmit menjadi `PENDING_APPROVAL`.
4. `MANAGEMENT` approve atau reject melalui `/api/approvals/purchase-orders`.
5. Setelah approved, barang diterima melalui `/api/purchase-orders/{id}/receive`.
6. Penerimaan barang menambah stok dan membuat stock movement `PURCHASE_RECEIVE`.

### Central Approval

Approval terpusat menyimpan `requestedByRole` dan `targetRole`. Self-approval ditolak. Target role berjalan silang:

- Request dari `MANAGEMENT` untuk stock adjustment atau void ditargetkan ke `GUDANG`.
- Request dari `GUDANG` untuk stock adjustment atau void ditargetkan ke `MANAGEMENT`.
- New ingredient ditargetkan ke `GUDANG`.

## Testing

Jalankan semua test:

```bash
./mvnw test
```

Status audit terakhir:

```text
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Test mencakup workflow management, POS checkout, shift, purchase order, approval, CSV reports, validation request transaksi, dan application context.

## Database dan Migration

Untuk development/test, Hibernate membuat atau memperbarui skema H2.

Untuk production:

- `ddl-auto=validate`, sehingga aplikasi tidak membuat/mengubah tabel otomatis.
- Siapkan skema PostgreSQL sebelum deploy.
- Repository sudah memiliki migration Supabase di `supabase/migrations/202606150001_merge_admin_into_management.sql`.
- Pastikan skema production sesuai entity terbaru, terutama tabel approval, purchase order, cashier shift, transaction, product category, dan audit log.

## Security dan Hardening Production

Checklist sebelum production:

- Set `JWT_SECRET` panjang dan unik per environment.
- Set `ADMIN_PASSWORD` kuat, lalu rotasi setelah bootstrap.
- Batasi CORS di `SecurityConfig` ke domain frontend resmi.
- Nonaktifkan atau lindungi endpoint debug `GET /api/test` bila tidak diperlukan.
- Pastikan `.env`, database dump, dan file lokal `data/` tidak masuk Git.
- Gunakan HTTPS di reverse proxy atau platform hosting.
- Jalankan migration database secara eksplisit sebelum deploy.
- Review retention audit log dan backup database.

## Dokumentasi Tambahan

- `API_DOCUMENTATION.md`: kontrak endpoint lengkap.
- `FRONTEND_INTEGRATION_GUIDE.md`: panduan integrasi frontend.
- `HEARTBEAT_FRONTEND_GUIDE.md`: panduan heartbeat user presence.
- `BACKEND_PROGRESS.md`: catatan progres historis.
- `AUDIT_REPORT.md`: hasil audit teknis terbaru repository.
