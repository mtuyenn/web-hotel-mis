# Delivery plan — hard cut B

Hard cut B treats the web system as the sole production application boundary.
There is no dual-writer phase, compatibility baseline or incremental handover
plan. The backend API owns all business writes from the first deploy.

## Schema and persistence contract

- `backend/src/main/resources/db/migration/V1__baseline_schema.sql` is the
  canonical schema V1.
- Flyway applies the complete ordered migration line to an empty MySQL
  database. Every new schema change is a new immutable versioned migration.
- Hibernate uses `ddl-auto=validate`; schema creation and changes belong to
  Flyway only.
- The backend is the only DB writer. Frontend, agent and integrations use
  authenticated backend use cases, transactions and audit controls.

## Delivery order

1. Keep the modular-monolith boundaries under `com.hospitality.mis` and expose
   typed API contracts for identity, rooms, guests, reservations, billing,
   operations, finance and governance.
2. Complete authorization, approval, idempotency, concurrency protection and
   audit behavior at the application-service boundary.
3. Build the operations UI against the backend API.
4. Add agent/RAG capabilities in this order: cited SOP/policy answers,
   read-only live-data tools, confirmed action proposals, and authorized action
   execution.
5. Add operational monitoring, backup/restore exercises and incident
   procedures for the canonical schema.

## Definition of done

- A clean MySQL instance migrates successfully from V1 through the current
  Flyway version.
- Hibernate validation passes against that migrated schema.
- The full backend test suite runs with the real-MySQL migration test enabled;
  the migration test is not skipped in CI.
- Every production write has an authenticated actor, authorization,
  transaction boundary and audit behavior appropriate to the use case.
- No production client requires direct MySQL credentials.
