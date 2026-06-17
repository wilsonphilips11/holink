# HoLink

A creator link-in-bio platform. Users can create profiles, manage links, publish public pages, and track click analytics. Built with Vue 3 + Spring Boot 3 + PostgreSQL.

## Quick start

```bash
docker compose up --build
```

| Service    | URL                            |
| ---------- | ------------------------------ |
| Frontend   | http://localhost:3000          |
| Backend    | http://localhost:8080          |
| PostgreSQL | localhost:5432 (holink/holink) |

### Local development (without Docker)

**Backend**

```bash
cd backend
# Start PostgreSQL locally or use docker compose up postgres -d
mvn spring-boot:run
```

**Frontend**

```bash
cd frontend
npm install
npm run dev
```

## Architecture

### Backend layers

```
controller/   → HTTP endpoints, request validation, auth boundaries
service/      → Business logic, ownership checks, orchestration
repository/   → Spring Data JPA persistence
entity/       → JPA domain models
dto/          → API request/response contracts
mapper/       → Entity ↔ DTO mapping
security/     → JWT filter, Spring Security config
validation/   → Bean Validation + sanitization utilities
exception/    → Global exception handler, standardized errors
config/       → Async executor, rate limiting
analytics/    → Async click event listener
```

**Flow:** Controller validates input → Service enforces ownership & rules → Repository persists → Mapper shapes responses.

### Frontend layers

```
pages/        → Route-level views (Dashboard, Public Profile, Auth)
components/   → Reusable UI (ProfileForm, LinkManager, AnalyticsSummary)
layouts/      → Page wrappers
stores/       → Pinia state (auth, profile/links/analytics)
services/     → Axios API client with JWT refresh interceptor
router/       → Vue Router with auth guards
types/        → TypeScript interfaces mirroring API
composables/  → Shared reactive logic
utils/        → Client-side validation (mirrors backend rules)
```

**Flow:** Page loads → Store fetches from API → Components render escaped text → User actions POST/PUT/DELETE → Store reloads from API.

## API documentation

All responses use a standard envelope:

**Success**

```json
{ "success": true, "data": {} }
```

**Error**

```json
{ "success": false, "message": "Username already exists", "errors": [] }
```

### Authentication

| Method | Endpoint             | Auth                                     | Description             |
| ------ | -------------------- | ---------------------------------------- | ----------------------- |
| POST   | `/api/auth/register` | No                                       | Register new user       |
| POST   | `/api/auth/login`    | No                                       | Login, returns JWT pair |
| POST   | `/api/auth/refresh`  | Refresh token in `Authorization: Bearer` | Refresh access token    |

Passwords are hashed with **bcrypt**. Access tokens expire in 15 minutes; refresh tokens in 7 days.

### Profiles

| Method | Endpoint                 | Auth | Description                   |
| ------ | ------------------------ | ---- | ----------------------------- |
| POST   | `/api/profiles`          | Yes  | Create profile (one per user) |
| PUT    | `/api/profiles/{id}`     | Yes  | Update profile (owner only)   |
| GET    | `/api/profiles/me`       | Yes  | Get current user's profile    |
| GET    | `/api/public/{username}` | No   | Public profile + active links |

**Username normalization:** `"John Doe"` → `"johndoe"` (lowercase, trim, strip special chars). Rejects all-uppercase input (`UPPERCASE`), lowercase with spaces (`john doe`), and `@`.

### Links

| Method | Endpoint                | Auth | Description                      |
| ------ | ----------------------- | ---- | -------------------------------- |
| GET    | `/api/links`            | Yes  | List user's links                |
| POST   | `/api/links`            | Yes  | Create link                      |
| PUT    | `/api/links/{id}`       | Yes  | Update link (owner)              |
| DELETE | `/api/links/{id}`       | Yes  | Soft delete (owner)              |
| PUT    | `/api/links/reorder`    | Yes  | Reorder links                    |
| POST   | `/api/links/{id}/click` | No   | Track click, return redirect URL |

### Analytics

| Method | Endpoint                 | Auth | Description                         |
| ------ | ------------------------ | ---- | ----------------------------------- |
| GET    | `/api/analytics/links`   | Yes  | Clicks per link, total, top, by day |
| GET    | `/api/analytics/profile` | Yes  | Profile-level summary               |

## Security decisions

### XSS prevention

- **Backend:** `@NoHtmlContent` Bean Validation on username, display name, bio, and link title. Rejects `<script>`, HTML tags, and event handlers.
- **Frontend:** Client-side validation mirrors backend rules. All user content rendered with Vue text interpolation (`{{ }}`) — **never `v-html`**.

### URL validation

- Only `http://` and `https://` schemes allowed.
- Rejects `javascript:`, `data:`, `vbscript:`, `file:`, bare domains, and `ftp://`.

### Ownership checks

Every mutating profile/link operation verifies the authenticated user owns the resource. Cross-user access returns **403 Forbidden**.

### JWT authentication

