## ADDED Requirements

### Requirement: Scoped time totals
The unified storage service SHALL support player time totals for global, server, and world scopes.

#### Scenario: Global total includes all scoped data
- **WHEN** runtime code queries a player's global total time
- **THEN** the unified storage service SHALL include all persisted session durations for that player
- **THEN** the unified storage service SHALL include all signed adjustment amounts for that player regardless of adjustment scope

#### Scenario: Server total includes matching server data
- **WHEN** runtime code queries a player's total time for a server scope
- **THEN** the unified storage service SHALL include persisted session durations for that player on that server
- **THEN** the unified storage service SHALL include server-scoped adjustments for that server
- **THEN** the unified storage service SHALL include world-scoped adjustments for worlds on that server
- **THEN** the unified storage service SHALL NOT include global adjustments

#### Scenario: World total includes exact world data
- **WHEN** runtime code queries a player's total time for a world scope
- **THEN** the unified storage service SHALL include persisted session durations for that player in that exact server and world
- **THEN** the unified storage service SHALL include world-scoped adjustments for that exact server and world
- **THEN** the unified storage service SHALL NOT include global or server-scoped adjustments

#### Scenario: Active session contributes to matching scoped total
- **WHEN** runtime code queries a player's scoped total while that player has an active session
- **THEN** the active session elapsed time SHALL contribute to the result only when the active session context matches the requested scope

### Requirement: Scoped manual adjustments
The unified storage service SHALL persist signed time adjustments with explicit global, server, or world scope.

#### Scenario: Global adjustment persists without server or world scope
- **WHEN** runtime code writes a signed adjustment without a scoped target
- **THEN** the unified storage service SHALL persist the adjustment as global
- **THEN** the adjustment SHALL preserve actor and reason metadata

#### Scenario: Server adjustment persists with server scope
- **WHEN** runtime code writes a signed adjustment for a server scope
- **THEN** the unified storage service SHALL persist the adjustment with that server scope
- **THEN** the adjustment SHALL preserve actor and reason metadata

#### Scenario: World adjustment persists with world scope
- **WHEN** runtime code writes a signed adjustment for a world scope
- **THEN** the unified storage service SHALL persist the adjustment with that server and world scope
- **THEN** the adjustment SHALL preserve actor and reason metadata

### Requirement: Scoped adjustment schema baseline
The database baseline schema SHALL represent manual adjustment scope directly and SHALL NOT rely on a compatibility migration for unscoped adjustment rows.

#### Scenario: Fresh database creates scoped adjustment columns
- **WHEN** the database schema is initialized for a fresh installation
- **THEN** the time adjustment table SHALL include a scope discriminator
- **THEN** the time adjustment table SHALL include nullable server and world references for scoped adjustments

#### Scenario: Baseline replaces legacy normalized adjustment shape
- **WHEN** this change is implemented
- **THEN** the normalized schema baseline SHALL be edited to include scoped adjustments
- **THEN** the implementation SHALL NOT add a separate legacy compatibility migration for existing unscoped adjustment rows

### Requirement: AFK adjustments preserve scope
AFK time-removal adjustments SHALL be written in the player's current world scope when current server and world context are available.

#### Scenario: AFK removal with current world context
- **WHEN** AFK handling removes counted online time for a player with known current server and world context
- **THEN** the unified storage service SHALL persist the signed `AFK_ADJUSTMENT` in that world scope

#### Scenario: AFK removal without specific context
- **WHEN** AFK handling removes counted online time for a player without specific current server or world context
- **THEN** the unified storage service SHALL persist the signed `AFK_ADJUSTMENT` using shared fallback context values

## MODIFIED Requirements

### Requirement: Unified storage service
The system SHALL provide a unified storage service that owns player identity, scoped time queries, scoped manual adjustments, player deletion, and reason-aware session persistence.

#### Scenario: Player identity and time are handled by one service
- **WHEN** runtime code needs to read or update player identity and time data
- **THEN** the runtime SHALL use the unified storage service instead of separate name and time storage services

#### Scenario: Manual adjustment persists with actor, reason, and scope
- **WHEN** runtime code adds or removes time outside normal session accumulation
- **THEN** the unified storage service SHALL persist a signed manual adjustment with a manual adjustment reason, actor information, and explicit scope

#### Scenario: AFK adjustment persists with actor, AFK reason, and scope
- **WHEN** AFK handling removes time outside normal session accumulation
- **THEN** the unified storage service SHALL persist a signed adjustment with an AFK adjustment reason, actor information, and explicit scope

#### Scenario: Console actor is auditable
- **WHEN** a manual adjustment is performed by the console
- **THEN** the unified storage service SHALL store the actor UUID as null and the actor name as a stable console label

#### Scenario: Manual adjustments contribute to totals
- **WHEN** runtime code queries a player's total time
- **THEN** the unified storage service SHALL include both persisted session durations and signed manual adjustment amounts that match the requested scope

#### Scenario: Player deletion removes player-owned data
- **WHEN** runtime code deletes a player from storage
- **THEN** the unified storage service SHALL delete the player identity and associated session and adjustment data according to the database constraints

### Requirement: Focused runtime storage contracts
The system SHALL expose focused runtime contracts for active session accumulation and durable storage operations so callers do not need concrete storage implementation types.

#### Scenario: Runtime code controls active sessions
- **WHEN** runtime code starts, stops, switches, flushes, or updates active player session context
- **THEN** the runtime SHALL use the active session accumulation contract
- **THEN** the runtime SHALL NOT require direct access to the concrete accumulating storage implementation

#### Scenario: Runtime code writes signed adjustments
- **WHEN** runtime code writes a manual or AFK signed time adjustment
- **THEN** the runtime SHALL use a durable storage or adjustment-writing contract that preserves actor, reason, and scope metadata
- **THEN** the runtime SHALL NOT depend on active session tracking implementation details

#### Scenario: Runtime code reads player identity or totals
- **WHEN** runtime code reads player names, UUIDs, or total time values
- **THEN** the runtime SHALL use durable storage read contracts
- **THEN** the runtime SHALL preserve total-time behavior that combines matching session durations and signed adjustments for the requested scope
