# Database migrations

`V1__baseline_schema.sql` is the canonical baseline schema for the web hotel
MIS. It is intentionally a hard cut: Flyway must run it against an empty
database. Applied migrations are immutable; extend the schema with the next
versioned migration and never edit an already-applied file or hide a checksum
mismatch with `flyway repair`.

Follow-up migrations may extend the canonical contract, but must not introduce
compatibility aliases, dual-read, dual-write, or repair behavior.

Identifiers use one English `snake_case` contract. Hibernate is configured with
`ddl-auto: validate`; schema changes belong in an explicit Flyway migration.
