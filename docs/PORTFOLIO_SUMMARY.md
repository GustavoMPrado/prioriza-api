# Portfolio Summary — Task Manager (Full Stack V2)

## Project overview

This is my main portfolio project, and I built it to practice and demonstrate a complete real-world workflow: backend, frontend, deployment, security improvements, and documentation.

It is a full-stack Task Manager system that I kept evolving in versions (V1 to V2) while studying and improving the project in practice.

In this project, I worked on:
- full CRUD
- pagination, filters, and search
- JWT authentication
- resilience improvements (rate limiting and pagination cap)
- observability (health checks)
- AI integration (priority suggestion endpoint)
- security documentation and controlled pentest evidence

My goal with this project is to show that I can build, deploy, secure, and explain a real application.
One practical decision I made in production was keeping the API on Render but moving the PostgreSQL database to Neon, so I could avoid Render Free Postgres expiration without changing the public API URL.
---

## Tech stack
I used the following stack in this project:

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
- Production PostgreSQL migrated to Neon (API URL unchanged)

---

## Live links

- Frontend (Production): https://gustavomprado.github.io/task-manager-frontend/
- Backend API (Production): https://task-manager-api-njza.onrender.com/
- Backend Health: https://task-manager-api-njza.onrender.com/actuator/health
 **Note (Render free tier): after some idle time, the first request may take longer because of cold start.**
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
- Prepared for real OpenAI integration server-to-server via environment variables

### 6) Security documentation and pentest evidence
- Security controls documented in `SECURITY.md`
- Controlled local pentest documented in `docs/PENTEST.md`
- Evidence files versioned in `docs/pentest-evidencias/`

I executed the pentest in a local authorized lab (Kali Linux + VirtualBox + host-only network), focusing on safe validation of:
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

This part of the project helps me show practical cybersecurity awareness applied directly to my own software in a controlled and ethical environment.

---

## Why this project is relevant for hiring

I built this project as a portfolio case to show the kind of work I want to do in junior backend/full-stack roles.

With this project, I can show that I have practical experience with:
- building a real application end-to-end
- working with APIs and databases
- deploying and validating a production version
- handling authentication and protected routes
- applying basic performance and abuse mitigations
- documenting technical decisions and test evidence
- integrating an AI feature into a real product flow

This project also reflects my interest in combining software engineering with cybersecurity practice.

---

## Repositories

- Backend: https://github.com/GustavoMPrado/task-manager-api
- Frontend: https://github.com/GustavoMPrado/task-manager-frontend

---

## Author

Gustavo Marinho Prado Alves  
GitHub: https://github.com/GustavoMPrado
Email: gmarinhoprado@gmail.com