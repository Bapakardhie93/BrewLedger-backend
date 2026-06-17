# BrewLedger API Documentation

Dokumentasi kontrak REST API BrewLedger berdasarkan implementasi backend saat ini. Dokumen ini dirancang lengkap sebagai panduan audit dan integrasi bagi frontend client (macOS/iOS SwiftUI).

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

Selain login, semua request memerlukan header berikut:

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

---

### Ringkasan Endpoint

| Method | Endpoint | Akses | Fungsi |
|---|---|---|---|
| **Auth** | | | |
| `POST` | `/api/auth/login` | Publik | Login pengguna |
| `POST` | `/api/auth/change-password` | Login | Ganti password pengguna |
| `GET` | `/api/auth/me` | Login | Ambil data profil pengguna yang sedang login |
| `GET` | `/api/test` | Login | Uji validitas token JWT |
| **User Presence & Mgmt** | | | |
| `GET` | `/api/users` | Login | Daftar seluruh user |
| `GET` | `/api/users/{id}` | Login | Detail user berdasarkan ID |
| `GET` | `/api/users/online` | Login | Daftar user online (mendukung filter `role`) |
| `POST` | `/api/users/heartbeat` | Login | Kirim heartbeat kehadiran user (presence tracking) |
| `POST` | `/api/users` | MANAGEMENT | Buat user baru |
| `PUT` | `/api/users/{id}` | MANAGEMENT | Perbarui data user |
| `DELETE` | `/api/users/{id}` | MANAGEMENT | Hapus user |
| `PATCH` | `/api/users/{id}/activate` | MANAGEMENT | Aktifkan user |
| `PATCH` | `/api/users/{id}/deactivate` | MANAGEMENT | Nonaktifkan user |
| **POS / Cashier Shift** | | | |
| `GET` | `/api/pos/catalog` | KASIR/MANAGEMENT | Katalog POS (dengan kalkulasi order maks) |
| `GET` | `/api/pos/summary` | KASIR/MANAGEMENT | Ringkasan dashboard kasir (POS Summary) |
| `POST` | `/api/pos/checkout` | KASIR/MANAGEMENT | Alias Checkout POS |
| `POST` | `/api/transactions` | KASIR/MANAGEMENT | Checkout POS utama |
| `GET` | `/api/transactions/my` | KASIR/MANAGEMENT | Daftar transaksi kasir login (History) |
| `GET` | `/api/transactions` | MANAGEMENT | Daftar semua transaksi |
| `GET` | `/api/transactions/{id}` | MANAGEMENT/KASIR | Detail transaksi |
| `POST` | `/api/transactions/{id}/void` | MANAGEMENT | Ajukan void transaksi (membuat ApprovalRequest) |
| `GET` | `/api/transactions/{id}/receipt` | KASIR/MANAGEMENT | Format struk belanja |
| `POST` | `/api/cashier-shifts/open` | MANAGEMENT | Buka shift kasir/gudang untuk user target |
| `POST` | `/api/cashier-shifts/{id}/close` | MANAGEMENT | Tutup shift kasir/gudang untuk user target |
| `GET` | `/api/cashier-shifts/current` | KASIR, GUDANG, MANAGEMENT | Cek shift aktif milik user login |
| `GET` | `/api/cashier-shifts` | MANAGEMENT | Daftar semua shift (dengan filter status, role, date) |
| `GET` | `/api/cashier-shifts/{id}` | MANAGEMENT, pemilik shift | Detail shift |
| `GET` | `/api/tables` | KASIR/MANAGEMENT | Daftar meja |
| `GET` | `/api/tables/{id}` | KASIR/MANAGEMENT | Detail meja |
| `POST` | `/api/tables` | MANAGEMENT | Buat meja baru |
| `PUT` | `/api/tables/{id}` | MANAGEMENT | Perbarui meja |
| `PATCH` | `/api/tables/{id}/status` | KASIR/MANAGEMENT | Ubah status meja |
| `DELETE` | `/api/tables/{id}` | MANAGEMENT | Hapus meja |
| **Kitchen** | | | |
| `GET` | `/api/kitchen/orders` | KASIR/MANAGEMENT | Daftar pesanan dapur (mendukung filter `cashier`) |
| `GET` | `/api/kitchen/orders/{id}` | KASIR/MANAGEMENT | Detail pesanan dapur |
| `PATCH` | `/api/kitchen/orders/{id}/status` | KASIR/MANAGEMENT | Ubah status pesanan dapur |
| **Warehouse & Inventory** | | | |
| `GET` | `/api/warehouse` | MANAGEMENT/GUDANG | Workspace inventori gudang lengkap |
| `PATCH` | `/api/warehouse/ingredients/{id}` | MANAGEMENT/GUDANG | Perbarui bahan baku gudang |
| `POST` | `/api/warehouse/ingredients/{id}/adjust-stock` | MANAGEMENT/GUDANG | Ajukan penyesuaian stok |
| `GET` | `/api/suppliers` | MANAGEMENT/GUDANG | Daftar supplier |
| `GET` | `/api/suppliers/{id}` | MANAGEMENT/GUDANG | Detail supplier |
| `POST` | `/api/suppliers` | MANAGEMENT | Buat supplier baru |
| `PUT` | `/api/suppliers/{id}` | MANAGEMENT | Perbarui supplier |
| `DELETE` | `/api/suppliers/{id}` | MANAGEMENT | Hapus supplier |
| `PATCH` | `/api/suppliers/{id}/activate` | MANAGEMENT | Aktifkan supplier |
| `PATCH` | `/api/suppliers/{id}/deactivate` | MANAGEMENT | Nonaktifkan supplier |
| `GET` | `/api/ingredients` | MANAGEMENT/GUDANG | Daftar bahan baku |
| `GET` | `/api/ingredients/search` | MANAGEMENT/GUDANG | Cari bahan baku |
| `GET` | `/api/ingredients/low-stock` | MANAGEMENT/GUDANG | Daftar bahan baku stok kritis |
| `GET` | `/api/ingredients/{id}` | MANAGEMENT/GUDANG | Detail bahan baku |
| `POST` | `/api/ingredients` | MANAGEMENT/GUDANG | Buat bahan baku langsung (bypass approval) |
| `POST` | `/api/ingredients/submit-new` | MANAGEMENT | Ajukan bahan baku baru (membuat ApprovalRequest) |
| `PUT` | `/api/ingredients/{id}` | MANAGEMENT/GUDANG | Perbarui data bahan baku |
| `DELETE` | `/api/ingredients/{id}` | MANAGEMENT | Hapus bahan baku |
| `PATCH` | `/api/ingredients/{id}/activate` | MANAGEMENT/GUDANG | Aktifkan bahan baku |
| `PATCH` | `/api/ingredients/{id}/deactivate` | MANAGEMENT/GUDANG | Nonaktifkan bahan baku |
| `GET` | `/api/product-recipes` | MANAGEMENT/GUDANG | Daftar seluruh resep |
| `GET` | `/api/product-recipes/product/{productId}` | MANAGEMENT/GUDANG | Resep per produk |
| `GET` | `/api/product-recipes/{id}` | MANAGEMENT/GUDANG | Detail resep |
| `POST` | `/api/product-recipes` | MANAGEMENT/GUDANG | Tambah komposisi resep |
| `PUT` | `/api/product-recipes/{id}` | MANAGEMENT/GUDANG | Perbarui komposisi resep |
| `DELETE` | `/api/product-recipes/{id}` | MANAGEMENT/GUDANG | Hapus komposisi resep |
| **Purchase & Stock Request** | | | |
| `GET` | `/api/purchase-orders` | MANAGEMENT/GUDANG | Daftar PO |
| `GET` | `/api/purchase-orders/{id}` | MANAGEMENT/GUDANG | Detail PO |
| `GET` | `/api/purchase-orders/{id}/items` | MANAGEMENT/GUDANG | Daftar item dalam PO |
| `POST` | `/api/purchase-orders` | MANAGEMENT/GUDANG | Buat PO baru (DRAFT) |
| `POST` | `/api/purchase-orders/{id}/items` | MANAGEMENT/GUDANG | Tambah item PO |
| `POST` | `/api/purchase-orders/{id}/submit` | MANAGEMENT/GUDANG | Ajukan approval PO |
| `POST` | `/api/purchase-orders/{id}/receive` | MANAGEMENT/GUDANG | Penerimaan barang PO (tambah stok) |
| `POST` | `/api/stock-requests` | MANAGEMENT/GUDANG | Buat pengajuan stock request |
| `GET` | `/api/stock-requests` | MANAGEMENT/GUDANG | Daftar stock request (mendukung filter `targetRole`) |
| `PATCH` | `/api/stock-requests/{id}/process` | MANAGEMENT/GUDANG | Proses stock request (status: PROCESSING) |
| `PATCH` | `/api/stock-requests/{id}/complete` | MANAGEMENT/GUDANG | Selesaikan stock request (status: COMPLETED) |
| `PATCH` | `/api/stock-requests/{id}/reject` | MANAGEMENT/GUDANG | Tolak stock request (status: REJECTED) |
| **Centralized Approvals** | | | |
| `GET` | `/api/approvals` | MANAGEMENT/GUDANG | Daftar approval terpusat (mendukung filter `targetRole`) |
| `GET` | `/api/approvals/{id}` | MANAGEMENT/GUDANG | Detail pengajuan approval |
| `POST` | `/api/approvals/{id}/approve` | MANAGEMENT/GUDANG | Setujui pengajuan terpusat |
| `POST` | `/api/approvals/{id}/reject` | MANAGEMENT/GUDANG | Tolak pengajuan terpusat |
| **Categories & Products** | | | |
| `GET` | `/api/categories` | Login | Daftar kategori produk |
| `POST` | `/api/categories` | MANAGEMENT | Buat kategori |
| `PUT` | `/api/categories/{id}` | MANAGEMENT | Perbarui kategori |
| `DELETE` | `/api/categories/{id}` | MANAGEMENT | Hapus kategori |
| `PATCH` | `/api/categories/{id}/activate` | MANAGEMENT | Aktifkan kategori |
| `PATCH` | `/api/categories/{id}/deactivate` | MANAGEMENT | Nonaktifkan kategori |
| `GET` | `/api/products` | Login | Daftar produk |
| `GET` | `/api/products/search` | Login | Cari produk |
| `POST` | `/api/products` | MANAGEMENT | Buat produk baru beserta resep nested |
| `PUT` | `/api/products/{id}` | MANAGEMENT | Perbarui produk beserta resep nested |
| `DELETE` | `/api/products/{id}` | MANAGEMENT | Hapus produk |
| `PATCH` | `/api/products/{id}/activate` | MANAGEMENT | Aktifkan produk |
| `PATCH` | `/api/products/{id}/deactivate` | MANAGEMENT | Nonaktifkan produk |
| **Reports & Audit Trail** | | | |
| `GET` | `/api/stock-movements` | MANAGEMENT/GUDANG | Audit historis pergerakan stok |
| `GET` | `/api/reports/sales` | MANAGEMENT | Laporan keuangan & analisa penjualan |
| `GET` | `/api/reports/sales/csv` | MANAGEMENT | Ekspor CSV laporan penjualan |
| `GET` | `/api/reports/purchases` | MANAGEMENT | Laporan analisa pembelian (PO) |
| `GET` | `/api/reports/purchases/csv` | MANAGEMENT | Ekspor CSV laporan pembelian |
| `GET` | `/api/reports/inventory` | MANAGEMENT | Laporan valuasi inventori |
| `GET` | `/api/reports/inventory/csv` | MANAGEMENT | Ekspor CSV laporan inventori |
| `GET` | `/api/activity-logs` | MANAGEMENT | Historis log audit aktivitas sistem |

