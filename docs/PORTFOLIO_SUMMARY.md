# Portfolio Summary — Task Manager (Full Stack V2)

## Project overview

This project is my main full-stack portfolio case and was built to demonstrate that I can design, implement, deploy, and evolve a real application with production-oriented practices.

It is a complete Task Manager system with:
- full CRUD
- pagination, filters, and search
- JWT authentication
- resilience improvements (rate limiting and pagination cap)
- observability (health checks)
- AI integration (priority suggestion endpoint)
- security documentation and controlled pentest evidence

The goal of this project is to show a strong job-ready combination of:
- backend development
- frontend development
- deployment
- applied security
- practical AI integration

---

## Tech stack

### Backend
- Java 21
- Spring Boot
- PostgreSQL
- Flyway
- Spring Boot Actuator
- Gradle
- Docker / Docker Compose

### Frontend
- React
- Vite
- TypeScript
- Tailwind CSS

### Production
- Backend deployed on Render
- Frontend deployed on GitHub Pages

---

## Live links

- Frontend (Production): https://gustavomprado.github.io/task-manager-frontend/
- Backend API (Production): https://task-manager-api-njza.onrender.com/
- Backend Health: https://task-manager-api-njza.onrender.com/actuator/health

---

## Key features implemented

### 1) Core product features
- Create, list, update, and delete tasks
- Pagination and sorting
- Search (`q`) and filters (`status`, `priority`)
- Improved UX with loading states and feedback messages

### 2) Authentication and route protection
- JWT authentication with `POST /auth/login`
- Protected endpoints (`/tasks/**` and `/ai/**`)
- Frontend session handling (logout on `401`)

### 3) Resilience and scaling safeguards
- Basic in-memory rate limit on login (`429` after repeated attempts)
- Pagination size cap to prevent abusive large queries
- Database evolution with Flyway migrations and indexing improvements

### 4) Observability
- Health endpoint with Spring Boot Actuator
- Production and local health checks validated

### 5) AI integration (demo-ready)
- Protected endpoint: `POST /ai/suggest-priority`
- Receives `title` and `description`
- Returns suggested `priority` and a `reason`
- Works in demo mode (deterministic fallback) when no OpenAI key is configured
- Designed to support real OpenAI integration server-to-server via environment variable

### 6) Security documentation and pentest evidence
- Security controls documented in `SECURITY.md`
- Controlled local pentest documented in `docs/PENTEST.md`
- Evidence files versioned in `docs/pentest-evidencias/`

The pentest was executed in a local authorized lab (Kali Linux + VirtualBox + host-only network), focusing on safe validation of:
- access control (`401` vs `200`)
- protected AI endpoint behavior
- request validation (`400`, not `500`)
- pagination cap enforcement
- basic recon visibility

---

## Security and pentest deliverables

- `SECURITY.md` — security posture and implemented protections
- `docs/PENTEST.md` — pentest scope, methodology, results, and conclusions
- `docs/pentest-evidencias/` — raw evidence files (Nmap and API request/response results)

This helps demonstrate practical cybersecurity awareness applied directly to my own software project in a controlled and ethical environment.

---

## Why this project is relevant for hiring

This project was designed as a portfolio case to demonstrate skills that are directly useful in junior backend/full-stack roles:

- building a real application end-to-end
- working with APIs and databases
- deploying to production
- handling authentication and route protection
- applying basic performance and abuse mitigations
- documenting technical decisions and evidence
- integrating AI features into a real product workflow

It also reflects my interest in combining software engineering with cybersecurity practices.

---

## Repositories

- Backend: https://github.com/GustavoMPrado/task-manager-api
- Frontend: https://github.com/GustavoMPrado/task-manager-frontend

---

## Author

Gustavo Marinho Prado Alves  
GitHub: https://github.com/GustavoMPrado