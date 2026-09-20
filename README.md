# AlgoAdda — Quantitative Algorithmic Strategy Platform

AlgoAdda is a transparent white-box strategy marketplace and official direct storefront for quantitative trading algorithms. It enables quantitative researchers and strategy creators to publish verified trading bots with fully disclosed logic, historical backtest reports, automated compliance verification, and verified-buyer reviews under SEBI's 2026 algo trading framework.

---

## 📌 Implementation Status

For the comprehensive, phase-by-phase record of all implemented features, database migrations, controllers, services, and test coverage, refer to **[IMPLEMENTATION.md](file:///c:/Users/harxs/OneDrive/Desktop/AlgoAdda/IMPLEMENTATION.md)**.

- **Current Backend Test Suite**: 126 JUnit / Spring integration tests passing (`100% BUILD SUCCESS`).
- **Current Python Test Suite**: 4 pytest tests passing (`100% passed`).
- **Current Web Frontend**: TypeScript compilation & Vite build passing.

---

## 🏗️ Monorepo Architecture

The repository is structured into three sub-projects:

- **`core-api`**: Spring Boot 3.3.4 (Java 21) REST Backend
  - Handles authentication (JWT & refresh rotation), RBAC (Buyer, Seller, Admin), bot lifecycle, S3 artifact vault, automated compliance gate, Postgres full-text search, multi-item cart, Razorpay orders & subscriptions, licensing engine, verified buyer reviews, listing reporting & seller suspension, Bucket4j rate limiting, seller analytics, version comparison diffs, and buyer watchlists.
  - Integrates Flyway migrations (V1 through V12) and PostgreSQL (with H2 in-memory mode for zero-setup local dev).
- **`web`**: React 18 + TypeScript + Vite + Tailwind CSS Frontend
  - Provides the `/store` (Official Storefront) and `/marketplace` (Public Marketplace), `/buyer/watchlist`, `/cart`, `/buyer/dashboard`, interactive listing detail pages, Recharts equity curves & revenue charts, strategy version comparison diffs, seller analytics, and admin moderation portals.
- **`backtest-service`**: Python FastAPI + Vectorbt + Pandas Microservice
  - Computes quantitative backtests, calculates performance metrics (Win Rate, Max Drawdown, Sharpe Ratio, Profit Factor, Total Returns), evaluates risk suitability labels, and generates simulated equity trajectories with synthetic fallbacks.

---

## ✨ Key Features Implemented

1. **AlgoAdda Direct Storefront (`/store`) & Public Marketplace (`/marketplace`)**
   - Segregated official in-house algorithms (`is_official = true`) and open third-party seller marketplace.
   - Postgres full-text search (`tsvector` + GIN indexing), multi-factor filtering (strategy type, price, metrics), and sorting.
2. **Automated Compliance Gate (SEBI White-Box Alignment)**
   - Mandatory 4-point verification before publishing: disclosed strategy logic, prohibited return guarantee scanner, risk disclaimer, and verified backtest. Includes admin manual review override queue.
3. **Payments, Licensing, Cart & Subscriptions**
   - Multi-item shopping cart, Razorpay one-time checkout & recurring subscriptions, cryptographically verified webhooks, immutable versioned licenses, presigned S3 downloads, and admin refunds with license revocation.
4. **Verified Buyer Reviews & Ratings**
   - 1–5 star reviews and comments restricted strictly to verified buyers owning an active license for that strategy.
5. **Listing Moderation & Seller Suspension**
   - Listing reporting system with admin review queue and resolution actions (dismiss, force-delist, suspend seller).
6. **Upload Rate Limiting & Transactional Emails**
   - Bucket4j token bucket rate limiting on bot uploads; AWS SES email notifications with graceful fallback logging.
7. **Strategy Version Comparison & Seller Analytics**
   - Visual line-by-line disclosed logic diff viewer, performance metric delta table, and seller revenue/view/conversion analytics.
8. **Buyer Watchlist / Favorites**
   - One-click strategy bookmarking on listings and cards with dedicated `/buyer/watchlist` view.
9. **Repeatable Demo Seeder (`DemoDataSeeder`)**
   - Spring Boot runner activated via `-Dspring-boot.run.profiles=local,seed-demo` seeding 3 official algorithms and 3 third-party marketplace algorithms.

---

## 🚀 How to Run Locally

### Quick Launch (All Services)
On Windows, you can launch all three services simultaneously:
```powershell
.\run-algoadda.bat
```

---

### Manual Step-by-Step Launch

#### 1. Start the Backtest Microservice (Python)
```powershell
cd backtest-service
.\venv\Scripts\python.exe -m uvicorn main:app --port 8000
```

#### 2. Start the Core API Backend (Spring Boot)

- **Mode A: Local In-Memory H2 DB (Recommended for quick dev)**
  ```powershell
  cd core-api
  .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
  ```

- **Mode B: Local Dev + Seed Demo Data (Recommended for full UI demo)**
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

#### 3. Start the Web Frontend (React + Vite)
```powershell
cd web
npm run dev
```

Open **`http://localhost:5173`** in your browser.

---

## ⚙️ Environment Configuration

Key configuration parameters (set via system environment variables or `.env`):

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/algoadda_dev` | PostgreSQL database connection string |
| `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` | Database credentials |
| `JWT_SECRET` | *(development secret)* | Secret key for JWT signing (256+ bits) |
| `S3_BUCKET_NAME` | `algoadda-bot-artifacts` | AWS S3 / MinIO storage bucket for strategy code |
| `AWS_REGION` | `ap-south-1` | AWS region for S3 and SES |
| `SES_FROM_EMAIL` | `noreply@algoadda.com` | Verified sender address for AWS SES |
| `BACKTEST_SERVICE_URL` | `http://localhost:8000` | URL for Python FastAPI backtest microservice |
| `OFFICIAL_SELLER_EMAIL` | `official@algoadda.com` | User email treated as the official AlgoAdda seller |
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | `rzp_test_dummyKeyId` / `...` | Razorpay payment gateway credentials |
| `RAZORPAY_WEBHOOK_SECRET` | `dummyWebhookSecret` | HMAC secret for Razorpay webhook verification |
| `RATE_LIMIT_BOT_UPLOAD_CAPACITY` | `10` | Maximum uploads allowed per seller per window |
| `RATE_LIMIT_BOT_UPLOAD_WINDOW_MINUTES` | `60` | Rate limiting sliding window duration in minutes |

---

## 📡 Key API Endpoints Overview

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new Buyer or Seller account |
| `POST` | `/api/auth/login` | Public | Authenticate and obtain JWT access & refresh tokens |
| `GET` | `/api/listings` | Public | Browse marketplace listings (search, filter, sort) |
| `GET` | `/api/store/listings` | Public | Browse official AlgoAdda store listings |
| `POST` | `/api/bots` | Seller | Upload new trading strategy & files (rate limited) |
| `POST` | `/api/bots/{id}/versions/{vId}/publish` | Seller | Run compliance gate and publish bot version |
| `GET` | `/api/bots/{id}/versions/compare` | Public | Compare strategy logic diffs and metric deltas |
| `GET` | `/api/cart` | Buyer | Get current buyer shopping cart |
| `POST` | `/api/orders` | Buyer | Initiate checkout order with Razorpay |
| `POST` | `/api/payments/webhook` | Public | Razorpay webhook for payment and license issuance |
| `GET` | `/api/buyers/me/licenses` | Buyer | List purchased licenses and presigned downloads |
| `GET` | `/api/buyers/me/favorites` | Buyer | Get favorited watchlist strategies |
| `POST` | `/api/bots/{id}/favorite` | Buyer | Idempotently favorite a trading strategy |
| `POST` | `/api/bots/{id}/reviews` | Buyer | Submit verified buyer review & rating (1–5 stars) |
| `GET` | `/api/sellers/me/analytics` | Seller | Get seller revenue, view counts, and conversion stats |
| `GET` | `/api/admin/reports/queue` | Admin | Review open listing abuse reports |
| `POST` | `/api/admin/reports/{id}/resolve` | Admin | Resolve report (dismiss, delist, suspend seller) |

---

## 🧪 Testing

Run backend unit and integration tests:
```powershell
cd core-api
.\mvnw.cmd test
```

Run Python backtest tests:
```powershell
cd backtest-service
.\venv\Scripts\pytest.exe
```

Run frontend type check & production build:
```powershell
cd web
npm run build
```
---
## 🔮 Future Improvements

The following capabilities are planned for upcoming releases as deferred Phase 10 / v2 roadmap milestones:

1. **Government Identity KYC API Integration**: Deep automated verification with DigiLocker / Aadhaar / PAN / GSTIN APIs.
2. **Live Broker Order Execution Bridge**: Direct automated trade execution webhooks and broker bridges (Zerodha Kite Connect, Upstox, AngelOne, Groww).
3. **Live Forward-Testing / Real-Time Paper Trading**: Continuous paper trading and live account forward-performance tracking alongside historical backtests.
4. **SEBI Research Analyst (RA) Black-Box Tier**: Dedicated closed-source tier for SEBI-registered Research Analysts with automated telemetry.
5. **Native Mobile Applications**: Dedicated iOS and Android client applications.
6. **Cloud Infrastructure & Managed Deployment**: Production AWS ECS/EKS container deployments and Terraform infrastructure configurations.

---

## ⚖️ Compliance & Risk Disclaimer

AlgoAdda operates under the **White Box transparency model** aligned with SEBI's 2026 algorithmic trading regulatory framework. 
- All published algorithmic strategies disclose their core quantitative logic, parameters, and risk disclaimers.
- No strategy listed on AlgoAdda provides or implies "guaranteed," "assured," or "risk-free" financial returns.
- Algorithmic and quantitative trading involves substantial risk of capital loss. Past backtested performance is hypothetical and does not guarantee future results.