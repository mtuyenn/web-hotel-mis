# Web Hotel MIS architecture

## Target boundary

```text
React/TypeScript
        |
        v
Spring Boot REST API  <--- agent typed API tools
  |       |        |
 Auth   Use cases  Audit/approval
        |
        v
Repositories -> MySQL

Chat UI -> Agent orchestrator
                       |
                       +-> RAG index: SOP, policy, forms, manuals
```

The backend API is the only business-data boundary and the only MySQL writer.
The agent must never connect directly to MySQL or generate production SQL.
Live room availability, booking, invoice, inventory and cash data are
structured business data and must be read or changed through authenticated API
tools. RAG is reserved for versioned documents and policies, with source
citations and access control.

## Canonical schema V1

`backend/src/main/resources/db/migration/V1__baseline_schema.sql` is the
authoritative starting schema for this system. The ordered Flyway migrations
from V1 onward form one canonical schema contract. A new environment starts
from an empty MySQL database and applies every migration in order; no external
SQL baseline is part of the application contract.

Hibernate is configured with `ddl-auto=validate`. It validates the schema
created by Flyway and is not permitted to create, update or otherwise mutate
database structure.

## Backend modules

The backend is a single Spring Boot modular monolith rooted at
`com.hospitality.mis`.

```text
com.hospitality.mis
├── <module>
│   ├── api           HTTP controllers and request/response DTOs
│   ├── application   use cases, transactions and ports
│   ├── domain        entities, value objects, enums and policies
│   └── adapter       JPA repositories and infrastructure implementations
├── auth              token and authentication use cases
├── common            shared API errors and exception handling
├── config            Spring and web configuration
└── security          JWT conversion, actor identity and authorization helpers
```

- `identity`: authentication, roles and permissions.
- `room`: room types, rooms, availability and maintenance state.
- `guest`: guest profile, membership and booking restrictions.
- `reservation`: booking, check-in, check-out, room transfer and cancellation.
- `billing`: pricing policy, services, deposits, invoices and payments.
- `operations`: housekeeping, minibar/inventory, equipment and maintenance.
- `finance`: cash handover, receipts, expenses and partner debts.
- `governance`: audit log, approval workflow and reporting.

Controllers depend on application services. Application services coordinate
domain objects and repository/port interfaces; adapters implement persistence
concerns. Domain objects may reference domain concepts in another module when
the business aggregate requires it, but HTTP and JPA concerns stay outside the
domain layer. Cross-module workflows are coordinated in application services,
not by controllers or direct database access.

Start as a modular monolith. Split services only after a measured operational
need exists.

## Non-negotiable safety rules

- All writes use application use cases with transaction boundaries.
- Sensitive actions (invoice deletion, deposit refund, price override) require
  RBAC, explicit confirmation, approver identity and audit log.
- Tools are allow-listed, typed and idempotent.
- The model may propose an action; the backend decides whether it is valid and
  authorized.
- Secrets come from environment/secret management, never source control.
