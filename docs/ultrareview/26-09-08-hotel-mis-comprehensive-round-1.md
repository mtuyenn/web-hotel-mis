# Ultra Review: hotel-mis-comprehensive Round 1

Date: 26-09-08
Review name: hotel-mis-comprehensive
Round: 1
Scope: Audit C:\web-hotel-mis comprehensively for architecture, API/HTTP contracts, security/authorization, persistence/migrations, business logic, frontend/backend integration, tests/proof, and operational/configuration risks. The working tree contains a large package/layout refactor plus new hotel MIS capabilities; inspect the full relevant production surface, including tracked modifications and untracked files. Do not modify, stage, format, generate, build, install, or run tests. Report every incidental in-scope candidate, including speculative ones, with exact file:line evidence, failure mode, confidence, durable fix hypothesis, and a disconfirming check.
Report path: docs/ultrareview/26-09-08-hotel-mis-comprehensive-round-1.md

## Prior Round Guard

Previous reports read:
- none

## Findings

<!--
Add findings below. Every candidate reported by scouts must be captured here cleanly.
If there are no candidates, write: No candidates reported.
-->

### F001 [P1] Payment/receipt writes do not settle or validate the invoice

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/service/billing/PaymentTransactionService.java:18-25; backend/src/main/java/com/hospitality/mis/service/billing/ReceiptService.java:16-22
Evidence:
- A payment transaction is saved with a positive amount and arbitrary PAYMENT or REFUND type, but no invoice lock, balance check, invoice status/amount update, idempotency, or reference uniqueness is applied.
- Receipt issuance likewise accepts any positive amount and only checks receipt-number existence; it does not verify the invoice state or reconcile the invoice.
Contract violated:
- A financial transaction/receipt must be an atomic, auditable application to one invoice and must not permit overpayment, arbitrary refunds, or duplicate replay.
Plausible failure mode:
- Replaying a request or submitting a REFUND larger than the paid amount creates ledger rows while the invoice remains unchanged; reports and checkout state disagree with cash movement.
Durable solution hypothesis:
- Lock the invoice, derive and validate the new settled balance, enforce transaction-type rules and idempotency/reference constraints, then update invoice state and append an audit event in one transaction.
Disconfirming check:
- Trace all readers of payment_transactions/receipts and verify an independent reconciliation job applies these rows; no such consumer is present in the repository.

### F002 [P1] Customer registration creates accounts that cannot authenticate

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/controller/auth/CustomerAccountController.java:9-17; backend/src/main/java/com/hospitality/mis/middleware/security/EmployeeUserDetailsService.java:34-108; backend/src/main/java/com/hospitality/mis/config/SecurityConfig.java:97-103
Evidence:
- Registration is public and persists CustomerAccount, but the only UserDetailsService loads Employee records and the public auth routes expose only employee login/refresh.
- Customer accounts have no login/token issuance path and are not accepted by the JWT converter/claims flow.
Contract violated:
- A published customer registration endpoint must produce a usable authenticated customer identity, or be explicitly an administrative-only record-creation API.
Plausible failure mode:
- Customers register successfully (201) and receive a response, then cannot log in or call any customer-scoped API; the account is dead data and phone uniqueness can block future recovery.
Durable solution hypothesis:
- Either remove/publicly deprecate registration until customer auth exists, or implement a distinct customer authentication provider/token subject and authorize customer-owned operations explicitly.
Disconfirming check:
- Search all controllers/services for CustomerAccount authentication and customer token issuance; only the registration service and entity are present.

### F003 [P1] Cash handover trusts caller-supplied actors

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/controller/finance/FinanceController.java:15; backend/src/main/java/com/hospitality/mis/service/finance/FinanceService.java:21-25; backend/src/main/java/com/hospitality/mis/dto/finance/FinanceDtos.java:16-18
Evidence:
- The handover request accepts both from_actor and to_actor, and the service copies them directly without comparing from_actor to Authentication or checking either employee exists/is active.
- The endpoint is available to any MANAGER, ACCOUNTING, or DIRECTOR.
Contract violated:
- Cash custody/audit records must bind the acting principal to the operation and validate custody participants.
Plausible failure mode:
- An authorized user can forge a handover attributed to another employee, including arbitrary expected/actual amounts, corrupting accountability and variance reporting.
Durable solution hypothesis:
- Derive the source actor from SecurityActor, validate the target employee and shift ownership, and require a controlled handover state transition with audit evidence.
Disconfirming check:
- Look for a downstream reconciliation that replaces from_actor/to_actor from identity data; repository search finds none.

### F004 [P1] Reservation object scope is enforced inconsistently and leaks operational data

