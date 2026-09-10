# Ultra Review: hotel-mis-security-business Round 1

Date: 26-09-08
Review name: hotel-mis-security-business
Round: 1
Scope: Audit security and business rules in C:\web-hotel-mis, especially JWT/authentication, RBAC, actor binding/ownership, customer accounts, phone login, and every rule in rule.md. Read-only.
Report path: docs/ultrareview/26-09-08-hotel-mis-security-business-round-1.md

## Prior Round Guard

Previous reports read:
- none

## Findings

<!--
Add findings below. Every candidate reported by scouts must be captured here cleanly.
If there are no candidates, write: No candidates reported.
-->

### F001 [P1] Customer authentication is unreachable/incomplete

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/config/SecurityConfig.java:81-103; backend/src/main/java/com/hospitality/mis/controller/auth/CustomerAccountController.java:14-17; docs/api-contract.md:111
Evidence:
- The global security chain permits only employee login and refresh; `/api/auth/customers/register` is not listed and therefore requires an employee JWT.
- The customer controller exposes registration only. There is no customer phone-login/token endpoint, while the API contract explicitly says phone login for both account types still needs implementation.
Contract violated:
- Customer accounts and phone login must be usable as specified; registration must be public if it is an account bootstrap flow.
Plausible failure mode:
- A new customer cannot register without an existing employee token, and no customer can authenticate or access customer-owned flows.
Durable solution hypothesis:
- Add an explicitly public registration route, implement a separate customer authentication principal/token flow, and enforce customer ownership on every customer endpoint; add negative tests proving employee tokens cannot impersonate customers.
Disconfirming check:
- Confirm an actual customer login route exists in another controller/configuration and that an unauthenticated registration request is permitted.

### F002 [P1] Customer phone identity is not normalized atomically

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/service/auth/CustomerAccountService.java:24-35; backend/src/main/java/com/hospitality/mis/dto/auth/CustomerAccountDtos.java:12-13
Evidence:
- Existence is checked with the raw request phone, but storage uses `request.phone().trim()`; there is no canonical phone format/length validation and no database-level normalized identity.
- The customer password has no DTO constraint and only a service length check; phone is only `@NotBlank`.
Contract violated:
- Phone login requires one canonical identity and safe account lifecycle behavior; uniqueness checks must match stored identity.
Plausible failure mode:
- Variants such as leading/trailing spaces or equivalent phone formats can create duplicate logical accounts or make login lookup inconsistent; malformed numbers are accepted.
Durable solution hypothesis:
- Centralize phone normalization/validation, apply it before lookup and persistence, persist a unique canonical phone column, and add rate-limited authentication/recovery behavior.
Disconfirming check:
- Attempt registration with the same logical phone in differently spaced/formatted variants and inspect whether the DB rejects the second account.

### F003 [P1] Reservation deposit and stay duration are client-controlled

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/service/reservation/ReservationService.java:48-70; backend/src/main/java/com/hospitality/mis/dto/reservation/ReservationDtos.java:39-45
Evidence:
- `deposit` is accepted as any non-negative client value and written directly to `depositAmount`; no 50%-of-room-price calculation exists.
- Room intervals are only null-validated. No hourly integer/minimum rule, package duration rule, or `expectedCheckIn < expectedCheckOut` validation is enforced before availability/billing.
Contract violated:
- `rule.md:22-28` requires 3-hour minimum/round-up semantics, and `rule.md:73-79` requires a 50% deposit and fixed total formula.
Plausible failure mode:
- A caller can underpay or overstate the deposit, create zero/invalid intervals, and obtain invoices whose deposit and room totals do not follow the canonical policy.
Durable solution hypothesis:
- Derive all monetary amounts server-side from canonical room rates and validated intervals; reject invalid interval/rental combinations and test boundary durations.
Disconfirming check:
- Submit a valid booking with deposit 0, 1, and an excessive amount and compare the persisted deposit to exactly 50% of the derived room total.

