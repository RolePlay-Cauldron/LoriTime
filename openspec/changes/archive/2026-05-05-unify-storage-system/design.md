## Context

LoriTime already migrated runtime persistence to normalized database tables for players, servers, worlds, and time entries. The application runtime still exposes the previous split storage model: `NameStorage` owns player names, `TimeStorage` owns aggregate time operations, and `AccumulatingTimeStorage` wraps `TimeStorage` while tracking only a player UUID and start timestamp.

That model no longer matches the database shape. Session rows need a player, server, world, timestamps, and a persistence reason. Multi-setup also needs to distinguish canonical writes from local reads: a Paper/Folia slave needs PlaceholderAPI values locally, but it must not become a second storage writer.

## Goals / Non-Goals

**Goals:**
- Replace split name/time runtime ownership with one unified storage service.
- Make accumulation context-aware so persisted rows can carry player, server, world, timestamp, and reason data.
- Keep only `standalone`, `master`, and `slave` as user-facing modes.
- Preserve multi-setup by using explicit slave-to-master session events and master-to-slave read snapshots.
- Support Paper/Folia placeholders on slaves through a local read cache.
- Allow breaking API changes where they simplify the storage model.

**Non-Goals:**
- Redesign all commands or command output.
- Add proxy PlaceholderAPI support.
- Add more than three user-facing modes.
- Require every platform to support every feature; platform modules decide feature registration from their capabilities.
- Replace the normalized database schema created by the previous migration work.

## Decisions

### Use one mode enum for storage responsibility

The runtime will expose only `standalone`, `master`, and `slave`. These modes describe storage responsibility:

- `standalone`: local canonical storage reader and writer.
- `master`: canonical storage reader and writer for a multi-setup.
- `slave`: remote client that reports writes/events to a master and maintains local read projections.

Alternative considered: separate modes for proxy master, Paper master, Paper slave, proxy standalone, and similar platform-specific combinations. That would encode platform capability into configuration and make mode selection harder to reason about. Platform modules should instead decide what they can register for the selected storage responsibility.

### Replace split storages with a unified storage service

The runtime storage service will own player identity, time totals, manual adjustments, deletion, and session persistence. The current `DatabaseTimeAndNameStorage` is already close to this concept but still implements legacy interfaces and writes fallback scope for most paths.

Alternative considered: keep `NameStorage`, `TimeStorage`, and `ReasonAwareTimeStorage` as public wrappers around the unified implementation. Because this release can be breaking and the old surface does not represent server/world context, compatibility wrappers are not required unless needed temporarily during the refactor.

### Make the accumulator session-context aware

The accumulator will store active session context per UUID, not only start time. A session context includes UUID, optional name, server, world, and start timestamp. Start, stop, context switch, and flush operations will persist session chunks with explicit reasons.

Alternative considered: keep computing durations and ask storage to infer join/leave timestamps from duration. That loses event timestamp precision and keeps server/world context outside the accumulator, so it does not fit the normalized schema.

### Redefine multi-setup around events and read snapshots

Slaves will not implement canonical storage. They will report session events or session chunks to the master and maintain a local read cache populated from master responses. This replaces `SlavedTimeStorageCache` acting as a `TimeStorage`.

Alternative considered: let subservers write directly to the same database. That simplifies proxy messaging but creates duplicate-writer risk and makes deployment behavior depend on every node having consistent database configuration.

### Use fallback scope where platform context is unavailable

Proxy platforms can observe network joins/leaves but cannot provide PlaceholderAPI and do not know a Paper/Folia world. When a proxy operates in a mode that writes sessions without subserver world context, it will use configured fallback server/world values such as `default/global`.

Alternative considered: disallow proxy standalone/master storage writing. That would remove an existing deployment shape and is unnecessary because the normalized schema already supports fallback scope.

## Risks / Trade-offs

- Breaking API surface -> Keep the new service names and responsibilities clear, and avoid carrying obsolete interfaces longer than necessary.
- Multi-setup message changes can break mixed-version networks -> Treat the protocol as part of the breaking change and fail loudly when slave/master protocol messages are unsupported.
- Slave placeholder values can be stale -> Provide explicit refresh requests and periodic snapshot updates from master to slave.
- Context switch handling can double count sessions -> Make accumulator transitions atomic per UUID and add focused tests around join, leave, flush, and context switch flows.
- Proxy world fallback loses detail -> Document that only Paper/Folia context can provide world-aware rows.

## Migration Plan

1. Introduce the new storage mode model and unified service interfaces.
2. Move database storage construction to the unified service while continuing to use the existing normalized schema.
3. Refactor the accumulator to operate on session contexts and persist explicit session chunks.
4. Update platform listeners to pass the context each platform can provide.
5. Replace storage-mirroring plugin messages with session reporting and read snapshot messages.
6. Wire Paper/Folia placeholders to local storage in standalone mode and to the remote read cache in slave mode.
7. Remove obsolete split storage implementations and accessors after call sites compile against the new model.

Rollback is a code rollback. The database schema remains the current normalized schema, so this change should not require a destructive data migration.

## Open Questions

- Should the new unified storage service expose aggregate-only methods for commands immediately, or should commands move through a separate query service later?
- What exact protocol version field should be included in multi-setup messages to reject mixed master/slave versions cleanly?
