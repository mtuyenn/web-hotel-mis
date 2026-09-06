# Web Hotel MIS architecture

## Target boundary

```text
React/TypeScript
        |
        v
Spring Boot REST API
  |       |        |
 Auth   Use cases  Audit/approval
        |
        v
Repositories -> MySQL

Chat UI -> Agent orchestrator -> API tools
                       |
                       +-> RAG index: SOP, policy, forms, manuals
```

The agent must never connect directly to MySQL. Live room availability, booking, invoice, inventory and cash data are structured business data and must be read through authenticated API tools. RAG is reserved for versioned documents and policies, with source citations and access control.

## Backend modules

- `identity`: authentication, roles and permissions.
- `room`: room types, rooms, availability and maintenance state.
- `guest`: guest profile, membership and booking restrictions.
- `reservation`: booking, check-in, check-out, room transfer and cancellation.
- `billing`: pricing policy, services, deposits, invoices and payments.
- `operations`: housekeeping, minibar/inventory, equipment and maintenance.
- `finance`: cash handover, receipts, expenses and partner debts.
- `governance`: audit log, approval workflow and reporting.

Start as a modular monolith. Split services only after a measured operational need exists.

## Non-negotiable safety rules

- All writes use application use cases with transaction boundaries.
- Sensitive actions (invoice deletion, deposit refund, price override) require RBAC, explicit confirmation, approver identity and audit log.
- Tools are allow-listed, typed and idempotent.
- The model may propose an action; the backend decides whether it is valid and authorized.
- Secrets come from environment/secret management, never source control.
