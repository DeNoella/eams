# E-AMS Backend Development Prompt — Spring Boot
## For: Cursor AI / Windsurf / OpenCode AI
---

## ROLE & OBJECTIVE

You are a senior Java/Spring Boot architect. Your task is to build the complete backend for the **E-Asset Management System (E-AMS)** — a multi-tenant, AI-integrated, enterprise IT asset management platform. The backend must be production-grade, secure, fully tested, and follow a strict Spring MVC project structure.

Do NOT take shortcuts. Implement every module described below completely. Every table, every endpoint, every service method, every security rule.

---

## TECHNOLOGY STACK

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.3.x |
| Architecture | Spring MVC (layered: Controller → Service → Repository) |
| Database | PostgreSQL 16+ |
| ORM | Spring Data JPA + Hibernate |
| Security | Spring Security 6.x |
| Authentication | Magic Link (email-based, no OTP, no TOTP) + SSO (SAML 2.0 / OIDC) |
| MFA | Magic Link ONLY — no OTP, no SMS code, no TOTP authenticator apps |
| Cache | Redis 7+ (via Spring Cache + Lettuce) |
| Message Queue | RabbitMQ (via Spring AMQP) |
| Search | Elasticsearch 8+ (via Spring Data Elasticsearch) |
| API Documentation | SpringDoc OpenAPI 3.0 (Swagger UI) |
| Validation | Jakarta Bean Validation (Hibernate Validator) |
| Mapping | MapStruct |
| Email | Spring Mail (SMTP) + Thymeleaf email templates |
| File Storage | MinIO (S3-compatible) via AWS SDK |
| Secrets | Environment variables + Spring Cloud Vault (optional) |
| Migrations | Flyway |
| Testing | JUnit 5 + Mockito + Testcontainers |
| Build Tool | Maven |
| Containerisation | Docker + docker-compose |

---

## MANDATORY PROJECT STRUCTURE (Spring MVC)

