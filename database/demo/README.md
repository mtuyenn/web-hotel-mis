# Demo database reset

These scripts are manual-only. The application must not drop or seed the
database during startup.

## Clean database flow

1. Create an empty MySQL 8.4 schema.

```sql
CREATE DATABASE QLKS
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

2. Run the backend with Flyway enabled and Hibernate validation enabled.

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/QLKS?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="<database-password>"
cd backend
mvn spring-boot:run
```

Expected configuration:

```yaml
spring:
  flyway:
    enabled: true
  jpa:
    hibernate:
      ddl-auto: validate
```

Do not use `ddl-auto=create`, `ddl-auto=update`, or startup scripts that drop
the schema.

## Reset demo data

After Flyway has migrated the schema, run:

```powershell
mysql -h localhost -u root -p QLKS < database/demo/reset_demo.sql
```

`reset_demo.sql` clears demo transactional/catalog data and reseeds rooms,
guests, services, and maintenance examples. It intentionally does not seed
employee passwords.

## Admin bootstrap

Provision employee login accounts through the security/admin bootstrap path for
the target environment. If a SQL-based emergency bootstrap is approved, pass
only a BCrypt hash from an environment secret into that one-off command; do not
write plaintext passwords into SQL files.

Example for a precomputed hash stored in `DEMO_ADMIN_BCRYPT_HASH`:

```powershell
if ($env:DEMO_ADMIN_BCRYPT_HASH -notmatch '^\$2[aby]\$\d{2}\$[./A-Za-z0-9]{53}$') {
  throw "DEMO_ADMIN_BCRYPT_HASH must be a BCrypt hash"
}

mysql -h localhost -u root -p QLKS --execute "
INSERT INTO employees (id, full_name, password, position, phone)
VALUES ('NV_ADMIN', 'Demo Admin', '$env:DEMO_ADMIN_BCRYPT_HASH', 'QUAN_LY', '0900000000')
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  position = VALUES(position)"
```
