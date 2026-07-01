## Context

The unified storage migration normalized play time into session rows in `_time`. That model works well for actual join/leave, flush, shutdown, and context-switch events, but manual changes still reuse session storage. Offline adjustments are written as synthetic sessions, while online adjustments change the active session start timestamp and become indistinguishable from real play time.

The player table already contains activity metadata (`last_seen` today), and that makes it a natural anchor for inactive-history cleanup. However, cleanup must preserve identity rows so UUID/name lookup and audit context remain available.

## Goals / Non-Goals

**Goals:**
- Store manual time changes in a dedicated adjustment table with signed seconds and actor metadata.
- Treat console actors as auditable non-player actors by storing a null actor UUID and a stable actor name such as `CONSOLE`.
- Include adjustment totals in all player total calculations.
- Replace the broad `removePlayer` storage API with `deletePlayer`.
- Add disabled-by-default `storageCleanup.*` configuration for deleting old time history only.
- Keep player identity rows after inactive-history cleanup.
- Update Paper/Folia sessions when player world context changes.
- Remove the redundant `multiSetup.enabled` gate and treat `multiSetup.mode` as the single source of storage responsibility.
- Make storage documentation precise about supported platform/mode combinations.

**Non-Goals:**
- Do not introduce a player-facing adjustment history command.
- Do not delete inactive player identity rows.
- Do not add external dependencies.
- Do not preserve binary API compatibility for `removePlayer`.

## Decisions

1. Manual adjustments use a separate table.

   The table should be named with the configured prefix, for example `<prefix>_time_adjustment`, and contain `id`, `player_id`, `amount_seconds`, `reason`, `actor_uuid`, `actor_name`, and `created_at`.

   This keeps real session records factual and lets manual corrections be audited independently. The alternative was to keep writing synthetic sessions with richer reasons, but that still pollutes session history and cannot reliably represent online adjustments.

2. Actor metadata is stored inline on each adjustment.

   `actor_uuid` is nullable because console and system actors do not have player UUIDs. `actor_name` is required and should use stable values such as the sender name, `CONSOLE`, or `SYSTEM`.

   The alternative was a separate actor table, but the current need is simple audit context, not actor lifecycle management.

3. Totals are computed from sessions plus adjustments.

   Player total queries should sum session durations and signed adjustment amounts. All-player totals should return players with only sessions, only adjustments, or both.

   The alternative was to materialize totals in the player table, but that would add cache invalidation and migration complexity.

4. Online manual adjustments write adjustment rows instead of changing session start time.

   Active session time should remain based on actual session start and flush boundaries. `AccumulatingTimeStorage#getTime` adds in-memory active time on top of persisted totals, and persisted totals include adjustments.

   This changes current behavior internally but preserves the visible total result.

5. Cleanup deletes history rows, not player rows.

   Inactive-history cleanup deletes `_time` rows and `_time_adjustment` rows for matching players, leaving `_player` intact. The feature is configured under `storageCleanup.*` and disabled by default.

   The alternative was to delete players and rely on cascade deletion, but that conflicts with the requested identity preservation and would remove name/UUID mapping.

6. Player deletion remains destructive and explicit.

   `deletePlayer(UUID)` deletes the player identity and associated history through database constraints. `removePlayer` is removed rather than deprecated.

7. Context changes are driven by world-change semantics, not death alone.

   Paper/Folia should update session context when the player's effective world changes. `PlayerChangedWorldEvent` is the primary Bukkit event for this. Teleports only need explicit handling if they can move a player across worlds without producing `PlayerChangedWorldEvent`; otherwise adding both can double-record the same transition. Death itself should not create a new entry, but respawn/world-change handling should capture cases where death causes a world switch.

   The listener should compare the accumulator's current context to the new server/world before switching, so duplicate or overlapping events become no-ops.

8. Start and switch create active session rows; flush and stop update them.

   The storage model should move toward an active persisted session row. `startAccumulating` inserts a row for the active context, `switchContext` closes the old row and inserts the new row, and `flush`/`stop` update the active row's `leave_time`. This keeps crash persistence without splitting a continuous session into many flush chunks.

   This means the accumulator should remember enough storage identity to update the active session row, not only the player UUID/context.

9. `multiSetup.mode` should become authoritative.

   Today `multiSetup.enabled=false` forces `standalone` even if `multiSetup.mode` is set. Since modes now describe storage responsibility directly, `enabled` is redundant and can be removed or ignored during migration. Default config should set `multiSetup.mode: 'standalone'`.

10. Platform/mode support should be documented as behavior, not just storage.

   Paper/Folia can run as `standalone`, `master`, or `slave`. `standalone` and `master` own canonical storage and can provide local session/world context; `slave` reports sessions and can support local read-cache consumers such as PlaceholderAPI. Velocity/Bungee can run as `standalone` or `master`; running them as `slave` is not useful because proxies are normally the canonical coordinator and do not host PlaceholderAPI.

11. Server context should use a configured logical server name on Paper/Folia.

   Bukkit's `Server#getName()` describes the server implementation and is not a reliable logical network server name. Paper/Folia instances should read a configured server context name, for example `server.name` or `context.server`, and use that value in session rows and slave reports. The world context still comes from the player's current Bukkit world.

   In proxy canonical modes, the proxy can derive the logical server context from the backend connection name. Velocity should use the connected server info name from `ServerConnectedEvent`; Bungee should use the connected server info name from its post-connect server event. Proxies still use `global` as the world context.

## Risks / Trade-offs

- Existing custom `UnifiedStorage` implementations will fail to compile because `removePlayer` is removed and actor-aware adjustment methods are added. Mitigation: document this as a breaking storage API change and update test fakes.
- All-player total queries become more complex because they must merge session and adjustment aggregates. Mitigation: keep this logic inside table helpers and add database tests.
- Cleanup based on current `last_seen` semantics may be too broad if storage-only writes update the field. Mitigation: update activity tracking so real join/name events refresh the activity timestamp, while manual adjustments do not make inactive players active.
- The current normalized schema migration is not released yet. Mitigation: add the adjustment table to migration version 2 instead of creating a new version.
- Teleport, world-change, and respawn events can overlap on Paper/Folia. Mitigation: centralize context switching behind a compare-and-switch operation that ignores unchanged server/world context.
- Removing `multiSetup.enabled` is a config behavior change. Mitigation: default absent/invalid mode to `standalone` and update docs/setup examples.

## Migration Plan

1. Add the adjustment table and indexes to the current normalized schema migration version 2.
2. Create the adjustment table and indexes for player lookup and creation time during fresh startup and legacy migration.
3. Do not backfill existing `MANUAL_ADJUSTMENT` synthetic rows automatically; they remain valid session rows from older versions and continue contributing to totals.
4. New manual writes use the adjustment table.
5. Rollback means older plugin versions ignore the adjustment table and therefore will not include new adjustment rows in totals; operators should restore from backup before downgrading.

## Open Questions

- Should `last_seen` be renamed to `last_joined` in the schema, or should a new activity column be added while preserving `last_seen` for compatibility?
