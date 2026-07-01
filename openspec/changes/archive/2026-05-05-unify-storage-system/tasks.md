## 1. Storage Mode And Service Contracts

- [x] 1.1 Add a `standalone` / `master` / `slave` storage mode model and wire configuration parsing to it.
- [x] 1.2 Define the unified storage service contract for player identity, time totals, manual adjustments, player deletion, and session persistence.
- [x] 1.3 Define session context/value types for UUID, optional name, server, world, timestamps, and persistence reason.
- [x] 1.4 Replace public runtime accessors for split name/time storages with unified storage and accumulator accessors.

## 2. Database Storage Implementation

- [x] 2.1 Refactor `DatabaseTimeAndNameStorage` into the unified storage implementation or replace it with an equivalent unified database storage.
- [x] 2.2 Update database writes to persist explicit server/world context instead of always using fallback `default/global` scope.
- [x] 2.3 Keep aggregate total queries and player lookup/update operations available through the unified storage service.
- [x] 2.4 Update `DataStorageManager` to construct unified storage services for SQLite, MySQL, and MariaDB backends.

## 3. Context-Aware Accumulation

- [x] 3.1 Refactor the accumulator to track active session contexts per player instead of only start timestamps.
- [x] 3.2 Implement start, stop, context switch, flush, and close behavior using explicit timestamps and persistence reasons.
- [x] 3.3 Preserve manual time adjustment behavior for online and offline players through the unified storage service.
- [x] 3.4 Add focused tests for join/leave, flush, shutdown flush, manual adjustment, and context switch accumulation.

## 4. Platform Integration

- [x] 4.1 Update Paper/Folia-compatible listeners to pass player name, server, world, and timestamp context into the accumulator.
- [x] 4.2 Update Velocity and Bungee listeners to pass available context and fallback world scope where no world context exists.
- [x] 4.3 Update platform bootstrapping so feature registration is derived from platform capabilities and selected storage mode.
- [x] 4.4 Keep command behavior compiling against the new storage API without redesigning command UX.

## 5. Multi-Setup Messaging

- [x] 5.1 Replace storage-mirroring plugin messages with slave-to-master session/write event messages.
- [x] 5.2 Add master handling that validates remote session/write events and persists them through unified storage.
- [x] 5.3 Add master-to-slave read response or snapshot messages for player totals.
- [x] 5.4 Add protocol/version handling so incompatible master/slave message formats fail clearly.

## 6. Slave Read Cache And Placeholders

- [x] 6.1 Replace `SlavedTimeStorageCache` with a slave read cache that does not implement canonical storage.
- [x] 6.2 Add a TODO reminder for updating Paper/Folia PlaceholderAPI integration to use local storage in `standalone` mode and the slave read cache in `slave` mode.
- [x] 6.3 Add a TODO reminder for deterministic placeholder behavior on slave cache misses while requesting refreshes from the master.
- [x] 6.4 Ensure Velocity and Bungee do not register PlaceholderAPI placeholders in any mode.

## 7. Cleanup And Verification

- [x] 7.1 Remove obsolete split storage implementations and compatibility interfaces once all call sites use the unified model.
- [x] 7.2 Update storage documentation for the three modes and platform capability behavior.
- [x] 7.3 Update or add tests for storage mode construction and multi-setup message handling.
- [x] 7.4 Run the relevant Maven test suite and fix regressions.
