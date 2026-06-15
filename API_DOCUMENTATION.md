# BrewLedger API Documentation

Dokumentasi kontrak REST API BrewLedger berdasarkan implementasi backend saat ini.

## Konfigurasi Dasar

| Item | Nilai |
|---|---|
| Local base URL | `http://localhost:8081` |
| API prefix | `/api` |
| Content type | `application/json` |
| Authentication | JWT Bearer |
| Token lifetime | 24 jam |
| Format tanggal | `yyyy-MM-dd` |
| Format waktu | ISO-8601, contoh `2026-06-15T10:30:00` |

Selain login, semua request memerlukan:

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

## Ringkasan Endpoint

| Method | Endpoint | Akses | Fungsi |
|---|---|---|---|
| `POST` | `/api/auth/login` | Publik | Login |
| `GET` | `/api/test` | Login | Tes JWT |
| `GET` | `/api/pos/catalog` | Kasir/Admin | Katalog POS |
| `POST` | `/api/pos/checkout` | Kasir/Admin | Checkout POS |
| `GET` | `/api/warehouse` | Gudang/Admin | Workspace gudang |
| `PATCH` | `/api/warehouse/ingredients/{id}` | Gudang/Admin | Update ingredient |
| `POST` | `/api/warehouse/ingredients/{id}/adjust-stock` | Gudang/Admin | Penyesuaian stok |
| `GET` | `/api/approvals/purchase-orders` | Management/Admin | Daftar approval PO |
| `POST` | `/api/approvals/purchase-orders/{id}/approve` | Management/Admin | Setujui PO |
| `POST` | `/api/approvals/purchase-orders/{id}/reject` | Management/Admin | Tolak PO |
| `GET` | `/api/dashboard` | Management/Admin | Dashboard lengkap |
| `GET` | `/api/dashboard/best-products` | Management/Admin | Produk terlaris |
| `GET` | `/api/users` | Admin | Daftar user |
| `GET` | `/api/users/{id}` | Admin | Detail user |
| `POST` | `/api/users` | Admin | Buat user |
| `PUT` | `/api/users/{id}` | Admin | Update user |
| `DELETE` | `/api/users/{id}` | Admin | Hapus user |
| `PATCH` | `/api/users/{id}/activate` | Admin | Aktifkan user |
| `PATCH` | `/api/users/{id}/deactivate` | Admin | Nonaktifkan user |
| `GET` | `/api/categories` | Management/Admin | Daftar kategori |
| `POST` | `/api/categories` | Management/Admin | Buat kategori |
| `GET` | `/api/products` | Login | Daftar produk |
| `GET` | `/api/products/search?keyword=` | Login | Cari produk |
| `POST` | `/api/products` | Management/Admin | Buat produk |
| `GET` | `/api/suppliers` | Gudang/Management/Admin | Daftar supplier |
| `POST` | `/api/suppliers` | Management/Admin | Buat supplier |
| `GET` | `/api/ingredients` | Gudang/Management/Admin | Daftar ingredient |
| `GET` | `/api/ingredients/search?keyword=` | Gudang/Management/Admin | Cari ingredient |
| `GET` | `/api/ingredients/low-stock` | Gudang/Management/Admin | Ingredient low stock |
| `POST` | `/api/ingredients` | Gudang/Admin | Buat ingredient |
| `GET` | `/api/product-recipes` | Gudang/Management/Admin | Daftar recipe |
| `GET` | `/api/product-recipes/product/{productId}` | Gudang/Management/Admin | Recipe per produk |
| `POST` | `/api/product-recipes` | Gudang/Admin | Tambah recipe |
| `GET` | `/api/purchase-orders` | Gudang/Management/Admin | Daftar PO |
| `GET` | `/api/purchase-orders/{id}` | Gudang/Management/Admin | Detail PO |
| `GET` | `/api/purchase-orders/{id}/items` | Gudang/Management/Admin | Item PO |
| `POST` | `/api/purchase-orders` | Gudang/Admin | Buat PO |
| `POST` | `/api/purchase-orders/{id}/items` | Gudang/Admin | Tambah item PO |
| `POST` | `/api/purchase-orders/{id}/receive` | Gudang/Admin | Terima PO |
| `GET` | `/api/transactions` | Management/Admin | Daftar transaksi |
| `GET` | `/api/transactions/{id}` | Management/Admin | Detail transaksi |
| `POST` | `/api/transactions` | Kasir/Admin | Checkout POS |
| `GET` | `/api/stock-movements` | Gudang/Management/Admin | Audit stok |
| `GET` | `/api/reports/sales` | Management/Admin | Laporan penjualan |
| `GET` | `/api/reports/purchases` | Management/Admin | Laporan pembelian |
| `GET` | `/api/reports/inventory` | Management/Admin | Laporan inventori |

