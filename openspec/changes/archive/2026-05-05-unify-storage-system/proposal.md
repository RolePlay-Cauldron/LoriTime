## Why

LoriTime now persists normalized player, server, world, and time rows, but the runtime storage API still exposes the old split `NameStorage` and `TimeStorage` model. This keeps new database context hidden behind fallback values and makes multi-setup behavior depend on storage mirroring instead of explicit session events.

## What Changes

- **BREAKING** Replace the split runtime storage surface with one unified storage service for player identity, time queries, manual adjustments, deletion, and reason-aware session persistence.
- **BREAKING** Refactor the accumulator to track session context instead of only `UUID -> start timestamp`; start, stop, context switch, and flush operations will include player, server, world, timestamp, and persistence reason context.
- **BREAKING** Keep only three user-facing storage modes: `standalone`, `master`, and `slave`.
- Redefine multi-setup around slave-to-master session events and master-to-slave read snapshots instead of making slave caches implement the canonical time storage API.
- Provide a slave-side read cache so Paper/Folia slaves can serve PlaceholderAPI values locally while the master remains the canonical storage writer.
- Let platform modules derive feature registration from platform capabilities and selected mode; modes define storage responsibility, not whether placeholders, world context, or proxy features are available.
- Keep command updates out of this change unless needed to compile against the new storage API.

## Capabilities

### New Capabilities
- `unified-storage-system`: Defines the unified storage API, context-aware accumulation, storage modes, and multi-setup read/write responsibilities.

### Modified Capabilities
- `storage-migration-preflight`: Runtime storage loading must continue to use the existing migrated database schema while constructing the new unified storage system instead of split name/time storages.

## Impact

- Common storage APIs: `NameStorage`, `TimeStorage`, `ReasonAwareTimeStorage`, `TimeAccumulator`, `AccumulatingTimeStorage`, and `DataStorageManager`.
- Database storage implementation and table helpers that currently write fallback `default/global` time rows.
- Platform listeners that start/stop accumulation on Paper, Folia-compatible Paper, Velocity, and Bungee.
- Multi-setup plugin messaging between master and slave instances.
- Paper PlaceholderAPI integration and the current `SlavedTimeStorageCache`.
- Public API compatibility for external plugins; breaking changes are acceptable for this release.
