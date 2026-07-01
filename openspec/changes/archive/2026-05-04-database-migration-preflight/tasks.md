## 1. Startup Routing

- [x] 1.1 Normalize storage backend selection to read `storageMethod` during runtime storage loading.
- [x] 1.2 Add a storage migration preflight hook before `DataStorageManager.loadStorages()` in master startup.
- [x] 1.3 Ensure the preflight does not run for slave-only startup paths.

## 2. SQL Migration Preflight

- [x] 2.1 Add a component that opens the configured SQL provider and detects whether `<tablePrefix>_version` exists.
- [x] 2.2 Add legacy SQL detection that validates the LoriTime 1.x aggregate table exists with expected legacy columns.
- [x] 2.3 Create the version table and insert version `1` when a valid legacy SQL table is found without a version table.
- [x] 2.4 Route versioned or seeded databases through `DatabaseUpdater.checkAndApplyUpdates()`.
- [x] 2.5 Route true fresh databases through `DatabaseUpdater.firstStartup()`.
- [x] 2.6 Keep version `2` first-startup queries limited to clean normalized schema creation and fallback scope setup.
- [x] 2.7 Keep version `2` normal update queries responsible for copying legacy aggregate data into normalized tables and removing or retiring the old table.

## 3. Legacy Flat-File Migration

- [x] 3.1 Detect legacy `names.yml` and `time.yml` files before normal storage loading.
- [x] 3.2 Back up detected legacy flat files using the existing backup service before importing.
- [x] 3.3 Abort flat-file migration without writes if backup fails.
- [x] 3.4 Initialize or open the SQLite 2.0 database for flat-file imports.
- [x] 3.5 Import legacy names and time values into normalized player/server/world/time tables using the fallback scope and migrated reason.
- [x] 3.6 Mark successfully imported legacy files so the import is not repeated on later startups.

## 4. Verification

- [x] 4.1 Add tests for SQL routing when the version table already exists.
- [x] 4.2 Add tests for legacy SQL detection, version `1` seeding, and update-path routing.
- [x] 4.3 Add tests for fresh SQL detection and first-startup routing.
- [x] 4.4 Add tests for legacy flat-file backup failure aborting migration.
- [x] 4.5 Add tests for successful flat-file import into SQLite and idempotency on repeated startup.
- [x] 4.6 Run the relevant Maven test suite for the common module.