Severity: P1 | Confidence: medium
Source pointer: backend/src/main/java/com/hospitality/mis/controller/reservation/ReservationController.java:77-95; backend/src/main/java/com/hospitality/mis/controller/operations/MaintenanceController.java:17; backend/src/main/java/com/hospitality/mis/controller/room/RoomEquipmentController.java:15-16
Evidence:
- Reservation GET has an owner/global-role check, but maintenance-by-room and room-equipment list are only protected by the global authenticated rule, with no role annotation on the former and broad role access on the latter.
- Equipment/maintenance responses expose room operational details to every authenticated identity that reaches those routes, and no service-level scope check exists.
Contract violated:
- Sensitive operational data must have an explicit least-privilege policy at the service boundary, not rely on the default authenticated rule.
Plausible failure mode:
- A newly added role or non-employee JWT can enumerate maintenance/equipment state, and future non-HTTP callers bypass the intended policy entirely.
Durable solution hypothesis:
- Add explicit read permissions/roles and enforce them at service/use-case boundaries; add object-scope checks where data is reservation- or room-sensitive.
Disconfirming check:
- Confirm that all deployed principals are exactly the listed employee roles and that these records are intentionally public to all authenticated roles; current code/docs do not establish that contract.

### F005 [P1] Checkout can mark an invoice paid without recording a payment

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/service/billing/BillingService.java:42-67
Evidence:
- checkOut computes due, sets invoice status to DA_THANH_TOAN, and updates reservation/guest state, but never creates a PaymentTransaction or Receipt and does not use the supplied payment method to record a tender.
- Separate payment endpoints merely append unrelated transaction rows (F001).
Contract violated:
- A paid invoice requires a corresponding settlement record and a single atomic state transition.
Plausible failure mode:
- Cash/card collection cannot be reconciled to invoices; a failed downstream payment capture cannot roll back the already checked-out, paid reservation.
Durable solution hypothesis:
- Make payment capture/recording the source of truth, lock invoice and reservation together, create the transaction/receipt as appropriate, and only then transition to paid/checked-out.
Disconfirming check:
- Search for an event listener or database trigger that creates payment rows from invoice status; none is present.

### F006 [P1] Inventory movement and service usage can diverge

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/service/reservation/ReservationService.java:92; backend/src/main/java/com/hospitality/mis/service/operations/InventoryMovementService.java:16-23
Evidence:
- addService decrements stock and increments service usage, while inventory movement record separately adjusts stock and appends a movement; there is no shared ledger/idempotency relation.
- Both paths can issue stock for the same business event, and addService has no movement row.
Contract violated:
- Inventory on-hand must be derivable from one authoritative ledger or every stock mutation must emit exactly one movement.
Plausible failure mode:
- Staff record an issue and later add the service, double-decrementing stock; audit history cannot explain the resulting quantity.
Durable solution hypothesis:
- Centralize stock mutation in one service that atomically writes the movement and usage/outbox record, with an idempotency key per business event.
Disconfirming check:
- Reconcile services.stock_quantity against inventory_movements plus service_usages for a sample dataset; current schema/code provide no reconciliation mechanism.

### F007 [P1] Reservation availability protection is not safe across application instances

Severity: P1 | Confidence: medium
Source pointer: backend/src/main/java/com/hospitality/mis/service/reservation/ReservationService.java:47-78
Evidence:
- create is synchronized, which serializes only calls within one JVM; the application has no distributed lock or database exclusion constraint for overlapping reservation intervals.
- Room row locks are acquired, but the overlap check occurs before insert and reservation_rooms has only a normal time index.
Contract violated:
- Concurrent requests on a horizontally scaled service must not create overlapping active room bookings.
Plausible failure mode:
- Two instances lock/check the same room and both pass the overlap query before either inserts, resulting in overbooking.
Durable solution hypothesis:
- Use a database-enforced interval strategy/serializable transaction with a durable lock protocol, or serialize per-room reservation commands through a shared mechanism; retain idempotency.
Disconfirming check:
- Inspect deployment topology and prove the service is permanently single-instance with no future scaling requirement; docker configuration alone does not establish this invariant.

### F008 [P2] Payment and adjustment endpoints expose insufficient business invariants

Severity: P2 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/controller/billing/InvoiceController.java:35-43; backend/src/main/java/com/hospitality/mis/service/billing/BillingService.java:70-71
Evidence:
- Adjustment accepts any nonzero BigDecimal (including negative) and changes only amount_due; it does not update the component totals, payment rows, or status.
- refundDeposit changes deposit_paid and amount_due without checking invoice status, prior refund state, or a payment transaction.
Contract violated:
- Invoice totals and settlement state must remain internally consistent across adjustments/refunds.
Plausible failure mode:
- Repeated approved refund calls inflate amount_due; reports show component totals that no longer sum to the due amount, and a paid invoice can be turned into an unpaid one without a refund ledger.
Durable solution hypothesis:
- Model adjustments/refunds as immutable signed ledger entries with one-use approval consumption and recompute invoice balances under lock.
Disconfirming check:
- Verify approvals are consumed/linked to exactly one mutation and that reports intentionally use amount_due alone; ApprovalService only checks for any prior approved request.

### F009 [P2] Guest search returns all PII when query is blank

