## Context

`UnifiedStorage` is the runtime storage contract for player identity, scoped totals, manual adjustments, sessions, and player deletion. It is also injectable, so adding mandatory transfer/delete methods to it would break custom storage implementations. The current database schema already has the relationships needed for maintenance operations: sessions reference worlds, worlds reference servers, and manual adjustments reference either a server or a world depending on scope.

Localization files currently load from the plugin data root using `general.language + ".yml"`, and bundled documentation still points at a localization path. New admin maintenance feedback would add many more message keys, so the key naming and file layout should be regularized before those commands are implemented.

## Goals / Non-Goals

**Goals:**
- Prepare admin storage maintenance primitives for future commands.
- Keep `UnifiedStorage` source and binary compatibility for existing consumers.
- Support preview-first transfer/delete workflows with explicit confirmation for merging or destructive changes.
- Move language files into a `language/` folder.
- Standardize language keys to camelCase path segments.
- Preserve existing installations through migration or compatibility loading.

**Non-Goals:**
- Do not add the final user-facing storage maintenance command syntax in this change.
- Do not change database schema unless implementation discovers a backend-specific constraint that requires it.
- Do not alter normal time accumulation, scoped totals, or player modify command behavior.
- Do not silently merge or delete data without an explicit preview/confirmation path.

## Decisions

### Add a Separate Maintenance Contract

Introduce a dedicated admin maintenance contract, for example `StorageMaintenance` or `AdminStorageMaintenance`, with preview and execute methods for storage-type transfer, server transfer, world transfer, and scoped deletion.

Alternative considered: add methods directly to `UnifiedStorage`. This is rejected because it would make every custom or test implementation implement high-risk administrative operations even when they only need runtime reads/writes.

### Preview Before Execute

Maintenance operations should produce a preview containing source/target scope, affected row counts, affected players, target collisions, and whether confirmation is required. Execution should accept a confirmation token or equivalent request fingerprint so future commands can verify the user confirmed the exact operation that was previewed.

Alternative considered: boolean `force` flags. This is weaker because it gives future command code less protection against stale previews or accidental merges.

### Treat Server and World Transfers as Repointing Data

Database-backed transfer should move existing session and adjustment references to the target server/world identities instead of recalculating totals into manual adjustments. This preserves timestamps, reasons, actor metadata, and range-query behavior.

Alternative considered: aggregate source data and write a single target adjustment. This loses historical detail and would make ranged totals inaccurate.

### Keep Storage-Type Transfer as a Separate Service

Storage-type transfer should be a maintenance/import service that can open a source and target storage backend and copy all player identities, sessions, and adjustments. The target must be empty unless the operation explicitly supports a later merge policy.

Alternative considered: reuse the existing startup migration service. That service is oriented around legacy startup migration and should not become a general admin import/export path.

### Migrate Language Layout with Compatibility

The new canonical location is `language/<language>.yml`. Startup should create bundled defaults there and move or copy existing root-level language files into the folder before loading. Root-level language files should not remain the preferred location after migration.

Alternative considered: only change bundled files and require users to move files manually. This is rejected because reload/startup would fail or silently reset translations for existing installations.

### Camel Case Applies to Key Segments

Language key paths should remain dot-separated, but each segment should use camelCase where it contains multiple words, for example `deleteUser`, `noPermission`, or `storageMaintenance`. Existing already-lowercase single-word segments can remain lowercase. Runtime lookups should be updated to canonical keys, with optional temporary fallback for old keys during migration.

## Risks / Trade-offs

- Existing translations may contain custom keys -> migrate known keys and preserve unknown user-defined keys where possible.
- Confirmation previews can become stale -> bind confirmation to operation details and reject mismatched or expired confirmations.
- Server/world transfers can affect active sessions -> future command execution must run only on storage-owning runtimes and should either flush/stop active sessions or reject while affected players are online.
- SQL differences across SQLite/MySQL/MariaDB can affect bulk updates -> implement table helper methods with dialect-neutral SQL where possible and cover each supported dialect with focused tests.
- Optional maintenance contract adds type checks -> command/service callers must report unsupported storage cleanly rather than casting blindly.

## Migration Plan

1. Add language file loading from `language/<language>.yml`.
2. On startup/update, migrate existing root-level `<language>.yml` files into `language/` with backup behavior.
3. Rename bundled language keys to camelCase and update runtime lookup constants/usages.
4. Provide compatibility fallback or migration mappings for known legacy keys.
5. Add the optional storage maintenance contract and database-backed implementation.
6. Add tests for preview/execute semantics and language migration.
