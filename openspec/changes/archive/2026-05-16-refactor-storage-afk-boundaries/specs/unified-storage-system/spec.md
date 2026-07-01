## ADDED Requirements

### Requirement: Focused runtime storage contracts
The system SHALL expose focused runtime contracts for active session accumulation and durable storage operations so callers do not need concrete storage implementation types.

#### Scenario: Runtime code controls active sessions
- **WHEN** runtime code starts, stops, switches, flushes, or updates active player session context
- **THEN** the runtime SHALL use the active session accumulation contract
- **THEN** the runtime SHALL NOT require direct access to the concrete accumulating storage implementation

#### Scenario: Runtime code writes signed adjustments
- **WHEN** runtime code writes a manual or AFK signed time adjustment
- **THEN** the runtime SHALL use a durable storage or adjustment-writing contract that preserves actor and reason metadata
- **THEN** the runtime SHALL NOT depend on active session tracking implementation details

#### Scenario: Runtime code reads player identity or totals
- **WHEN** runtime code reads player names, UUIDs, or total time values
- **THEN** the runtime SHALL use durable storage read contracts
- **THEN** the runtime SHALL preserve existing total-time behavior that combines session durations and signed adjustments

### Requirement: Concrete accumulator implementation is encapsulated
The system SHALL keep the concrete accumulating storage implementation behind runtime contracts except where construction or tests require the implementation.

#### Scenario: Storage manager constructs default storage
- **WHEN** default storage is loaded for a standalone or master instance
- **THEN** the storage manager SHALL construct the database-backed durable storage and accumulating storage implementation
- **THEN** external callers SHALL receive focused runtime contracts instead of needing the concrete accumulating storage type

#### Scenario: Custom storage is injected
- **WHEN** custom storage is injected for tests or external integration
- **THEN** the injection path SHALL support the same focused runtime contracts used by production callers
- **THEN** the injected storage SHALL preserve current close and reload ownership behavior

### Requirement: Shared session context defaults
The system SHALL provide shared fallback session context values for code paths that cannot observe a more specific server or world context.

#### Scenario: Platform lacks world context
- **WHEN** a session-related code path cannot observe the current world
- **THEN** the system SHALL use the shared fallback world context

#### Scenario: Platform lacks server context
- **WHEN** a session-related code path cannot observe a canonical server context
- **THEN** the system SHALL use the shared fallback server context

#### Scenario: Existing platform context remains authoritative
- **WHEN** a platform listener observes a specific backend server or world context
- **THEN** the observed context SHALL remain authoritative over shared fallback values
