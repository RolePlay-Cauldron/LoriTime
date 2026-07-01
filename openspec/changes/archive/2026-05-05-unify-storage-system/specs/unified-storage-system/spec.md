## ADDED Requirements

### Requirement: Storage mode responsibilities
The system SHALL expose only `standalone`, `master`, and `slave` as user-facing storage modes, and each mode SHALL define storage responsibility rather than platform feature availability.

#### Scenario: Standalone owns local storage
- **WHEN** an instance runs in `standalone` mode
- **THEN** the instance SHALL use local canonical storage for reads and writes

#### Scenario: Master owns canonical multi-setup storage
- **WHEN** an instance runs in `master` mode
- **THEN** the instance SHALL own canonical storage writes and answer read requests from slave instances

#### Scenario: Slave delegates canonical storage
- **WHEN** an instance runs in `slave` mode
- **THEN** the instance SHALL NOT write canonical storage directly and SHALL communicate storage writes or session events to a master

### Requirement: Platform capability based feature registration
The system SHALL derive feature registration from platform capabilities and selected storage mode without adding platform-specific user-facing modes.

#### Scenario: Paper or Folia slave provides placeholders
- **WHEN** a Paper or Folia-compatible instance runs in `slave` mode and PlaceholderAPI is available
- **THEN** the instance SHALL serve placeholder values from its local read cache

#### Scenario: Proxy does not provide PlaceholderAPI placeholders
- **WHEN** a Velocity or Bungee instance runs in any storage mode
- **THEN** the instance SHALL NOT register PlaceholderAPI placeholders

#### Scenario: Platform lacks world context
- **WHEN** a platform cannot provide world context for a session write
- **THEN** the system SHALL use configured fallback world context for the persisted session

### Requirement: Unified storage service
The system SHALL provide a unified storage service that owns player identity, time queries, manual adjustments, player deletion, and reason-aware session persistence.

#### Scenario: Player identity and time are handled by one service
- **WHEN** runtime code needs to read or update player identity and time data
- **THEN** the runtime SHALL use the unified storage service instead of separate name and time storage services

#### Scenario: Manual adjustment persists with reason
- **WHEN** runtime code adds or removes time outside normal session accumulation
- **THEN** the unified storage service SHALL persist the adjustment with a manual adjustment reason

#### Scenario: Player deletion removes player-owned data
- **WHEN** runtime code deletes a player from storage
- **THEN** the unified storage service SHALL remove the player identity and associated time data according to the database constraints

### Requirement: Context-aware session accumulation
The system SHALL accumulate active sessions with UUID, optional player name, server, world, start timestamp, and persistence reason context.

#### Scenario: Session start records context
- **WHEN** a player session starts on a platform that provides player and location context
- **THEN** the accumulator SHALL store the UUID, optional name, server, world, and start timestamp for that active session

#### Scenario: Session stop persists context
- **WHEN** an active session stops
- **THEN** the accumulator SHALL persist a session chunk with the stored context, stop timestamp, and stop reason

#### Scenario: Session context changes
- **WHEN** an active player's server or world context changes
- **THEN** the accumulator SHALL persist the previous context as a context-switch session chunk and start tracking the new context

#### Scenario: Flush preserves active sessions
- **WHEN** the online time cache is flushed
- **THEN** the accumulator SHALL persist elapsed time for active sessions and continue tracking them from the flush timestamp

### Requirement: Multi-setup session reporting
The system SHALL use explicit slave-to-master session reporting for canonical writes in multi-setup deployments.

#### Scenario: Slave reports session write
- **WHEN** a slave observes a session start, stop, context switch, flush, or manual adjustment that must affect canonical storage
- **THEN** the slave SHALL send the corresponding session or write event to the master

#### Scenario: Master persists slave event
- **WHEN** a master receives a valid session or write event from a slave
- **THEN** the master SHALL persist it through the unified storage service

#### Scenario: Slave does not write database directly
- **WHEN** an instance runs in `slave` mode
- **THEN** the instance SHALL avoid direct canonical database writes for storage data

### Requirement: Slave read cache
The system SHALL provide a slave-side read cache populated by master read responses or snapshots for local read consumers.

#### Scenario: Slave requests player total
- **WHEN** a slave needs a player's current total for a local consumer
- **THEN** the slave SHALL request the value from the master and update its local read cache from the response

#### Scenario: Placeholder reads cache
- **WHEN** a Paper or Folia-compatible slave renders a placeholder for a player with a cached value
- **THEN** the placeholder SHALL use the local read cache value without directly querying canonical storage

#### Scenario: Cache miss has fallback behavior
- **WHEN** a slave renders a placeholder for a player without a cached value
- **THEN** the system SHALL request a refresh from the master and return a deterministic fallback value until data is available
