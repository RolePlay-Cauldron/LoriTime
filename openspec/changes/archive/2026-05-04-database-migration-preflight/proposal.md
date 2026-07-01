## Why

LoriTime 2.0 introduces a normalized database schema, but existing LoriTime 1.x SQL databases do not have a version table. Without a preflight check, the database updater treats those legacy databases as fresh installs and runs first-startup schema creation instead of the legacy-to-2.0 migration path.

Legacy `names.yml` and `time.yml` files also need a controlled migration path into SQLite so users can upgrade from flat-file storage without manual conversion or data loss.

## What Changes

- Add a database migration preflight before normal storage loading.
- Detect existing LoriTime 1.x SQL aggregate tables when the version table is missing.
- Seed the database version table with version `1` for detected legacy SQL databases, then run the normal update path to version `2`.
- Use first-startup database creation only for databases that have neither a version table nor a legacy LoriTime 1.x table.
- Automatically back up legacy `names.yml` and `time.yml` before migrating their contents into a SQLite 2.0 database.
- Normalize runtime storage configuration lookup to `storageMethod`; documentation updates are intentionally out of scope.

## Capabilities

### New Capabilities

- `storage-migration-preflight`: Detects legacy storage state before startup and routes SQL and flat-file migrations through the correct upgrade path.

### Modified Capabilities

None.

## Impact

- Affects common storage initialization, database updater invocation, SQL migration routing, and legacy flat-file import.
- Adds migration tests for legacy SQL detection/version seeding and `names.yml`/`time.yml` backup plus SQLite import.
- Does not change public command behavior or documentation in this change.
