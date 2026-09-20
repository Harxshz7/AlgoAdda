# AlgoAdda — Implementation & System Architecture Record

This document is the authoritative, ground-truth record of all features, services, database schemas, and integration points implemented across the AlgoAdda codebase. It is verified directly against active source files, Flyway migrations, and automated test suites.

---

## 📊 Summary Status Matrix

| Phase / Feature Area | Implementation Status | Test Suite Status | Migrations & Endpoints |
|---|---|---|---|
| **Phase 0: Foundation & Microservices** | ✅ Fully Built | 126 Backend + 4 Python + Frontend Build Passed | Monorepo (`core-api`, `web`, `backtest-service`) |
| **Phase 1: Core Domain & Auth** | ✅ Fully Built | Verified (`AuthServiceTest`, `RbacSecurityIntegrationTest`) | `V1__init_schema.sql` (`/api/auth/*`, `/api/sellers/*`) |
| **Phase 2: Seller Upload & Backtest** | ✅ Fully Built | Verified (`BotControllerIntegrationTest`, `AsyncBacktestQueueIntegrationTest`) | `V2`, `V9` (`/api/bots`, `/api/bots/{id}/versions`) |
| **Phase 3: Compliance Gate** | ✅ Fully Built | Verified (`ComplianceServiceTest`, `PublishAndReviewIntegrationTest`) | `V3` (`/api/admin/compliance/*`) |
| **Phase 4: Marketplace & Official Store** | ✅ Fully Built | Verified (`ListingPublicEndpointTest`, `StoreEndpointTest`) | `V4`, `V5` (`/api/listings/*`, `/api/store/listings/*`) |
| **Phase 5: Orders, Licensing, Cart & Subscriptions** | ✅ Fully Built | Verified (`OrderServiceTest`, `CartServiceTest`, `SubscriptionIntegrationTest`) | `V6`, `V7`, `V10` (`/api/cart/*`, `/api/orders/*`, `/api/subscriptions/*`) |
| **Phase 6: Trust, Moderation, Rate Limiting & Legal** | ✅ Fully Built | Verified (`ReviewIntegrationTest`, `AdminSuspendIntegrationTest`, `BotRateLimitIntegrationTest`) | `V8`, `V11` (`/api/bots/{id}/reviews`, `/api/listings/{id}/report`) |
| **Phase 7: Analytics, Version Diff, Watchlist & Seeding** | ✅ Fully Built | Verified (`SellerAnalyticsTest`, `BotVersionCompareTest`, `FavoriteIntegrationTest`) | `V12` (`/api/sellers/me/analytics`, `/api/bots/{id}/versions/compare`, `/api/buyers/me/favorites`) |

---

## 🏗️ Phase-by-Phase Implementation Details

### Phase 0: Foundation & Microservices Architecture
- **Description**: Monorepo structure organizing the Spring Boot 3.3.4 (Java 21) REST backend, Python FastAPI quantitative backtest service, and React 18 TypeScript Vite web frontend with shared styling and end-to-end integration.
- **Concrete Evidence**:
  - `core-api/pom.xml`: Spring Boot 3.3.4, Java 21, JPA/Hibernate, Flyway, JJWT 0.12.6, AWS SDK v2 (S3/SES), Razorpay SDK 1.4.7, Bucket4j 8.10.1.
  - `backtest-service/main.py`: FastAPI application exposing `/health` and `/backtest` with Pandas/NumPy calculation and synthetic/live market data retrieval via yfinance.
  - `web/src/App.tsx` & `web/src/index.css`: React Router 6 routing, context providers (`AuthContext`, `CartContext`), Tailwind CSS design system with custom palette and responsive layouts.
  - `run-algoadda.bat`: Windows developer launcher launching all three sub-projects in parallel.
- **Test Coverage**:
  - `core-api`: `RepositoryTest.java`, `PingController.java`, `HealthController.java`.
  - `backtest-service`: `test_backtest.py` (4 tests passing).