> [!NOTE]
> `Login` pada tabel berarti seluruh role terautentikasi memiliki akses. Endpoint lain mengikuti role yang tertulis dan mengembalikan `403 Forbidden` untuk role yang tidak diizinkan.

---

## Error Response

Seluruh error response bisnis dan validasi dikembalikan dalam format standar:

```json
{
  "success": false,
  "message": "Pesan error terperinci"
}
```

### HTTP Status Code Map

| Status | Deskripsi |
|---|---|
| `400 Bad Request` | Gagal validasi data input (Bean validation gagal, atau mismatch userId query dan body). |
| `401 Unauthorized` | Login gagal, token JWT hilang, kedaluwarsa, atau tidak valid. |
| `403 Forbidden` | Peran pengguna (role) tidak diizinkan untuk mengakses resource ini. |
| `404 Not Found` | Resource yang diminta tidak terdaftar di database. |
| `409 Conflict` | Konflik status pada database (misal memproses data yang sudah selesai, atau target user sudah memiliki shift aktif). |
| `422 Unprocessable Entity` | Kegagalan aturan bisnis (misal stok kurang, shift kasir belum dibuka, atau pelanggaran validasi harga/cash negatif). |
| `500 Internal Server Error` | Terjadi kesalahan sistem internal di backend. |

