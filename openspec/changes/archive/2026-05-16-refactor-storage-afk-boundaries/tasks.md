## 1. Storage Contract Boundaries

- [x] 1.1 Identify current callers of `getAccumulatingStorage()`, `getAccumulator()`, and broad `UnifiedStorage` methods.
- [x] 1.2 Introduce focused runtime contracts for active session accumulation and signed adjustment writes while preserving existing storage behavior.
- [x] 1.3 Update `DataStorageManager` and `LoriTimePlugin` accessors so runtime callers can depend on focused contracts instead of `AccumulatingTimeStorage`.
- [x] 1.4 Migrate storage and AFK callers away from direct concrete accumulator access where construction is not required.
- [x] 1.5 Add or update tests proving totals, signed adjustments, session start/stop, server/world switch, flush, and close behavior remain unchanged.

## 2. Session Context Defaults

- [x] 2.1 Add a shared source for fallback server and world context values.
- [x] 2.2 Replace hard-coded fallback context literals in AFK resume and platform session paths with the shared defaults.
- [x] 2.3 Add tests proving observed platform context overrides fallback values and fallback values are used only when no specific context is available.

## 3. AFK Transition Model

- [x] 3.1 Introduce a small internal AFK transition model for AFK start, AFK resume, and AFK kick outcomes.
- [x] 3.2 Refactor `AfkStatusProvider` and mastered AFK handling so player AFK state changes and side effects flow through explicit transition decisions.
- [x] 3.3 Preserve current AFK permission behavior for time removal, stop-count bypass, kick bypass, and announcement visibility.
- [x] 3.4 Preserve current session reasons for AFK stop, AFK kick disconnect, AFK adjustment rows, and normal player leave.
- [x] 3.5 Add or update tests for AFK start, resume, stop-count bypass, auto-kick marking, and unchanged player-visible messages.

## 4. Versioned AFK Messaging

- [x] 4.1 Remove legacy unversioned AFK string-boolean payload support from the master decode path.
- [x] 4.2 Define AFK protocol version constants and typed transition values for slave-to-master messages.
- [x] 4.3 Update Paper slave AFK message encoding to send the versioned AFK payload.
- [x] 4.4 Update master AFK message decoding to reject unsupported protocol versions and invalid transition values with warnings.
- [x] 4.5 Add tests for supported AFK start/resume messages, unsupported protocol versions, invalid transitions, legacy unversioned payloads, and malformed payloads.

## 5. Verification

- [x] 5.1 Run focused common tests for storage accumulation, database storage, plugin messaging, AFK status provider, and mastered AFK handling.
- [x] 5.2 Run compile checks for Paper, Bungee, and Velocity modules.
- [x] 5.3 Run `mvn verify`.
- [x] 5.4 Review public API impact and remove or deprecate redundant concrete storage access only when callers have been migrated.
