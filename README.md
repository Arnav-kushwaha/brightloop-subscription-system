# Brightloop Subscription Management System

Enterprise-grade subscription management and recurring billing platform engineered for high-performance SaaS operations.

---

## 🌟 Key Features

1. **Role-Based Authentication**: Secure customer, staff, and admin management with BCrypt password hashing.
2. **Product Catalog & Variants**: Multi-tier product offerings with custom add-on variant pricing.
3. **Flexible Billing Cycles**: Daily, weekly, monthly, and yearly recurring subscription plans.
4. **Quotations Engine**: Proposal templates with one-click conversion into active subscriptions.
5. **Subscription Lifecycle**: Robust state machine (`Draft` → `Quotation` → `Confirmed` → `Active` → `Closed`) with pause, resume, renew, and termination controls.
6. **Automated Recurring Invoices**: Scheduled billing generation with duplicate protection and tax computations.
7. **Discounts & Promotions**: Percentage and fixed discounts with minimum spend conditions and admin controls.
8. **Payment Reconciliation**: Partial payment support with automatic outstanding balance adjustments and overdue flagging.
9. **Executive Finance Dashboard**: Real-time aggregated financial analytics (MRR, Total Revenue, Receivables, Overdue Invoices).

---

## 🏗️ Technology Stack

- **Backend**: Java 24, Spring Boot 3.4.3, Maven Wrapper
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA / Hibernate 6.6
- **Security**: Spring Security & Jakarta Validation
- **Frontend**: Vanilla HTML5, Modern CSS (Design Tokens, Glassmorphism, Responsive Grid), Vanilla JavaScript

---

## 👥 Team Module Architecture

| Member | Focus Area | Package |
| :--- | :--- | :--- |
| **Arnav** | Authentication, Roles, Security, Finance Dashboard Aggregations | `backend/.../auth`, `backend/.../dashboard` |
| **Aviral** | Products, Variants, Subscription Plans, Subscription Engine | `backend/.../product`, `backend/.../plan`, `backend/.../subscription` |
| **Aditi** | Quotations, Recurring Invoices, Discounts, Payments | `backend/.../quotation`, `backend/.../invoice`, `backend/.../discount`, `backend/.../payment` |
| **Chirag** | Modern SaaS Frontend, UI Components, API Integration | `frontend/` |

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java**: JDK 21 or later (Java 24 installed)
- **Maven**: No installation needed! The self-contained Maven Wrapper (`mvnw` / `mvnw.cmd`) is included.
- **MySQL**: Local MySQL server (port 3306)

### 2. Environment Configuration
Copy the sample environment file:
```powershell
cp .env.example .env
```
Default database configuration in `backend/src/main/resources/application.properties`:
- `DB_URL`: `jdbc:mysql://localhost:3306/brightloop_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- `DB_USERNAME`: `root`
- `DB_PASSWORD`: (configured via `DB_PASSWORD` environment variable)

### 3. Build & Run the Backend
From the `backend/` directory:
```powershell
# Compile and test dependencies
.\mvnw.cmd test-compile

# Launch Spring Boot development server
.\mvnw.cmd spring-boot:run
```
Once started, the backend runs on `http://localhost:8080`.

Verify system health:
```powershell
curl http://localhost:8080/api/health
```

### 4. Run the Frontend
Simply open `frontend/index.html` in your web browser, or serve it via a local static server:
```powershell
# Optional: using Python
python -m http.server 3000 --directory frontend

# Optional: using Node npx
npx serve frontend
```
The frontend automatically detects and indicates live Spring Boot backend connectivity via the top status badge.

---

## 📁 Repository Layout

```text
brightloop-subscription-system/
├── .env.example                            # Template for database & server environment variables
├── README.md                               # Project documentation & instructions
├── backend/                                # Spring Boot backend application
│   ├── mvnw / mvnw.cmd                     # Maven wrapper scripts
│   ├── pom.xml                             # Dependencies & build configuration
│   └── src/main/
│       ├── java/com/brightloop/system/
│       │   ├── BrightloopApplication.java  # Main application entry point
│       │   ├── common/                     # ApiResponse, GlobalExceptionHandler, HealthController
│       │   ├── config/                     # CorsConfig, SecurityConfig
│       │   ├── auth/                       # [Arnav] Authentication & Roles
│       │   ├── dashboard/                  # [Arnav] Finance Dashboard
│       │   ├── product/                    # [Aviral] Product & Variants
│       │   ├── plan/                       # [Aviral] Subscription Plans
│       │   ├── subscription/               # [Aviral] Subscription Lifecycle
│       │   ├── quotation/                  # [Aditi] Quotations Engine
│       │   ├── invoice/                    # [Aditi] Recurring Invoices
│       │   ├── discount/                   # [Aditi] Discount Rules
│       │   └── payment/                    # [Aditi] Payments & Ledger
│       └── resources/
│           └── application.properties      # MySQL datasource & JPA configuration
├── frontend/                               # [Chirag] Responsive SaaS Web App
│   ├── index.html                          # Dashboard landing view
│   ├── css/                                # main.css, components.css, dashboard.css
│   ├── js/                                 # api.js, app.js, dashboard.js
│   └── pages/                              # subscriptions.html, invoices.html, payments.html, products.html
└── docs/
    ├── project-plan.md                     # Roadmap and module boundaries
    └── api-contracts.md                    # REST endpoint specifications & JSON envelopes
```

---

## 📄 License
Internal Hackathon Project - Brightloop Team.