```
eams-backend/
├── src/
│   ├── main/
│   │   ├── java/com/eams/
│   │   │   ├── EamsApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JpaConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── RabbitMQConfig.java
│   │   │   │   ├── ElasticsearchConfig.java
│   │   │   │   ├── MinioConfig.java
│   │   │   │   ├── MailConfig.java
│   │   │   │   ├── FlywayConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── CorsConfig.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── OrganisationController.java
│   │   │   │   ├── LocationController.java
│   │   │   │   ├── DepartmentController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── RoleController.java
│   │   │   │   ├── AssetCategoryController.java
│   │   │   │   ├── AssetController.java
│   │   │   │   ├── AssetRelationshipController.java
│   │   │   │   ├── CheckInOutController.java
│   │   │   │   ├── CertificateController.java
│   │   │   │   ├── LicenceController.java
│   │   │   │   ├── AccessGrantController.java
│   │   │   │   ├── ChangeRequestController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   ├── AuditLogController.java
│   │   │   │   ├── ReportController.java
│   │   │   │   ├── AiController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   └── AdminController.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── MagicLinkService.java
│   │   │   │   ├── OrganisationService.java
│   │   │   │   ├── LocationService.java
│   │   │   │   ├── DepartmentService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── RoleService.java
│   │   │   │   ├── AssetCategoryService.java
│   │   │   │   ├── AssetService.java
│   │   │   │   ├── AssetIdGeneratorService.java
│   │   │   │   ├── AssetRelationshipService.java
│   │   │   │   ├── CheckInOutService.java
│   │   │   │   ├── CertificateService.java
│   │   │   │   ├── LicenceService.java
│   │   │   │   ├── AccessGrantService.java
│   │   │   │   ├── ChangeRequestService.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   ├── AuditLogService.java
│   │   │   │   ├── ReportService.java
│   │   │   │   ├── AiService.java
│   │   │   │   ├── WorkflowService.java
│   │   │   │   ├── FileStorageService.java
│   │   │   │   └── SchedulerService.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── OrganisationRepository.java
│   │   │   │   ├── LocationRepository.java
│   │   │   │   ├── DepartmentRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── MagicLinkTokenRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   ├── RolePermissionRepository.java
│   │   │   │   ├── AssetCategoryRepository.java
│   │   │   │   ├── CustomFieldRepository.java
│   │   │   │   ├── AssetRepository.java
│   │   │   │   ├── AssetRelationshipRepository.java
│   │   │   │   ├── CheckInOutRepository.java
│   │   │   │   ├── CertificateRepository.java
│   │   │   │   ├── LicenceRepository.java
│   │   │   │   ├── AccessGrantRepository.java
│   │   │   │   ├── ChangeRequestRepository.java
│   │   │   │   ├── NotificationRuleRepository.java
│   │   │   │   ├── AuditLogRepository.java
│   │   │   │   ├── AiModelRepository.java
│   │   │   │   ├── AiPredictionRepository.java
│   │   │   │   ├── AiAnomalyRepository.java
│   │   │   │   ├── AiRecommendationRepository.java
│   │   │   │   └── AiChatSessionRepository.java
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── base/
│   │   │   │   │   └── BaseEntity.java          ← id, created_at, updated_at, created_by, is_deleted
│   │   │   │   ├── Organisation.java
│   │   │   │   ├── Location.java
│   │   │   │   ├── Department.java
│   │   │   │   ├── User.java
│   │   │   │   ├── MagicLinkToken.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── RolePermission.java
│   │   │   │   ├── AssetCategory.java
│   │   │   │   ├── CustomField.java
│   │   │   │   ├── Asset.java
│   │   │   │   ├── AssetCustomValue.java
│   │   │   │   ├── AssetRelationship.java
│   │   │   │   ├── CheckInOutTransaction.java
│   │   │   │   ├── Certificate.java
│   │   │   │   ├── Licence.java
│   │   │   │   ├── AccessGrant.java
│   │   │   │   ├── ChangeRequest.java
│   │   │   │   ├── NotificationRule.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── AuditLog.java
│   │   │   │   ├── AiModel.java
│   │   │   │   ├── AiPrediction.java
│   │   │   │   ├── AiAnomaly.java
│   │   │   │   ├── AiRecommendation.java
│   │   │   │   ├── AiChatSession.java
│   │   │   │   └── extended/
│   │   │   │       ├── AssetExtendedApplication.java
│   │   │   │       ├── AssetExtendedDatabase.java
│   │   │   │       ├── AssetExtendedServer.java
│   │   │   │       ├── AssetExtendedNetworkDevice.java
│   │   │   │       └── AssetExtendedClientDevice.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/         ← One request DTO per controller action
│   │   │   │   ├── response/        ← One response DTO per entity/view
│   │   │   │   └── common/
│   │   │   │       ├── PageResponse.java
│   │   │   │       ├── ApiResponse.java
│   │   │   │       └── ErrorResponse.java
│   │   │   │
│   │   │   ├── mapper/              ← MapStruct mappers (Entity ↔ DTO)
│   │   │   │
│   │   │   ├── enums/
│   │   │   │   ├── LocationType.java
│   │   │   │   ├── EmploymentType.java
│   │   │   │   ├── EmploymentStatus.java
│   │   │   │   ├── AssetStatus.java
│   │   │   │   ├── AssetCriticality.java
│   │   │   │   ├── LifecycleStage.java
│   │   │   │   ├── CertificateType.java
│   │   │   │   ├── CertificateStatus.java
│   │   │   │   ├── LicenceType.java
│   │   │   │   ├── LicenceStatus.java
│   │   │   │   ├── AccessType.java
│   │   │   │   ├── RiskLevel.java
│   │   │   │   ├── ChangeType.java
│   │   │   │   ├── ChangeStatus.java
│   │   │   │   ├── ActionType.java
│   │   │   │   ├── RelationshipType.java
│   │   │   │   ├── TransactionType.java
│   │   │   │   └── AiModelType.java
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── AccessDeniedException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── WorkflowException.java
│   │   │   │   ├── MagicLinkExpiredException.java
│   │   │   │   └── TenantAccessException.java
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── EamsUserDetails.java
│   │   │   │   ├── EamsUserDetailsService.java
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── TenantContextHolder.java
│   │   │   │   ├── TenantFilter.java
│   │   │   │   └── PermissionEvaluator.java
│   │   │   │
│   │   │   ├── scheduler/
│   │   │   │   ├── CertificateExpiryScheduler.java
│   │   │   │   ├── LicenceExpiryScheduler.java
│   │   │   │   ├── AccessGrantExpiryScheduler.java
│   │   │   │   ├── ChangeRequestRevertScheduler.java
│   │   │   │   ├── AiPredictionScheduler.java
│   │   │   │   └── AuditArchivalScheduler.java
│   │   │   │
│   │   │   └── util/
│   │   │       ├── AssetIdGenerator.java
│   │   │       ├── HmacSigner.java
│   │   │       ├── PaginationUtil.java
│   │   │       └── DateUtil.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── db/migration/           ← Flyway SQL scripts
│   │       │   ├── V1__create_core_tables.sql
│   │       │   ├── V2__create_asset_tables.sql
│   │       │   ├── V3__create_transaction_tables.sql
│   │       │   ├── V4__create_certificate_licence_tables.sql
│   │       │   ├── V5__create_access_change_tables.sql
│   │       │   ├── V6__create_audit_notification_tables.sql
│   │       │   ├── V7__create_ai_tables.sql
│   │       │   ├── V8__create_extended_asset_tables.sql
│   │       │   └── V9__seed_default_roles_categories.sql
│   │       └── templates/email/        ← Thymeleaf email templates
│   │           ├── magic-link.html
│   │           ├── expiry-alert.html
│   │           ├── overdue-alert.html
│   │           └── workflow-action.html
│   │
│   └── test/
│       └── java/com/eams/
│           ├── controller/             ← Integration tests (MockMvc)
│           ├── service/                ← Unit tests (Mockito)
│           └── repository/             ← Repository tests (Testcontainers)
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml             ← PostgreSQL, Redis, RabbitMQ, MinIO, Elasticsearch
├── pom.xml
└── README.md
```