- **Notes / Gaps**: Containerization (`docker-compose.yml`) was originally conceptualized in early planning, but was replaced with native local execution profiles (`application-local.yml` H2 in-memory DB mode) per environment guidelines.

---

### Phase 1: Core Domain & Authentication
- **Description**: Multi-role user management (BUYER, SELLER, ADMIN) with stateless JWT access tokens (15-min expiry) and database-persisted refresh token rotation (7-day expiry), BCrypt password hashing, and basic seller onboarding.
- **Concrete Evidence**:
  - Schema Migration: `V1__init_schema.sql` (creates `users`, `seller_profiles`, `refresh_tokens`).
  - Entities & Services: `User.java`, `SellerProfile.java`, `RefreshToken.java`, `AuthService.java`, `SellerService.java`, `JwtTokenProvider.java`, `JwtAuthenticationFilter.java`, `SecurityConfig.java`.
  - Controllers & Endpoints:
    - `POST /api/auth/register` (Public)
    - `POST /api/auth/login` (Public)
    - `POST /api/auth/refresh` (Public)
    - `POST /api/sellers/onboard` (`SELLER` only)
    - `GET /api/sellers/profile` (`SELLER` only)
  - Frontend Pages: `LoginPage.tsx`, `RegisterPage.tsx`, `SellerOnboardPage.tsx`, `ProtectedRoute.tsx`.
- **Test Coverage**:
  - `AuthServiceTest.java`: Registration, login, duplicate email prevention, invalid credentials.
  - `RbacSecurityIntegrationTest.java`: Role-based endpoint authorization and unauthorized access checks.
  - `JwtTokenProviderTest.java`: Token signing, validation, claims extraction, expiration handling.

---

### Phase 2: Seller Upload, Backtesting & Risk Suitability
- **Description**: Strategy code/artifact ingestion into AWS S3, backtest generation via the Python microservice, asynchronous job queue processing, automated risk classification, and interactive equity curve visualization.
- **Concrete Evidence**:
  - Schema Migrations:
    - `V1__init_schema.sql` (`bots`, `bot_versions`, `backtest_results`).
    - `V2__add_backtest_status_to_bot_versions.sql` (adds `backtest_status` ENUM).
    - `V9__add_risk_label_to_backtest_results.sql` (adds `risk_label` column).
  - Entities & Services: `Bot.java`, `BotVersion.java`, `BacktestResult.java`, `BacktestJob.java`, `BotService.java`, `S3StorageService.java`, `BacktestServiceClient.java`, `BacktestJobWorker.java`, `RiskClassifier.java`.
  - Controllers & Endpoints:
    - `POST /api/bots` (`SELLER` only, multipart file upload)
    - `POST /api/bots/{botId}/versions` (`SELLER` only, new version upload)
    - `GET /api/sellers/me/bots` (`SELLER` only, seller bot portfolio)
    - `GET /api/bots/{botId}` (Public)
    - `GET /api/bots/{botId}/versions` (Public)
    - `GET /api/bots/{botId}/versions/{versionId}/backtest` (Public)
  - Frontend Components: `BotUploadPage.tsx`, `SellerDashboardPage.tsx`, `BotDetailPage.tsx`, `EquityCurveChart.tsx`, `MetricGauge.tsx`, `RiskBadge.tsx`.
- **Test Coverage**:
  - `BotControllerIntegrationTest.java`: Bot creation and version upload flows.
  - `BotServiceTest.java`: Bot lifecycle state transitions.
  - `BacktestServiceClientTest.java`: REST communication to Python engine.
  - `AsyncBacktestQueueIntegrationTest.java`: Asynchronous backtesting job queue worker.
  - `RiskClassifierTest.java`: Risk label assignment (`CONSERVATIVE`, `MODERATE`, `AGGRESSIVE`).

---

