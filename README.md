# AlgoAdda — Quantitative Algorithmic Strategy Platform

AlgoAdda is a transparent white-box strategy marketplace and official direct storefront for quantitative trading algorithms. It allows strategy creators to publish verified trading bots with fully disclosed logic, backtest reports, and automated compliance verification.

---

## 🏗️ Monorepo Architecture

The repository is structured into three primary sub-projects:

- **`core-api`**: Spring Boot 3.3.4 (Java 21) REST Backend
  - Handles authentication (JWT), users, seller profiles, bot versioning, compliance checks, listings, orders, and licensing.
  - Integrates Flyway migrations, JPA/Hibernate, and S3 artifact storage.
- **`web`**: React 18 + TypeScript + Vite + Tailwind CSS Frontend
  - Provides the `/store` (AlgoAdda Direct Storefront) and `/marketplace` (Public Marketplace) interfaces, interactive detail pages, equity curve charts, and seller/buyer dashboards.
- **`backtest-service`**: Python FastAPI + Vectorbt + yfinance Microservice
  - Executes quantitative backtests, calculates performance metrics (Win Rate, Max Drawdown, Sharpe Ratio, Profit Factor), and generates simulated equity trajectories.

---

## ✨ Features Implemented

1. **AlgoAdda Direct Storefront (`/store`)**
   - Distinct public section for official algorithms built by AlgoAdda's in-house quant team.
   - Database schema uses an `is_official` boolean tag on `Listing` (migration `V5__add_is_official_to_listings.sql`).
   - Configurable official seller identification via `OFFICIAL_SELLER_EMAIL` environment variable.
   - Dedicated REST endpoints (`GET /api/store/listings` and `GET /api/store/listings/{listingId}`).

2. **Public Marketplace (`/marketplace`)**
   - General marketplace displaying both third-party seller algorithms and official AlgoAdda algorithms.
   - Supports search filtering by strategy type, price range, query text, and sorting (newest, price, win rate).

3. **Automated Compliance Gate**
   - Every algorithm (official or third-party) passes through a mandatory 4-point compliance check before publishing:
     1. Disclosed strategy logic present
     2. Prohibited financial guarantee language scanner
     3. Risk disclaimer present
     4. Completed backtest result verification

4. **Repeatable Demo Seeder (`DemoDataSeeder`)**
   - Spring Boot `CommandLineRunner` activated via profile `-Dspring-boot.run.profiles=local,seed-demo`.
   - Idempotently seeds **3 Third-Party Marketplace listings** (AlphaQuant Labs, DeltaHedge Trading, BankNifty Systems) and **3 Official Store listings** (AlgoAdda Direct Labs) through the full application flow.

---

## 🚀 How to Run Locally

### 1. Start the Backtest Microservice (Python)
```powershell
cd backtest-service
.\venv\Scripts\python.exe -m uvicorn main:app --port 8000
```

### 2. Start the Core API Backend (Spring Boot)

- **Mode A: Local In-Memory H2 DB (No external Postgres required)**
  ```powershell
  cd core-api
  .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
  ```

- **Mode B: Local Dev + Seed Demo Data (Recommended for UI Demo)**
  ```powershell
  cd core-api
  .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local,seed-demo"
  ```

- **Mode C: Production PostgreSQL Mode**
  Ensure PostgreSQL is running on port `5432` with database `algoadda_dev`, then:
  ```powershell
  cd core-api
  .\mvnw.cmd spring-boot:run
  ```

### 3. Start the Web Frontend (React + Vite)
```powershell
cd web
npm run dev
```

Open **`http://localhost:5173`** in your browser.

---

## 📡 Key API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/listings` | Public marketplace listings (all published bots) |
| `GET` | `/api/listings/{id}` | Marketplace listing detail view |
| `GET` | `/api/store/listings` | AlgoAdda Direct Storefront listings (`is_official = true`) |
| `GET` | `/api/store/listings/{id}` | Official store listing detail view |
| `POST` | `/api/bots` | Upload bot strategy & initial version |
| `POST` | `/api/bots/{botId}/versions/{versionId}/publish` | Publish bot version (runs compliance check & sets official tag) |

---

## 🧪 Testing

Run backend unit and integration tests:
```powershell
# From project root (AlgoAdda)
cd core-api
.\mvnw.cmd test
cd ..
```

Run frontend type check & production build:
```powershell
# From project root (AlgoAdda)
cd web
npm run build
cd ..
```