# AlgoAdda - Core API (`core-api`)

Spring Boot 3 backend service managing authentication, marketplace listings, orders, licensing, and SEBI compliance checks.

## Tech Stack
- **Java 21**
- **Spring Boot 3.3.4**
- **Spring Web, Spring Data JPA, Spring Security, Spring Validation, Lombok**
- **PostgreSQL**

---

## Database Setup (Local PostgreSQL)

Make sure PostgreSQL is running locally on port `5432`.

### 1. Create the Local Database
Using `psql` or PostgreSQL CLI tools:

```bash
# Using createdb command
createdb algoadda_dev

# OR via psql
psql -U postgres -c "CREATE DATABASE algoadda_dev;"
```

### 2. Environment Variables (Optional)
The service uses default credentials for local development if not provided:
- `DB_URL` (default: `jdbc:postgresql://localhost:5432/algoadda_dev`)
- `DB_USER` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)

### 3. Database Migrations
> **Note**: Database schema migrations (Flyway / Liquibase) will be introduced in **Phase 1: Core Domain & Auth**.

---

## Running the Application

### Run via Maven
```bash
cd core-api
mvn spring-boot:run
```

The server starts at `http://localhost:8080`.

---

## Health Check Endpoint

To verify the service is running and responding:

```bash
curl http://localhost:8080/api/health
```

Expected Response:
```json
{"status": "ok"}
```