---

## 1. Authentication

### Login Pengguna
Mengeluarkan token JWT baru jika username dan password cocok.

```http
POST /api/auth/login
Content-Type: application/json
```

#### Request:
```json
{
  "username": "satriyadm9311",
  "password": "my-secret-password"
}
```

#### Response `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYXRyaXlhZG05MzExIiwi...",
  "username": "satriyadm9311",
  "role": "MANAGEMENT"
}
```

### Ambil Data Profil Login (Current User)
Mendapatkan info user yang sedang aktif berdasarkan header JWT.

```http
GET /api/auth/me
Authorization: Bearer <JWT_TOKEN>
```

#### Response `200 OK`:
```json
{
  "id": 1,
  "fullName": "Satriya Dwi Mahardhika",
  "username": "satriyadm9311",
  "active": true,
  "mustChangePassword": false,
  "lastLogin": "2026-06-15T23:09:00",
  "phoneNumber": "08123456789",
  "lastActivity": "2026-06-16T23:05:12",
  "isOnline": true,
  "role": {
    "id": 1,
    "name": "MANAGEMENT",
    "description": "Pengelolaan penuh sistem, bisnis, user, dan laporan"
  }
}
```

### Ganti Password
Mengubah password user yang sedang login.

```http
POST /api/auth/change-password
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

