# unified-storage-system Specification

## Purpose
Defines storage responsibility modes, unified player time persistence, active session accumulation, multi-setup storage messaging, slave read caching, and storage history behavior.
## Requirements
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

### Requirement: Multi-setup AFK handling
The system SHALL keep AFK detection and AFK side effects coordinated across multi-setup instances while using the existing AFK configuration keys.

#### Scenario: Paper or Folia slave detects AFK
- **WHEN** a Paper or Folia-compatible instance runs in `slave` mode with `afk.enabled` enabled
- **THEN** the instance SHALL observe local player activity and run AFK idle detection
- **THEN** the instance SHALL send AFK state changes to the master over the configured AFK plugin messaging channel

#### Scenario: Proxy master applies AFK side effects
- **WHEN** a Velocity or Bungee instance runs in `master` mode with `afk.enabled` enabled and receives a valid AFK state message for an online player
- **THEN** the proxy master SHALL apply configured AFK side effects including time adjustment, kick handling, and announcements

#### Scenario: Proxy master does not run idle detection
- **WHEN** a Velocity or Bungee instance runs as a proxy
- **THEN** the instance SHALL NOT run the repeated AFK idle detection loop

#### Scenario: Existing AFK settings remain authoritative
- **WHEN** AFK handling is configured
- **THEN** the system SHALL use the existing `afk.enabled`, `afk.after`, `afk.removeTime`, `afk.autoKick`, and `afk.repeatCheck` settings
- **THEN** the system SHALL NOT require separate detection or enforcement AFK settings

#### Scenario: AFK reload refreshes scheduling
- **WHEN** runtime configuration is reloaded and the AFK feature has been initialized
- **THEN** the system SHALL reload AFK configuration values and restart AFK idle detection according to the current settings

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

### Requirement: AFK session stop reason
The system SHALL persist active session stops caused by AFK handling with dedicated AFK session reasons that are distinct from normal player leave and AFK adjustment rows.

#### Scenario: AFK without stop-count bypass stops active session as AFK
- **WHEN** a player becomes AFK and does not have the stop-count bypass permission
- **THEN** the active session SHALL be stopped with `PLAYER_AFK`
- **THEN** the active session SHALL NOT be stopped with `PLAYER_LEAVE`

#### Scenario: Stop-count bypass keeps active session counting
- **WHEN** a player becomes AFK and has the stop-count bypass permission
- **THEN** the active session SHALL continue counting
- **THEN** the active session SHALL NOT be stopped with `PLAYER_AFK`
- **THEN** the active session SHALL NOT be stopped with `PLAYER_AFK_KICK`

#### Scenario: AFK kick stops active session with AFK kick reason
- **WHEN** a player is kicked by AFK auto-kick enforcement
- **THEN** the active session SHALL be stopped with `PLAYER_AFK_KICK`
- **THEN** the active session SHALL NOT be stopped with `PLAYER_AFK`
- **THEN** the active session SHALL NOT be stopped with `PLAYER_LEAVE`

#### Scenario: AFK adjustment remains adjustment reason
- **WHEN** AFK handling removes counted online time through a signed adjustment
- **THEN** the adjustment row SHALL continue to use `AFK_ADJUSTMENT`
- **THEN** the session row SHALL use an AFK session reason only when the active session lifecycle is stopped because of AFK handling

#### Scenario: Normal leave remains player leave
- **WHEN** a player disconnects without an AFK-caused stop or AFK kick marker
- **THEN** the active session SHALL be stopped with `PLAYER_LEAVE`

### Requirement: Context-aware session accumulation
The system SHALL accumulate active sessions with UUID, optional player name, canonical server, current world context, start timestamp, and persistence reason context.

#### Scenario: Session start records context
- **WHEN** a player session starts on a platform that owns canonical session storage
- **THEN** the accumulator SHALL store the UUID, optional name, canonical server, current world context, and start timestamp for that active session
- **THEN** the accumulator SHALL create a persisted active session row for that context

