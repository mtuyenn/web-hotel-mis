# web-hotel-mis

Hotel MIS web application: a Spring Boot API, a React/TypeScript frontend and
an agent integration boundary. This repository is the canonical source for
the web system.

## Repository layout

```text
backend/              Spring Boot modular monolith and database migrations
frontend/             React/TypeScript operations UI
agent/                Chat orchestration, RAG and typed API tools
knowledge/sop/        Versioned SOP and policy documents for RAG
docs/                 Architecture, decisions and delivery guidance
infra/                Local infrastructure notes
scripts/              Developer and CI helper scripts
docker-compose.yml    Local MySQL 8.4 service
```

## Runtime boundaries

1. The backend API is the only business-data boundary for the frontend and
   agent.
2. The backend is the only component allowed to write the MySQL database.
   Frontend, agent and operational tools use authenticated API use cases.
3. Agents never connect to MySQL or generate production SQL. Structured live
   data is read through typed API tools; RAG is reserved for versioned
   documents and policies.
4. All database changes are Flyway migrations. Hibernate runs with
   `ddl-auto=validate` and never changes the schema.

## Canonical schema

The schema contract starts at
`backend/src/main/resources/db/migration/V1__baseline_schema.sql`. This
Flyway line is the canonical schema V1 for the web system; later versioned
migrations extend that same contract. A clean database must apply the complete
ordered migration line before the application starts.

## Local development

```powershell
docker compose up -d mysql
cd backend
mvn spring-boot:run
```

Set `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` when using a database other than
the local Compose service. Do not commit credentials or `.env` files.

## Verification

Backend CI provisions a clean MySQL 8.4 instance, runs the full Maven test
suite with the real-MySQL migration test enabled, applies Flyway, and verifies
Hibernate mappings with `ddl-auto=validate`. Frontend CI runs the production
build.

See [docs/architecture.md](docs/architecture.md),
[docs/file-structure.md](docs/file-structure.md) and
[docs/migration-plan.md](docs/migration-plan.md).