Severity: P2 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/service/guest/GuestService.java:89-105; backend/src/main/java/com/hospitality/mis/controller/guest/GuestController.java:57-60
Evidence:
- An omitted or blank q calls findAllShared and maps identity number, phone, email, address, spending, and booking flags into every response.
- There is no pagination or result cap.
Contract violated:
- Guest directory APIs should minimize PII exposure and bound response size.
Plausible failure mode:
- Any permitted front-desk user can bulk-export the entire guest database with one request, causing privacy and performance risk.
Durable solution hypothesis:
- Require a meaningful search or paginate/cap results, return a redacted list projection, and expose full PII only through narrowly scoped detail access with audit logging.
Disconfirming check:
- Confirm a gateway enforces pagination/redaction globally; no such control is represented in this repository.

### F010 [P2] Frontend is not an executable integration surface

Severity: P2 | Confidence: high
Source pointer: frontend/src/shared/api/http.ts:3-12; frontend/src/app/routes.tsx:1-8; frontend/src/features/*/README.md
Evidence:
- The client exposes only a GET helper with no Authorization/token handling, mutation helper, error decoding, or retry/cancellation behavior.
- routes.tsx is a string registry, while feature directories are README-only placeholders; no actual screens or route guards consume the backend API.
Contract violated:
- The repository’s frontend/backend product surface must implement the documented authenticated workflows, not only enumerate route names.
Plausible failure mode:
- The deployed UI cannot perform login, reservations, billing, or protected calls; once a route is added, it will also silently omit bearer credentials and surface generic errors.
Durable solution hypothesis:
- Implement auth lifecycle and typed request/mutation/error handling first, then wire real route components and contract tests against the documented API.
Disconfirming check:
- Inventory all frontend source files and verify there are no additional generated/remote UI packages; current tree contains only the listed skeleton.

### F011 [P2] Public API documentation and implementation are already out of sync

Severity: P2 | Confidence: medium
Source pointer: docs/api-contract.md:1-200; backend/src/main/java/com/hospitality/mis/controller: all controllers
Evidence:
- The repository added docs/api-contract.md alongside many new controllers, but the docs describe a contract surface that is not generated from annotations and the frontend has no consumer implementation.
- The new customer/payment/receipt/finance/operations routes have no corresponding frontend types or calls.
Contract violated:
- API documentation must be an executable or verified contract for the shipped surface.
Plausible failure mode:
- Clients integrate paths/fields/statuses that drift from controllers and DTO naming, while repository tests continue to cover only older domain slices.
Durable solution hypothesis:
- Generate OpenAPI from the running application and add contract tests that compare documented paths/schemas to controllers and client types in CI.
Disconfirming check:
- Compare docs/api-contract.md line-by-line with generated OpenAPI after a clean context starts; no generated spec or CI contract check is present now.

### F012 [P2] Authentication error handling is incomplete for framework failures

Severity: P2 | Confidence: medium
Source pointer: backend/src/main/java/com/hospitality/mis/config/SecurityConfig.java:121-147; backend/src/main/java/com/hospitality/mis/common/exception/GlobalExceptionHandler.java:27-69
Evidence:
- Security entry/access handlers write a minimal JSON object manually, while GlobalExceptionHandler handles only domain, validation, and data-integrity exceptions.
- JWT decode failures and generic framework authentication errors therefore bypass the documented ApiError shape (timestamp/status/code/message/details).
Contract violated:
- All API errors should use one stable error schema and should not leak framework-dependent response differences.
Plausible failure mode:
- Expired/malformed tokens return a different body than application authorization failures, breaking clients and observability; content may also be committed without cache/security headers.
Durable solution hypothesis:
- Centralize authentication/access-denied serialization through the same error model and add tests for missing, malformed, expired, and insufficient-role tokens.
Disconfirming check:
- Inspect an integration test asserting ApiError for resource-server decoder failures; current test names cover security generally but no evidence in the production handlers proves it.

## Verification Queue

- F001: Trace invoice/payment/receipt reconciliation and verify atomic balance updates.
- F002: Search for customer authentication provider, customer login route, and customer JWT issuance.
- F003: Verify handover actor binding and employee validation at every caller.
- F004: Enumerate all authenticated roles and intended room/maintenance read policy; test unauthorized role access.
- F005: Trace payment-row creation, capture failure, and checkout transaction boundaries.
- F006: Reconcile stock quantity with movement and usage ledgers for duplicate issue scenarios.
- F007: Validate behavior with two application instances/concurrent transactions against MySQL.
- F008: Check approval consumption, refund idempotency, and invoice component-total invariants.
- F009: Confirm gateway-level redaction/pagination and audit behavior for blank guest search.
- F010: Inventory frontend runtime files and exercise bearer-authenticated mutation flows.
- F011: Generate/inspect OpenAPI and compare it to docs/api-contract.md and client types.
- F012: Exercise malformed/expired JWT and access-denied responses against ApiError schema.

## Strongest Reason Not To Merge Yet

The strongest reason not to merge is the financial state model: checkout marks invoices paid without a payment ledger, while separate payment/receipt endpoints can append unbounded, unreconciled rows. This can produce irreversible divergence between operational state, cash movement, and audit records even when each individual HTTP request returns success. Authorization and customer-auth gaps add material exposure around that core inconsistency.

## Next Receive Prompt

Use $ultra-review-receive to verify docs/ultrareview/26-09-08-hotel-mis-comprehensive-round-1.md and implement confirmed owner-clean fixes.