#### Scenario: Session stop persists context
- **WHEN** an active session stops
- **THEN** the accumulator SHALL update the active persisted session row with the stop timestamp and stop reason

#### Scenario: Server context changes
- **WHEN** an active player's canonical server context changes
- **THEN** the accumulator SHALL update the previous active session row with `SERVER_SWITCH`
- **THEN** the accumulator SHALL create and track a persisted active session row for the new server context

#### Scenario: World context changes on storage-owning Paper or Folia
- **WHEN** a Paper or Folia standalone or master instance observes a player changing effective world
- **THEN** the system SHALL update the previous active session row with `WORLD_SWITCH`
- **THEN** the system SHALL create and track a persisted active session row for the new world context

#### Scenario: Paper or Folia slave reports current world context
- **WHEN** a Paper or Folia slave observes a player's current effective world without a world-change event
- **THEN** the slave SHALL report the current world context to the master without sending a completed session row

#### Scenario: Master applies slave world context update to active session
- **WHEN** a proxy master receives a valid world context update for a player with an active master-owned session
- **THEN** the master SHALL apply the reported world to that active session context without creating a new canonical time row

#### Scenario: Master applies slave world switch to active session
- **WHEN** a proxy master receives a valid world switch for a player with an active master-owned session
- **THEN** the master SHALL update the previous active session row with `WORLD_SWITCH`
- **THEN** the master SHALL create and track a persisted active session row for the new world context

#### Scenario: Master ignores orphan slave world context
- **WHEN** a proxy master receives a world context update or world switch for a player without an active master-owned session
- **THEN** the master SHALL NOT create a standalone canonical time row from that world context message

#### Scenario: Paper or Folia records configured server context outside proxy ownership
- **WHEN** a Paper or Folia standalone or master instance persists local session context
- **THEN** the system SHALL use the configured logical server name as the session server
- **THEN** the system SHALL use the player's current Bukkit world as the session world

#### Scenario: Proxy player changes backend server
- **WHEN** a proxy player connects to a different backend server
- **THEN** the system SHALL switch the active session context to the backend server name as the canonical server
- **THEN** the system SHALL keep the latest known Paper or Folia world context for that active session when available, or fallback world `global` otherwise

#### Scenario: Duplicate context event is ignored
- **WHEN** a platform event reports a context that matches the active session's canonical server and current world
- **THEN** the system SHALL NOT create a new session row

#### Scenario: Flush preserves active sessions
- **WHEN** the online time cache is flushed
- **THEN** the accumulator SHALL update elapsed time for active session rows and continue tracking them from the same context

### Requirement: Multi-setup session reporting
The system SHALL use explicit slave-to-master reporting only for data that the slave owns, while proxy masters SHALL own canonical session row lifecycle in proxy multi-setup deployments.

#### Scenario: Proxy master owns canonical session lifecycle
- **WHEN** a Velocity or Bungee instance runs in `master` mode
- **THEN** the proxy master SHALL create, update, switch, flush, and stop canonical session rows from proxy-observed player lifecycle events

#### Scenario: Canonical server is proxy backend name
- **WHEN** a proxy master persists or updates a canonical session row for a player connected to a backend server
- **THEN** the session server SHALL be the backend server name reported by the proxy
- **THEN** Paper or Folia `server.name` SHALL NOT create a separate canonical server entry for that same proxy-owned session

#### Scenario: Slave reports world context observation
- **WHEN** a Paper or Folia slave observes join, periodic update, or leave world context
- **THEN** the slave SHALL report a current world context update
- **THEN** the slave SHALL NOT cause the master to insert a new canonical time row for that observation

#### Scenario: Slave reports world switch
- **WHEN** a Paper or Folia slave observes a player world-change event
- **THEN** the slave SHALL report a world switch message
- **THEN** the master SHALL split the active canonical session row for that world switch when the player has an active master-owned session

