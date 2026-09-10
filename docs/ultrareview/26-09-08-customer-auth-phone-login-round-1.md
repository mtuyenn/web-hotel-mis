# Ultra Review: customer-auth-phone-login Round 1

Date: 26-09-08
Review name: customer-auth-phone-login
Round: 1
Scope: Repository C:\\web-hotel-mis. Read-only audit. Focus customer accounts and phone login: registration, login, OTP/password, enumeration, normalization, duplicate accounts, session/token binding, account recovery and authorization. Full relevant production surface, exact file:line evidence, fixes/tests/checks. No edits/tests/builds.
Report path: docs/ultrareview/26-09-08-customer-auth-phone-login-round-1.md

## Prior Round Guard

Previous reports read:
- none

## Findings

### F001 [P1] Customer auth surface is not wired for public phone login or recovery

Severity: P1 | Confidence: high
Source pointer: `C:/web-hotel-mis/backend/src/main/java/com/hospitality/mis/controller/auth/CustomerAccountController.java:14-18`, `C:/web-hotel-mis/backend/src/main/java/com/hospitality/mis/config/SecurityConfig.java:77-103`, `C:/web-hotel-mis/docs/api-contract.md:108-112`, `C:/web-hotel-mis/backend/README.md:45-47`
Evidence:
- The only customer-account controller exposes `POST /api/auth/customers/register` and nothing for login, refresh, OTP/password recovery, or customer ownership authorization.
- `SecurityConfig` only permits `/api/auth/login` and `/api/auth/refresh`; every other route, including customer registration, requires an authenticated principal.
- The published contract explicitly says customer-account phone login and customer ownership authorization still need implementation.
Contract violated:
- Customer accounts are meant to be a real loginable account type, but the production surface only has a registration stub and no customer principal/authorization path.
Plausible failure mode:
- Anonymous customers cannot create or use customer accounts, and no account-recovery or phone-login flow exists for the customer aggregate.
Durable solution hypothesis:
- Add a dedicated customer auth flow with explicit public or otherwise intended routes, a customer principal/role, ownership checks, and a recovery path; or remove the dead controller until the feature is fully wired.
Disconfirming check:
- Prove there is a public customer login/recovery route and a customer-auth principal/guard path, not just the employee JWT flow.

### F002 [P2] Customer phone values are not canonicalized before uniqueness and persistence

Severity: P2 | Confidence: high
Source pointer: `C:/web-hotel-mis/backend/src/main/java/com/hospitality/mis/service/auth/CustomerAccountService.java:24-35`, `C:/web-hotel-mis/backend/src/main/java/com/hospitality/mis/dto/auth/CustomerAccountDtos.java:12-13`, `C:/web-hotel-mis/backend/src/main/resources/db/migration/V1__baseline_schema.sql:283-293`, `C:/web-hotel-mis/backend/src/main/java/com/hospitality/mis/dto/guest/GuestDtos.java:27-31`
Evidence:
- Registration checks `existsByPhone(request.phone())` on the raw request string, then trims only after that check and persists the trimmed but otherwise unnormalized value.
- `RegisterRequest` has only `@NotBlank` on `phone`; there is no format constraint or shared normalizer.
- The database uniqueness constraint is exact-string uniqueness on `customer_accounts.phone`.
- Guest creation elsewhere already accepts formatted phone input with spaces, dots, pluses, and dashes, which shows the system currently treats formatted phone strings as valid user input.
Contract violated:
- The phone number is supposed to be the unique login identifier for customer accounts, so the identifier must be canonical before uniqueness checks and persistence.
Plausible failure mode:
- Semantically identical numbers such as `0900123456` and `0900-123-456` can become distinct rows, and later lookup or recovery by phone can miss the stored account if formatting differs.
Durable solution hypothesis:
- Introduce a shared phone normalizer/validator, store only the canonical form, use that canonical value in existence checks and lookups, and back it with a normalized unique key if possible.
Disconfirming check:
- Prove the same canonical phone value is produced and used for both `existsByPhone()` and `save()`, and that alternate formatting collapses to one account.

### F003 [P2] Public auth responses leak employee account state

Severity: P2 | Confidence: high
Source pointer: `C:/web-hotel-mis/backend/src/main/java/com/hospitality/mis/service/auth/AuthService.java:90-97,148-169`, `C:/web-hotel-mis/backend/src/main/java/com/hospitality/mis/service/auth/AuthFailureException.java:16-27`, `C:/web-hotel-mis/backend/src/main/java/com/hospitality/mis/middleware/AuthErrorHandler.java:20-24`
Evidence:
- Wrong credentials return the default `INVALID_CREDENTIALS` code.
- Disabled and locked accounts return distinct codes: `ACCOUNT_DISABLED` and `ACCOUNT_LOCKED`.
- The auth error handler sends the exception code straight back to the client in the 401 body.
Contract violated:
- Public authentication failures should not reveal whether an employee ID is valid or which state the account is in.
Plausible failure mode:
- An attacker can probe login or refresh and learn which employee IDs exist, which ones are disabled, and which ones are locked.
Durable solution hypothesis:
- Collapse all public auth failures to one generic 401 response and keep the specific reason only in server-side logs or audit records.
Disconfirming check:
- Prove that bad password, nonexistent user, disabled user, locked user, and malformed auth input all return the same public error code.

## Verification Queue

- F001: Verify there is a real customer login/recovery route and customer principal binding, not just register-only plumbing.
- F002: Verify normalized phone canonicalization is applied before existence checks and persistence, and that alternate formats collide as intended.
- F003: Verify all public auth failures collapse to the same 401 code and do not expose account state.

## Strongest Reason Not To Merge Yet

The customer account flow is only half wired: the public production surface has no customer phone-login, recovery, or ownership path, and the single registration endpoint is not reachable anonymously under the current security config.

## Next Receive Prompt

Use $ultra-review-receive to verify docs/ultrareview/26-09-08-customer-auth-phone-login-round-1.md and implement confirmed owner-clean fixes.
