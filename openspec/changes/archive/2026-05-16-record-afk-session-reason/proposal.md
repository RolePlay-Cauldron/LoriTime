## Why

AFK handling currently stops or ends player sessions using the normal `PLAYER_LEAVE` reason in cases that are actually caused by AFK state or AFK kick enforcement. This makes session history less accurate and hides AFK-driven session boundaries from storage consumers.

## What Changes

- Add a dedicated session entry reason for AFK-ended sessions, for example `PLAYER_AFK`.
- When AFK handling stops counting for a player with the stop-count bypass permission, persist the active session stop with the AFK session reason instead of `PLAYER_LEAVE`.
- When AFK auto-kick removes a player from the network, ensure the resulting active session stop is persisted with the AFK session reason instead of `PLAYER_LEAVE`.
- Keep `AFK_ADJUSTMENT` for signed time-removal adjustment rows; the new reason is for session row lifecycle, not manual adjustment rows.

## Capabilities

### New Capabilities

### Modified Capabilities
- `unified-storage-system`: Persist AFK-caused session stops with a dedicated AFK session reason.

## Impact

- Affected modules: common storage reason enum, AFK handling, time accumulator leave handling, proxy/Paper leave listeners as needed, tests.
- Storage schema does not need to change because reasons are stored as enum names in existing reason fields.
- Existing session rows with `PLAYER_LEAVE` remain unchanged.
