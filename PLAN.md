# AlgoAdda — PLAN.md

This is the living roadmap for AlgoAdda. Check boxes as phases complete;
update it at the start of each phase rather than task-by-task (use GitHub
Issues for granular tracking).

Marketplace for buying/selling automated trading bots. White Box compliance model
(disclosed logic, no guaranteed returns) under SEBI's 2026 algo trading framework.

**Stack:** Java 21 + Spring Boot 3, React 18, PostgreSQL, Python + vectorbt (backtest
service), S3-compatible storage, Razorpay.

---

## Phase 0 — Foundation (Week 1)
- [ ] Create `algoadda` repo, set up monorepo or multi-repo structure (core-api, backtest-service, web)
- [ ] Set up Spring Boot 3 project skeleton (Java 21, Maven/Gradle, Spring Security, JPA)
- [ ] Set up PostgreSQL locally via Docker Compose
- [ ] Set up React 18 project skeleton (Vite recommended over CRA)
- [ ] Set up Python backtest-service skeleton (FastAPI + vectorbt, isolated venv)
- [ ] Write a `docker-compose.yml` that runs all four pieces together locally
- [ ] Push initial scaffolds, confirm all services start and can ping each other

## Phase 1 — Core Domain & Auth (Weeks 2–3)
- [x] Design and migrate DB schema: User, Bot, BotVersion, Listing, Order, License, ComplianceCheck, BacktestResult
- [x] Implement JWT auth (register/login/refresh) — reuse pattern from quant-journal
- [x] Role-based access: buyer / seller / admin
- [x] Basic seller onboarding flow (profile, no KYC yet — stub it)
- [x] Unit tests for auth and core entities

## Phase 2 — Seller Flow: Upload & Backtest (Weeks 4–5)
- [x] Bot upload endpoint — accepts strategy config/code, stores in S3/MinIO
- [x] BotVersion creation with mandatory `disclosed_logic` field (compliance requirement)
- [x] Backtest-service API contract: request (config + date range) → response (metrics + report)
- [x] Core API → Backtest Service integration (REST call, handle timeouts/failures)
- [x] Store BacktestResult, render metrics (win rate, drawdown, Sharpe, equity curve) back to seller
- [x] Seller dashboard: view bot status (draft → pending review → published/rejected)

## Phase 3 — Compliance Gate (Week 6)
-  [x] Build ComplianceCheck: automated checklist (disclosed_logic present, no "guaranteed"/"assured" language, risk disclaimer present)
- [x] Manual review queue for edge cases (admin view)
- [x] Block publish until ComplianceCheck passes
- [x] Versioning rule: any BotVersion update re-enters this gate; old version stays live for existing buyers

## Phase 4 — Marketplace & Listings (Weeks 7–8)
- [x] Public listing pages (browse, filter by strategy type, price, backtest metrics)
- [x] Listing detail page: disclosed logic summary, backtest report, risk disclaimer, seller info
- [x] Search + filtering (Postgres full-text search is enough for v1 — skip Elasticsearch)
- [x] Frontend: marketplace grid, listing detail, seller profile pages

## Phase 5 — Payments & Licensing (Weeks 9–10)
- [x] Razorpay integration (checkout, webhook handling)
- [x] Order → License issuance flow, tied to exact BotVersion purchased
- [x] Buyer dashboard: purchased bots, download access, license status
- [x] Refund/dispute handling (basic — manual admin action for v1)

## Phase 6 — Trust & Polish (Weeks 11–12)
- [x] Seller ratings/reviews on listings
- [x] Report/flag a listing (abuse, false claims)
- [x] Admin dashboard: compliance queue, user management, order overview
- [x] Email notifications (purchase confirmation, compliance status updates)
- [x] Legal pages: terms of service, risk disclosure, privacy policy (get these reviewed properly before real money moves)

## Phase 7 — Beta Launch (Week 13+)
- [x] Deploy core-api + backtest-service + frontend (Railway/Render/AWS — pick based on budget)
- [x] Onboard a small set of test sellers (yourself + a few known bots first)
- [x] Closed beta with a handful of buyers, gather feedback
- [x] Monitor compliance edge cases in the wild before opening signups publicly

---

## Deferred to v2 (post-launch)
- Black Box tier (requires SEBI Research Analyst registration — heavier compliance)
- Live performance tracking (vs backtest-only)
- Revenue share / subscription pricing models
- Mobile app 