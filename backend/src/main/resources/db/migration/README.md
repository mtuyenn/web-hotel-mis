# Database migrations

`V1__baseline_schema.sql` is the complete canonical schema for the web hotel
MIS. It is intentionally a hard cut: Flyway must run it against an empty
database, and this directory must contain no follow-up compatibility,
dual-read, dual-write, or repair migrations.

Identifiers use one English `snake_case` contract. Hibernate is configured with
`ddl-auto: validate`; schema changes belong in an explicit Flyway migration.