#### Request:
```json
{
  "oldPassword": "temporary-password",
  "newPassword": "newSecretPassword123"
}
```

#### Response `200 OK` (atau melempar `422` jika password lama salah):
*(No body)*

---

## 2. User Presence & Management

### Send Heartbeat (Presence Tracking)
Digunakan oleh frontend client untuk memperbarui status aktif secara real-time.

```http
POST /api/users/heartbeat
Authorization: Bearer <JWT_TOKEN>
```

* **Rekomendasi Frontend**: Kirim heartbeat request setiap **1 menit** sekali menggunakan timer latar belakang.
* **Logika Status Online**: User dinilai online (`isOnline: true`) jika `lastActivity` terdeteksi dalam **5 menit terakhir** sejak saat ini.

### Daftar User Online
Mendapatkan user yang status aktivitasnya masih aktif (online), opsional difilter berdasarkan nama role.

```http
GET /api/users/online?role=GUDANG
Authorization: Bearer <JWT_TOKEN>
```

#### Response `200 OK`:
```json
[
  {
    "id": 5,
    "fullName": "Budi Gudang",
    "username": "gudang_budi",
    "active": true,
    "mustChangePassword": false,
    "lastLogin": "2026-06-16T22:00:00",
    "phoneNumber": "08129999999",
    "lastActivity": "2026-06-16T23:10:00",
    "isOnline": true,
    "role": {
      "id": 2,
      "name": "GUDANG",
      "description": "Warehouse"
    }
  }
]
```

### CRUD User (Hanya MANAGEMENT)
Mutasi user seperti membuat, mengubah, menghapus, mengaktifkan, dan menonaktifkan dibatasi hanya untuk role `MANAGEMENT`.

> [!IMPORTANT]
> **Batasan Peran Pengguna (Role Constraints)**:
> Sistem secara ketat hanya mendukung 3 peran bawaan: `MANAGEMENT`, `GUDANG`, dan `KASIR`. Upaya untuk membuat atau memperbarui user dengan role lain akan ditolak dengan error `400 Bad Request` dan pesan:
> ```json
> {
>   "success": false,
>   "message": "Role tidak valid. Hanya role KASIR, GUDANG, dan MANAGEMENT yang diizinkan."
> }
> ```

```http
POST /api/users
Authorization: Bearer <JWT_TOKEN>
```
#### Request:
```json
{
  "fullName": "Jane Kasir",
  "username": "jane.kasir",
  "password": "temporary-password",
  "roleId": 3,
  "phoneNumber": "08122334455"
}
```

---

## 3. Product & Composition

Setiap penambahan atau pembaruan produk dapat menyertakan komposisi resep bahan baku langsung di dalam satu form payload (nested composition).

