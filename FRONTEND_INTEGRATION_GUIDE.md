# BrewLedger Frontend Integration Guide
This guide provides the API contracts, payloads, query parameters, and role constraints for the new features implemented in the BrewLedger backend. Use this document to update your macOS/iOS SwiftUI frontend.

---

## 1. User Profile & Active User Details

### Active User Profile
To display the currently logged-in user's name, role, and phone number in the navbar or sidebar on the **Monitor Orderan** (Kitchen Orders) and **Monitor Bahan Baku** (Warehouse) dashboards, use this new endpoint:

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
  "role": {
    "id": 1,
    "name": "MANAGEMENT",
    "description": "Pengelolaan penuh sistem, bisnis, user, dan laporan"
  }
}
```

### Warehouse Workspace Active User
The **Monitor Bahan Baku** workspace endpoint now directly returns the active user profile in the payload so you can display it without a separate fetch request:

```http
GET /api/warehouse
Authorization: Bearer <JWT_TOKEN>
```

#### Response `200 OK`:
```json
{
  "generatedAt": "2026-06-15T23:19:30",
  "totalIngredients": 7,
  "totalStock": 18300.0,
  "lowStockCount": 0,
  "requestApprovalCount": 1,
  "inventory": [ ... ],
  "productComposition": [ ... ],
  "stockMovements": [ ... ],
  "approvalRequests": [ ... ],
  "currentUser": {
    "id": 1,
    "fullName": "Satriya Dwi Mahardhika",
    "username": "satriyadm9311",
    "active": true,
    "mustChangePassword": false,
    "lastLogin": "2026-06-15T23:09:00",
    "phoneNumber": "08123456789",
    "role": {
      "id": 1,
      "name": "MANAGEMENT",
      "description": "Pengelolaan penuh sistem, bisnis, user, dan laporan"
    }
  }
}
```

### Profile Clicks Detail
When a user clicks on any user profile/avatar in the app, you can retrieve their full profile details (including role and phone number/WhatsApp) using this endpoint:

```http
GET /api/users/{id}
Authorization: Bearer <JWT_TOKEN>
```
*Note: This endpoint is now accessible by all authenticated roles (`MANAGEMENT`, `GUDANG`, `KASIR`).*

---

## 2. User Management Constraints & Phone Numbers
For security, user mutations are strictly restricted to the `MANAGEMENT` role. Non-management users trying to create, edit, or delete users will receive a `403 Forbidden` response.

### Create User
```http
POST /api/users
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```
#### Request Body:
```json
{
  "fullName": "Kasir Pagi",
  "username": "kasir.pagi",
  "password": "temporary-password",
  "roleId": 3,
  "phoneNumber": "08123456789"
}
```

### Update User
```http
PUT /api/users/{id}
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```
#### Request Body:
```json
{
  "fullName": "Kasir Shift Pagi",
  "username": "kasir.pagi",
  "password": "", 
  "roleId": 3,
  "phoneNumber": "08129999999"
}
```
*Note: Send an empty password string or null if you do not wish to update the password.*

---

## 3. Product HPP & Selling Price Recommendations

### Create Product with Nested Recipe & Pricing Options
When adding a new product (e.g. category Non-Kopi: "Matcha"), you can submit the ingredients recipe, HPP customization preferences, and profit margin in a single form payload.

```http
POST /api/products
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

