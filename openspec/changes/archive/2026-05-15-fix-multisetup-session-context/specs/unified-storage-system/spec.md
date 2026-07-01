## MODIFIED Requirements

### Requirement: Context-aware session accumulation
The system SHALL accumulate active sessions with UUID, optional player name, canonical server, current world context, start timestamp, and persistence reason context.

#### Scenario: Session start records context
- **WHEN** a player session starts on a platform that owns canonical session storage
- **THEN** the accumulator SHALL store the UUID, optional name, canonical server, current world context, and start timestamp for that active session
- **THEN** the accumulator SHALL create a persisted active session row for that context

#### Scenario: Session stop persists context
- **WHEN** an active session stops
- **THEN** the accumulator SHALL update the active persisted session row with the stop timestamp and stop reason

#### Scenario: Session context changes
- **WHEN** an active player's canonical server context changes
- **THEN** the accumulator SHALL update the previous active session row as a context-switch session chunk and start tracking the new context
- **THEN** the accumulator SHALL create a persisted active session row for the new context

#### Scenario: Paper or Folia standalone player changes world
- **WHEN** a Paper or Folia standalone or master instance observes a player changing effective world
- **THEN** the system SHALL switch the active session context to the new world

#### Scenario: Paper or Folia slave reports current world context
- **WHEN** a Paper or Folia slave observes a player's current effective world
- **THEN** the slave SHALL report the current world context to the master without sending a completed session row

#### Scenario: Master applies slave world context to active session
- **WHEN** a proxy master receives a valid world context update for a player with an active master-owned session
- **THEN** the master SHALL apply the reported world to that active session context without creating a new canonical time row

#### Scenario: Master ignores orphan slave world context
- **WHEN** a proxy master receives a world context update for a player without an active master-owned session
- **THEN** the master SHALL NOT create a standalone canonical time row from that world context update

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

#### Scenario: Slave reports world context instead of session write
- **WHEN** a Paper or Folia slave observes a join, world change, periodic update, or leave
- **THEN** the slave SHALL report current world context only
- **THEN** the slave SHALL NOT report a completed session chunk for canonical persistence

#### Scenario: Slave autoflush does not create rows
- **WHEN** a Paper or Folia slave reaches its periodic update interval
- **THEN** the slave SHALL NOT cause the master to insert a new canonical time row

#### Scenario: Master persists slave manual writes
- **WHEN** a master receives a valid manual storage write or adjustment event from a slave
- **THEN** the master SHALL persist it through the unified storage service

#### Scenario: Slave does not write database directly
- **WHEN** an instance runs in `slave` mode
- **THEN** the instance SHALL avoid direct canonical database writes for storage data
