# Task Manager API (V2) — JWT + Scaling + Observability

A production-ready RESTful **Task Manager API** built with **Java 21** and **Spring Boot**.

This project is part of a full-stack portfolio (React + Vite + TypeScript + Tailwind) and demonstrates:
- Clean architecture and validation
- Database migrations (Flyway)
- Observability (Actuator)
- Security (JWT auth + CORS + basic rate limiting on login)
- Practical scaling/resilience improvements (pagination cap, indexes)
- Safe logging (standardized logs without leaking tokens/passwords)

---

## Production (Render)

- Base URL: https://task-manager-api-njza.onrender.com  
- Health (Actuator): https://task-manager-api-njza.onrender.com/actuator/health  
- Root status: https://task-manager-api-njza.onrender.com/  
  - Returns: `{"status":"ok","service":"task-manager-api"}`

**Note:** Render Free may have a cold start (~50s) on the first request.

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

### Health (Actuator)
- `GET /actuator/health` — should return `UP`

---

## Security Notes (V2)

### JWT protection
- `/tasks/**` requires `Authorization: Bearer <token>`
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

---

## Repositories

- Backend: https://github.com/GustavoMPrado/task-manager-api
- Frontend: https://github.com/GustavoMPrado/task-manager-frontend

---

## Contact

Gustavo Marinho Prado Alves  
GitHub: https://github.com/GustavoMPrado