#### Request Body (e.g. Matcha requiring 100ml Milk and 12g Matcha Powder):
```json
{
  "code": "PROD-MATCHA",
  "name": "Matcha",
  "categoryId": 2,
  "sellingPrice": 25000.0,
  "description": "Matcha Drink",
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

### Update Product with Nested Recipe & Pricing Options
```http
PUT /api/products/{id}
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```
#### Request Body:
```json
{
  "code": "PROD-MATCHA",
  "name": "Matcha Premium",
  "categoryId": 2,
  "sellingPrice": 28000.0,
  "description": "Premium Matcha Latte",
  "active": true,
  "useCustomHpp": true,
  "customHpp": 12000.0,
  "margin": 40.0,
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

### Product Detail Response Contracts
When fetching product details (e.g. listing or search), the response returns computed fields:

- `calculatedHpp`: Dynamic cost price sum of the ingredient base unit prices multiplied by quantity required.
- `hpp`: Active HPP. Returns `customHpp` if `useCustomHpp` is true, otherwise returns `calculatedHpp`.
- `recommendedSellingPrice`: Recommended price based on the margin percentage, calculated as:
  $$\text{Recommended Selling Price} = \frac{\text{HPP}}{1 - \frac{\text{margin}}{100}}$$
  *(Falls back to markup $\text{HPP} \times (1 + \text{margin}/100)$ if margin $\ge 100$).*
- `recipeItems`: Detailed list of ingredients linked to the product.

#### Response JSON:
```json
{
  "id": 10,
  "code": "PROD-MATCHA",
  "name": "Matcha",
  "categoryName": "Non Coffee",
  "sellingPrice": 25000.0,
  "description": "Matcha Drink",
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
      "productName": "Matcha",
      "ingredientName": "Susu UHT",
      "quantityRequired": 100.0
    },
    {
      "id": 16,
      "productName": "Matcha",
      "ingredientName": "Bubuk Matcha",
      "quantityRequired": 12.0
    }
  ]
}
```

---

## 4. Ingredient Package Cost Calculations
Ingredients are purchased as bulk packages but used in custom units (ml/gram). The frontend registers the packaging variables, and the backend computes the cost price per base unit automatically.

### Create/Update Ingredient
```http
POST /api/ingredients
# or PUT /api/warehouse/ingredients/{id}
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```
#### Request Body:
- `purchasePrice`: The bulk packaging cost (e.g., Rp24,000 for UHT milk, Rp320,000 for Matcha powder).
- `packSize`: The total capacity of the package in usage units (e.g., 1000 for 1L milk, 500 for 500g powder).
```json
{
  "code": "ING-UHT",
  "name": "Susu UHT",
  "supplierId": 2,
  "unit": "ml",
  "minimumStock": 1000.0,
  "costPrice": 0.0,
  "purchasePrice": 24000.0,
  "packSize": 1000.0
}
```
*Note: Backend will compute `costPrice` (cost price per base unit) as `purchasePrice / packSize` (e.g., $24000 / 1000 = \text{Rp24 per ml}$).*

---

## 5. New Ingredient Approval Workflow

### Submit New Ingredient (Management Role)
Management users can submit a request for a new ingredient to the warehouse team.

```http
POST /api/ingredients/submit-new
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```
#### Request Body:
```json
{
  "code": "ING-ALMOND",
  "name": "Susu Almond",
  "supplierId": 2,
  "unit": "ml",
  "minimumStock": 1000.0,
  "costPrice": 0.0,
  "purchasePrice": 45000.0,
  "packSize": 1000.0
}
```

#### Response `200 OK` (Pending Request):
```json
{
  "id": 5,
  "requestNumber": "APR-ING-1781540370000",
  "type": "NEW_INGREDIENT",
  "status": "PENDING",
  "requestedBy": "satriyadm9311",
  "approvedBy": null,
  "reason": "Pengajuan bahan baku baru: Susu Almond",
  "rejectReason": null,
  "referenceId": null,
  "payloadJson": "{\"code\":\"ING-ALMOND\",\"name\":\"Susu Almond\",\"supplierId\":2,\"unit\":\"ml\",\"minimumStock\":1000.0,\"costPrice\":0.0,\"purchasePrice\":45000.0,\"packSize\":1000.0}",
  "createdAt": "2026-06-15T23:20:00"
}
```

### Process Approval (Warehouse Role)
Warehouse staff (`GUDANG` role) can fetch centralized approval requests, review the new ingredient request details from `payloadJson`, and approve or reject it.

#### Fetch Pending Approvals:
```http
GET /api/approvals
Authorization: Bearer <JWT_TOKEN>
```
*Filter the responses where `type == "NEW_INGREDIENT"` and `status == "PENDING"`.*

#### Approve (by GUDANG):
```http
POST /api/approvals/{id}/approve
Authorization: Bearer <JWT_TOKEN>
```
*Response returns approved status and the created ingredient ID inside `referenceId`.*

#### Reject (by GUDANG):
```http
POST /api/approvals/{id}/reject
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```
```json
{
  "rejectReason": "Supplier yang dipilih sedang tidak aktif"
}
```

---

## 6. User Presence Tracking (Heartbeat)
To monitor whether users are currently online or offline, the frontend should periodically send a heartbeat request.

### Send Heartbeat
Send this request periodically (recommended every 1 minute) while a user is active in the application session.

```http
POST /api/users/heartbeat
Authorization: Bearer <JWT_TOKEN>
```

#### Response `200 OK`:
*(No body returned, status updated on DB).*

### Dynamic Status in User Details
When retrieving user details (via `/api/auth/me`, `/api/warehouse`, or `/api/users/{id}`), the response payload dynamically includes presence tracking fields:
* `lastActivity`: Timestamp of the last successful heartbeat or login.
* `isOnline`: Dynamic boolean (`true` or `false`), set to `true` if `lastActivity` is within the last 5 minutes.

#### Example Response Body:
```json
{
  "id": 5,
  "fullName": "Budi Gudang",
  "username": "gudang_budi",
  "active": true,
  "mustChangePassword": false,
  "lastLogin": "2026-06-16T00:15:30",
  "phoneNumber": "08123456789",
  "lastActivity": "2026-06-16T00:20:15",
  "isOnline": true,
  "role": {
    "id": 2,
    "name": "GUDANG",
    "description": "Warehouse"
  }
}
```

### Quick JavaScript/React Snippet:
```javascript
// Start heartbeat timer after successful login
const heartbeatInterval = setInterval(async () => {
  try {
    await axios.post('/api/users/heartbeat', {}, {
      headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
    });
  } catch (error) {
    console.error("Heartbeat failed", error);
    if (error.response?.status === 401) {
      clearInterval(heartbeatInterval);
    }
  }
}, 60000); // 1 minute
```

---

## Summary of Role Access Restrictions

### 7. Approval & Request Permission Matrix
To ensure secure and compliant operations, the following rules apply when processing/handling approval requests (e.g., stock adjustments, voids, ingredient approvals):

1. **If request is created by role `MANAGEMENT`**:
   - `MANAGEMENT` can only submit the request and monitor its status.
   - Only role `GUDANG` is allowed to process, approve, or reject.
   - Other roles (such as `KASIR`) are denied.
2. **If request is created by role `GUDANG`**:
   - `GUDANG` can only submit the request and monitor its status.
   - Only role `MANAGEMENT` is allowed to process, approve, or reject.
   - Other roles (such as `KASIR`) are denied.
3. **Self-Approval Prevention**:
   - The user who originally created the request is strictly forbidden from approving or rejecting their own request under any circumstances.
4. **Error Handling**:
   - If a user who is not allowed attempts to approve or reject a request, the backend will return HTTP `403 Forbidden` with the error payload:
     ```json
     {
       "success": false,
       "message": "Anda tidak memiliki izin untuk memproses pengajuan ini."
     }
     ```
   - **Frontend UI Requirements**:
     - Tombol **Approve/Reject** dan **Proses** hanya boleh muncul untuk user dengan role penerima pengajuan yang berhak (bukan pembuat).
     - Pembuat pengajuan hanya boleh melihat status pengajuan.
     - Jika API mengembalikan HTTP status `403 Forbidden`, tampilkan pesan kesalahan secara konsisten:
       **“Anda tidak memiliki izin untuk memproses pengajuan ini.”**

| Endpoint | Method | Role Allowed | Description |
|---|---|---|---|
| `/api/auth/me` | `GET` | **Any authenticated user** | Get profile of logged-in user |
| `/api/users/heartbeat` | `POST` | **Any authenticated user** | Send periodic heartbeat ping to report active status |
| `/api/users/**` (mutations) | `POST`, `PUT`, `DELETE`, `PATCH` | `MANAGEMENT` | Manage users |
| `/api/users/{id}` | `GET` | **Any authenticated user** | Click and view details of users |
| `/api/products` (mutations) | `POST`, `PUT` | `MANAGEMENT` | Create/update product & recipes |
| `/api/ingredients/submit-new` | `POST` | `MANAGEMENT` | Propose new ingredient request |
| `/api/approvals/{id}/approve` | `POST` | `GUDANG` (if created by `MANAGEMENT`) / `MANAGEMENT` (if created by `GUDANG` / others) | Approve request (strictly no self-approval) |
| `/api/approvals/{id}/reject` | `POST` | `GUDANG` (if created by `MANAGEMENT`) / `MANAGEMENT` (if created by `GUDANG` / others) | Reject request (strictly no self-approval) |
