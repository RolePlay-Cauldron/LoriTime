## 1. Schema And Storage Model

- [x] 1.1 Add a manual adjustment value object or method parameters that carry target UUID, signed seconds, reason, actor UUID, and actor name.
- [x] 1.2 Add a database table helper for `<prefix>_time_adjustment` inserts, totals, all-player totals, player deletion support, and inactive-history cleanup support.
- [x] 1.3 Add the adjustment table and required indexes to SQLite migration version 2.
- [x] 1.4 Add the adjustment table, indexes, and foreign key constraints to MySQL/MariaDB migration version 2.
- [x] 1.5 Wire the new table helper into runtime storage construction and legacy migration storage construction.

## 2. Unified Storage API

- [x] 2.1 Remove `UnifiedStorage#removePlayer` and replace it with `deletePlayer(UUID)`.
- [x] 2.2 Add actor-aware manual adjustment methods to `UnifiedStorage` and `AccumulatingTimeStorage`.
- [x] 2.3 Change online manual adjustments so they write adjustment rows without changing active session start timestamps.
- [x] 2.4 Update database-backed player totals and all-player totals to include session durations plus signed adjustment amounts.
- [x] 2.5 Update player deletion to delete the identity row and rely on cascades for session and adjustment history.
- [x] 2.6 Remove unused server/world parameters from stop accumulation APIs.
- [x] 2.7 Change session accumulation so start/switch create active session rows and flush/stop update the active row.

## 3. Cleanup Configuration And Execution

- [x] 3.1 Add disabled-by-default `storageCleanup.*` keys to the default config.
- [x] 3.2 Add configuration parsing for cleanup enablement and inactivity threshold.
- [x] 3.3 Implement history-only cleanup that deletes `_time` and `_time_adjustment` rows for inactive players while preserving `_player`.
- [x] 3.4 Run cleanup only when explicitly enabled.
- [x] 3.5 Ensure player activity metadata used by cleanup reflects actual player activity rather than manual storage writes.
- [x] 3.6 Remove or ignore `multiSetup.enabled` and make `multiSetup.mode` the authoritative storage responsibility setting.

## 4. Callers And Actor Attribution

- [x] 4.1 Update admin `set`, `modify`, and `reset` commands to pass the command sender as actor.
- [x] 4.2 Represent console actors as null actor UUID plus a stable console actor name.
- [x] 4.3 Update AFK removal and internal/system adjustments to pass a stable system actor name.
- [x] 4.4 Update plugin-message adjustment handling to preserve or assign actor metadata for remote writes.
- [x] 4.5 Update command deletion flow to call `deletePlayer`.
- [x] 4.6 Update Paper/Folia canonical session listeners to switch context when the player's effective world changes.
- [x] 4.7 Update Paper/Folia slave session reporting to report context switches when the player's effective world changes.
- [x] 4.8 Ensure duplicate teleport/respawn/world-change signals do not create duplicate session rows for unchanged context.
- [x] 4.9 Add configured logical server-name context for Paper/Folia canonical writes and slave reports.
- [x] 4.10 Update Velocity and Bungee canonical listeners to switch context when a player changes backend server.

## 5. Documentation

- [x] 5.1 Update `docs/Storage.md` with precise Paper/Folia, Velocity, and Bungee mode support.
- [x] 5.2 Update setup documentation and config comments to explain `multiSetup.mode` without `multiSetup.enabled`.
- [x] 5.3 Document configured Paper/Folia server context names and proxy backend server context tracking.
- [x] 5.4 Document that Paper/Folia context changes are tracked by effective world changes and that proxies use fallback world context.

## 6. Verification

- [x] 6.1 Add unit tests for offline and online manual adjustments writing adjustment rows and preserving session durations.
- [x] 6.2 Add database tests proving player totals and all-player totals include adjustment-only, session-only, and mixed players.
- [x] 6.3 Add tests for console actor persistence.
- [x] 6.4 Add tests for `deletePlayer` cascading identity-owned data.
- [x] 6.5 Add tests for inactive-history cleanup deleting only history rows and preserving identity rows.
- [x] 6.6 Add tests for world-change context switching and duplicate unchanged-context no-ops.
- [x] 6.7 Add tests for direct `multiSetup.mode` parsing/defaults.
- [x] 6.8 Add tests for configured Paper/Folia server context and proxy backend server context switching.
- [x] 6.9 Run the focused common-module Maven tests for storage, migrations, accumulation, and plugin messaging.
