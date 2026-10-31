# Brightloop Subscription Management System - Project Plan

## 1. Project Overview
Brightloop is an enterprise-grade subscription management and recurring billing platform built for multi-tenant scalability, lifecycle flexibility, and financial transparency.

---

## 2. Technology Stack
- **Backend**: Java 24, Spring Boot 3.4.3, Maven Wrapper (`mvnw.cmd` / `mvnw`)
- **Frontend**: Vanilla HTML5, Modern CSS (Design Tokens, Glassmorphism, Responsive Grid), Vanilla JavaScript (Modular ES6+)
- **Database**: MySQL (persistent relational storage with configurable environment variables)
- **ORM / Persistence**: Spring Data JPA, Hibernate 6.6
- **Security**: Spring Security (BCrypt password encoding, stateless REST architecture)
- **Validation**: Jakarta Validation (`@Valid`, `@NotNull`, `@Min`, `@Email`)

---

## 3. Team Module Ownership & Architecture Boundaries

To ensure all four team members can develop simultaneously without merge conflicts, the codebase is strictly separated by package and responsibility:

| Team Member | Module & Package Path | Primary Responsibilities |
| :--- | :--- | :--- |
| **Arnav** | `backend/.../auth`<br>`backend/.../dashboard` | Authentication, BCrypt password hashing, Roles (`ADMIN`, `STAFF`, `CUSTOMER`), Admin-only staff creation, password resets, and Finance Dashboard SQL aggregation queries (MRR, Total Revenue, Receivables). |
| **Aviral** | `backend/.../product`<br>`backend/.../plan`<br>`backend/.../subscription` | Product & variant catalog with add-on pricing, billing plans (Daily, Weekly, Monthly, Yearly), subscription state machine (`Draft` → `Quotation` → `Confirmed` → `Active` → `Closed`), pause, resume, renew, and close operations. |
| **Aditi** | `backend/.../quotation`<br>`backend/.../invoice`<br>`backend/.../discount`<br>`backend/.../payment` | Quotation templates & 1-click subscription conversion, recurring invoice auto-generation with duplicate protection and tax calculation, discounts (fixed & percentage), and partial payment reconciliation. |
| **Chirag** | `frontend/` (HTML, CSS, JS) | Modern, responsive SaaS user interface, dashboard overview cards, life-cycle action modals, live backend health integration, and responsive navigation across all modules. |

---

## 4. Implementation Phases

- **Phase 1: Project Scaffolding & Foundation (Current)**
  - Maven project setup with embedded Maven Wrapper.
  - Multi-package modular layout (`config`, `common`, `auth`, `dashboard`, `product`, `plan`, `subscription`, `quotation`, `invoice`, `discount`, `payment`).
  - Environment-variable-driven MySQL configuration in `application.properties`.
  - REST API envelope (`ApiResponse<T>`), Global Exception Handling, CORS, and Spring Security foundation.
  - Health endpoint `GET /api/health`.
  - Modern SaaS frontend dashboard with live health polling and empty states.
  - Complete documentation.

- **Phase 2: Authentication & Role-Based Access Control (Arnav)**
  - User and Role entities, BCrypt password hashing, Spring Security Filter Chain.
  - Endpoints: `/api/auth/signup`, `/api/auth/login`, `/api/auth/create-staff` (Admin only), `/api/auth/forgot-password`.

- **Phase 3: Products, Variants & Subscription Engine (Aviral)**
  - Product catalog CRUD and variant add-on pricing.
  - Subscription plans with daily, weekly, monthly, and yearly cycles.
  - Subscription entity and lifecycle workflow (pause, resume, renew, close).

- **Phase 4: Quotations, Recurring Invoices, Discounts & Payments (Aditi)**
  - Quotation drafting and conversion into active subscriptions.
  - Automated recurring invoice generator with duplicate prevention and tax calculation.
  - Discount rules engine (fixed/percentage, min spend, usage limits).
  - Payment capture against invoices with partial payment support.

- **Phase 5: Finance Dashboard Analytics & Metrics (Arnav)**
  - Real-time aggregation queries for active subscriptions, MRR, outstanding invoices, and collected revenue.

- **Phase 6: Frontend UI Integration (Chirag)**
  - Wire all CRUD modals, tables, and workflows to the REST endpoints.

- **Phase 7: End-to-End Testing & Hackathon Demonstration**
  - Database seed data, integration tests, and presentation setup.

---

## 5. Safe Git Collaboration Protocol
1. Each member works on their assigned feature branch (e.g., `feature/arnav-work`, `feature/aditi-finance`).
2. Do not commit `.env` or IDE configuration folders (`.idea`, `.vscode`).
3. Commit messages must be clear and descriptive.
4. Merge or pull from `main` only after module compilation tests pass (`.\mvnw.cmd test-compile`).