#### Scenario: Slave autoflush does not create rows
- **WHEN** a Paper or Folia slave reaches its periodic update interval
- **THEN** the slave SHALL NOT cause the master to insert a new canonical time row

#### Scenario: Master persists slave manual writes
- **WHEN** a master receives a valid manual storage write or adjustment event from a slave
- **THEN** the master SHALL persist it through the unified storage service

#### Scenario: Slave does not write database directly
- **WHEN** an instance runs in `slave` mode
- **THEN** the instance SHALL avoid direct canonical database writes for storage data

### Requirement: Runtime database access avoids platform main-thread paths
The system SHALL keep database-backed runtime storage reads and writes off platform main-thread request and tick execution paths while retaining synchronous lifecycle startup migration and storage initialization.

#### Scenario: Runtime database work is scheduled off main thread
- **WHEN** normal runtime code needs to perform a database-backed storage read or write
- **THEN** the storage call SHALL execute from an asynchronous or otherwise non-main-thread runtime path

#### Scenario: Startup migration stays synchronous
- **WHEN** LoriTime enables canonical storage and performs required database migration or storage initialization before normal runtime ticking uses storage
- **THEN** the lifecycle operation MAY remain synchronous

#### Scenario: Blocking storage contracts remain internal
- **WHEN** runtime orchestration uses `UnifiedStorage`, focused storage contracts, or the accumulator
- **THEN** those contracts MAY remain synchronous blocking contracts
- **THEN** runtime thread placement SHALL protect platform main-thread paths from database latency

### Requirement: Synchronous runtime data surfaces avoid database requests
Runtime surfaces that must return immediate synchronous values SHALL not issue database-backed storage operations from their request paths.

#### Scenario: Tab completion avoids stored-name query on request path
- **WHEN** a command tab completion request needs player name suggestions
- **THEN** the completion path SHALL use cached or non-database suggestion data
- **THEN** the completion path SHALL NOT query database-backed stored names directly

#### Scenario: Placeholder render avoids direct database total query
- **WHEN** a Paper or Folia-compatible instance renders an online-time placeholder during normal runtime
- **THEN** the placeholder path SHALL use cached or non-database time data with deterministic fallback behavior
- **THEN** the placeholder path SHALL NOT query database-backed total time directly

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

### Requirement: Placeholder time rendering is online-player-only
Paper/Folia time placeholder rendering SHALL only request cache refreshes for online players and SHALL return deterministic fallback values for offline or missing players without touching database-backed storage.

#### Scenario: Online placeholder uses cache refresh path
- **WHEN** a Paper or Folia-compatible instance renders an online-time placeholder for an online player during normal runtime
- **THEN** the placeholder path SHALL read cached or non-database time data
- **THEN** the placeholder path SHALL request an asynchronous refresh for that online player
- **THEN** the placeholder path SHALL NOT query database-backed total time directly

#### Scenario: Offline placeholder uses fallback without refresh
- **WHEN** a Paper or Folia-compatible instance renders an online-time placeholder for an offline player
- **THEN** the placeholder path SHALL return deterministic fallback values based on zero online time
- **THEN** the placeholder path SHALL NOT request an asynchronous storage refresh
- **THEN** the placeholder path SHALL NOT send a plugin-message refresh request

#### Scenario: Missing placeholder player uses fallback without refresh
- **WHEN** a Paper or Folia-compatible instance renders an online-time placeholder without a player object
- **THEN** the placeholder path SHALL return deterministic fallback values based on zero online time
- **THEN** the placeholder path SHALL NOT request an asynchronous storage refresh
- **THEN** the placeholder path SHALL NOT send a plugin-message refresh request

### Requirement: AFK state messages are independent from stop-count policy
The system SHALL send AFK state messages for non-kicked AFK state transitions independently from whether AFK handling stops online-time accumulation.

