## Context

The current V1-to-V2 config migration moves old `mysql.*` settings into the new `data.*` section and then normalizes `data.tablePrefix` to `loritime`. That normalization is intentional: V2 should have a predictable canonical storage namespace instead of inheriting every legacy table prefix.

Legacy flat-file migration is related but separate. When an old config used `general.storage: yml`, the config migration switches the storage mode to SQLite and marks `storageMigration.legacyFlatFileImport`. The later storage migration imports `names.yml` and `time.yml` into SQLite. That import path should also use the canonical `loritime` prefix rather than any legacy remote SQL prefix.

## Goals / Non-Goals

**Goals:**
- Make canonical `loritime` table prefix behavior explicit in the specs.
- Keep V1-to-V2 config migration deterministic by setting `data.tablePrefix` to `loritime`.
- Ensure YML-to-SQLite migration imports into `loritime_*` tables.
- Add regression coverage so the intended prefix normalization is not mistaken for a bug later.

**Non-Goals:**
- Do not preserve custom legacy SQL table prefixes in migrated V2 config.
- Do not rename existing SQL tables from custom prefixes to `loritime`.
- Do not change the V2 default config template beyond the existing `loritime` default.
- Do not change normal runtime storage selection after migration is complete.

## Decisions

1. Use `loritime` as the canonical V2 migration prefix.

   Rationale: migrated V2 storage should be predictable and match the bundled default. This is especially important for legacy flat-file imports, where there was no SQL table prefix in the source data.

   Alternative considered: preserve `mysql.tablePrefix` when migrating config. Rejected because it carries a legacy remote-SQL concern into the normalized V2 defaults and conflicts with the desired canonical SQLite import target.

2. Keep prefix normalization in config migration, but make it intentional and tested.

   Rationale: the current code already normalizes to `loritime`; the gap is that the intended behavior was not clear from the tests and comments. Tests should assert that even a custom legacy prefix becomes `loritime`.

   Alternative considered: remove the `mysql.tablePrefix` move before setting the canonical value. This is acceptable as an implementation cleanup, but not required by the behavior as long as the final migrated value is `loritime`.

3. Isolate flat-file import from migrated remote SQL prefix values.

   Rationale: `names.yml` and `time.yml` are legacy flat data files. Their SQLite import should not depend on old `mysql.*` settings. If implementation needs a temporary migration config or forced prefix, it should be scoped to the import path and not affect unrelated config values beyond the explicit migration to `storageMethod: sqlite`.

## Risks / Trade-offs

- Users with custom V1 SQL prefixes may expect the value to carry forward -> Document and test that the V2 migration standardizes to `loritime`.
- Existing code comments can be read as promising no prefix rename in migration -> Update wording so it distinguishes "does not rename existing database tables" from "new V2 config defaults to `loritime`".
- Flat-file migration changing runtime config before import can persist `storageMethod: sqlite` early -> Keep current behavior, but ensure prefix selection for the import is explicit and regression-tested.

## Migration Plan

Implement the spec clarification with focused tests first:
- Config migration test with legacy `mysql.tablePrefix: custom_prefix` and expected `data.tablePrefix: loritime`.
- Flat-file migration test with config returning a non-`loritime` prefix and expected imported rows under `loritime_*` tables.

Then adjust code/comments only where needed to make those tests pass and make the intent readable.