### Phase 3: Compliance Gate (SEBI White-Box Alignment)
- **Description**: Mandatory 4-point automated compliance verification required before publishing any bot version to ensure compliance with SEBI algo guidelines (disclosed strategy logic, prohibited financial guarantee regex scanner, risk disclaimer, completed backtest). Includes an admin review override queue.
- **Concrete Evidence**:
  - Schema Migrations:
    - `V1__init_schema.sql` (`compliance_checks` table).
    - `V3__add_risk_disclaimer_to_bots.sql` (adds `risk_disclaimer` to `bots`).
  - Entities & Services: `ComplianceCheck.java`, `ComplianceService.java`, `AdminComplianceController.java`.
  - Controllers & Endpoints:
    - `POST /api/bots/{botId}/versions/{versionId}/publish` (`SELLER` only)
    - `GET /api/admin/compliance/queue` (`ADMIN` only)
    - `POST /api/admin/compliance/{id}/review` (`ADMIN` only)
  - Versioning Rule: Publishing a new version re-enters the compliance gate while keeping previous published versions active for existing licensees.
- **Test Coverage**:
  - `ComplianceServiceTest.java`: Automated check evaluation logic and prohibited language regex scanner.
  - `PublishAndReviewIntegrationTest.java`: Publish blocking when checks fail and admin review overrides.
  - `VersioningComplianceIntegrationTest.java`: Version upgrade isolation and independent compliance verification.

---

### Phase 4: Public Marketplace & AlgoAdda Direct Storefront
- **Description**: Two distinct storefront interfaces: `/store` for curated official in-house quantitative algorithms and `/marketplace` for third-party strategies, featuring Postgres full-text search, multi-factor filtering, and public seller profiles.
- **Concrete Evidence**:
  - Schema Migrations:
    - `V4__add_search_to_bots.sql` (Postgres `tsvector` column and GIN index with automatic update triggers).
    - `V5__add_is_official_to_listings.sql` (adds `is_official` boolean column).
  - Entities & Services: `Listing.java`, `ListingRepository.java`, `ListingService.java`, `StoreController.java`, `ListingController.java`, `PublicSellerController.java`.
  - Controllers & Endpoints:
    - `GET /api/listings` (Public, search query, strategy type, price filters, sorting, pagination)
    - `GET /api/listings/{id}` (Public, detailed strategy breakdown & metrics)
    - `GET /api/store/listings` (Public, official AlgoAdda bots only)
    - `GET /api/store/listings/{id}` (Public, official bot details)
    - `GET /api/sellers/{sellerId}` (Public, seller profile & active listings)
  - Frontend Pages: `MarketplacePage.tsx`, `ListingDetailPage.tsx`, `StorePage.tsx`, `StoreListingDetailPage.tsx`, `PublicSellerProfilePage.tsx`, `OfficialBadge.tsx`.
- **Test Coverage**:
  - `ListingPublicEndpointTest.java`: Marketplace querying, full-text search, and price filtering.
  - `StoreEndpointTest.java`: Official listing segregation and configuration.

---

