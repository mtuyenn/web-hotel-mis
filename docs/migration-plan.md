# Migration plan

## Phase 0 — baseline

- Freeze the current desktop behavior with characterization tests.
- Decide ambiguous policies: hourly minimum, late checkout grace period, cancellation threshold and membership discount thresholds.
- Baseline the legacy schema in `database/legacy/`; do not use Hibernate `ddl-auto=update` in production.

## Phase 1 — backend foundation

- Add Flyway migrations and environment-based DB configuration.
- Extract domain/application services from Swing controllers.
- Introduce repository interfaces, dependency injection and use-case transactions.
- Add password hashing, RBAC and audit primitives.

The desktop repository remains unchanged and runnable.

## Phase 2 — read-only web

Expose authenticated read APIs for rooms, availability, guests, reservations, services, invoices and current reports. Build the first web screens against these APIs while desktop remains the operational write client.

## Phase 3 — incremental writes

Move writes in this order: guest -> rooms -> reservations -> check-in/out -> services -> billing. Use feature flags so desktop can switch one use case at a time to the API. Use idempotency, concurrency protection against overbooking, contract tests and rollback per use case.

## Phase 4 — missing business areas

Add housekeeping, equipment compensation, minibar/inventory, shifts/cash handover, receipts/expenses, OTA/supplier debt, approvals and detailed audit reporting.

## Phase 5 — agent/RAG

1. SOP/policy question answering with citations.
2. Read-only live-data tools through the API.
3. Action proposal and user confirmation.
4. Approved action execution with RBAC and audit.

## Exit criteria for desktop retirement

- All required use cases have API contract tests and operational monitoring.
- Web and desktop produce equivalent totals for a defined regression dataset.
- No production client requires direct DB credentials.
- Backup/restore, audit, approval and rollback procedures are tested.
