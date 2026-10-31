# Brightloop REST API Contracts

All endpoints use the base path `/api` and exchange data in standard JSON format.

---

## 1. Standard API Envelope

Every API response follows the consistent `ApiResponse<T>` envelope:

### Success Response Format
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-10-31T10:45:00.000"
}
```

### Error Response Format
```json
{
  "success": false,
  "message": "Validation failed / Reason for failure",
  "data": {
    "fieldName": "Must not be empty"
  },
  "timestamp": "2026-10-31T10:45:00.000"
}
```

---

## 2. System Health Check Endpoint

### `GET /api/health`
Checks backend responsiveness, Spring Boot profile, and database target.

- **Access**: Public (Permitted without authentication)
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Brightloop Backend is active and running",
  "data": {
    "status": "UP",
    "service": "Brightloop Subscription Management System",
    "version": "1.0.0-SNAPSHOT",
    "timestamp": "2026-10-31T10:45:00.000",
    "javaVersion": "24.0.2",
    "activeProfile": "dev",
    "databaseTarget": "jdbc:mysql://localhost:3306/brightloop_db?createDatabaseIfNotExist=true&useSSL=false"
  },
  "timestamp": "2026-10-31T10:45:00.000"
}
```

---

## 3. Module Endpoint Contracts (Roadmap)

### Module 1: Authentication & Roles (Arnav)
- `POST /api/auth/signup`: Customer registration.
- `POST /api/auth/login`: Authentication returning JWT / Session.
- `POST /api/auth/create-staff`: Admin-only endpoint to provision Staff accounts.
- `POST /api/auth/forgot-password`: Password reset request.

### Module 2: Products & Plans (Aviral)
- `GET /api/products`: Retrieve all products and variants.
- `POST /api/products`: Create a product with pricing and notes.
- `PUT /api/products/{id}`: Update product details.
- `DELETE /api/products/{id}`: Soft delete product.
- `GET /api/plans`: Retrieve subscription plans (Daily, Weekly, Monthly, Yearly).
- `POST /api/plans`: Create new subscription plan.

### Module 3: Subscriptions (Aviral)
- `GET /api/subscriptions`: Filterable subscription list with pagination.
- `POST /api/subscriptions`: Create a new subscription agreement.
- `PATCH /api/subscriptions/{id}/pause`: Pause active subscription.
- `PATCH /api/subscriptions/{id}/resume`: Resume paused subscription.
- `PATCH /api/subscriptions/{id}/renew`: Extend subscription duration.
- `PATCH /api/subscriptions/{id}/close`: Terminate subscription.

### Module 4: Quotations & Recurring Invoices (Aditi)
- `POST /api/quotations`: Create quotation template.
- `POST /api/quotations/{id}/convert`: Convert confirmed quotation to subscription.
- `POST /api/invoices/generate`: Trigger recurring invoice generation cycle.
- `GET /api/invoices`: List invoices with status filters (Draft, Confirmed, Paid, Overdue).

### Module 5: Discounts & Payments (Aditi)
- `POST /api/discounts`: Admin-only coupon/discount creation.
- `POST /api/payments`: Capture invoice payment (supports partial payments).
- `GET /api/payments`: Transaction audit log.

### Module 6: Finance Dashboard (Arnav)
- `GET /api/dashboard/metrics`: High-performance aggregated metrics (MRR, Total Revenue, Receivables, Overdue Invoices).
