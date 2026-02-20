# Task Manager API (V2) — JWT + Scaling + Observability + AI (Demo-ready)

A production-ready RESTful **Task Manager API** built with **Java 21** and **Spring Boot**.

This project is part of a full-stack portfolio (React + Vite + TypeScript + Tailwind) and demonstrates:
- Clean architecture and validation
- Database migrations (Flyway)
- Observability (Actuator)
- Security (JWT auth + CORS + basic rate limiting on login)
- Practical scaling/resilience improvements (pagination cap, indexes)
- Safe logging (standardized logs without leaking tokens/passwords)
- AI feature (demo-ready with fallback; OpenAI key optional)

---

## Production (Render)

- Base URL: https://task-manager-api-njza.onrender.com  
- Health (Actuator): https://task-manager-api-njza.onrender.com/actuator/health  
- Root status: https://task-manager-api-njza.onrender.com/  
  - Returns: `{"status":"ok","service":"task-manager-api"}`

**Note:** Render Free may have a cold start (~50s) on the first request.

---

## Frontend (Production Demo)

- https://gustavomprado.github.io/task-manager-frontend/  
  - Includes a JWT-protected AI feature: **"Sugerir prioridade"** (calls this API endpoint `POST /ai/suggest-priority`)

---

## API Overview

### Auth (JWT)
- `POST /auth/login` — returns `{ "token": "<jwt>" }`

### Tasks (`/tasks`) — Protected (JWT required)
- `POST /tasks` — create task
- `GET /tasks` — list tasks (paginated)
  - supports: `page`, `size`, `sort`, `q`, `status`, `priority`
- `GET /tasks/{id}` — get by id
- `PUT /tasks/{id}` — update (full)
- `PATCH /tasks/{id}` — partial update
- `DELETE /tasks/{id}` — delete

### AI (V2) (`/ai`) — Protected (JWT required)
- `POST /ai/suggest-priority` — suggests `priority` based on `title/description`
  - Request: `{ "title": "...", "description": "..." }`
  - Response: `{ "priority": "LOW|MEDIUM|HIGH", "reason": "..." }`

**AI behavior**
- If `OPENAI_API_KEY` is configured in the backend runtime, the API will call OpenAI server-to-server.
- If the key is missing/empty (demo mode) or if OpenAI fails, the API falls back to a deterministic mock response.

### Health (Actuator)
- `GET /actuator/health` — should return `UP`

---

## Security Notes (V2)

### JWT protection
- `/tasks/**` requires `Authorization: Bearer <token>`
- `/ai/**` requires `Authorization: Bearer <token>`
- Without token: `401`
- With valid token: `200`

### CORS
CORS is configured to allow requests from the GitHub Pages frontend:
- https://gustavomprado.github.io

### Login rate limit (basic)
A basic in-memory rate limit is applied to `/auth/login`:
- After **5 attempts per minute per IP**, returns **429**.

### Pagination cap
To prevent abusive queries, the API enforces a **page size cap**:
- Requests with `size` above the cap are coerced (e.g. `size=999` becomes `size=50`).

### Safe logging (no sensitive leaks)
Logging is standardized via `logback-spring.xml` and must not leak:
- JWT tokens
- Authorization headers
- passwords/secrets

---

## Security Documentation / Pentest Evidence

- Security summary and controls: `SECURITY.md`
- Local pentest report (controlled scope, Kali): `docs/PENTEST.md`
- Portfolio project summary (recruiter-friendly): `docs/PORTFOLIO_SUMMARY.md`

### Pentest scope (D2)
A light, controlled pentest was executed in a local authorized lab (Kali Linux + VirtualBox + host-only network) against the local API instance.  
The objective was to generate real portfolio evidence of:
- access control (`401` without token / `200` with token)
- AI endpoint protection
- validation behavior (`400`, not `500`)
- pagination cap enforcement
- basic recon visibility (port open / service reachable)

Evidence files are versioned in:
- `docs/pentest-evidencias/`

---

## Scaling / Database (Flyway)

Flyway migrations are used to version the schema:
- `V1__create_tasks_table.sql`
- `V2__add_indexes_timestamps.sql`

Evidence is recorded in `flyway_schema_history`.

---

## Tech Stack

- Java 21
- Spring Boot 3 (Web, Validation, Data JPA)
- PostgreSQL
- Flyway (migrations)
- Spring Boot Actuator
- OpenAPI / Swagger (SpringDoc)
- H2 (tests)
- JUnit 5 & Mockito
- Docker & Docker Compose
- Gradle