Stateless JWT with access + refresh tokens. Protected routes require `Authorization: Bearer <accessToken>`. Refresh endpoint accepts refresh token to issue new pair.

### Privacy

- Raw passwords never stored (bcrypt hash only).
- Client IP addresses hashed with SHA-256 + server salt before storage in `click_events.ip_hash`.

### Rate limiting

Click endpoint (`POST /api/links/{id}/click`) is limited to **100 requests/minute per IP** using in-memory Bucket4j token buckets. Returns **429 Too Many Requests** when exceeded.

> **Note:** In-memory rate limiting works for single-instance deployments. Production should use Redis-backed Bucket4j for horizontal scaling.

## Analytics design

### Click tracking flow

```
Client clicks link
    → POST /api/links/{id}/click
    → Validate link exists & is active
    → Return redirect URL immediately  ← user never waits
    → Publish ClickTrackingEvent (async)
    → ClickTrackingListener persists to click_events
```

**Tracking never blocks redirect.** If the async database write fails, the error is logged and the user still reaches their destination. The public profile page also falls back to direct URL navigation if the tracking API fails.

### Async processing

Uses Spring `@Async` + `@EventListener` on a dedicated thread pool (`AsyncConfig`). This decouples redirect latency from database write latency.

### Duplicate clicks

Each click creates a separate `click_events` row. Duplicate clicks from the same visitor are counted individually (no deduplication). This is intentional for a simplified analytics model — production would add session/fingerprint deduplication and bot filtering.

## Database schema

See `backend/src/main/resources/db/migration/V1__init_schema.sql`.

| Table          | Key constraints                                                 |
| -------------- | --------------------------------------------------------------- |
| `users`        | UUID PK, unique email                                           |
| `profiles`     | UUID PK, unique username (indexed), one per user                |
| `links`        | UUID PK, soft delete via `deleted_at`, ordered by `position`    |
| `click_events` | UUID PK, indexed on `link_id`, `profile_username`, `created_at` |

## Testing

**Backend**

```bash
cd backend
mvn test
```

Tests cover:

- `ProfileService` — username normalization, duplicate handling, ownership
- `LinkService` — CRUD, async click tracking, redirect URL
- `UrlValidationUtil` — safe/unsafe URL matrix
- `ApiIntegrationTest` — full flow with MockMvc (register → profile → link → click → analytics → delete)

**Frontend**

```bash
cd frontend
npm test
```

Tests cover validation utilities and component rendering (`ConfirmModal`).

## Tradeoffs

| Area          | Simplified                 | Production improvement                  |
| ------------- | -------------------------- | --------------------------------------- |
| Rate limiting | In-memory per IP           | Redis-backed distributed buckets        |
| Click events  | Spring `@Async` in-process | Kafka + consumer workers                |
| Analytics     | Raw click counts           | Unique visitors, bot filtering, funnels |
| Auth          | JWT in localStorage        | HttpOnly cookies + CSRF protection      |
| Avatar        | URL string only            | S3 upload with image processing         |
| Caching       | None                       | Redis cache for public profiles         |
| Monitoring    | Actuator health only       | Prometheus, Grafana, alerting           |
| Audit         | None                       | Audit log table for admin actions       |

## Future improvements

- **Redis caching** for hot public profiles
- **Kafka click events** for scalable analytics pipeline
- **CDN** for static assets and public profile pages
- **SEO & Open Graph** meta tag generation for social sharing
  - Each public profile page would generate dynamic SEO metadata based on the user's profile information. The page title could use the display name (e.g. "Wilson | HoLink") and the meta description could be derived from the profile bio. For social sharing, Open Graph tags (og:title, og:description, og:image, and og:url) would be generated per profile. When a user shares their HoLink URL on WhatsApp, Telegram, Facebook, LinkedIn, or X, these platforms would fetch the Open Graph metadata and display a rich preview card containing the user's name, bio, and a dynamically generated profile image.
- **Unique visitor analytics** via hashed session fingerprints
  - Generate a fingerprint from non-sensitive request attributes such as IP address, User-Agent, language, or device informationa
- **Monitoring & observability** (Datadog, Sentry, structured logging)
- **Audit logs** for compliance and admin review
- **Custom domains** per creator
- **Link scheduling** (publish/unpublish by date)

## Project structure

```
holink/
├── backend/          # Spring Boot 3 API
├── frontend/         # Vue 3 SPA
├── docker-compose.yml
└── README.md
```

## Environment variables

| Variable                | Default     | Description                                  |
| ----------------------- | ----------- | -------------------------------------------- |
| `JWT_SECRET`            | dev default | JWT signing key (min 256 bits in production) |
| `IP_HASH_SALT`          | dev default | Salt for IP hashing                          |
| `CLICK_RATE_LIMIT`      | 100         | Max click requests per IP per minute         |
| `SPRING_DATASOURCE_URL` | localhost   | PostgreSQL JDBC URL                          |
| `VITE_API_BASE_URL`     | `/api`      | Frontend API base path                       |

---
