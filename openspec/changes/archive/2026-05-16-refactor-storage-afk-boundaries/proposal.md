## Why

Storage and AFK handling now have the right broad responsibilities, but several implementation boundaries remain too wide or implicit. Tightening these boundaries will make future storage, proxy, and AFK changes easier to reason about without changing player-visible behavior.

## What Changes

- Split storage-facing contracts so callers depend on focused interfaces instead of the full unified storage implementation surface.
- Prefer `TimeAccumulator` and focused storage APIs over exposing `AccumulatingTimeStorage` as a concrete runtime dependency.
- Make AFK state transitions explicit so AFK detection, enforcement, session stopping, resume handling, and AFK kick marking have a clearer contract.
- Version the AFK plugin messaging payload and replace string boolean status messages with typed protocol values.
- Centralize fallback session context values such as default server and global world so AFK resume and platform listeners do not hard-code them independently.
- Preserve existing config keys, storage schema behavior, and player-visible AFK behavior.

## Capabilities

### New Capabilities
- `afk-session-coordination`: Covers AFK transition decisions, master/slave AFK message protocol behavior, and how AFK side effects coordinate with active session accumulation.

### Modified Capabilities
- `unified-storage-system`: Refines the storage and accumulation API boundary while preserving the existing unified storage behavior and session persistence requirements.

## Impact

- Affected common code: `UnifiedStorage`, `TimeAccumulator`, `AccumulatingTimeStorage`, `DataStorageManager`, `LoriTimePlugin`, `AfkStatusProvider`, `AfkHandling`, `MasteredAfkPlayerHandling`, and `PluginMessaging`.
- Affected platform code: Paper slave AFK messaging and Paper/Bungee/Velocity session listener usage of accumulator APIs and context defaults.
- Tests should cover storage API usage through focused contracts, AFK transition outcomes, versioned AFK plugin messages, and unchanged session reason behavior.
- No database schema change is intended.
- No configuration migration is intended.
