# BrewLedger Backend Audit Report

Tanggal audit: 2026-06-22

## Scope

Audit ini membaca struktur repository, konfigurasi Spring Boot, security, workflow service, controller endpoint, test suite, dokumentasi yang ada, status Git, dan hygiene dasar repository. Audit dilakukan pada branch `main` dengan beberapa perubahan lokal yang sudah ada sebelum dokumentasi ini ditambahkan.

## Ringkasan Status

| Area | Status | Catatan |
| --- | --- | --- |
| Build dan test | PASS | `./mvnw test` sukses, 39 test hijau |
| Struktur aplikasi | PASS | Layered architecture jelas: controller, service, repository, entity, dto |
| Autentikasi | PASS dengan catatan | JWT, BCrypt, dan RBAC aktif |
| Validasi request | PASS dengan catatan | Validasi transaksi sudah diperketat untuk nested item dan nominal negatif |
| Database dev/test | PASS | H2 berjalan untuk dev/test |
| Database production | PERLU MIGRATION DISCIPLINE | Profile `prod` memakai `ddl-auto=validate` |
| Secrets hygiene | PASS dengan catatan | `.env` di-ignore, default dev masih lemah dan hanya aman untuk lokal |
| Dokumentasi | PASS | README diperbarui dan API documentation sudah tersedia |

## Verifikasi Yang Dijalankan

```bash
./mvnw test
```

Hasil:

```text
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Temuan Utama

### 1. CORS masih terlalu permisif untuk production

Lokasi: `src/main/java/com/brewledger/brewledger/backend/config/SecurityConfig.java`

Konfigurasi `setAllowedOriginPatterns(List.of("*"))` nyaman untuk development, tetapi terlalu luas untuk production. Karena `allowCredentials=true`, domain frontend production sebaiknya ditentukan eksplisit.

Rekomendasi:

- Tambahkan property seperti `BREWLEDGER_ALLOWED_ORIGINS`.
- Parse comma-separated origins dari environment.
- Gunakan domain resmi frontend di production.

### 2. Endpoint debug `/api/test` masih aktif

Lokasi: `src/main/java/com/brewledger/brewledger/backend/controller/TestController.java`

Endpoint ini membutuhkan autentikasi karena security global hanya membuka `/api/auth/**` dan `/api/health`, tetapi tetap bersifat debug.

Rekomendasi:

- Hapus sebelum production, atau aktifkan hanya pada profile `dev`.

### 3. Production membutuhkan migration database eksplisit

Lokasi:

- `src/main/resources/application-prod.properties`
- `supabase/migrations/`

Profile production memakai `spring.jpa.hibernate.ddl-auto=validate`. Ini pilihan yang aman, tetapi deployment akan gagal jika schema belum sama dengan entity terbaru.

Rekomendasi:

- Tambahkan migration untuk semua perubahan entity terbaru.
- Jalankan migration di Supabase/PostgreSQL sebelum deploy aplikasi.
- Pertimbangkan Flyway/Liquibase agar migration menjadi bagian standar build/deploy.

### 4. Default credential development lemah

Lokasi: `src/main/resources/application-dev.properties`

Default `ADMIN_USERNAME=admin` dan `ADMIN_PASSWORD=admin` hanya layak untuk lokal. Ini tidak bocor ke production bila environment production benar, tetapi mudah terbawa bila profile salah.

Rekomendasi:

- Pastikan production selalu set `SPRING_PROFILES_ACTIVE=prod`.
- Set `ADMIN_PASSWORD` kuat di secret manager.
- Rotasi admin bootstrap setelah akun management final dibuat.

### 5. SQL logging test/dev sangat verbose

Lokasi:

- `src/main/resources/application-dev.properties`
- `src/test/resources/application.properties`

`spring.jpa.show-sql=true` membantu debugging, tetapi membuat output test sangat panjang.

Rekomendasi:

- Biarkan untuk development bila dibutuhkan.
- Pertimbangkan `show-sql=false` di test jika CI log terlalu besar.

## Hal Yang Sudah Baik

- Password user di-hash dengan BCrypt.
- JWT dipakai sebagai stateless authentication.
- Method-level security aktif melalui `@EnableMethodSecurity`.
- Endpoint publik terbatas pada login dan health.
- Workflow POS berjalan atomik dan diuji.
- Approval workflow menyimpan `requestedByRole` dan `targetRole`.
- Self-approval ditolak.
- Stock movement dan activity log menjaga audit trail operasional.
- `.env`, `data/`, `target/`, IDE files, dan file lokal umum sudah di-ignore.
- Request transaksi sudah memvalidasi nested item, quantity, cash received, dan discount amount.
- Test suite mencakup workflow utama dan regresi validasi.

## Risiko Residual

- Tidak ada OpenAPI/Swagger spec otomatis, sehingga API contract masih manual di Markdown.
- Belum ada pipeline CI di repository.
- Belum ada mekanisme database migration otomatis.
- Belum ada rate limiting untuk login.
- Belum ada konfigurasi CORS berbasis environment.
- Belum ada test khusus untuk konfigurasi profile production.

## Rekomendasi Prioritas

1. Tambahkan migration SQL lengkap untuk schema production terbaru.
2. Buat GitHub Actions untuk menjalankan `./mvnw test` pada pull request.
3. Jadikan CORS configurable dari environment.
4. Hapus atau profile-gate `TestController`.
5. Tambahkan OpenAPI generation agar frontend dan backend punya contract machine-readable.
6. Tambahkan rate limiting atau lockout ringan untuk endpoint login.

## Kesimpulan

Repository berada dalam kondisi fungsional dan test suite hijau. Backend sudah layak untuk development dan staging internal. Untuk production, fokus utama sebelum go-live adalah migration database yang disiplin, hardening CORS, pengelolaan secret, dan penghapusan endpoint debug.

