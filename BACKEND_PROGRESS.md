# BrewLedger Backend Progress Report

## Project Information

Nama Project: BrewLedger

Jenis Aplikasi:
- Point of Sale (POS)
- Inventory Management
- Purchase Management

Target Platform:
- macOS (SwiftUI)
- iOS (SwiftUI)

Backend:
- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL (Supabase)
- JWT Authentication

---

# Arsitektur Sistem

SwiftUI macOS
↓
Spring Boot API
↓
PostgreSQL (Supabase)
↑
SwiftUI iOS

Semua aplikasi menggunakan backend yang sama.

---

# Layer Architecture

## Controller Layer

Tugas:

- Menerima request dari client
- Validasi request
- Memanggil service
- Mengembalikan response

Contoh:

- TransactionController
- ProductController
- IngredientController
- PurchaseOrderController
- DashboardController

---

## Service Layer

Pusat logika bisnis aplikasi.

Contoh:

- TransactionService
- ProductService
- IngredientService
- PurchaseOrderService
- DashboardService

Semua validasi dan proses bisnis berada di layer ini.

---

## Repository Layer

Tugas:

- Mengakses database
- Menyimpan data
- Mengambil data
- Query database

Contoh:

- ProductRepository
- IngredientRepository
- TransactionRepository
- PurchaseOrderRepository

---

# Modul Yang Sudah Selesai

## Authentication

Status: SELESAI

Fitur:

- Login
- JWT Authentication
- Role Based Access

Endpoint:

POST /api/auth/login

---

## User Management

Status: SELESAI

Fitur:

- User
- Role
- Admin Seeder

---

## Supplier Management

Status: SELESAI

Endpoint:

GET /api/suppliers

POST /api/suppliers

Fungsi:

Mengelola supplier bahan baku.

---

## Product Category

Status: SELESAI

Endpoint:

GET /api/product-categories

POST /api/product-categories

Fungsi:

Mengelola kategori produk.

Contoh:

- Coffee
- Non Coffee
- Tea
- Snack

---

## Product Management

Status: SELESAI

Endpoint:

GET /api/products

POST /api/products

Fungsi:

Mengelola produk yang dijual.

Contoh:

- Americano
- Latte
- Cappuccino

Fitur:

- Validasi kode unik
- Relasi kategori

---

## Ingredient Management

Status: SELESAI

Endpoint:

GET /api/ingredients

POST /api/ingredients

Fungsi:

Mengelola bahan baku.

Data:

- Code
- Name
- Supplier
- Unit
- Current Stock
- Minimum Stock
- Cost Price

Contoh:

- Arabica Bean
- Milk
- Sugar

---

## Product Recipe

Status: SELESAI

Fungsi:

Menghubungkan produk dengan ingredient.

Contoh:

Americano

Arabica Bean = 18 gram

Latte

Arabica Bean = 18 gram

Milk = 150 ml

Endpoint:

GET /api/product-recipes

POST /api/product-recipes

---

## Purchase Order

Status: SELESAI

Flow:

Create PO

↓

Add Item

↓

Receive

↓

Stock Bertambah

Status:

- DRAFT
- RECEIVED

Endpoint:

POST /api/purchase-orders

GET /api/purchase-orders

POST /api/purchase-orders/{id}/items

GET /api/purchase-orders/{id}/items

POST /api/purchase-orders/{id}/receive

---

## Receive Purchase Order

Status: SELESAI

Ketika receive dilakukan:

1. Ambil seluruh item PO
2. Tambahkan stock ingredient
3. Simpan stock movement
4. Ubah status menjadi RECEIVED

Contoh:

Stock Lama = 5

Receive = 100

Stock Baru = 105

---

## Transaction POS

Status: SELESAI

Endpoint:

POST /api/transactions

Flow:

Kasir membuat transaksi

↓

Sistem membaca recipe

↓

Sistem validasi stok

↓

Sistem mengurangi stok

↓

Transaksi disimpan

↓

Stock movement dibuat

---

## Validasi Stock

Status: SELESAI

Sebelum transaksi:

Current Stock >= Kebutuhan Recipe

Jika tidak cukup:

Transaksi dibatalkan.

Contoh:

Arabica Bean tersedia = 5

Recipe membutuhkan = 18

Hasil:

Stok tidak cukup

---

## Stock Movement

Status: SELESAI

Endpoint:

GET /api/stock-movements

Fungsi:

Audit seluruh perubahan stok.

Movement Type:

- PURCHASE
- SALE

Data:

- Stock Before
- Stock After
- Quantity
- Reference Number
- Movement Date

Contoh:

Purchase

5 → 105

Sale

105 → 87

---

## Dashboard

Status: SELESAI

Endpoint:

GET /api/dashboard

Menampilkan:

- Total Product
- Total Ingredient
- Total Supplier
- Total Transaction
- Total Sales
- Total Stock Movement

---

## Low Stock Alert

Status: SELESAI

Endpoint:

GET /api/ingredients/low-stock

Logic:

Current Stock < Minimum Stock

Contoh:

Current Stock = 87

Minimum Stock = 1000

Maka akan muncul alert.

---

# Alur Sistem

## Restock

Supplier

↓

Purchase Order

↓

Receive

↓

Ingredient Stock Bertambah

↓

Stock Movement PURCHASE

---

## Penjualan

Transaction

↓

Recipe Dibaca

↓

Validasi Stock

↓

Kurangi Stock

↓

Stock Movement SALE

↓

Transaksi Disimpan

---

# Progress Saat Ini

Authentication
Status: Selesai

User Management
Status: Selesai

Role Management
Status: Selesai

Supplier
Status: Selesai

Product Category
Status: Selesai

Product
Status: Selesai

Ingredient
Status: Selesai

Product Recipe
Status: Selesai

Purchase Order
Status: Selesai

Receive PO
Status: Selesai

Transaction POS
Status: Selesai

Stock Validation
Status: Selesai

Stock Movement
Status: Selesai

Dashboard
Status: Selesai

Low Stock Alert
Status: Selesai

---

# Yang Masih Kurang

## Transaction History

Endpoint:

GET /api/transactions

Status:

Belum dibuat

Digunakan untuk:

- Riwayat transaksi
- Dashboard
- Reporting

---

## Transaction Detail

Endpoint:

GET /api/transactions/{id}

Status:

Belum dibuat

---

## Purchase Order Detail

Endpoint:

GET /api/purchase-orders/{id}

Status:

Belum dibuat

---

## Sales Report

Endpoint:

GET /api/reports/sales

Status:

Belum dibuat

---

## Purchase Report

Endpoint:

GET /api/reports/purchases

Status:

Belum dibuat

---

## Inventory Report

Endpoint:

GET /api/reports/inventory

Status:

Belum dibuat

---

## Best Selling Product

Endpoint:

GET /api/dashboard/best-products

Status:

Belum dibuat

---

# Estimasi Progress

Core Business Logic

95%

Reporting

20%

Analytics

10%

Overall Backend

90% - 95%

---

# Kesimpulan

Backend BrewLedger saat ini sudah memiliki seluruh fitur inti untuk operasional coffee shop:

- Authentication
- Product Management
- Ingredient Management
- Supplier Management
- Product Recipe
- Purchase Order
- Inventory Management
- Stock Movement
- POS Transaction
- Dashboard
- Low Stock Alert

Backend sudah siap digunakan sebagai fondasi aplikasi macOS dan iOS.

Tahap berikutnya adalah:

1. Transaction History
2. Transaction Detail
3. Reporting
4. Analytics
5. SwiftUI macOS Development
6. SwiftUI iOS Development