# Hotel MIS backend

Spring Boot API for the Hotel MIS. Java code lives under
`src/main/java/com/hospitality/mis`, grouped into `controller`, `service`,
`dao`, `dto`, `entity`, and `config` packages.

## Chạy local

Start local SQL Server from the repository root, clear any stale database
variables from the current PowerShell session, then run from this directory.
The backend accepts only `jdbc:sqlserver://` URLs and SQL Server credentials.

```powershell
docker compose -f ..\docker-compose.yml up -d
Remove-Item Env:DB_URL, Env:DB_USERNAME, Env:DB_PASSWORD -ErrorAction SilentlyContinue
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=QLKS;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME = "sa"
$env:DB_PASSWORD = "<local-sqlserver-password>"
mvn spring-boot:run
```

`JWT_SECRET` is optional for local/demo startup; if omitted, the backend uses
its built-in demo key. Set a private, stable value for shared or deployed
environments. Changing the key invalidates existing JWTs. The owner's local
SMTP and VNPay configuration is governed by [AGENTS.md](../AGENTS.md).

Flyway applies exactly six demo-baseline files from
`src/main/resources/db/migration/`: V1 schema, V2 indexes, V3 functions,
V4 views, V5 stored procedures/business transactions, and V6 triggers. Edit the owning file;
never add V7+ or auxiliary DDL files. After checksum changes, recreate only an
authorized demo/disposable test database; never repair checksums or rewrite
Flyway history. Root `AGENTS.md` owns this rule. Hibernate uses
`ddl-auto=validate` and does not create or update tables.

## Lần theo một thay đổi nghiệp vụ

1. Find the endpoint in the domain controller and read its request/response DTO.
2. Follow the service method for validation, authorization scope, state changes,
   and transaction boundaries.
3. Check the explicit database adapter and its SQL Server view/function/procedure.
   Production has no JPA repository writer; entities remain for schema validation.
4. If database behavior changes, edit its owning V1–V6 file and rebuild the
   authorized demo/test database before validating Flyway.
5. Update the focused test and the [API contract](../docs/api-contract.md) if
   the externally visible behavior changes.

Controllers should not write to the database directly, and entities should not
be returned as API responses. Authenticated actor identity comes from the
security context, not a client-supplied actor field.

## SQL Server acceptance tests

Tests requiring SQL Server must use a separate disposable database. Set
`MIGRATION_TEST_DB_URL`, `MIGRATION_TEST_DB_USERNAME`, and
`MIGRATION_TEST_DB_PASSWORD` explicitly; these variables are separate from the
running application's `DB_*` settings. Never point an acceptance test at a
shared or production schema. The live HTTP contract scenarios have a separate
setup and safety guide at [`frontend/e2e/README.md`](../frontend/e2e/README.md).

The backend is the only business-data writer. Frontend, agent, and operational
clients must use authorized backend use cases rather than direct SQL Server writes.

## VNPay sandbox

For an environment override, set these variables before starting the backend:

```powershell
$env:VNPAY_TMN_CODE = "<vnp_TmnCode>"
$env:VNPAY_HASH_SECRET = "<vnp_HashSecret>"
$env:VNPAY_RETURN_URL = "https://<public-backend-host>/api/public/payments/vnpay/return"
$env:VNPAY_FRONTEND_RESULT_URL = "http://localhost:5173/payment/vnpay-result"
mvn spring-boot:run
```

The public backend URL must be reachable by VNPay. A local Cloudflare tunnel
can expose port 8080 with `cloudflared tunnel --url http://localhost:8080`;
use the resulting HTTPS origin for the Return URL above and configure the
sandbox IPN URL as `/api/public/payments/vnpay/ipn` on that same origin.
Restart the backend after changing process environment variables. A named
tunnel provides a stable hostname when needed; keep temporary tunnel URLs
out of tracked configuration.

The backend leaves `vnp_BankCode` unset so VNPay presents its supported methods.
Customer deposit amounts, deadlines and cancellation terms belong to
[customer-policy.md](../customer-policy.md), not to setup instructions.