### F004 [P1] Checkout time and payment completion are caller-controlled

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/service/billing/BillingService.java:42-67; backend/src/main/java/com/hospitality/mis/dto/reservation/ReservationDtos.java:48-50
Evidence:
- `checkoutAt` comes directly from the request and is used for room charge, late surcharge, invoice issue time, actual checkout, and completed-stay calculation.
- Checkout marks the invoice `DA_THANH_TOAN` without creating a payment transaction or receipt, despite `rule.md:80-82` requiring payment/receipt records and reconciliation.
Contract violated:
- Actual timestamps must represent observed events, and payment completion must be backed by the required payment and receipt records.
Plausible failure mode:
- A client can submit a past/future timestamp to manipulate charges, late penalties, VIP counters, and audit history; an invoice can appear paid with no tender evidence.
Durable solution hypothesis:
- Take event time from a trusted server/controlled clock (or an explicitly authorized operational event), validate it against check-in/planned times, and make payment/receipt creation an atomic checkout invariant.
Disconfirming check:
- Send checkout requests with timestamps far in the past/future and verify they are rejected or ignored; verify a successful checkout creates the required transaction and receipt rows.

### F005 [P1] Reservation idempotency leaks another actor's booking

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/service/reservation/ReservationService.java:48-54; backend/src/main/java/com/hospitality/mis/service/reservation/ReservationService.java:96
Evidence:
- The existing idempotency key is looked up and returned before `requireActor(actor, request.employeeId())` runs.
- The idempotency key is globally unique on `Reservation` (line 39), with no actor/request fingerprint binding.
Contract violated:
- `rule.md:56,117` requires every request to be bound to its authenticated actor; idempotency must not become a cross-principal read channel.
Plausible failure mode:
- Any authorized employee who guesses/reuses another employee's key receives the prior reservation response, including guest and employee identifiers, without passing the ownership check.
Durable solution hypothesis:
- Bind idempotency records to actor plus canonical request hash, authenticate/bind the actor before lookup, and return conflict on mismatched fingerprints.
Disconfirming check:
- Create a reservation as employee A, repeat the same key as employee B, and verify the second request is denied rather than returning A's reservation.

### F006 [P1] Room status writes bypass the room state machine

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/service/room/RoomService.java:119-139
Evidence:
- `updateStatus` locks the row and assigns `room.setStatus(status)` directly; no current-to-next transition validation is present.
- The service accepts an arbitrary actor string and does not independently require a bound authenticated actor.
Contract violated:
- `rule.md:55,119` requires state validation before every transition and authenticated actor binding for every request.
Plausible failure mode:
- Illegal transitions (for example directly to an occupied/returned state) can bypass reservation/maintenance invariants, and non-HTTP callers can write unauthenticated audit actors.
Durable solution hypothesis:
- Put allowed transitions in the room aggregate/service, require a security-context actor at the application boundary, and audit only the resolved principal.
Disconfirming check:
- Enumerate `RoomStatus` transitions and invoke the service for each illegal edge; verify all are rejected and a missing/mismatched actor cannot write.

### F007 [P1] Finance handover trusts caller-supplied actors

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/controller/finance/FinanceController.java:15; backend/src/main/java/com/hospitality/mis/service/finance/FinanceService.java:21-24; backend/src/main/java/com/hospitality/mis/dto/finance/FinanceDtos.java:16-20
Evidence:
- `fromActor` and `toActor` are taken from the request and persisted; the service does not compare either to `SecurityActor.currentActor()` or resolve the target employee.
- The handover has no audit event.
Contract violated:
- `rule.md:109,117,120` requires trusted actor identity and auditability for changes needing traceability.
Plausible failure mode:
- An authorized finance user can forge who handed over or received cash, creating unreliable accountability and reconciliation records.
Durable solution hypothesis:
- Derive the requester/from actor from the security context, validate the recipient against the employee store and role policy, and write an audit event in the same transaction.
Disconfirming check:
- Submit a handover with a forged `from_actor` and confirm the persisted/audited actor is rejected or derived from the JWT principal.

### F008 [P1] Nested inventory route can write a different service than its path