`Login` pada tabel berarti seluruh role terautentikasi. Endpoint lain mengikuti role yang tertulis dan mengembalikan `403` untuk role yang tidak diizinkan.

## Error Response

Error aplikasi menggunakan bentuk:

```json
{
  "success": false,
  "message": "Pesan error"
}
```

| Status | Arti |
|---|---|
| `400 Bad Request` | Bean validation gagal |
| `401 Unauthorized` | Login gagal, user nonaktif, token hilang/tidak valid |
| `403 Forbidden` | Role tidak diizinkan |
| `404 Not Found` | Resource tidak ditemukan |
| `422 Unprocessable Entity` | Aturan bisnis gagal |
| `500 Internal Server Error` | Runtime error yang tidak ditangani secara khusus |

Response Spring Security untuk token hilang/tidak valid atau akses role dapat berbeda dari format error aplikasi di atas.

## 1. Authentication

### Login

```http
POST /api/auth/login
```

Request:

```json
{
  "username": "admin",
  "password": "change-this-admin-password"
}
```

Validasi:

- `username`: wajib.
- `password`: wajib.
- User harus aktif.

Response `200 OK`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

Token memiliki subject berupa username dan claim `role`. Response tidak memiliki field `type`; gunakan prefix `Bearer` saat mengirim token.

Contoh:

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"change-this-admin-password"}' \
  | jq -r '.token')