### Phase 5: Orders, Licensing, Cart & Subscriptions
- **Description**: Full checkout and license lifecycle supporting one-time bot purchases, multi-item shopping carts, recurring subscriptions (Razorpay Subscriptions), cryptographically verified webhooks, license downloads (S3 presigned URLs), and admin refund revocation.
- **Concrete Evidence**:
  - Schema Migrations:
    - `V1__init_schema.sql` (`orders`, `licenses`).
    - `V6__add_revoked_to_licenses.sql` (adds `revoked` boolean to `licenses`).
    - `V7__add_cart_and_order_items.sql` (`carts`, `cart_items`, `order_items`).
    - `V10__add_subscription_support.sql` (`subscriptions`, `billing_interval`, `razorpay_plan_id`).
  - Entities & Services: `Order.java`, `OrderItem.java`, `License.java`, `Cart.java`, `CartItem.java`, `Subscription.java`, `OrderService.java`, `LicenseService.java`, `CartService.java`, `SubscriptionService.java`, `RazorpayService.java`.
  - Controllers & Endpoints:
    - `GET /api/cart`, `POST /api/cart/items`, `DELETE /api/cart/items/{id}`, `DELETE /api/cart` (`BUYER` only)
    - `POST /api/orders` (`BUYER` only, creates Razorpay order for single listing or cart items)
    - `POST /api/payments/webhook` (Public, verified via `X-Razorpay-Signature` HMAC SHA-256)
    - `GET /api/buyers/me/licenses` (`BUYER` only, active and historical licenses)
    - `GET /api/licenses/{licenseId}/download` (`BUYER` only, time-limited S3 download URL)
    - `POST /api/admin/orders/{id}/refund` (`ADMIN` only, marks order refunded and revokes license)
    - `POST /api/subscriptions`, `POST /api/subscriptions/{id}/cancel` (`BUYER` only)
  - Frontend Pages & Context: `CartContext.tsx`, `CartPage.tsx`, `BuyerDashboardPage.tsx`, `PurchasedBotsList.tsx`.
- **Test Coverage**:
  - `CartServiceTest.java`: Server-side cart operations and uniqueness constraints.
  - `OrderServiceTest.java`: Single and multi-item order creation.
  - `PaymentWebhookTest.java`: Razorpay signature validation and automatic license issuance.
  - `LicenseServiceTest.java`: License validity, expiry calculation, and presigned download URL generation.
  - `AdminRefundTest.java`: Admin refund processing and license revocation.
  - `SubscriptionIntegrationTest.java`: Subscription creation, webhooks (`subscription.charged`, `subscription.cancelled`), and license association.

---

### Phase 6: Trust, Moderation, Rate Limiting, Legal & Notifications
- **Description**: Trust and safety architecture featuring verified-buyer ratings and reviews (1–5 stars), listing abuse reporting with admin moderation and seller suspension, Bucket4j upload rate limiting, AWS SES transactional emails, and legal disclosure pages.
- **Concrete Evidence**:
  - Schema Migrations:
    - `V8__add_listing_reports_and_suspended.sql` (`listing_reports` table, `suspended` column on `users`).
    - `V11__add_reviews.sql` (`reviews` table with unique constraint on `(bot_id, buyer_id)`).
  - Entities & Services: `Review.java`, `ListingReport.java`, `ReviewService.java`, `ReportService.java`, `RateLimiterService.java`, `EmailService.java`.
  - Controllers & Endpoints:
    - `GET /api/bots/{botId}/reviews` (Public, reviews list and aggregate rating stats)
    - `POST /api/bots/{botId}/reviews` (`BUYER` only, verified license check, idempotent upsert)
    - `DELETE /api/admin/reviews/{reviewId}` (`ADMIN` only)
    - `POST /api/listings/{listingId}/report` (Authenticated users, listing flag)
    - `GET /api/admin/reports/queue` (`ADMIN` only)
    - `POST /api/admin/reports/{reportId}/resolve` (`ADMIN` only, actions: `DISMISS`, `UNPUBLISH_LISTING`, `SUSPEND_SELLER`)
    - `POST /api/admin/listings/{id}/force-delist` (`ADMIN` only)
    - `POST /api/admin/sellers/{id}/suspend` & `/unsuspend` (`ADMIN` only)
  - Rate Limiting: Token-bucket algorithm (`Bucket4j`) applied to `POST /api/bots` and `POST /api/bots/{botId}/versions` (default 10 requests / 60 mins).
  - Email Notifications: AWS SES `EmailService` with graceful fallback logging when unconfigured.
  - Frontend Pages: `AdminReportsPage.tsx`, `ReviewsSection.tsx`, `StarRating.tsx`, `TermsPage.tsx`, `RiskDisclosurePage.tsx`, `PrivacyPage.tsx`, `AboutPage.tsx`.