Severity: P1 | Confidence: high
Source pointer: backend/src/main/java/com/hospitality/mis/controller/operations/InventoryMovementController.java:12-19; backend/src/main/java/com/hospitality/mis/service/operations/InventoryMovementService.java:17-23
Evidence:
- The route contains `{serviceId}`, but `record` accepts only the request and uses `request.serviceId()`; the path value is never compared or passed to the service.
Contract violated:
- Resource paths must bind the mutated child to the addressed parent; otherwise authorization and audit scope are ambiguous.
Plausible failure mode:
- `POST /services/A/inventory-movements` with body `service_id=B` records a movement for B while the caller believes/controls A, enabling integrity and audit confusion.
Durable solution hypothesis:
- Remove the duplicate body identifier or require equality with the path, and authorize/lock the service identified by the path.
Disconfirming check:
- Send mismatched path/body IDs and assert a 4xx with no movement row created.

### F009 [P1] Equipment compensation is based on attacker-supplied value/date

Severity: P1 | Confidence: medium
Source pointer: backend/src/main/java/com/hospitality/mis/service/operations/EquipmentIncidentService.java:49-60; backend/src/main/java/com/hospitality/mis/service/billing/PricingPolicy.java:127-137
Evidence:
- The incident request supplies `originalValue`, `purchasedAt`, and `quantity`; the service passes them directly into the 150%/200% formula without loading an equipment catalog value or validating ownership/quantity against stored equipment.
Contract violated:
- `rule.md:98-103` defines compensation from the equipment's value and age, not an untrusted arbitrary request amount.
Plausible failure mode:
- A staff caller can inflate original value or quantity and create an excessive compensation charge.
Durable solution hypothesis:
- Resolve equipment identity and immutable acquisition/value data from the database, validate incident room/reservation state, and make quantity bounded by inventory.
Disconfirming check:
- Create incidents with extreme request values for the same equipment and verify compensation remains tied to stored facts.

### F010 [P1] Billing read/write endpoints lack service-level object authorization

Severity: P1 | Confidence: medium
Source pointer: backend/src/main/java/com/hospitality/mis/controller/billing/InvoiceController.java:30-43; backend/src/main/java/com/hospitality/mis/service/billing/BillingService.java:69-71; backend/src/main/java/com/hospitality/mis/service/billing/PaymentTransactionService.java:19-30
Evidence:
- Invoice, payment, and receipt routes authorize broad roles only; their services load by invoice/reservation ID without checking the caller's reservation/guest scope or an explicit ownership policy.
- The authorization matrix states service-level ownership checks are required after coarse controller authorization.
Contract violated:
- `docs/authorization-matrix.md:3-25` requires ownership/scope checks at the service boundary; object IDs must not be sufficient authorization.
Plausible failure mode:
- Any employee with one of the broad roles who obtains another reservation/invoice ID can read or mutate financial data outside their permitted scope.
Durable solution hypothesis:
- Pass the resolved principal into each service, enforce reservation/guest/property scope before every load/mutation, and add IDOR tests for every nested billing route.
Disconfirming check:
- Authenticate as the lowest permitted role and request another employee's invoice/payment/receipt IDs; verify the result is denied consistently.

## Verification Queue

- F001: Confirm route authorization and search all controllers for a customer phone-login endpoint.
- F002: Test canonical-phone collision variants and inspect DB uniqueness/normalization.
- F003: Exercise duration and deposit boundary inputs; compare persisted values with policy-derived values.
- F004: Exercise forged checkout timestamps and inspect payment/receipt rows after checkout.
- F005: Replay an idempotency key under a different employee principal.
- F006: Enumerate room state edges and invoke illegal transitions/missing actors.
- F007: Submit forged finance actor fields and inspect audit/persistence.
- F008: Submit mismatched nested path/body service IDs.
- F009: Compare compensation for extreme request values against stored equipment facts.
- F010: Execute IDOR requests against invoice/payment/receipt resources with a lower-scope principal.

## Strongest Reason Not To Merge Yet

The current implementation allows caller-controlled financial facts and actor identity while several required customer/authentication flows are unreachable or absent. These are authorization and accounting-integrity failures, not merely coverage gaps.

## Next Receive Prompt

Use $ultra-review-receive to verify docs/ultrareview/26-09-08-hotel-mis-security-business-round-1.md and implement confirmed owner-clean fixes.
