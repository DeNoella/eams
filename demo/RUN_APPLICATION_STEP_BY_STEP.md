# E-AMS Backend Run Guide (Step by Step)

## 1) Prerequisites

- Java 21
- Maven Wrapper (already included as `mvnw` / `mvnw.cmd`)
- PostgreSQL 16+ running locally
- Redis running locally (for magic-link rate limiting)
- RabbitMQ running locally (wired in config)

## 2) Create PostgreSQL Database

Run in PostgreSQL shell:

```sql
CREATE DATABASE eams_db;
```

Default credentials configured in app:

- Username: `postgres`
- Password: `123`

If your local credentials differ, set env vars:

- `DB_USERNAME`
- `DB_PASSWORD`

## 3) Configure Environment Variables

PowerShell example:

```powershell
$env:JWT_SECRET="replace-with-long-random-256-bit-secret"
$env:MAIL_USERNAME="mutesideno@gmail.com"
$env:MAIL_PASSWORD="xxrn vndo tnwr hvef"
$env:FRONTEND_URL="http://localhost:5173"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:RABBITMQ_HOST="localhost"
$env:RABBITMQ_PORT="5672"
```

Optional AI:

```powershell
$env:ANTHROPIC_API_KEY="your-api-key"
```

## 4) Build and Verify

```powershell
./mvnw clean compile -DskipTests
```

Expected: `BUILD SUCCESS`

## 5) Run the Backend

```powershell
./mvnw spring-boot:run
```

App URLs:

- API base: `http://localhost:8081/api/v1`
- Swagger: `http://localhost:8081/swagger-ui.html`
- Health: `http://localhost:8081/actuator/health`

## 6) First Login Flow (Magic Link)

1. Create account:
   - `POST /api/v1/auth/register`
2. Request magic link:
   - `POST /api/v1/auth/magic-link/request`
3. Open email, copy link token.
4. Verify magic link:
   - `GET /api/v1/auth/magic-link/verify?token=...&email=...`
5. Use returned access token as Bearer token.

## 7) Postman Auth Setup

- Authorization type: `Bearer Token`
- Token value: access token from verify endpoint
- Enable cookie jar so refresh token cookie is stored.

## 8) Quick Functional Smoke Test

1. Register user.
2. Request + verify magic link.
3. `GET /api/v1/users`
4. `POST /api/v1/assets`
5. `GET /api/v1/assets`
6. `POST /api/v1/certificates`
7. `POST /api/v1/licences`
8. `GET /api/v1/dashboard/executive`

## 9) Common Issues

- **401 on protected endpoints**: access token missing/expired.
- **Magic link mail not sent**: check `MAIL_USERNAME` and `MAIL_PASSWORD`.
- **DB connection errors**: verify PostgreSQL is running and `eams_db` exists.
- **Redis errors**: start Redis service locally.

## 10) Notes on Current Backend State

- Backend compiles and core modules are available.
- Full SRS is broad; implementation is progressing module-by-module with working API foundations for auth, assets, certificates, licences, access grants, changes, transactions, users, dashboard, reports, and AI chat endpoint.
