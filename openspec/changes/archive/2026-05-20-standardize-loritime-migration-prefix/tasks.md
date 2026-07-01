## 1. Regression Tests

- [x] 1.1 Add a config migration test proving a legacy custom `mysql.tablePrefix` becomes `data.tablePrefix: loritime`.
- [x] 1.2 Add a flat-file migration test proving YML-to-SQLite imports use `loritime_*` tables even when config exposes another `data.tablePrefix`.
- [x] 1.3 Add or update assertions that the legacy flat-file import marker and `storageMethod: sqlite` behavior remain unchanged.

## 2. Implementation

- [x] 2.1 Make the V1-to-V2 config migration intentionally set `data.tablePrefix` to `loritime` with clear code structure.
- [x] 2.2 Ensure the legacy flat-file import path constructs its migration database storage with the canonical `loritime` prefix.
- [x] 2.3 Keep normal runtime storage loading unchanged after migration completes.

## 3. Documentation And Verification

- [x] 3.1 Update config comments or migration docs where wording could imply custom prefixes are preserved.
- [x] 3.2 Run focused common-module tests for config migration and storage migration.
- [x] 3.3 Run `openspec validate standardize-loritime-migration-prefix --strict`.