### Buat Produk Baru beserta Resep
```http
POST /api/products
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

#### Request:
* `useCustomHpp`: Jika `true`, maka HPP produk diset manual lewat nilai `customHpp`. Jika `false`, HPP dihitung otomatis dari komposisi bahan baku (`calculatedHpp`).
* `margin`: Margin keuntungan target (%).
```json
{
  "code": "PROD-MATCHA",
  "name": "Matcha Latte",
  "categoryId": 2,
  "sellingPrice": 25000.0,
  "description": "Matcha Drink dengan Susu UHT",
  "useCustomHpp": false,
  "customHpp": 0.0,
  "margin": 50.0,
  "recipeItems": [
    {
      "ingredientId": 4,
      "quantityRequired": 100.0
    },
    {
      "ingredientId": 5,
      "quantityRequired": 12.0
    }
  ]
}
```

#### Response `201 Created`:
* `calculatedHpp`: Jumlah total HPP dinamis dari komposisi bahan baku.
* `hpp`: HPP aktif produk (dapat diset manual atau otomatis).
* `recommendedSellingPrice`: Rekomendasi harga jual produk yang dihitung dengan rumus:
  $$\text{Recommended Selling Price} = \frac{\text{HPP}}{1 - \frac{\text{margin}}{100}}$$
```json
{
  "id": 10,
  "code": "PROD-MATCHA",
  "name": "Matcha Latte",
  "categoryName": "Non-Kopi",
  "sellingPrice": 25000.0,
  "description": "Matcha Drink dengan Susu UHT",
  "active": true,
  "useCustomHpp": false,
  "customHpp": 0.0,
  "calculatedHpp": 10080.0,
  "hpp": 10080.0,
  "margin": 50.0,
  "recommendedSellingPrice": 20160.0,
  "recipeItems": [
    {
      "id": 15,
      "productName": "Matcha Latte",
      "ingredientName": "Susu UHT",
      "quantityRequired": 100.0
    },
    {
      "id": 16,
      "productName": "Matcha Latte",
      "ingredientName": "Bubuk Matcha",
      "quantityRequired": 12.0
    }
  ]
}
```

---

## 4. Bahan Baku & Perhitungan HPP

### Validasi Angka Input
Saat membuat atau mengubah bahan baku (`Ingredient`), backend menerapkan validasi angka ketat:
1. `purchasePrice` tidak boleh negatif.
2. `costPrice` tidak boleh negatif.
3. `packSize` harus bernilai positif (`> 0.0`) jika `purchasePrice` disediakan.

Kegagalan aturan ini memicu error `422 Unprocessable Entity` dengan pesan validasi numerik.

### Rumus Perhitungan Biaya Bahan Baku Per Unit
Jika `purchasePrice` (harga pembelian bulk) dan `packSize` (isi kemasan pack) diisi, backend menghitung `costPrice` secara otomatis:
$$\text{costPrice} = \frac{\text{purchasePrice}}{\text{packSize}}$$
*Contoh: Pembelian Susu UHT Rp24.000 dengan kemasan 1000ml menghasilkan unit cost Rp24/ml.*

---

## 5. POS Checkout, Shift & History

### Buka Shift Kasir/Gudang oleh Manajemen
Membuka shift kerja untuk user operasional target. Hanya dapat dipicu oleh role `MANAGEMENT`. `KASIR` dan `GUDANG` tidak boleh membuka shift-nya sendiri.

```http
POST /api/cashier-shifts/open?userId=3
Authorization: Bearer <JWT_TOKEN_MANAGEMENT>
Content-Type: application/json
```

* **Query & Body parameter**: Backend menerima `userId` dari query param maupun request body. Jika keduanya diisi, nilainya harus sama (mismatch menghasilkan `400 Bad Request`).
* **Validasi**: Target user ID wajib mengarah ke user aktif. Peran target user hanya boleh `KASIR` atau `GUDANG`. User target tidak boleh memiliki shift aktif (OPEN) yang belum ditutup (memicu `409 Conflict`). Nilai `openingCash` tidak boleh negatif (memicu `422`). Jika target role `GUDANG`, `openingCash` diperbolehkan bernilai `0.0`.

#### Request Body:
```json
{
  "userId": 3,
  "openingCash": 100000.0,
  "notes": "Shift pagi kasir outlet"
}
```

#### Response `201 Created` / `200 OK`:
```json
{
  "id": 12,
  "cashierId": 3,
  "cashierName": "Jane Kasir",
  "cashierUsername": "jane.kasir",
  "cashierRole": "KASIR",
  "openingCash": 100000.0,
  "closingCash": null,
  "expectedCash": null,
  "cashDifference": null,
  "openedAt": "2026-06-16T09:00:00",
  "closedAt": null,
  "status": "OPEN",
  "notes": "Shift pagi kasir outlet"
}
```

### Tutup Shift Kerja oleh Manajemen
Menutup shift kerja target user. Hanya dapat diakses oleh role `MANAGEMENT`.

```http
POST /api/cashier-shifts/{id}/close
Authorization: Bearer <JWT_TOKEN_MANAGEMENT>
Content-Type: application/json
```

* **Validasi**: Hanya untuk role `MANAGEMENT`. Shift harus ada dan statusnya wajib masih `OPEN`. Nilai `closingCash` tidak boleh negatif.
* **Kalkulasi**: Backend menghitung secara otomatis:
  * `expectedCash` = `openingCash` + `totalCashTransactionsDuringShift`
  * `cashDifference` = `closingCash` - `expectedCash`

#### Request Body:
```json
{
  "closingCash": 250000.0,
  "notes": "Shift ditutup oleh supervisor"
}
```

#### Response `200 OK`:
```json
{
  "id": 12,
  "cashierId": 3,
  "cashierName": "Jane Kasir",
  "cashierUsername": "jane.kasir",
  "cashierRole": "KASIR",
  "openingCash": 100000.0,
  "closingCash": 250000.0,
  "expectedCash": 240000.0,
  "cashDifference": 10000.0,
  "openedAt": "2026-06-16T09:00:00",
  "closedAt": "2026-06-16T17:00:00",
  "status": "CLOSED",
  "notes": "Shift ditutup oleh supervisor"
}
```

### Cek Shift Aktif Pengguna Login (Current Shift)
Mengecek status shift aktif milik pengguna yang sedang login. Digunakan oleh POS untuk validasi sebelum melayani transaksi.

```http
GET /api/cashier-shifts/current
Authorization: Bearer <JWT_TOKEN>
```

* **Response**:
  * `200 OK` dengan detail shift jika ada shift aktif (OPEN).
  * `204 No Content` jika tidak ada shift aktif (menyatakan status "belum aktif").

### Daftar Semua Shift (List All)
Mendapatkan semua riwayat shift kasir/gudang. Hanya diizinkan bagi role `MANAGEMENT`.

```http
GET /api/cashier-shifts?status=OPEN&role=KASIR&date=2026-06-16
Authorization: Bearer <JWT_TOKEN_MANAGEMENT>
```
* **Query parameters (Opsional)**: `status` (OPEN/CLOSED), `role` (KASIR/GUDANG), `date` (format `yyyy-MM-dd` mencocokkan tanggal pembukaan).

### Detail Shift Kerja
Mendapatkan rincian shift berdasarkan ID.

```http
GET /api/cashier-shifts/{id}
Authorization: Bearer <JWT_TOKEN>
```
* **Akses**: Diizinkan bagi role `MANAGEMENT` atau pemilik shift bersangkutan (`pemilik shift`). Jika peran lain mencoba mengakses milik user lain, mengembalikan `403 Forbidden`.

### Ringkasan Kasir (POS Summary)
Mendapatkan statistik ringkas kasir untuk tampilan dashboard POS.

```http
GET /api/pos/summary
Authorization: Bearer <JWT_TOKEN>
```

#### Response `200 OK`:
```json
{
  "shiftActive": true,
  "activeShiftId": 1,
  "todaySalesCount": 1,
  "todaySalesAmount": 16650.0,
  "pendingKitchenOrdersCount": 0
}
```

### Riwayat Transaksi Pribadi (My History)
Mendapatkan daftar transaksi kasir yang sedang login.

```http
GET /api/transactions/my
Authorization: Bearer <JWT_TOKEN>
```

#### Response `200 OK`:
*(Mengembalikan array list `TransactionResponse` diurutkan dari yang paling baru).*

---

## 6. Kitchen Order (Pesanan Dapur)

Merekam pesanan makanan/minuman yang dikirim ke layar dapur secara otomatis setelah transaksi selesai.

### Daftar Pesanan Dapur
Mendukung filter query parameter `cashier` untuk melihat pesanan yang diinput kasir tertentu atau kasir yang sedang aktif login (`current`).

```http
GET /api/kitchen/orders?cashier=current
Authorization: Bearer <JWT_TOKEN>
```

#### Response `200 OK`:
```json
[
  {
    "id": 1,
    "transactionId": 4,
    "transactionNumber": "TRX-1781626216791",
    "tableNumber": "12",
    "status": "WAITING",
    "notes": "Dine In Meja 12",
    "createdAt": "2026-06-16T23:10:15",
    "items": [
      {
        "id": 1,
        "productName": "Espresso",
        "quantity": 2,
        "notes": "Less sugar"
      }
    ]
  }
]
```

---

## 7. Stock Request

Digunakan untuk mengajukan permintaan bahan baku antar departemen (outlet/bar ke gudang utama).

### Buat Stock Request
```http
POST /api/stock-requests
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```
#### Request:
```json
{
  "ingredientId": 1,
  "requestedQuantity": 50.0,
  "notes": "Susu UHT untuk stok espresso bar"
}
```

### Daftar Stock Request (dengan filter targetRole)
```http
GET /api/stock-requests?targetRole=MANAGEMENT
Authorization: Bearer <JWT_TOKEN>
```

#### Response `200 OK`:
* `requestedByRole`: Peran dari pembuat pengajuan.
* `targetRole`: Peran dari penerima pengajuan yang wajib menyetujui.
* `type`: Selalu bernilai `"STOCK_REQUEST"`.
* `rejectReason`: Alasan jika stock request ditolak.
```json
[
  {
    "id": 1,
    "requestNumber": "SR-1781626216812",
    "ingredientId": 1,
    "ingredientCode": "ING-001",
    "ingredientName": "Arabica Bean",
    "ingredientUnit": "gram",
    "requestedQuantity": 50.0,
    "notes": "Susu UHT untuk stok espresso bar",
    "status": "REQUESTED",
    "requestedBy": "gudang_user",
    "requestedByRole": "GUDANG",
    "targetRole": "MANAGEMENT",
    "type": "STOCK_REQUEST",
    "rejectReason": null,
    "requestedAt": "2026-06-16T23:10:15",
    "processedBy": null,
    "processedAt": null,
    "completedAt": null
  }
]
```

### Tolak Stock Request (Status: REJECTED)
```http
PATCH /api/stock-requests/{id}/reject
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```
#### Request:
```json
{
  "reason": "Stok gudang pusat sedang kritis"
}
```

---

## 8. Persetujuan Terpusat (Centralized Approvals)

Menampung seluruh perizinan transaksi sensitif (seperti void transaksi, penyesuaian stok manual, atau pengajuan bahan baku baru) ke dalam satu wadah persetujuan terpusat.

### Daftar Pengajuan Approval (dengan filter targetRole)
Mendapatkan semua pengajuan approval terpusat, dengan filter penerima (`targetRole`).

```http
GET /api/approvals?targetRole=GUDANG
Authorization: Bearer <JWT_TOKEN>
```

#### Response `200 OK`:
* `requestedByRole`: Peran dari pembuat pengajuan.
* `targetRole`: Peran dari penerima pengajuan yang wajib memproses.
```json
[
  {
    "id": 1,
    "requestNumber": "APR-VOID-1781626216802",
    "type": "VOID_TRANSACTION",
    "status": "PENDING",
    "requestedByUsername": "admin_user",
    "requestedByName": "Admin POS Test",
    "requestedByRole": "MANAGEMENT",
    "targetRole": "GUDANG",
    "approvedByUsername": null,
    "reason": "Pengajuan void transaksi TRX-1781626216791 oleh admin_user",
    "rejectReason": null,
    "referenceId": 4,
    "payloadJson": "{}",
    "createdAt": "2026-06-16T23:10:15",
    "requestedAt": "2026-06-16T23:10:15"
  }
]
```

---

## 9. Matriks Aturan Hak Akses & Rejection (PENTING!)

Untuk menjaga keamanan operasional dan audit kepatuhan, sistem backend menerapkan matriks validasi ketat di tingkat controller dan service:

### A. Matriks Penerimaan & Target Role

| Tipe Aksi Pengajuan | Pembuat Pengajuan | Peran Penerima (`targetRole`) | Keterangan |
|---|---|---|---|
| **STOCK_REQUEST (oleh Gudang)** | `GUDANG` | `MANAGEMENT` | Hanya Management yang berhak memproses/approve/reject. |
| **STOCK_REQUEST (oleh Management)** | `MANAGEMENT` | `GUDANG` | Hanya Gudang yang berhak memproses/approve/reject. |
| **VOID_TRANSACTION** | `MANAGEMENT` / `KASIR` | `GUDANG` | Hanya Gudang yang berhak memproses/approve/reject. |
| **STOCK_ADJUSTMENT** | `GUDANG` | `MANAGEMENT` | Hanya Management yang berhak memproses/approve/reject. |
| **NEW_INGREDIENT** | `MANAGEMENT` | `GUDANG` | Hanya Gudang yang berhak memproses/approve/reject. |

### B. Aturan Perlindungan Keamanan (Audit Constraints)

1. **Perlindungan Self-Processing (Dilarang Memproses Sendiri)**:
   * Pengguna yang **mengajukan** (pembuat request asli) **tidak boleh** memproses, menyetujui (`approve`), atau menolak (`reject`) pengajuannya sendiri.
   * Pelanggaran aturan ini menghasilkan error HTTP `403 Forbidden` / `Access Denied` dengan pesan:
     ```json
     {
       "success": false,
       "message": "Anda tidak memiliki izin untuk memproses pengajuan ini."
     }
     ```
2. **Pencocokan Target Role**:
   * Pengguna yang sedang masuk harus memiliki role aktif yang terdaftar sama dengan `targetRole` pengajuan tersebut.
   * Pengguna dengan role lain (seperti `KASIR`) yang mencoba menyetujui atau menolak pengajuan akan diblokir dengan error HTTP `403 Forbidden` / `Access Denied`.

### C. Proteksi Validasi Shift Sebelum Checkout (POS Checkout Rule)
* Pengguna dengan role **`KASIR`** wajib memiliki shift aktif (status `OPEN`) sebelum dapat melakukan checkout POS (`POST /api/transactions`).
* Jika role **`KASIR`** terdeteksi tidak memiliki shift aktif, checkout akan ditolak dengan error HTTP `422 Unprocessable Entity` dan pesan kesalahan:
  ```json
  {
    "success": false,
    "message": "Shift kasir belum dibuka oleh Manajemen."
  }
  ```

### D. Persyaratan Integrasi Frontend (UI Requirements)

* **Tombol Aksi**: Di layar aplikasi client, tombol **Approve**, **Reject**, atau **Proses** hanya boleh aktif/muncul bagi pengguna dengan role penerima (`targetRole`) **dan** bukan pembuat request.
* **Pesanan/Pengajuan Sendiri**: Tampilkan status pengajuan secara informatif saja ("Menunggu Persetujuan"), dan hilangkan pilihan aksi tombol.
* **Penyampaian Error**: Jika API mengembalikan status `403`, frontend wajib menangani error tersebut secara anggun dan menampilkan pesan konsisten: **“Anda tidak memiliki izin untuk memproses pengajuan ini.”**

---

## 10. WhatsApp Integrasi untuk Kontak Gudang

Saat terjadi kondisi stok menipis (critical/low stock), aplikasi kasir/POS dapat mencari kontak staf gudang yang sedang **online** (`isOnline: true`) melalui `/api/users/online?role=GUDANG`.

Frontend dapat memfasilitasi komunikasi langsung dengan menampilkan tombol kirim pesan instan melalui WhatsApp API menggunakan format URL:

```swift
let waURL = "https://wa.me/\(phoneNumber)?text=\(messageText.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "")"
```

* **Format Nomor Telepon**: Gunakan kode negara di depan nomor (contoh: `628123456789`).
* **Pesan Contoh**: *"Halo Staf Gudang, stok bahan baku Susu UHT terdeteksi menipis di POS outlet. Mohon segera dicek. Terima kasih."*

---

## 11. Endpoint Health Check, Reports, & Audit Trail Update

### A. Health Check Endpoint
* **URL**: `GET /api/health`
* **Authorization**: Tidak memerlukan autentikasi (`permitAll`)
* **Response (200 OK)**:
  ```json
  {
    "status": "UP",
    "database": "UP",
    "timestamp": "2026-06-17T10:00:00.123456"
  }
  ```

### B. Standard Report Query Parameters
* **Endpoints**: 
  * `GET /api/reports/sales`
  * `GET /api/reports/sales/csv`
  * `GET /api/reports/purchases`
  * `GET /api/reports/purchases/csv`
* **Query Parameters**:
  * `from` / `startDate`: Format `yyyy-MM-dd`
  * `to` / `endDate`: Format `yyyy-MM-dd`
  * `groupBy`: `DAY` (default), `WEEK`, `MONTH`
* **CSV Content-Type**: `text/csv; charset=utf-8` dengan header `Content-Disposition`.

### C. Conflict Error Matrix (409 Conflict)
* **Aturan**: Pengajuan approval (Void, Stock Adjustment, New Ingredient) atau Purchase Order yang statusnya sudah final (telah disetujui, ditolak, atau diproses) **tidak boleh** diproses kembali.
* **Response (409 Conflict)**:
  ```json
  {
    "success": false,
    "message": "Hanya pengajuan PENDING yang dapat disetujui / ditolak."
  }
  ```

### D. Stock Movement Response Format
* **Endpoint**: `GET /api/warehouse` / `GET /api/stock-movements`
* **Response JSON Structure**:
  ```json
  {
    "id": 100,
    "ingredientId": 1,
    "ingredientName": "Fresh Milk",
    "movementType": "SALE_CONSUMPTION",
    "quantity": -150.0,
    "stockBefore": 1000.0,
    "stockAfter": 850.0,
    "referenceNumber": "TRX-20260617-0001",
    "movementDate": "2026-06-17T10:00:00",
    "createdBy": "jane.kasir"
  }
  ```
* **Movement Types**: `SALE_CONSUMPTION`, `PURCHASE_RECEIVE`, `STOCK_REQUEST_COMPLETED`, `MANUAL_ADJUSTMENT`, `VOID_REVERSAL`.
* **Quantity**: Bertanda negatif (`-`) untuk pengurangan stok dan positif (`+`) untuk penambahan stok.