---

## Running locally (Docker Compose)

From the folder where `docker-compose.yml` is located:

```powershell
docker compose up -d --build
```

API:
- http://localhost:8081

Health:
- http://localhost:8081/actuator/health

Stop:
```powershell
docker compose down
```

---

## AI Setup (Optional)

### Demo mode (default)
If `OPENAI_API_KEY` is empty, the AI endpoint works in demo mode (mock fallback), which is suitable for portfolio demos without external dependencies.

### Enable OpenAI (server-to-server)
Set the environment variable in the backend runtime (Docker Compose / Render):
- `OPENAI_API_KEY` = your OpenAI API key
- (optional) `OPENAI_MODEL` = `gpt-4.1-mini`

Important:
- The key must stay in the **backend only** (never in the frontend).
- If OpenAI fails, the endpoint falls back to mock to keep the product usable.

---

## Evidence (PowerShell)

### Important note (PowerShell)
`curl.exe` on Windows can conflict with `-H` / `-d` flags.  
For authenticated calls, prefer `Invoke-RestMethod`.

### 0) Health (quick check)

```powershell
Invoke-RestMethod -Method Get -Uri "https://task-manager-api-njza.onrender.com/actuator/health"
```

Expected:
- `status : UP`

### 1) Login (get token)

```powershell
$base = "https://task-manager-api-njza.onrender.com"
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body $loginBody).token
$token
```

Expected:
- Prints a JWT token string.

### 2) List tasks (Bearer token)

```powershell
Invoke-RestMethod -Method Get -Uri "$base/tasks?page=0&size=5&sort=id,desc" -Headers @{ Authorization = "Bearer $token" }
```

Expected:
- `content` array with tasks and pagination fields.

### 3) Create task (Bearer token)

```powershell
$body = @{
  title = "Prod task"
  description = "created via PowerShell"
  status = "TODO"
  priority = "LOW"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$base/tasks" -ContentType "application/json" -Headers @{ Authorization = "Bearer $token" } -Body $body
```

Expected:
- Returns the created task (with `id`).

### 4) Pagination cap proof (LOCAL, shows capped size explicitly)

```powershell
$base = "http://localhost:8081"
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body $loginBody).token

Invoke-RestMethod -Method Get -Uri "$base/tasks?page=0&size=999" -Headers @{ Authorization = "Bearer $token" } | ConvertTo-Json -Depth 6
```

Expected:
- In the JSON response, `page.size` shows the effective capped size (e.g. `50`), even if `size=999` was requested.

### 5) Login rate limit proof (429) — PROD

```powershell
$base="https://task-manager-api-njza.onrender.com"
$body = @{ username="admin"; password="admin123" } | ConvertTo-Json

1..6 | ForEach-Object {
  try {
    Invoke-RestMethod -Method POST -Uri "$base/auth/login" -ContentType "application/json" -Body $body | Out-Null
    "try $_ -> 200"
  } catch {
    $code = $_.Exception.Response.StatusCode.value__
    "try $_ -> $code"
  }
}
```

Expected:
- First 5 attempts: `200`
- Then: `429`

### 6) Safe logging proof (no token/password leak)

```powershell
cd C:\workspace\springboot-api
docker compose logs api | Select-String -Pattern "Authorization|Bearer|eyJhbGci|token|password|admin123"
```

Expected:
- No matches / empty output.

### 7) AI endpoint proof (PROD, protected + response)

```powershell
$base = "https://task-manager-api-njza.onrender.com"
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body $loginBody).token

Invoke-RestMethod -Method Post -Uri "$base/ai/suggest-priority" -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body '{"title":"Pagar aluguel","description":"Vence hoje"}'
```

Expected:
- Returns `priority` and `reason` (in demo mode, reason may be deterministic mock text).

### 8) AI endpoint proof (LOCAL, protected + response)

```powershell
$base = "http://localhost:8081"
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body $loginBody).token

Invoke-RestMethod -Method Post -Uri "$base/ai/suggest-priority" -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body '{"title":"Pagar aluguel","description":"Vence hoje"}'
```

Expected:
- Returns `priority` and `reason`.
- In demo mode (no key), the reason may be a deterministic mock message.

---

## Repositories

- Backend: https://github.com/GustavoMPrado/task-manager-api
- Frontend: https://github.com/GustavoMPrado/task-manager-frontend

---

## Contact

Gustavo Marinho Prado Alves  
GitHub: https://github.com/GustavoMPrado





