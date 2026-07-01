## Context

LoriTime 2.0 uses Spellbook's `DatabaseUpdater` to apply database versions. The updater determines the current version from `<tablePrefix>_version`; if that table is missing, the current version is `0`. Calling `firstStartup()` with current version `0` runs first-startup queries for the highest version that defines them.

That behavior is correct for fresh 2.0 databases, but incorrect for LoriTime 1.x SQL databases because they contain the legacy aggregate table `<tablePrefix>` without a version table. Those databases need to be treated as version `1` and upgraded through the normal version `2` migration.

Legacy flat-file storage is a separate input format. `names.yml` and `time.yml` should be backed up first, then imported into a new SQLite database using the 2.0 schema.

## Goals / Non-Goals

**Goals:**

- Distinguish fresh databases from legacy LoriTime 1.x SQL databases before invoking the updater.
- Seed a missing version table to version `1` only when a legacy SQL table is detected.
- Route legacy SQL databases through `checkAndApplyUpdates()` so version `2` migration queries run.
- Route fresh databases through `firstStartup()` so only clean 2.0 schema setup runs.
- Back up and migrate legacy `names.yml` and `time.yml` files into SQLite automatically.
- Use `storageMethod` as the runtime storage configuration key.

**Non-Goals:**

- Updating public documentation.
- Redesigning command behavior or public storage APIs.
- Changing timestamp-diff SQL behavior.
- Reworking the full database schema beyond migration routing.
- Solving every historical migration edge case in one pass.

## Decisions

### Add a storage migration preflight before storage loading

The plugin should run a dedicated preflight step before `DataStorageManager.loadStorages()`. This keeps migration routing separate from runtime storage construction and makes the startup flow explicit:

```text
enableAsMaster
  -> run storage migration preflight
  -> load storages
  -> start cache
```

Alternative considered: putting all detection directly inside migration version SQL. That does not solve the root problem because Spellbook chooses first-startup versus update behavior before version SQL can express legacy state.

### Treat the legacy SQL aggregate table as version 1 evidence

If `<tablePrefix>_version` is missing and `<tablePrefix>` exists, LoriTime should create `<tablePrefix>_version`, insert version `1`, and call `checkAndApplyUpdates()`.

This preserves Spellbook as the migration engine while correcting its missing context. The version seeding is intentionally narrow: it only happens when the old aggregate table exists.

Alternative considered: remove first-startup queries and always run full migrations from version `0`. That would make fresh installs pass through legacy schema creation and migration cleanup, which is noisier and more fragile than explicit preflight detection.

### Keep first-startup for true fresh databases

If neither `<tablePrefix>_version` nor the legacy aggregate table exists, LoriTime should call `firstStartup()`. Version `2` first-startup queries should create the clean normalized schema and fallback server/world without running legacy copy/drop statements.

### Keep legacy file migration separate from SQL migration

`names.yml` and `time.yml` are not database versions. They should be handled by a file-to-SQLite migration path that backs up both files through the existing backup service, initializes a SQLite 2.0 schema, imports players and migrated time rows, and prevents repeated imports.

Alternative considered: converting files into an intermediate legacy SQL table and then running the SQL migration. That would reuse SQL migration logic, but it adds a synthetic intermediate format and makes rollback/debugging harder.

## Risks / Trade-offs

- Legacy table detection can misclassify a manually created table named exactly like `<tablePrefix>` as LoriTime 1.x data -> validate required legacy columns before seeding version `1`.
- Version table creation has dialect differences -> centralize the SQL in the preflight component and test SQLite plus MySQL/MariaDB syntax where practical.
- File migration can duplicate data on repeated startup -> mark migrated files by moving/renaming them after successful import or by recording migration state.
- Backup can fail before import -> abort file migration if backup fails; do not import without a recoverable copy.
- SQL migration can fail after version seeding -> run version table seeding in a small transaction where supported and leave clear logs explaining manual recovery state.

## Migration Plan

1. Normalize storage method lookup to `storageMethod`.
2. Add a preflight component invoked before normal storage loading.
3. For SQL storage:
   - Open the configured provider.
   - Detect version table and legacy aggregate table state.
   - Seed version `1` only for detected legacy SQL databases.
   - Call `checkAndApplyUpdates()` for versioned or seeded databases.
   - Call `firstStartup()` only for fresh databases.
4. For legacy flat-file storage:
   - Detect `names.yml` and `time.yml`.
   - Back up both files.
   - Create or open SQLite storage.
   - Initialize the 2.0 schema.
   - Import names and times into fallback server/world entries with migrated reasons.
   - Mark source files as migrated after success.
5. Add tests for detection, routing, backup behavior, and import idempotency.

Rollback is manual for SQL databases after migration begins. For flat-file migration, rollback is supported by restoring the backed-up YAML files and removing the generated SQLite database before restarting.