---

## DATABASE — FULL SCHEMA IMPLEMENTATION

Use Flyway for all migrations. Implement **every** table below exactly as specified. Every table must include: `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`, `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`, `updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`, `created_by UUID REFERENCES users(id)`, `is_deleted BOOLEAN NOT NULL DEFAULT FALSE`.

### Tables to implement (in migration order):

**V1 — Core:**
- `organisations` — multi-tenant root. Columns: name, slug (UNIQUE), logo_url, primary_color, timezone, locale, country_code, subscription_plan, max_assets, is_active, settings (JSONB)
- `locations` — self-referencing hierarchy (parent_id). Columns: organisation_id, parent_id, location_type (ENUM: country/region/branch/building/floor/room/rack), name, code, address, latitude, longitude, contact_email, is_active
- `departments` — self-referencing (parent_id). Columns: organisation_id, location_id, parent_id, name, code (UNIQUE), head_user_id, cost_centre, is_active

**V2 — Users & Roles:**
- `users` — Columns: organisation_id, employee_id, full_name, email (UNIQUE), phone, job_title, department_id, location_id, line_manager_id, ad_username, employment_type (ENUM), employment_status (ENUM), access_expiry_date, mfa_enabled (DEFAULT FALSE — Magic Link handles MFA), last_login_at, failed_login_count, locked_at, avatar_url, is_active
- `magic_link_tokens` — Columns: user_id, token (VARCHAR 255 UNIQUE), token_hash (TEXT), expires_at (TIMESTAMPTZ), used_at (TIMESTAMPTZ NULLABLE), ip_address (INET), is_used (BOOLEAN DEFAULT FALSE)
- `roles` — Columns: organisation_id, name, code, description, is_system_role, requires_magic_link_auth (BOOLEAN DEFAULT TRUE), session_timeout_minutes
- `role_permissions` — Columns: role_id, permission_code, asset_category_id (NULLABLE), location_id (NULLABLE), can_create, can_read, can_update, can_delete, can_approve, can_export
- `user_roles` — junction table: user_id, role_id, assigned_by_user_id, assigned_at

**V3 — Assets:**
- `asset_categories` — Columns: organisation_id, name, code, icon, description, id_format, default_criticality, depreciation_years, is_physical, requires_location, is_active, sort_order
- `custom_fields` — Columns: organisation_id, asset_category_id, field_label, field_name, field_type (ENUM: text/number/date/dropdown/boolean/url/email/file), options (JSONB), is_mandatory, is_searchable, default_value, validation_regex, help_text, sort_order, is_active
- `assets` (CORE) — All columns from SRS Section 4.8 exactly: asset_id_display, asset_category_id, name, description, location_id, department_id, assigned_user_id, technical_owner_id, business_owner_id, backup_owner_id, criticality, status, year_of_installation, acquisition_cost, currency_code, annual_cost, purchase_order_ref, vendor_name, support_contract_ref, support_expiry_date, warranty_expiry_date, last_audit_date, next_audit_due_date, notes, qr_code_url, barcode, lifecycle_stage, decommission_date, decommission_reason
- `asset_custom_values` — Columns: asset_id, custom_field_id, field_value (TEXT)
- `asset_relationships` — Columns: organisation_id, source_asset_id, target_asset_id, relationship_type (ENUM), description, is_active
- `asset_attachments` — Columns: asset_id, file_name, file_url, file_size_bytes, uploaded_by_user_id