```

### Tes Token

```http
GET /api/test
```

Response:

```text
JWT BERHASIL
```

## API Berdasarkan Role

| Role | Endpoint utama | Kegunaan |
|---|---|---|
| `ADMIN` | Semua endpoint role-specific | Administrasi dan supervisi |
| `MANAGEMENT` | `/api/dashboard`, `/api/approvals/**`, `/api/reports/**` | Dashboard, approval, laporan |
| `KASIR` | `/api/pos/**` | Katalog dan checkout POS |
| `GUDANG` | `/api/warehouse/**`, endpoint PO operasional | Inventori, recipe, movement, request PO |

### Workspace Kasir

```http
GET /api/pos/catalog?keyword=caramel
```

Pencarian mencocokkan nama atau kode/SKU produk.

Response:

```json
{
  "cashierName": "Kasir Pagi",
  "taxRate": 0.11,
  "paymentMethods": ["CASH", "QRIS", "CARD"],
  "products": [
    {
      "id": 1,
      "code": "BEV-001",
      "name": "Caramel Macchiato",
      "categoryName": "Coffee",
      "sellingPrice": 35000.0,
      "available": true,
      "maximumOrderQuantity": 12
    }
  ]
}
```

`maximumOrderQuantity` dihitung dari stok ingredient dan recipe. Nilainya `null` untuk produk tanpa recipe. Frontend wajib memakai `taxRate` dari response; backend saat ini menghitung pajak 11%.

Checkout:

```http
POST /api/pos/checkout
```

Payload dan response sama dengan [Checkout Transaction / POS](#checkout). Metode pembayaran menerima `CASH`, `QRIS`, dan `CARD`.

### Workspace Gudang

```http
GET /api/warehouse?keyword=susu
```

Satu request menyediakan seluruh kartu dan tab pada layar gudang:

```json
{
  "generatedAt": "2026-06-15T12:30:00",
  "totalIngredients": 7,
  "totalStock": 18300.0,
  "lowStockCount": 0,
  "requestApprovalCount": 1,
  "inventory": [
    {
      "id": 1,
      "code": "ING-001",
      "name": "Susu",
      "unit": "ml",
      "currentStock": 5000.0,
      "minimumStock": 2000.0,
      "costPrice": 15000.0,
      "supplierName": "Supplier Susu",
      "stockStatus": "SAFE",
      "active": true
    }
  ],
  "productComposition": [
    {
      "recipeId": 1,
      "productId": 1,
      "productCode": "BEV-001",
      "productName": "Caramel Macchiato",
      "ingredientId": 1,
      "ingredientName": "Susu",
      "ingredientUnit": "ml",
      "quantityRequired": 150.0
    }
  ],
  "stockMovements": [],
  "approvalRequests": []
}
```

Pemetaan tab:

| Tab/kartu | Field |
|---|---|
| Total bahan baku | `totalIngredients` |
| Total stok | `totalStock` |
| Stok rendah | `lowStockCount` |
| Request approval | `requestApprovalCount` |
| Inventori bahan baku | `inventory` |
| Komposisi produk | `productComposition` |
| Riwayat pergerakan | `stockMovements` |
| Request approval | `approvalRequests` |

Update master ingredient:

```http
PATCH /api/warehouse/ingredients/{id}
```

```json
{
  "name": "Susu",
  "supplierId": 1,
  "unit": "ml",
  "minimumStock": 2000,
  "costPrice": 15000,
  "active": true
}
```

Penyesuaian stok manual:

```http
POST /api/warehouse/ingredients/{id}/adjust-stock
```

```json
{
  "newStock": 5100,
  "reason": "Hasil stock opname"
}
```

Penyesuaian membuat movement `ADJUSTMENT_IN` atau `ADJUSTMENT_OUT`.

### Workflow Approval PO

```text
DRAFT -> PENDING_APPROVAL -> APPROVED -> RECEIVED
                         \-> REJECTED -> PENDING_APPROVAL
```

Gudang mengajukan PO:

```http
POST /api/purchase-orders/{id}/submit
```

Management mengambil daftar pengajuan:

```http
GET /api/approvals/purchase-orders
```

Setujui:

```http
POST /api/approvals/purchase-orders/{id}/approve
```

Tolak:

```http
POST /api/approvals/purchase-orders/{id}/reject
```

```json
{
  "reason": "Harga perlu dinegosiasi ulang"
}
```

## 2. Dashboard

### Dashboard Lengkap

```http
GET /api/dashboard
```

Response `200 OK`:

```json
{
  "generatedAt": "2026-06-15T12:18:00",
  "todaySales": 95000.0,
  "salesChangePercentage": 5.56,
  "todayTransactions": 1,
  "transactionChangePercentage": 0.0,
  "criticalStockCount": 1,
  "pendingApprovalCount": 2,
  "activeUsers": 3,
  "totalProducts": 24,
  "totalIngredients": 50,
  "totalSuppliers": 5,
  "totalTransactions": 128,
  "totalSales": 5625000.0,
  "totalStockMovements": 256,
  "topSellingProducts": [
    {
      "productName": "Cafe Latte",
      "quantitySold": 42,
      "revenue": 1260000.0
    }
  ],
  "recentStockMovements": [
    {
      "productName": "Fresh Milk",
      "ingredientName": "Fresh Milk",
      "type": "OUT",
      "quantity": 150.0
    }
  ],
  "recentTransactions": [
    {
      "id": 128,
      "invoiceNumber": "TRX-1781519400000",
      "customerName": "Meja 4",
      "cashierName": "Kasir Pagi",
      "transactionDate": "2026-06-15T12:09:00",
      "total": 95000.0,
      "status": "PAID",
      "transactionType": "DINE_IN",
      "paymentMethod": "QRIS"
    }
  ],
  "topMovingIngredients": [
    {
      "ingredientName": "Fresh Milk",
      "totalQuantityIn": 10000.0,
      "totalQuantityOut": 4500.0,
      "totalQuantityMoved": 14500.0,
      "movementCount": 31
    }
  ],
  "lastSevenDays": [
    {
      "date": "2026-06-09",
      "dayLabel": "Sen",
      "sales": 1450000.0,
      "transactionCount": 45
    }
  ],
  "salesByCategory": [
    {
      "categoryName": "Kopi",
      "quantitySold": 74,
      "revenue": 2220000.0,
      "percentage": 74.0
    },
    {
      "categoryName": "Non-Kopi",
      "quantitySold": 26,
      "revenue": 780000.0,
      "percentage": 26.0
    }
  ],
  "criticalStocks": [
    {
      "ingredientId": 7,
      "code": "ING-007",
      "name": "Fresh Milk",
      "unit": "ml",
      "currentStock": 500.0,
      "minimumStock": 1000.0,
      "shortage": 500.0
    }
  ]
}
```

Catatan:

- `todaySales` dan `todayTransactions` hanya menghitung transaksi `PAID`.
- Persentase membandingkan hari ini dengan kemarin. Jika kemarin nol dan hari ini lebih dari nol, hasilnya `100.0`.
- `pendingApprovalCount` adalah jumlah purchase order berstatus `PENDING_APPROVAL`.
- `activeUsers` menghitung user dengan `active=true`.
- `lastSevenDays` selalu berisi tujuh tanggal termasuk hari ini.
- `salesByCategory` dihitung dari transaksi `PAID` selama tujuh hari terakhir.
- `criticalStocks` menggunakan kondisi `currentStock <= minimumStock`.
- Transaksi baru menyimpan user login sebagai kasir. Data lama tanpa kasir menggunakan `Tidak diketahui`.
- `topSellingProducts`, `recentStockMovements`, dan `topMovingIngredients` dibatasi lima data.
- `recentTransactions` dibatasi delapan data.
- `recentStockMovements.productName` dipertahankan untuk kompatibilitas. Client baru sebaiknya memakai `ingredientName`.
- `recentTransactions.customerName` berasal dari `notes`; jika kosong nilainya `Umum`.
- `totalSales` adalah total seluruh transaksi, tidak dibatasi tanggal.

Pemetaan ke dashboard frontend:

| Komponen | Field |
|---|---|
| Penjualan hari ini | `todaySales` |
| Perubahan penjualan | `salesChangePercentage` |
| Transaksi hari ini | `todayTransactions` |
| Perubahan transaksi | `transactionChangePercentage` |
| Stok kritis | `criticalStockCount`, `criticalStocks` |
| Pending approval | `pendingApprovalCount` |
| Total pendapatan | `totalSales` |
| Total transaksi | `totalTransactions` |
| Total pengguna aktif | `activeUsers` |
| Tren penjualan | `lastSevenDays[].sales` |
| Volume transaksi | `lastSevenDays[].transactionCount` |
| Diagram kategori | `salesByCategory` |
| Produk terlaris | `topSellingProducts` |
| Transaksi terbaru | `recentTransactions` |

### Produk Terlaris

```http
GET /api/dashboard/best-products?limit=5
```

Query:

| Parameter | Wajib | Default | Keterangan |
|---|---:|---:|---|
| `limit` | Tidak | `5` | Jumlah hasil |

Response:

```json
[
  {
    "productName": "Cafe Latte",
    "quantitySold": 42,
    "revenue": 1260000.0
  }
]
```

## 3. User Management

Semua endpoint pada bagian ini membutuhkan role `ADMIN`.

Role yang dibuat saat startup:

- `ADMIN`
- `MANAGEMENT`
- `GUDANG`
- `KASIR`

Belum tersedia endpoint daftar role. ID role mengikuti data database.

### Daftar User

```http
GET /api/users
```

Response:

```json
[
  {
    "id": 1,
    "fullName": "BrewLedger Administrator",
    "username": "admin",
    "active": true,
    "mustChangePassword": true,
    "lastLogin": "2026-06-15T10:30:00",
    "role": {
      "id": 1,
      "name": "ADMIN",
      "description": "Administrator sistem"
    }
  }
]
```

### Detail User

```http
GET /api/users/{id}
```

Response menggunakan bentuk user yang sama seperti daftar user.

### Buat User

```http
POST /api/users
```

Request:

```json
{
  "fullName": "Kasir Pagi",
  "username": "kasir.pagi",
  "password": "temporary-password",
  "roleId": 4
}
```

Validasi: seluruh field wajib. User baru otomatis aktif dan memiliki `mustChangePassword=true`.

Response: `201 Created` dengan object user.

### Update User

```http
PUT /api/users/{id}
```

Request:

```json
{
  "fullName": "Kasir Shift Pagi",
  "username": "kasir.pagi",
  "password": "",
  "roleId": 4
}
```

`password` opsional. Nilai null, kosong, atau hanya spasi tidak mengubah password lama.

### Hapus User

```http
DELETE /api/users/{id}
```

Response: `200 OK` tanpa body.

### Aktifkan User

```http
PATCH /api/users/{id}/activate
```

Response: `200 OK` tanpa body.

### Nonaktifkan User

```http
PATCH /api/users/{id}/deactivate
```

Response: `200 OK` tanpa body. User nonaktif tidak dapat login lagi.

## 4. Product Category

### Daftar Kategori

```http
GET /api/categories
```

Response:

```json
[
  {
    "id": 1,
    "name": "Coffee",
    "description": "Minuman berbasis kopi",
    "active": true
  }
]
```

### Buat Kategori

```http
POST /api/categories
```

Request:

```json
{
  "name": "Coffee",
  "description": "Minuman berbasis kopi"
}
```

`name` wajib dan unik.

## 5. Product

### Daftar Produk

```http
GET /api/products
```

Response:

```json
[
  {
    "id": 1,
    "code": "PRD-001",
    "name": "Cafe Latte",
    "categoryName": "Coffee",
    "sellingPrice": 30000.0,
    "description": "Espresso dan susu",
    "active": true
  }
]
```

### Cari Produk

```http
GET /api/products/search?keyword=latte
```

Pencarian dilakukan terhadap nama, case-insensitive, dengan partial match.

### Buat Produk

```http
POST /api/products
```

Request:

```json
{
  "code": "PRD-001",
  "name": "Cafe Latte",
  "categoryId": 1,
  "sellingPrice": 30000,
  "description": "Espresso dan susu"
}
```

Validasi:

- `code`: wajib dan unik.
- `name`: wajib.
- `categoryId`: wajib dan harus ada.
- `sellingPrice`: wajib dan lebih dari nol.
- `description`: opsional.

## 6. Supplier

### Daftar Supplier

```http
GET /api/suppliers
```

Response:

```json
[
  {
    "id": 1,
    "name": "PT Biji Kopi",
    "contactPerson": "Budi",
    "phone": "08123456789",
    "email": "sales@example.com",
    "address": "Jakarta",
    "active": true
  }
]
```

### Buat Supplier

```http
POST /api/suppliers
```

Request:

```json
{
  "name": "PT Biji Kopi",
  "contactPerson": "Budi",
  "phone": "08123456789",
  "email": "sales@example.com",
  "address": "Jakarta"
}
```

Hanya `name` yang wajib. Nama supplier harus unik sesuai pemeriksaan service.

## 7. Ingredient

### Daftar Ingredient

```http
GET /api/ingredients
```

Response:

```json
[
  {
    "id": 1,
    "code": "ING-001",
    "name": "Arabica Bean",
    "supplierName": "PT Biji Kopi",
    "unit": "gram",
    "currentStock": 5000.0,
    "minimumStock": 1000.0,
    "costPrice": 150.0,
    "active": true
  }
]
```

### Cari Ingredient

```http
GET /api/ingredients/search?keyword=arabica
```

Pencarian dilakukan terhadap nama, case-insensitive, dengan partial match.

### Low Stock

```http
GET /api/ingredients/low-stock
```

Response:

```json
[
  {
    "id": 1,
    "code": "ING-001",
    "name": "Arabica Bean",
    "currentStock": 800.0,
    "minimumStock": 1000.0
  }
]
```

Kondisi low stock adalah `currentStock <= minimumStock`.

### Buat Ingredient

```http
POST /api/ingredients
```

Request:

```json
{
  "code": "ING-001",
  "name": "Arabica Bean",
  "supplierId": 1,
  "unit": "gram",
  "minimumStock": 1000,
  "costPrice": 150
}
```

Validasi:

- `code`, `name`, dan `unit`: wajib.
- `code`: unik.
- `supplierId`: wajib dan harus ada.
- `minimumStock` dan `costPrice`: wajib, minimal nol.

`currentStock` tidak diterima dari request dan selalu dimulai dari `0.0`. Stok ditambah melalui penerimaan PO.

## 8. Product Recipe

### Daftar Semua Recipe

```http
GET /api/product-recipes
```

### Recipe Berdasarkan Produk

```http
GET /api/product-recipes/product/{productId}
```

Response:

```json
[
  {
    "id": 1,
    "productName": "Cafe Latte",
    "ingredientName": "Arabica Bean",
    "quantityRequired": 18.0
  },
  {
    "id": 2,
    "productName": "Cafe Latte",
    "ingredientName": "Fresh Milk",
    "quantityRequired": 150.0
  }
]
```

### Tambah Recipe

```http
POST /api/product-recipes
```

Request:

```json
{
  "productId": 1,
  "ingredientId": 1,
  "quantityRequired": 18
}
```

Pasangan produk dan ingredient harus unik. `quantityRequired` wajib lebih dari nol.

## 9. Purchase Order

### Buat PO

```http
POST /api/purchase-orders
```

Request:

```json
{
  "supplierId": 1,
  "notes": "Restock mingguan"
}
```

Response:

```json
{
  "id": 1,
  "poNumber": "PO-1781519400000",
  "supplierName": "PT Biji Kopi",
  "orderDate": "2026-06-15",
  "status": "DRAFT",
  "notes": "Restock mingguan"
}
```

`orderDate` dibuat oleh server dan tidak diterima dari request.

### Daftar PO

```http
GET /api/purchase-orders
```

Response berupa array object PO seperti response create.

### Detail PO

```http
GET /api/purchase-orders/{id}
```

Response:

```json
{
  "id": 1,
  "poNumber": "PO-1781519400000",
  "supplierName": "PT Biji Kopi",
  "orderDate": "2026-06-15",
  "status": "DRAFT",
  "notes": "Restock mingguan",
  "items": [
    {
      "id": 1,
      "ingredientName": "Arabica Bean",
      "quantity": 5000.0,
      "unitPrice": 150.0,
      "subtotal": 750000.0
    }
  ]
}
```

### Daftar Item PO

```http
GET /api/purchase-orders/{id}/items
```

Response hanya berupa array `items` seperti pada detail PO.

### Tambah Item PO

```http
POST /api/purchase-orders/{id}/items
```

Request:

```json
{
  "ingredientId": 1,
  "quantity": 5000,
  "unitPrice": 150
}
```

`quantity` dan `unitPrice` wajib lebih dari nol. Item hanya dapat ditambahkan ketika PO berstatus `DRAFT`.

### Terima PO

```http
POST /api/purchase-orders/{id}/receive
```

Tidak memiliki request body.

Efek:

1. Memastikan PO sudah berstatus `APPROVED`.
2. Memastikan PO memiliki item.
3. Menambah stok setiap ingredient.
4. Membuat stock movement `PURCHASE`.
5. Mengubah status PO menjadi `RECEIVED`.

Response adalah object PO setelah status berubah.

## 10. Transaction / POS

### Checkout

```http
POST /api/transactions
```

Request:

```json
{
  "transactionType": "DINE_IN",
  "paymentMethod": "QRIS",
  "notes": "Meja 4",
  "items": [
    {
      "productId": 1,
      "quantity": 1
    },
    {
      "productId": 2,
      "quantity": 2
    }
  ]
}
```

Nilai yang diterima:

- `transactionType`: `DINE_IN`, `TAKE_AWAY`.
- `paymentMethod`: `CASH`, `QRIS`, `CARD`.
- `items`: minimal satu item.
- `items[].productId`: wajib dan harus ada.
- `items[].quantity`: bilangan bulat lebih dari nol.

Response:

```json
{
  "id": 1,
  "transactionNumber": "TRX-1781519400000",
  "subtotal": 30000.0,
  "tax": 3300.0,
  "total": 33300.0,
  "items": [
    {
      "productId": 1,
      "productName": "Cafe Latte",
      "quantity": 1,
      "unitPrice": 30000.0,
      "subtotal": 30000.0
    }
  ]
}
```

Aturan bisnis:

- Harga diambil dari produk pada saat checkout.
- Pajak dihitung `subtotal * 0.11`.
- Payment status otomatis `PAID`.
- Ingredient dikurangi berdasarkan `quantityRequired * quantity`.
- Stock movement `SALE` dibuat per recipe.
- Seluruh proses atomic dan di-rollback jika salah satu stok tidak cukup.
- Request tidak menerima `customerName`, `amountPaid`, diskon, atau kembalian.

Contoh error stok `422`:

```json
{
  "success": false,
  "message": "Stok tidak cukup untuk bahan: Fresh Milk. Tersedia: 100.0, Dibutuhkan: 150.0"
}
```

### Daftar Transaksi

```http
GET /api/transactions
```

Response berupa array transaction beserta item.

### Detail Transaksi

```http
GET /api/transactions/{id}
```

Response menggunakan bentuk yang sama dengan response checkout.

## 11. Stock Movement

### Daftar Pergerakan Stok

```http
GET /api/stock-movements
```

Response:

```json
[
  {
    "id": 1,
    "ingredientName": "Arabica Bean",
    "movementType": "PURCHASE",
    "quantity": 5000.0,
    "stockBefore": 0.0,
    "stockAfter": 5000.0,
    "referenceNumber": "PO-1781519400000",
    "movementDate": "2026-06-15T10:30:00"
  }
]
```

`movementType` yang dibuat sistem saat ini adalah `PURCHASE` dan `SALE`.

## 12. Reports

### Laporan Penjualan

```http
GET /api/reports/sales?startDate=2026-06-01&endDate=2026-06-15
```

Query:

| Parameter | Wajib | Default |
|---|---:|---|
| `startDate` | Tidak | Hari ini dikurangi 30 hari |
| `endDate` | Tidak | Hari ini |

Response:

```json
{
  "totalSalesAmount": 5625000.0,
  "totalTransactions": 128,
  "averageTransactionValue": 43945.3125,
  "taxAmount": 557432.43,
  "dailySales": [
    {
      "date": "2026-06-15",
      "totalSales": 450000.0,
      "transactionCount": 12
    }
  ],
  "salesByProduct": [
    {
      "productName": "Cafe Latte",
      "quantitySold": 42,
      "totalRevenue": 1260000.0
    }
  ]
}
```

Rentang tanggal bersifat inklusif. Hari tanpa transaksi tetap muncul pada `dailySales` dengan nilai nol.

### Laporan Pembelian

```http
GET /api/reports/purchases?startDate=2026-06-01&endDate=2026-06-15
```

Default query sama dengan laporan penjualan.

Response:

```json
{
  "totalPurchaseAmount": 2500000.0,
  "totalOrders": 4,
  "receivedOrders": 3,
  "draftOrders": 1,
  "purchaseBySupplier": [
    {
      "supplierName": "PT Biji Kopi",
      "orderCount": 2,
      "totalSpent": 1750000.0
    }
  ],
  "dailyPurchases": [
    {
      "date": "2026-06-15",
      "totalSpent": 750000.0,
      "orderCount": 1
    }
  ]
}
```

Nilai pembelian dihitung dari subtotal item seluruh PO dalam rentang tanggal, termasuk PO berstatus `DRAFT`.

### Laporan Inventori

```http
GET /api/reports/inventory
```

Response:

```json
{
  "totalIngredients": 2,
  "lowStockIngredientsCount": 1,
  "totalInventoryValue": 825000.0,
  "ingredientStockStatus": [
    {
      "ingredientId": 1,
      "ingredientCode": "ING-001",
      "ingredientName": "Arabica Bean",
      "currentStock": 5000.0,
      "minimumStock": 1000.0,
      "costPrice": 150.0,
      "totalValue": 750000.0,
      "isLowStock": false
    }
  ]
}
```

`totalValue` dihitung dari `currentStock * costPrice`. Pada laporan ini low stock menggunakan kondisi `currentStock < minimumStock`.

## Contoh Integrasi Swift

```swift
struct LoginRequest: Encodable {
    let username: String
    let password: String
}

struct LoginResponse: Decodable {
    let token: String
    let username: String
    let role: String
}

func authorizedRequest(url: URL, token: String) -> URLRequest {
    var request = URLRequest(url: url)
    request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    return request
}
```

Simpan token di Keychain, bukan `UserDefaults`, untuk aplikasi production. Saat menerima `401`, hapus session lokal dan minta user login kembali.

## Urutan Setup Data

Urutan yang disarankan untuk lingkungan baru:

1. Login menggunakan admin bootstrap.
2. Ambil ID role langsung dari database jika perlu membuat user.
3. Buat kategori.
4. Buat supplier.
5. Buat ingredient.
6. Buat produk.
7. Buat recipe produk.
8. Buat PO dan item PO.
9. Receive PO untuk mengisi stok.
10. Buat transaksi POS.
11. Periksa dashboard, stock movement, dan reports.
