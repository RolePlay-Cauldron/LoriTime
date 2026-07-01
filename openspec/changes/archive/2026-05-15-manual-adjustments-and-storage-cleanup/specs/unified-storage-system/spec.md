## MODIFIED Requirements

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

#### Scenario: Mode is authoritative
- **WHEN** storage responsibility is configured
- **THEN** the system SHALL use `multiSetup.mode` as the configured storage mode without requiring a separate enable flag

### Requirement: Unified storage service
The system SHALL provide a unified storage service that owns player identity, time queries, manual adjustments, player deletion, and reason-aware session persistence.

#### Scenario: Player identity and time are handled by one service
- **WHEN** runtime code needs to read or update player identity and time data
- **THEN** the runtime SHALL use the unified storage service instead of separate name and time storage services

#### Scenario: Manual adjustment persists with actor and reason
- **WHEN** runtime code adds or removes time outside normal session accumulation
- **THEN** the unified storage service SHALL persist a signed manual adjustment with a manual adjustment reason and actor information

#### Scenario: AFK adjustment persists with actor and AFK reason
- **WHEN** AFK handling removes time outside normal session accumulation
- **THEN** the unified storage service SHALL persist a signed adjustment with an AFK adjustment reason and actor information

#### Scenario: Console actor is auditable
- **WHEN** a manual adjustment is performed by the console
- **THEN** the unified storage service SHALL store the actor UUID as null and the actor name as a stable console label

#### Scenario: Manual adjustments contribute to totals
- **WHEN** runtime code queries a player's total time
- **THEN** the unified storage service SHALL include both persisted session durations and signed manual adjustment amounts

#### Scenario: Player deletion removes player-owned data
- **WHEN** runtime code deletes a player from storage
- **THEN** the unified storage service SHALL delete the player identity and associated session and adjustment data according to the database constraints

### Requirement: Context-aware session accumulation
The system SHALL accumulate active sessions with UUID, optional player name, server, world, start timestamp, and persistence reason context.

#### Scenario: Session start records context
- **WHEN** a player session starts on a platform that provides player and location context
- **THEN** the accumulator SHALL store the UUID, optional name, server, world, and start timestamp for that active session
- **THEN** the accumulator SHALL create a persisted active session row for that context

#### Scenario: Session stop persists context
- **WHEN** an active session stops
- **THEN** the accumulator SHALL update the active persisted session row with the stop timestamp and stop reason

#### Scenario: Session context changes
- **WHEN** an active player's server or world context changes
- **THEN** the accumulator SHALL update the previous active session row as a context-switch session chunk and start tracking the new context
- **THEN** the accumulator SHALL create a persisted active session row for the new context

#### Scenario: Paper or Folia player changes world
- **WHEN** a Paper or Folia player changes effective world
- **THEN** the system SHALL switch the active session context to the new world

#### Scenario: Paper or Folia records configured server context
- **WHEN** a Paper or Folia instance persists or reports session context
- **THEN** the system SHALL use the configured logical server name as the session server
- **THEN** the system SHALL use the player's current Bukkit world as the session world

#### Scenario: Proxy player changes backend server
- **WHEN** a proxy player connects to a different backend server
- **THEN** the system SHALL switch the active session context to the backend server name and fallback world `global`

#### Scenario: Duplicate context event is ignored
- **WHEN** a platform event reports a context that matches the active session's server and world
- **THEN** the system SHALL NOT create a new session row

#### Scenario: Flush preserves active sessions
- **WHEN** the online time cache is flushed
- **THEN** the accumulator SHALL update elapsed time for active session rows and continue tracking them from the same context
