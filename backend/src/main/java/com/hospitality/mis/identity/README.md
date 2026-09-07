# Identity module

Identity owns the canonical employee account aggregate used by authentication,
authorization, reservations, and audit boundaries.

`Employee` is the concrete JPA entity mapped to the V1 `employees` table:
`id`, `full_name`, `password`, `position`, `address`, `phone`, `enabled`,
`account_non_locked`, `failed_login_attempts`, `last_failed_login_at`, and
`last_login_at`.

`EmployeeRole` is the canonical role policy. Its permissions are projected by
the security boundary as `ROLE_*` and `PERMISSION_*` authorities. Account state
is enforced during password authentication and JWT authority resolution.
