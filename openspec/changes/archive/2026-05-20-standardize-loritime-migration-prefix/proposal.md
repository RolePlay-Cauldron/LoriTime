## Why

The V2 storage layout should use a predictable canonical table prefix after migration instead of carrying forward legacy prefix choices. This avoids old configuration values leaking into the new normalized storage model and keeps YML-to-SQLite imports on the default `loritime` schema.

## What Changes

- Legacy `config.yml` migration will set the V2 `data.tablePrefix` value to `loritime` during the V1-to-V2 config migration.
- Legacy `general.storage: yml` migrations will continue to set `storageMethod: sqlite` and request a legacy flat-file import.
- Legacy flat-file imports from `names.yml` and `time.yml` will import into the SQLite database using the canonical `loritime` table prefix, regardless of any legacy or user-provided table-prefix value.
- Existing successful flat-file migration behavior remains: migrated files are marked so the import is not repeated.

## Capabilities

### New Capabilities

### Modified Capabilities
- `configuration-management`: Clarify that the V1-to-V2 config migration intentionally standardizes `data.tablePrefix` to `loritime`.
- `storage-migration-preflight`: Clarify that legacy flat-file migration imports into SQLite using the canonical `loritime` table prefix.

## Impact

- Affected code: config schema migration, storage migration service, related tests.
- Affected data: legacy flat-file imports will target the `loritime_*` SQLite tables.
- Compatibility: legacy custom SQL table prefixes are not carried into V2 config during migration; the new default is intentional for the normalized V2 schema.
