# Security Policy & Evidence — Task Manager API

This document summarizes the security measures implemented in the Task Manager API (V2) and provides reproducible evidence commands (PowerShell) to validate them.

Scope: lightweight, safe checks. No heavy load testing in production.

## Production endpoints (Render)

- Base URL: https://task-manager-api-njza.onrender.com
- Health: https://task-manager-api-njza.onrender.com/actuator/health
- Root status: https://task-manager-api-njza.onrender.com/

Note: Render Free can cold start on the first request.

## Implemented security measures (V2)

### Authentication & Authorization (JWT)
- `POST /auth/login` returns a JWT token (`{ "token": "..." }`).
- Protected endpoints (e.g., `/tasks/**`, `/ai/**`) require:
  - `Authorization: Bearer <token>`
- Expected behavior:
  - Without token: `401 Unauthorized`
  - With valid token: `200 OK`

### CORS (Frontend integration)
- CORS is configured to allow the GitHub Pages frontend.
- Preflight (`OPTIONS`) is allowed, preventing infinite loading on the browser.

### Rate limiting (login)
- `/auth/login` is rate limited in-memory per IP:
  - 5 attempts per minute
  - Exceeding limit returns `429 Too Many Requests`

### Pagination safeguards
- Page size is capped to prevent abuse:
  - Example: `size=999` is limited to `size=50`

### Safe logging (no secrets)
- Logging is standardized and reviewed to avoid leaking secrets.
- No `Authorization`, `Bearer`, `token`, or `password` values should appear in logs.

### Observability
- Actuator health endpoint enabled:
  - `/actuator/health` should report `UP`

---

## Evidence (reproducible commands)

All commands below are intended to be run on Windows PowerShell.

### 1) Production health is UP

```powershell
$base = "https://task-manager-api-njza.onrender.com"
Invoke-RestMethod "$base/actuator/health"
```

Expected:
- JSON containing `status` = `UP`

### 2) Production root status

```powershell
$base = "https://task-manager-api-njza.onrender.com"
Invoke-RestMethod "$base/"
```

Expected:
- `{"status":"ok","service":"task-manager-api"}`

### 3) Login returns a token (Production)

```powershell
$base = "https://task-manager-api-njza.onrender.com"
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body $loginBody
```

Expected:
- JSON with a `token` field

### 4) Protected endpoint without token returns 401 (Production)

```powershell
$base = "https://task-manager-api-njza.onrender.com"
Invoke-WebRequest -Uri "$base/tasks?page=0&size=1" -Method Get -SkipHttpErrorCheck | Select-Object StatusCode
```

Expected:
- `StatusCode` = `401`

### 5) Protected endpoint with token returns 200 (Production)

```powershell
$base = "https://task-manager-api-njza.onrender.com"
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body $loginBody).token

Invoke-WebRequest -Uri "$base/tasks?page=0&size=1" -Method Get -Headers @{ Authorization = "Bearer $token" } -SkipHttpErrorCheck | Select-Object StatusCode
```

Expected:
- `StatusCode` = `200`

### 6) AI endpoint without token returns 401 (Production)

```powershell
$base = "https://task-manager-api-njza.onrender.com"
Invoke-WebRequest -Uri "$base/ai/suggest-priority" -Method Post -ContentType "application/json" -Body '{"title":"Pay rent","description":"Due today"}' -SkipHttpErrorCheck | Select-Object StatusCode
```

Expected:
- `StatusCode` = `401`

### 7) AI endpoint with token returns 200 (Production)

```powershell
$base = "https://task-manager-api-njza.onrender.com"
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body $loginBody).token

Invoke-WebRequest -Uri "$base/ai/suggest-priority" -Method Post -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body '{"title":"Pay rent","description":"Due today"}' -SkipHttpErrorCheck | Select-Object StatusCode
```

Expected:
- `StatusCode` = `200`

### 8) Rate limit evidence on /auth/login (Production)

Run the command below 6 times quickly (same minute):

```powershell
$base = "https://task-manager-api-njza.onrender.com"
$loginBody = @{ username = "admin"; password = "wrong" } | ConvertTo-Json
Invoke-WebRequest -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body $loginBody -SkipHttpErrorCheck | Select-Object StatusCode
```

Expected:
- First attempts: `401`
- After threshold: `429`

### 9) Pagination cap evidence (Local)

Start local environment first:
- API local: `http://localhost:8081`

```powershell
$local = "http://localhost:8081"
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri "$local/auth/login" -ContentType "application/json" -Body $loginBody).token

Invoke-RestMethod -Method Get -Uri "$local/tasks?page=0&size=999" -Headers @{ Authorization = "Bearer $token" }
```

Expected:
- Response indicates effective `size` is limited (e.g., `size` becomes `50`)

### 10) Logs do not leak secrets (Local)

After running some requests locally, validate logs (adjust path if needed):

```powershell
cd C:\workspace\springboot-api
Get-ChildItem -Recurse -File | Where-Object { $_.Name -match "log" }
```

If you log to a specific file (example `logs/app.log`), search for sensitive tokens:

```powershell
Select-String -Path .\logs\*.log -Pattern "Authorization|Bearer|token|password" -SimpleMatch
```

Expected:
- No matches (or only safe static words without actual secrets)

---

## Notes
- Production testing is lightweight to avoid impacting the Render Free service.
- For deeper testing, use the controlled local environment (Docker Compose) and the planned D2 Pentest (Kali, light recon + validation).