**V4 — Transactions:**
- `check_in_out_transactions` — All columns from SRS Section 4.11 exactly
- `certificates` — All columns from SRS Section 4.12 exactly
- `licences` — All columns from SRS Section 4.13 exactly

**V5 — Governance:**
- `access_grants` — All columns from SRS Section 4.14 exactly
- `change_requests` — All columns from SRS Section 4.15 exactly

**V6 — Audit & Notifications:**
- `audit_logs` — All columns from SRS Section 4.10. CRITICAL: Add PostgreSQL trigger to PREVENT any UPDATE or DELETE on this table. Add HMAC-SHA256 signature column.
- `notification_rules` — All columns from SRS Section 4.16
- `notifications` — Columns: user_id, rule_id, channel (ENUM: email/in_app/sms), subject, body, sent_at, read_at, is_read

**V7 — AI:**
- `ai_models` — All columns from SRS Section 5.1
- `ai_predictions` — All columns from SRS Section 5.2
- `ai_anomalies` — All columns from SRS Section 5.3
- `ai_recommendations` — All columns from SRS Section 5.4
- `ai_chat_sessions` — All columns from SRS Section 5.5

**V8 — Extended Asset Tables:**
- `asset_extended_applications` — All fields from SRS Section 6.1
- `asset_extended_databases` — All fields from SRS Section 6.2
- `asset_extended_servers` — All fields from SRS Section 6.3
- `asset_extended_network_devices` — All fields from SRS Section 6.4
- `asset_extended_client_devices` — All fields from SRS Section 6.5

**V9 — Seed Data:**
- Insert default system roles: System Admin, Asset Manager, Security Officer, Change Manager, Auditor, Branch Staff, Procurement Officer, AI Analyst, Read-Only Viewer
- Insert default asset categories: Software Applications (APP), Databases (DB), Servers (SRV), Network Devices (NET), Client Devices (DEV)

---

## AUTHENTICATION — MAGIC LINK (NO OTP, NO TOTP)

This is the ONLY authentication method for local users. Do NOT implement OTP, TOTP, SMS codes, or authenticator apps.

### Magic Link Flow:
1. User enters their email on the login page → POST `/api/v1/auth/magic-link/request`
2. System generates a cryptographically secure random token (256-bit, URL-safe Base64)
3. Store the SHA-256 hash of the token in `magic_link_tokens` table (never store raw token)
4. Set expiry: 15 minutes from generation
5. Send email with link: `https://{frontend-domain}/auth/verify?token={raw-token}&email={email}`
6. User clicks link → GET `/api/v1/auth/magic-link/verify?token=...&email=...`
7. System: hash the incoming token → look up by hash → validate not used, not expired
8. Mark token as used (`is_used = true`, `used_at = NOW()`)
9. Generate JWT access token (15 min expiry) + refresh token (7 days, stored in HttpOnly cookie)
10. Return JWT to frontend
11. Previous unused tokens for same user are invalidated on new request

### JWT Implementation:
- Use `io.jsonwebtoken:jjwt` library
- Access token payload: `{ sub: userId, org: organisationId, roles: [...], exp: ... }`
- Refresh token: stored as HttpOnly Secure SameSite=Strict cookie
- POST `/api/v1/auth/refresh` — issue new access token using refresh cookie
- POST `/api/v1/auth/logout` — invalidate refresh token, clear cookie

### SSO (Secondary):
- Implement SAML 2.0 using Spring Security SAML2
- Implement OIDC using Spring Security OAuth2 Client
- On SSO login, auto-provision user if not exists (linked by email)

---

## SECURITY IMPLEMENTATION

Implement ALL of these in `SecurityConfig.java`:

```java
// Required security rules:
- Stateless JWT authentication (no server-side sessions for API)
- CORS configured for React frontend origin only
- CSRF disabled for REST API (JWT mitigates this)
- All endpoints require authentication except: /api/v1/auth/**, /actuator/health
- Method-level security: @PreAuthorize with custom permission expressions
- Rate limiting on magic link endpoint: max 3 requests per email per 15 minutes (use Redis)
- OWASP security headers: CSP, HSTS, X-Frame-Options, X-Content-Type-Options, Referrer-Policy
- TLS 1.2+ enforced (configure in application.yml)
- Account lockout: after 5 failed magic link requests → lock for 30 minutes
- Tenant isolation: TenantFilter extracts org from JWT → sets TenantContextHolder → all JPA queries automatically scoped by organisation_id
- Sensitive fields encrypted at rest using @Convert with AES-256 JPA attribute converter
```

### Multi-Tenancy Filter:
Every repository query MUST be scoped by `organisation_id`. Implement a `@EntityListener` or Hibernate filter that automatically appends `WHERE organisation_id = :currentTenantId` to prevent cross-tenant data leakage.

### RBAC Permission Check:
```java
@PreAuthorize("hasPermission(#assetId, 'asset', 'can_read')")
// Custom PermissionEvaluator checks role_permissions table
// Supports category-scoped and location-scoped permissions
```

---

## COMPLETE API ENDPOINTS

All endpoints: prefix `/api/v1/`, return `ApiResponse<T>` wrapper, use proper HTTP status codes, support pagination (`page`, `size`, `sort`) where applicable.

### Auth:
- POST `/auth/magic-link/request` — request magic link email
- GET `/auth/magic-link/verify` — verify token, return JWT
- POST `/auth/refresh` — refresh access token
- POST `/auth/logout` — invalidate session
- GET `/auth/saml2/login` — SAML SSO initiation
- GET `/auth/oidc/login` — OIDC SSO initiation

### Assets (FR-AR):
- GET `/assets` — paginated, filterable by category/location/department/status/criticality
- POST `/assets` — create (triggers audit log)
- GET `/assets/{id}` — full asset detail with extended fields and relationships
- PUT `/assets/{id}` — update (every field change logged to audit_logs)
- DELETE `/assets/{id}` — soft delete
- POST `/assets/import` — CSV/Excel bulk import with validation report
- PUT `/assets/bulk` — bulk update with mandatory approval workflow trigger
- GET `/assets/{id}/history` — full audit history for asset
- GET `/assets/{id}/relationships` — CMDB relationships
- POST `/assets/{id}/relationships` — add relationship
- GET `/assets/{id}/qr-code` — generate QR code PNG
- POST `/assets/{id}/attachments` — upload file to MinIO
- GET `/assets/search` — full-text search via Elasticsearch

### Check-In/Out (FR-CO):
- POST `/transactions/checkout` — check out asset to user
- POST `/transactions/checkin` — check in asset
- POST `/transactions/transfer` — transfer between locations/departments
- GET `/transactions` — paginated transaction history
- GET `/transactions/overdue` — all overdue returns
- GET `/assets/{id}/transactions` — transaction history for specific asset

### Certificates (FR-CL):
- GET `/certificates` — all, filterable by status/environment/expiry-range
- POST `/certificates` — register certificate
- PUT `/certificates/{id}` — update
- GET `/certificates/{id}/renewal-history` — renewal chain
- POST `/certificates/{id}/renew` — trigger renewal workflow
- GET `/certificates/expiry-calendar` — calendar view for next N months

### Licences (FR-CL):
- GET `/licences` — all, with utilisation stats
- POST `/licences` — register licence
- PUT `/licences/{id}` — update seat counts etc.
- GET `/licences/compliance-report` — FR-RP-003 report data
- GET `/licences/{id}/utilisation` — seat usage over time

### Access Grants (FR-AC):
- GET `/access-grants` — all, filterable by status/type
- POST `/access-grants` — create grant (requires approver ≠ requestor)
- POST `/access-grants/{id}/revoke` — formal revocation with confirmation
- GET `/access-grants/overdue` — expired-but-not-revoked grants

### Change Requests (FR-AC):
- GET `/change-requests` — all, filterable by status
- POST `/change-requests` — propose change
- POST `/change-requests/{id}/approve` — approve (approver ≠ proposer enforced)
- POST `/change-requests/{id}/apply` — mark as applied
- POST `/change-requests/{id}/revert` — mark as reverted
- POST `/change-requests/{id}/make-permanent` — make permanent with justification
- GET `/change-requests/overdue-reverts` — pending reverts past scheduled date