- **Test Coverage**:
  - `ReviewIntegrationTest.java`: Verified buyer license check, rating validation (1–5), upsert behavior, and aggregate rating calculation.
  - `ReportServiceTest.java` & `AdminSuspendIntegrationTest.java`: Report filing, admin resolution, listing delisting, seller suspension, and login blocking.
  - `BotRateLimitIntegrationTest.java`: Bucket4j capacity exhaustion (429 Too Many Requests).
  - `EmailServiceTest.java` & `OrderServiceEmailIntegrationTest.java`: Email dispatch on purchase and fallback execution.

---

### Phase 7: Analytics, Version Comparison, Watchlist & Demo Seeder
- **Description**: Seller analytics dashboard with view tracking and conversion metrics, strategy version comparison with line-by-line disclosed logic diffing and performance deltas, buyer watchlist/favorites, and a repeatable demo seeder.
- **Concrete Evidence**:
  - Schema Migrations:
    - `V12__add_favorites.sql` (`favorites` table with unique constraint on `(buyer_id, bot_id)`).
    - `ListingView.java` (records individual listing page view events).
  - Entities & Services: `Favorite.java`, `ListingView.java`, `FavoriteService.java`, `SellerAnalyticsService.java`, `BotVersionCompareService.java`, `DemoDataSeeder.java`.
  - Controllers & Endpoints:
    - `GET /api/sellers/me/analytics` (`SELLER` only, views, purchases, gross/net revenue, conversion rate, bot breakdown)
    - `GET /api/bots/{botId}/versions/compare?from={fromVersionId}&to={toVersionId}` (Public/Authenticated)
    - `POST /api/bots/{botId}/favorite` (`BUYER` only, idempotent)
    - `DELETE /api/bots/{botId}/favorite` (`BUYER` only, idempotent)
    - `GET /api/buyers/me/favorites` (`BUYER` only, returns favorited bot summaries)
    - Integrated `isFavorited` boolean on `GET /api/listings` and `GET /api/store/listings` when called by authenticated buyers.
  - Frontend Pages & Components: `SellerAnalyticsPage.tsx`, `WatchlistPage.tsx`, `FavoriteButton.tsx`, `BotDetailPage.tsx` (Version Comparison diff viewer).
  - Demo Seeder: `DemoDataSeeder.java` activated via `-Dspring-boot.run.profiles=local,seed-demo` seeding 3 official algorithms and 3 third-party marketplace algorithms.
- **Test Coverage**:
  - `SellerAnalyticsTest.java`: Revenue, view counting, and conversion rate calculations.
  - `ListingViewTest.java`: View event recording and listing view counting.
  - `BotVersionCompareTest.java`: Logic diff engine (`ADDED`, `REMOVED`, `UNCHANGED`) and performance metric delta calculation.
  - `FavoriteIntegrationTest.java`: Watchlist idempotency, buyer isolation, draft bot blocking, and listing payload enrichment.

---

## 🔮 Not Yet Built (Future Roadmap / v2 Scope)

The following items are deferred to future milestones and currently have zero code presence in the repository:
1. **Government Identity KYC API Integration**: Deep automated verification with DigiLocker / Aadhaar / PAN / GSTIN APIs (the platform currently uses a profile stub and admin approval model).
2. **Live Broker Order Execution Bridge**: Direct automated trading execution webhooks or broker bridges (Zerodha Kite Connect, Upstox, AngelOne, Groww).
3. **Live Forward-Testing / Real-Time Tracking**: Continuous paper trading and live account performance tracking (the platform currently evaluates historical backtests).
4. **SEBI Research Analyst (RA) Black-Box Tier**: Proprietary, closed-source automated trading algorithms requiring formal SEBI RA registration.
5. **Native Mobile Applications**: iOS and Android mobile apps (the platform is currently a responsive web application).
6. **Cloud Infrastructure & Production CI/CD Deployment**: AWS ECS/EKS, Terraform configurations, or cloud container pipelines.
