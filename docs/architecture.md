# Web Hotel MIS architecture

## Runtime boundary

```text
Public portal --------> public read controllers/DTO
Employee React app ---> JWT/RBAC application API <--- agent typed API tools
                              |       |        |
                            Auth   Use cases  Audit/approval
                                      |
                                      v
                              Database adapters -> SQL Server

Chat UI -> Agent orchestrator -> RAG index: approved customer-policy.md
```

The backend API is the only business-data boundary and the only SQL Server writer.
Anonymous public endpoints are read-only and return allow-listed public DTOs;
they never reuse employee responses or expose guest, reservation, invoice or
internal operational data.
The agent must never connect directly to SQL Server or generate production SQL.
Live room availability, booking, invoice, inventory and cash data are
structured business data and must be read or changed through allow-listed
backend APIs. Customer answers retrieve only the approved root
`customer-policy.md`; internal operating rules are not indexed for customers.

## Database ownership

[AGENTS.md](../AGENTS.md) owns the six-file Flyway baseline and database
rebuild rules. The [migration guide](../backend/src/main/resources/db/migration/README.md)
describes SQL object organization; the [access matrix](database-access-matrix.md)
maps runtime callers to those objects and their focused tests.

Hibernate is configured with `ddl-auto=validate`. It validates the schema
created by Flyway and is not permitted to create, update or otherwise mutate
database structure.

## Backend modules

The backend is a single Spring Boot modular monolith rooted at
`com.hospitality.mis`.

```text
com.hospitality.mis
├── middleware        JWT, actor identity and authorization
├── controller        REST controllers grouped by module
├── service           business services and transaction boundaries
├── dao               Bound JDBC view/function/procedure adapters
├── dto               request/response objects grouped by module
├── entity            JPA entities and business types grouped by module
├── common            shared API errors and exception handling
└── config            Spring and web configuration
```

- `identity`: authentication, roles and permissions.
- `room`: room types, rooms, availability and maintenance state.
- `guest`: guest profile, membership and booking restrictions.
- `reservation`: booking, check-in, check-out, room transfer and cancellation.
- `billing`: pricing policy, services, deposits, invoices and payments.
- `operations`: housekeeping, minibar/inventory, equipment and maintenance.
- `finance`: cash handover, receipts, expenses and supplier debts (some current
  API paths retain the historical `partner-debts` name).
- `governance`: audit log, approval workflow and reporting.

Controllers depend on services. Services coordinate database adapters and projections;
controllers do not access the database directly. DTOs are used at the HTTP
boundary; entities remain for Hibernate schema validation, not runtime writes. Cross-module workflows are
coordinated in services, not by controllers or direct database access.

OTA reconciliation and VAT belong to `FinanceBackofficeService` and
`FinanceBackofficeDatabase` in `finance`; attendance and leave belong to
`AttendanceLeaveService` and `AttendanceLeaveDatabase` in `identity`; technical
assets belong to `TechnicalAssetService` and `TechnicalAssetDatabase` in
`operations`. Their request/response DTOs live in the corresponding domain
packages. JDBC row mapping belongs in the database adapters.

Frontend screens live under `src/features/`. Components within a feature share
view-model types through that feature's `types.ts`, without importing types from
their parent screen. API calls belong to the specific shared domain API module.

Business transitions, approvals and actor permissions are governed by
[rule.md](../rule.md). Customer-facing terms belong to
[customer-policy.md](../customer-policy.md), and transport behavior belongs to
the [API contract](api-contract.md). Local/demo configuration exceptions are
owned by AGENTS.md.
