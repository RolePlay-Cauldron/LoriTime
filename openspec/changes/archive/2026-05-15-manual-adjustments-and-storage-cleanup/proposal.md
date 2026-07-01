## Why

Manual time changes are currently stored as synthetic session rows, and online manual changes can be folded into the active session by changing its start timestamp. This makes real play sessions, administrative corrections, AFK removals, and plugin-message adjustments hard to distinguish and weakens auditability.

The storage deletion API also uses the broad name `removePlayer`, while the desired behavior is a precise player deletion operation plus a separate, opt-in cleanup that removes old time history without deleting player identity.

## What Changes

- Add a dedicated manual adjustment table for positive and negative time changes.
- Include actor information for manual adjustments; console actors use a null actor UUID and an explicit console actor name.
- Update total time queries to include real session duration plus manual adjustment deltas.
- **BREAKING**: Remove `UnifiedStorage#removePlayer` and replace it with a precise `deletePlayer` operation.
- Add an opt-in `storageCleanup.*` configuration area for deleting old time history for players whose player table join/activity information is older than the configured threshold.
- Ensure cleanup deletes only history rows, not player identity rows.
- Clarify session context update behavior so Paper/Folia context changes create a new session segment and stop/flush update the active segment.
- Replace the `multiSetup.enabled` gate with direct `multiSetup.mode` semantics, where `standalone` is the default mode.
- Update storage documentation to state which platforms can run as `standalone`, `master`, or `slave`.

## Capabilities

### New Capabilities
- `storage-history-cleanup`: Configurable cleanup of inactive players' stored time history while preserving player identity.

### Modified Capabilities
- `unified-storage-system`: Manual adjustment persistence, total calculation, and player deletion semantics change in the unified storage contract.

## Impact

- Storage API: `UnifiedStorage`, `AccumulatingTimeStorage`, database-backed storage implementation, custom storage implementors.
- Session tracking: Paper/Folia context-change listeners and slave session reporting.
- Database schema and migrations: SQLite, MySQL, and MariaDB gain a manual adjustment table and supporting indexes/foreign keys.
- Time queries: player totals and all-player totals must include adjustment rows.
- Commands and internal write paths: admin `set`, `modify`, `reset`, AFK removal, and plugin-message adjustment writes must use actor-aware adjustment persistence.
- Configuration: add `storageCleanup.*` keys, disabled by default.
- Configuration: simplify storage mode selection by relying on `multiSetup.mode`.
- Documentation: `docs/Storage.md` and setup references for platform/mode compatibility.
- Tests: storage totals, manual adjustment audit rows, player deletion, and inactive-history cleanup need focused coverage.