#### Scenario: Non-kicked AFK player receives AFK start message
- **WHEN** a player becomes AFK and is not kicked by AFK auto-kick enforcement
- **THEN** the player SHALL receive the AFK self message
- **THEN** online players with the AFK announce permission SHALL receive the AFK announce message

#### Scenario: Stop-count bypass does not suppress AFK start message
- **WHEN** a player becomes AFK and has the stop-count bypass permission
- **THEN** the player SHALL receive the AFK self message
- **THEN** online players with the AFK announce permission SHALL receive the AFK announce message

#### Scenario: AFK resume message is sent for non-kicked AFK resume
- **WHEN** a non-kicked AFK player resumes from AFK
- **THEN** the player SHALL receive the AFK resume self message
- **THEN** online players with the AFK announce permission SHALL receive the AFK resume announce message

#### Scenario: AFK kick uses kick messaging instead of AFK start message
- **WHEN** a player is kicked by AFK auto-kick enforcement
- **THEN** the player SHALL receive the configured AFK kick message through the kick operation
- **THEN** online players with the AFK kick announce permission SHALL receive the AFK kick announce message
- **THEN** the player SHALL NOT receive the AFK self message as a separate chat message

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

### Requirement: Typed storage plugin message operations
The system SHALL decode storage plugin messages into explicit operation types before applying storage side effects.

#### Scenario: Master receives supported storage operation
- **WHEN** a master receives a storage plugin message with a known operation and supported protocol version
- **THEN** the master SHALL apply the operation through the appropriate storage or accumulator contract

#### Scenario: Master receives unknown storage operation
- **WHEN** a master receives a storage plugin message with an unknown operation
- **THEN** the master SHALL ignore the message
- **THEN** the master SHALL log a warning that identifies the unknown storage operation

#### Scenario: Master receives unsupported storage protocol version
- **WHEN** a master receives a storage plugin message with an unsupported protocol version
- **THEN** the master SHALL ignore the message
- **THEN** the master SHALL log a warning that identifies the storage message family, operation when known, and unsupported version

#### Scenario: Master receives malformed storage payload
- **WHEN** a master receives a storage plugin message whose payload is incomplete or cannot be decoded for the requested operation
- **THEN** the master SHALL ignore the message
- **THEN** the master SHALL log a warning that identifies the malformed storage operation when it can be determined

### Requirement: External storage injection lifecycle is documented
The system SHALL document custom storage injection lifecycle behavior consistently with runtime behavior.

#### Scenario: Custom storage is injected
- **WHEN** custom storage and accumulator contracts are injected
- **THEN** the documentation SHALL state whether the data storage manager closes those injected contracts during shutdown or reload
- **THEN** the documented behavior SHALL match the implementation

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

### Requirement: Time-ranged storage totals
The unified storage service SHALL support player time totals filtered by an optional time range while preserving existing global, server, and world scope semantics.

#### Scenario: Unbounded total remains unchanged
- **WHEN** runtime code queries a player's total time without a time range
- **THEN** the unified storage service SHALL preserve the existing unbounded total behavior

#### Scenario: Global ranged total includes matching data
- **WHEN** runtime code queries a player's global total time with a time range
- **THEN** the unified storage service SHALL include overlapping persisted session duration for that player within the range
- **THEN** the unified storage service SHALL include signed manual adjustments for that player whose `created_at` is inside the range regardless of adjustment scope

#### Scenario: Server ranged total includes matching server data
- **WHEN** runtime code queries a player's server total time with a time range
- **THEN** the unified storage service SHALL include overlapping persisted session duration for that player on that server within the range
- **THEN** the unified storage service SHALL include server-scoped adjustments for that server whose `created_at` is inside the range
- **THEN** the unified storage service SHALL include world-scoped adjustments for worlds on that server whose `created_at` is inside the range
- **THEN** the unified storage service SHALL NOT include global adjustments

