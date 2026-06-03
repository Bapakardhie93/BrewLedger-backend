# BrewLedger API Documentation (Frontend Integration Guide)

This document is specifically designed for **macOS and iOS frontend developers (SwiftUI)** integrating with the BrewLedger backend. It outlines the authentication flow, API endpoints, and data structures required to communicate with the Spring Boot server.

## Base Configuration

- **Base URL (Local)**: `http://localhost:8080`
- **Content-Type**: `application/json`
- **Authentication**: Bearer Token (JWT). You must include the token in the `Authorization` header for all requests except `/api/auth/login`.

```swift
// Example URLRequest in Swift
var request = URLRequest(url: URL(string: "http://localhost:8080/api/endpoint")!)
request.setValue("Bearer \(yourJwtToken)", forHTTPHeaderField: "Authorization")
request.setValue("application/json", forHTTPHeaderField: "Content-Type")
```

---

## 1. Authentication

### Login
- **Endpoint**: `POST /api/auth/login`
- **Auth Required**: No
- **Description**: Authenticates a user and returns a JWT token.

**Request Body (JSON):**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ROLE_ADMIN"
}
```
*Note for SwiftUI: Save this `token` in Keychain or UserDefaults to append to subsequent requests.*

---

## 2. Dashboard

### Get Dashboard Stats
- **Endpoint**: `GET /api/dashboard`
- **Auth Required**: Yes

**Response (200 OK):**
```json
{
  "totalProducts": 24,
  "totalIngredients": 50,
  "totalSuppliers": 5,
  "totalTransactions": 128,
  "totalSales": 5000000,
  "totalStockMovements": 256
}
```

---

## 3. Product & Category Management

### Get All Categories
- **Endpoint**: `GET /api/categories`
- **Auth Required**: Yes

### Create Category
- **Endpoint**: `POST /api/categories`
- **Auth Required**: Yes

**Request Body:**
```json
{
  "name": "Coffee",
  "description": "Coffee based drinks"
}
```

### Get All Products
- **Endpoint**: `GET /api/products`
- **Auth Required**: Yes

### Create Product
- **Endpoint**: `POST /api/products`
- **Auth Required**: Yes

**Request Body:**
```json
{
  "code": "PRD-001",
  "name": "Americano",
  "categoryId": 1,
  "price": 25000
}
```

### Search Products
- **Endpoint**: `GET /api/products/search?query=americano`
- **Auth Required**: Yes

---

## 4. Ingredients & Suppliers

### Get All Suppliers
- **Endpoint**: `GET /api/suppliers`
- **Auth Required**: Yes

### Create Supplier
- **Endpoint**: `POST /api/suppliers`
- **Auth Required**: Yes

**Request Body:**
```json
{
  "name": "Supplier A",
  "contact": "08123456789",
  "address": "Jakarta"
}
```

### Get All Ingredients
- **Endpoint**: `GET /api/ingredients`
- **Auth Required**: Yes

### Create Ingredient
- **Endpoint**: `POST /api/ingredients`
- **Auth Required**: Yes

**Request Body:**
```json
{
  "code": "ING-001",
  "name": "Arabica Bean",
  "supplierId": 1,
  "unit": "gram",
  "currentStock": 1000,
  "minimumStock": 200,
  "costPrice": 150
}
```

### Low Stock Alert
- **Endpoint**: `GET /api/ingredients/low-stock`
- **Auth Required**: Yes
- **Description**: Returns all ingredients where `currentStock < minimumStock`. Use this to display a badge or alert in the macOS app.

---

## 5. Product Recipes

### Create Recipe
- **Endpoint**: `POST /api/product-recipes`
- **Auth Required**: Yes

**Request Body:**
```json
{
  "productId": 1,
  "ingredientId": 1,
  "quantityRequired": 18
}
```

### Get Recipes By Product
- **Endpoint**: `GET /api/product-recipes/product/{productId}`
- **Auth Required**: Yes

---

## 6. Purchase Orders (Restock Flow)

### Get All Purchase Orders
- **Endpoint**: `GET /api/purchase-orders`
- **Auth Required**: Yes

### Create Purchase Order
- **Endpoint**: `POST /api/purchase-orders`
- **Auth Required**: Yes

**Request Body:**
```json
{
  "supplierId": 1,
  "expectedDate": "2026-06-05"
}
```

### Add PO Items
- **Endpoint**: `POST /api/purchase-orders/{id}/items`
- **Auth Required**: Yes

**Request Body:**
```json
{
  "ingredientId": 1,
  "quantity": 1000,
  "unitPrice": 150
}
```

### Get PO Items
- **Endpoint**: `GET /api/purchase-orders/{id}/items`
- **Auth Required**: Yes

### Receive PO (Update Stock)
- **Endpoint**: `POST /api/purchase-orders/{id}/receive`
- **Auth Required**: Yes
- **Description**: Marks the PO as RECEIVED and automatically updates the ingredient stock.

---

## 7. Point of Sale (POS) Transactions

### Create Transaction (Checkout)
- **Endpoint**: `POST /api/transactions`
- **Auth Required**: Yes
- **Description**: This is the core POS endpoint. Submitting this will validate stock via recipes, deduct the stock, create stock movements, and save the transaction.

**Request Body:**
```json
{
  "customerName": "John Doe",
  "paymentMethod": "CASH",
  "amountPaid": 50000,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

### Get All Transactions
- **Endpoint**: `GET /api/transactions`
- **Auth Required**: Yes

---

## 8. Stock Movements (Audit)

### Get All Stock Movements
- **Endpoint**: `GET /api/stock-movements`
- **Auth Required**: Yes
- **Description**: Fetch audit logs for all stock changes (type `PURCHASE` or `SALE`). Useful for reporting.

---

## Best Practices for macOS (SwiftUI) Integration

1. **Codable Models**: Create `Codable` structs for all the JSON requests and responses above.
2. **Error Handling**: 
   - 401 Unauthorized: Trigger a logout and redirect the user back to the Login View.
   - 400 Bad Request: Display validation errors from the backend.
   - 409 Conflict: Usually returned when trying to create a transaction without sufficient stock. Catch this and show a descriptive alert "Stok tidak cukup".
3. **Async/Await**: Use Swift's `async/await` with `URLSession` for clean, readable network calls without callback hell.
4. **Environment Objects**: Store the JWT token in an `@EnvironmentObject` or `@AppStorage` so it can be easily accessed globally across the application views.