### Users & Roles (FR-UM):
- GET `/users` — paginated user list
- POST `/users` — create user
- PUT `/users/{id}` — update
- PUT `/users/{id}/status` — change employment status (triggers offboarding if Terminated)
- GET `/users/{id}/assets` — all assets attributed to user
- GET `/users/{id}/access-rights` — full access review report
- POST `/users/{id}/roles` — assign role (requires second admin approval)
- GET `/roles` — all roles with permissions
- POST `/roles` — create custom role
- PUT `/roles/{id}/permissions` — update permission set

### Notifications (FR-NW):
- GET `/notifications/my` — current user's notifications
- PUT `/notifications/{id}/read` — mark as read
- GET `/notification-rules` — all rules
- POST `/notification-rules` — create rule
- PUT `/notification-rules/{id}` — update rule

### Audit Log (FR-AU):
- GET `/audit-logs` — searchable, filterable, paginated
- GET `/audit-logs/export` — PDF export of filtered results
- GET `/audit-logs/{id}` — single entry with tamper verification
- POST `/audit-reviews` — create annual audit review record
- GET `/audit-compliance` — compliance certificate data per category

### Reports (FR-RP):
- GET `/reports/asset-inventory` — export PDF/XLSX/CSV
- GET `/reports/licence-compliance` — FR-RP-003
- GET `/reports/it-audit` — FR-RP-004 for date range
- GET `/reports/depreciation` — FR-RP-008
- GET `/reports/security-posture` — FR-RP-009
- GET `/reports/dashboard` — executive dashboard data

### AI (FR-AI):
- GET `/ai/predictions` — all predictions for org
- GET `/ai/anomalies` — all anomalies
- GET `/ai/recommendations` — all recommendations
- POST `/ai/chat` — send message, get AI response (calls Anthropic Claude API)
- GET `/ai/chat/sessions` — list chat sessions
- GET `/ai/models` — registered AI models
- POST `/ai/models` — register model
- PUT `/ai/models/{id}/approve` — approve for production
- POST `/ai/predictions/{id}/feedback` — human feedback loop

### Dashboard (FR-RP-001):
- GET `/dashboard/executive` — summary counts, expiry distribution, open items
- GET `/dashboard/ai-insights` — AI-powered summary in plain English

### Admin (FR-SY):
- GET/POST/PUT `/admin/organisations` — tenant management
- GET/POST/PUT `/admin/asset-categories` — category configuration
- GET/POST/PUT `/admin/custom-fields` — custom field management
- GET/POST/PUT `/admin/notification-rules` — notification configuration
- GET `/admin/config/export` — export full config as JSON
- POST `/admin/config/import` — import config JSON

---

## BUSINESS LOGIC — IMPLEMENT ALL OF THESE

### Asset ID Generation:
```java
// Template: {CODE}-{LOC}-{YYYY}-{SEQ:4}
// Example: APP-NBI-2026-0042
// AssetIdGeneratorService generates next sequential number per org+category+year
// Store last sequence in DB; use DB-level locking to prevent duplicates
```

### Audit Log — Every Write Operation:
```java
// AuditLogService.log() must be called from every Service method that mutates data
// Log: actor_user_id, actor_ip (from request context), action_type, resource_type,
//      resource_id, field_name (for updates), old_value, new_value, change_reason
// Sign each entry: HMAC-SHA256(entry_data, rotating_secret_key)
// NEVER allow UPDATE or DELETE on audit_logs — enforce via DB trigger
```

### Scheduled Jobs (run daily at 02:00 AM org timezone):
- **CertificateExpiryScheduler**: Calculate `days_remaining` for all certs. Update status bands. Trigger notifications at: 180, 90, 30, 14, 7, 1 days. Initiate renewal workflow at `renewal_lead_days`.
- **LicenceExpiryScheduler**: Same pattern for licences. Also check seat utilisation — alert if > `seat_alert_threshold_pct`.
- **AccessGrantExpiryScheduler**: Flag grants past `expires_at` as OVERDUE. Escalate to Security Officer if not revoked within 1 hour of expiry.
- **ChangeRequestRevertScheduler**: Flag changes past `scheduled_revert_date` with status=pending_revert as OVERDUE. Escalate.
- **AiPredictionScheduler**: Trigger AI batch prediction jobs during off-peak hours.