#### Scenario: World ranged total includes exact world data
- **WHEN** runtime code queries a player's world total time with a time range
- **THEN** the unified storage service SHALL include overlapping persisted session duration for that player in that exact server and world within the range
- **THEN** the unified storage service SHALL include world-scoped adjustments for that exact server and world whose `created_at` is inside the range
- **THEN** the unified storage service SHALL NOT include global or server-scoped adjustments

#### Scenario: Persisted sessions are clipped to range boundaries
- **WHEN** a persisted session starts before the requested range and ends inside or after the requested range
- **THEN** the unified storage service SHALL count only the portion of the session that overlaps the requested range

#### Scenario: Non-overlapping persisted sessions are excluded
- **WHEN** a persisted session has no overlap with the requested time range
- **THEN** the unified storage service SHALL exclude that session from the ranged total
- **THEN** that excluded session SHALL NOT cause an otherwise empty ranged query to return a zero-valued hit

#### Scenario: Active session contributes to matching ranged total
- **WHEN** runtime code queries a player's ranged total while that player has an active session
- **THEN** the active session elapsed time SHALL contribute only when the active session context matches the requested scope and overlaps the requested range
- **THEN** only the overlapping active session duration SHALL be counted

#### Scenario: Active session outside ranged total is excluded
- **WHEN** runtime code queries a player's ranged total and the player's active session does not overlap the requested range
- **THEN** the active session SHALL NOT contribute to the result

### Requirement: Time range API contract
Runtime and public API time query contracts SHALL expose time-range-aware lookup methods.

#### Scenario: Storage exposes ranged lookup
- **WHEN** runtime code needs a player total for a scope and time range
- **THEN** the focused storage query contract SHALL provide a ranged lookup method
- **THEN** implementations SHALL combine matching session durations and matching manual adjustments for that range

#### Scenario: Public service exposes ranged lookup
- **WHEN** API consumers need a player total for a scope and time range
- **THEN** the public LoriTime service SHALL provide a ranged lookup method returning an optional duration asynchronously

#### Scenario: Invalid time range is not accepted by storage API
- **WHEN** runtime or API code attempts to construct or pass an invalid time range
- **THEN** the system SHALL reject the invalid range before issuing storage queries

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

### Requirement: Runtime storage API compatibility for maintenance
Admin storage maintenance support SHALL NOT change the required runtime storage API surface.

#### Scenario: UnifiedStorage does not expose maintenance methods
- **WHEN** this change is implemented
- **THEN** `UnifiedStorage` SHALL remain focused on runtime identity, time query, adjustment, session, and player deletion behavior
- **THEN** storage maintenance transfer and scoped delete methods SHALL NOT be added as required `UnifiedStorage` methods

#### Scenario: Maintenance uses focused storage internals
- **WHEN** database-backed maintenance transfers or deletes scoped storage data
- **THEN** the implementation SHALL use focused maintenance contracts and table helpers
- **THEN** normal runtime callers SHALL continue using existing storage contracts

### Requirement: Maintenance preserves scoped total semantics
Storage maintenance operations SHALL preserve scoped time total behavior for sessions and manual adjustments.

#### Scenario: Server transfer preserves server totals
- **WHEN** a server dataset is transferred to a target server
- **THEN** player totals for the target server SHALL include transferred sessions and matching server/world-scoped adjustments
- **THEN** global player totals SHALL remain unchanged except for the source and target scope labels

#### Scenario: World transfer preserves world totals
- **WHEN** a world dataset is transferred to a target world
- **THEN** player totals for the target world SHALL include transferred sessions and matching world-scoped adjustments
- **THEN** global player totals SHALL remain unchanged except for the source and target scope labels

#### Scenario: Scoped delete removes matching contributions
- **WHEN** server or world data is deleted through maintenance behavior
- **THEN** scoped and global totals SHALL no longer include the deleted session or adjustment rows
- **THEN** unrelated scopes SHALL retain their existing contributions
