# E-Asset Management System (E-AMS)

Enterprise IT Asset Management Platform with AI-powered insights.

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 16+
- Docker & Docker Compose (for local development)
- Node.js 20+ (for frontend)

## Tech Stack

### Backend
- Spring Boot 3.3.x
- Spring Security 6.x with JWT
- Spring Data JPA (Hibernate)
- PostgreSQL 16
- Redis 7
- RabbitMQ 3
- Elasticsearch 8
- MinIO (S3-compatible)

### Frontend
- React 18 + TypeScript
- Vite
- Zustand (state)
- TanStack Query v5
- Tailwind CSS v3
- Recharts

## Quick Start

### 1. Database Setup

```bash
# Start PostgreSQL only (for initial setup)
docker run -d \
  --name eams-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=123 \
  -e POSTGRES_DB=eams_db \
  -p 5432:5432 \
  postgres:16
```

### 2. Backend Setup

```bash
# Clone or navigate to project
cd demo

# Configure environment
cp .env.example .env
# Edit .env with your settings

# Run migrations
mvn flyway:migrate

# Start application
mvn spring-boot:run
```

The backend runs on `http://localhost:8081`

### 3. Full Development Stack

```bash
# Start all services via Docker Compose
docker-compose up -d

# Backend logs
docker-compose logs -f app
```

### 4. Frontend Setup

```bash
# Navigate to frontend directory
cd eams-frontend

# Install dependencies
npm install

# Configure environment
cp .env.example .env

# Start development server
npm run dev
```

The frontend runs on `http://localhost:5173`

## Default Admin Credentials

After initial setup, create the first admin user via the Magic Link flow:

1. Go to `http://localhost:5173`
2. Enter admin email
3. Check email for Magic Link
4. Click link to authenticate

## Environment Variables

### Backend (.env)

| Variable | Description | Default |
|----------|-------------|---------|
| DB_USERNAME | PostgreSQL username | postgres |
| DB_PASSWORD | PostgreSQL password | 123 |
| JWT_SECRET | JWT signing secret | (change in prod) |
| MAIL_HOST | SMTP host | smtp.gmail.com |
| MAIL_PORT | SMTP port | 587 |
| MAIL_USERNAME | SMTP username | - |
| MAIL_PASSWORD | SMTP app password | - |
| REDIS_HOST | Redis host | localhost |
| REDIS_PORT | Redis port | 6379 |
| RABBITMQ_HOST | RabbitMQ host | localhost |
| RABBITMQ_PORT | RabbitMQ port | 5672 |
| ELASTICSEARCH_URI | Elasticsearch URI | http://localhost:9200 |
| ANTHROPIC_API_KEY | Anthropic API key | - |

### Frontend (.env)

| Variable | Description | Default |
|----------|-------------|---------|
| VITE_API_BASE_URL | Backend API URL | http://localhost:8081/api/v1 |
| VITE_APP_NAME | App display name | E-AMS |
| VITE_ENABLE_AI | Enable AI features | true |

## Module Overview

### Backend Modules

| Module | Description |
|--------|-------------|
| auth | Authentication (Magic Link, JWT, SSO) |
| core | Base entities & common utilities |
| assets | Asset registration & management |
| checkinout | Check-in/Check-out transactions |
| certificates | Certificate management |
| licences | Licence compliance |
| access | Temporary access grants |
| changes | Configuration change requests |
| users | User & role management |
| ai | AI predictions & chat |
| reports | Report generation |
| notifications | Notification dispatch |
| audit | Audit logging |
| admin | System administration |
| integration | External integrations |
| scheduler | Scheduled jobs |

### Frontend Pages

| Route | Page |
|-------|------|
| /login | Login |
| /dashboard | Executive Dashboard |
| /assets | Asset Register |
| /assets/:id | Asset Detail |
| /certificates | Certificate Register |
| /licences | Licence Management |
| /transactions | Check-In/Check-Out |
| /access-grants | Temporary Access |
| /change-requests | Change Requests |
| /users | User Management |
| /audit | Audit Log |
| /reports | Reports |
| /ai | AI Dashboard |
| /ai/chat | AI Chat |
| /admin | Administration |

## API Documentation

API documentation available at: `http://localhost:8081/swagger-ui.html`

OpenAPI spec: `http://localhost:8081/api-docs`

## Scheduled Jobs

| Job | Schedule | Description |
|-----|----------|-------------|
| CertificateExpiryScheduler | Daily 00:05 | Calculate days_remaining, status bands |
| LicenceExpiryScheduler | Daily 00:10 | Licence expiry & utilisation alerts |
| AccessGrantExpiryScheduler | Every 15 min | Flag overdue grants |
| ChangeRequestRevertScheduler | Every 15 min | Flag overdue reverts |
| AiPredictionScheduler | Daily 00:15 | AI batch predictions |

## Security

- MFA via Magic Link (email-based)
- JWT access tokens (15 min expiry)
- Refresh tokens (7 days, HttpOnly cookie)
- RBAC with category/location-scoped permissions
- HMAC-signed audit logs
- TLS 1.2+ required in production

## License

Proprietary - All rights reserved