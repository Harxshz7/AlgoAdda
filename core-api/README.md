# AlgoAdda - Core API (`core-api`)

Spring Boot 3 backend service managing authentication, marketplace listings, orders, licensing, and SEBI compliance checks.

## Tech Stack
- **Java 21**
- **Spring Boot 3.3.4**
- **Spring Web, Spring Data JPA, Spring Security, Validation**
- **Flyway Database Migrations**
- **JJWT 0.12.6 (HMAC-SHA256 JWT + Refresh Token Rotation)**
- **PostgreSQL**

---

## Database Schema & Migrations

Database migrations are managed via **Flyway** in `src/main/resources/db/migration/`:
- `V1__init_schema.sql` — Initializes core domain schema:
  - `users` — Email, BCrypt password_hash, role (`BUYER`, `SELLER`, `ADMIN`).
  - `seller_profiles` — Display name, bio, `kyc_status` (`NOT_STARTED`).
  - `refresh_tokens` — Secure rotation tokens with revocation tracking.
  - `bots` & `bot_versions` — White Box strategies with mandatory `disclosed_logic`.
  - `backtest_results` — Vectorbt engine evaluation metrics (`metrics` JSONB).
  - `compliance_checks` — Automated and manual SEBI compliance results.
  - `listings` — Versioned marketplace offerings (`ONE_TIME`, `TIMED`).
  - `orders` & `licenses` — Immutable purchasing & broker execution rights.

---

## Running the Application

### 1. Database Setup
```bash
createdb algoadda_dev
# Or in psql: CREATE DATABASE algoadda_dev;
```

### 2. Run Backend
```bash
cd core-api
mvn spring-boot:run
# Or on Windows using wrapper:
.\mvn.cmd spring-boot:run
```
The server starts at `http://localhost:8080`.

### 3. Run Automated Tests
```bash
cd core-api
mvn test
# Or using wrapper:
.\mvn.cmd test
```

---

## API Endpoints & Verification

### 1. Health Check
```bash
curl http://localhost:8080/api/health
```
**Response (200 OK):**
```json
{"status": "ok"}
```

---

### 2. Authentication (JWT + Refresh Rotation)

#### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "trader@algoadda.com",
    "password": "SecurePassword123",
    "role": "SELLER"
  }'
```
**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "48b3f...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "c1f7b...",
    "email": "trader@algoadda.com",
    "role": "SELLER",
    "createdAt": "2026-09-12T08:00:00Z"
  }
}
```
*(Also sets `Set-Cookie: refreshToken=...; HttpOnly; Path=/api/auth; Max-Age=604800`)*

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "trader@algoadda.com",
    "password": "SecurePassword123"
  }'
```

#### Refresh Token (Rotates Token)
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "48b3f..."
  }'
```

#### Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "48b3f..."
  }'
```

---

### 3. Role-Based Access Control (RBAC Guards)

| Endpoint | Required Role | Description |
| :--- | :--- | :--- |
| `GET /api/buyer/ping` | `BUYER` | Guard verification for buyer routes |
| `GET /api/seller/ping` | `SELLER` | Guard verification for seller routes |
| `GET /api/admin/ping` | `ADMIN` | Guard verification for admin routes |

#### Example Authorized Request:
```bash
curl http://localhost:8080/api/seller/ping \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```
**Response (200 OK):**
```json
{
  "status": "ok",
  "role": "SELLER",
  "userId": "c1f7b...",
  "message": "Seller access granted"
}
```

---

### 4. Seller Onboarding (Stubbed)
```bash
curl -X POST http://localhost:8080/api/sellers/onboard \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "Alpha Quant Systems",
    "bio": "Specializing in statistical arbitrage and mean reversion algorithms."
  }'
```
**Response (200 OK):**
```json
{
  "id": "f8a1...",
  "userId": "c1f7b...",
  "displayName": "Alpha Quant Systems",
  "bio": "Specializing in statistical arbitrage and mean reversion algorithms.",
  "kycStatus": "NOT_STARTED",
  "createdAt": "2026-09-12T08:00:00Z",
  "updatedAt": "2026-09-12T08:00:00Z"
}
```