### Workflow Engine (FR-NW-003):
All multi-step workflows must enforce:
- Proposer ≠ Approver (enforced at DB constraint AND service layer)
- Configurable step sequences from `notification_rules`
- Automatic escalation to line manager after configurable deadline
- Every workflow state transition logged to audit_logs

### Offboarding Trigger (FR-UM-002):
When `employment_status` → TERMINATED or SUSPENDED:
1. Find all assets where `assigned_user_id = userId`
2. Create return tasks in `check_in_out_transactions` with 24-hour deadline
3. Find all active `access_grants` where `requestor_user_id = userId` → auto-flag for revocation
4. Send notifications to line manager + Asset Manager + Security Officer
5. Log entire offboarding sequence to audit_log

---

## AI INTEGRATION (Anthropic Claude API)

### AiService.java:
```java
// POST to https://api.anthropic.com/v1/messages
// Model: claude-sonnet-4-20250514
// Use for:
//   1. Natural language asset queries (FR-AI-004) — convert user question to structured query
//   2. Executive dashboard narration (FR-AI-002) — plain English summary
//   3. Report narration (FR-AI-013) — auto-generate executive summary
//   4. Recommendations engine (FR-AI-009) — analyse utilisation data
// 
// IMPORTANT: Pseudonymise PII before sending to API (replace names with role+id tokens)
// Store API key in environment variable ANTHROPIC_API_KEY — never in code or DB
// Log every AI call to audit_logs with token counts
// Store full conversation history in ai_chat_sessions
```

---

## NON-FUNCTIONAL IMPLEMENTATION

- **Response time**: Add `@Cacheable` (Redis) to all dashboard and report aggregation queries
- **Pagination**: All list endpoints use Spring Data `Pageable`, return `Page<T>` wrapped in `PageResponse`
- **Soft deletes**: All entities have `is_deleted`. Use `@Where(clause = "is_deleted = false")` on all entities
- **Logging**: SLF4J + Logback. Structured JSON logs in production. Separate audit log vs application log
- **Error handling**: `GlobalExceptionHandler` catches all exceptions, returns consistent `ErrorResponse{code, message, details, timestamp}`
- **Validation**: `@Valid` on all request bodies. Custom validators for business rules (e.g., approver ≠ requestor)
- **Database indexes**: Add indexes on: organisation_id (all tables), email (users), status columns, expiry date columns, asset_id_display
- **Connection pooling**: HikariCP with pool size = 20 (configurable)
- **Health checks**: Spring Actuator endpoints for DB, Redis, RabbitMQ, Elasticsearch

---

## DOCKER COMPOSE (development)

```yaml
services:
  postgres:    image: postgres:16, port 5432
  redis:       image: redis:7-alpine, port 6379
  rabbitmq:    image: rabbitmq:3-management, ports 5672, 15672
  minio:       image: minio/minio, ports 9000, 9001
  elasticsearch: image: elasticsearch:8.13.0, port 9200
  app:         build from Dockerfile, depends on all above
```

---

## DELIVERABLES CHECKLIST

Ensure ALL of the following are implemented before considering the backend complete:

- [ ] All Flyway migrations (V1–V9) with complete schemas
- [ ] All JPA entities with proper relationships and constraints
- [ ] All repositories with custom JPQL queries where needed
- [ ] All service classes with full business logic
- [ ] All REST controllers with proper validation and error handling
- [ ] Magic Link authentication end-to-end
- [ ] JWT access + refresh token flow
- [ ] SAML 2.0 + OIDC SSO
- [ ] Multi-tenancy isolation (every query scoped by organisation_id)
- [ ] RBAC with category and location-scoped permissions
- [ ] All scheduled jobs
- [ ] Audit log with HMAC signatures and immutability enforcement
- [ ] Anthropic Claude API integration for AI features
- [ ] RabbitMQ consumers for async notification dispatch
- [ ] MinIO file upload/download
- [ ] Elasticsearch indexing for assets
- [ ] Full OpenAPI 3.0 documentation
- [ ] Unit tests for all service classes
- [ ] Integration tests for all controllers
- [ ] docker-compose.yml for full local development stack
- [ ] README with setup and run instructions
