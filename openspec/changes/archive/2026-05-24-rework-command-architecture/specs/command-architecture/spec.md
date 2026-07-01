## ADDED Requirements

### Requirement: Runtime command profiles
The system SHALL register commands from code-defined runtime profiles based on platform family and storage responsibility.

#### Scenario: Proxy registers canonical and local commands
- **WHEN** a Velocity or Bungee instance runs in a storage-owning mode
- **THEN** the system SHALL register canonical data commands
- **THEN** the system SHALL register local operational commands

#### Scenario: Backend canonical registers full command set
- **WHEN** a backend instance runs in `standalone` or `master` mode
- **THEN** the system SHALL register canonical data commands
- **THEN** the system SHALL register local operational commands

#### Scenario: Backend slave registers local command set
- **WHEN** a backend instance runs in `slave` mode
- **THEN** the system SHALL register local operational commands
- **THEN** the system SHALL NOT register canonical data commands

#### Scenario: AFK command follows AFK availability
- **WHEN** AFK is enabled on a runtime that supports local AFK command behavior
- **THEN** the system SHALL register the AFK command for that runtime profile

### Requirement: Canonical data commands
Canonical data commands SHALL only be available on runtimes that own or access canonical LoriTime storage.

#### Scenario: Time lookup command on storage owner
- **WHEN** a sender executes a time lookup command on a proxy storage owner or backend canonical runtime
- **THEN** the command SHALL use canonical storage behavior

#### Scenario: Top command unavailable on backend slave
- **WHEN** a backend instance runs in `slave` mode
- **THEN** the system SHALL NOT register the top-time command

#### Scenario: Admin time mutation unavailable on backend slave
- **WHEN** a backend instance runs in `slave` mode
- **THEN** the system SHALL NOT register admin time mutation commands

### Requirement: Local operational commands
Local operational commands SHALL operate only on the plugin instance that receives the command.

#### Scenario: Local reload affects current instance
- **WHEN** a sender executes the reload command on an instance
- **THEN** the system SHALL reload that instance's local LoriTime runtime
- **THEN** the system SHALL NOT automatically reload other proxy or backend instances

#### Scenario: Debug command affects current instance
- **WHEN** a sender executes the debug command on an instance
- **THEN** the system SHALL toggle debug behavior only for that local instance

#### Scenario: Info command reports current instance
- **WHEN** a sender executes the info command on an instance
- **THEN** the system SHALL report information for that local instance

### Requirement: Common command dispatcher
The system SHALL route native platform command execution and completion through a common command dispatcher.

#### Scenario: Platform adapter delegates execution
- **WHEN** a native Paper-compatible, Bungee, or Velocity command execution event occurs
- **THEN** the platform adapter SHALL translate the sender to `CommonSender`
- **THEN** the platform adapter SHALL delegate execution to the common command dispatcher

#### Scenario: Platform adapter delegates completion
- **WHEN** a native Paper-compatible, Bungee, or Velocity completion request occurs
- **THEN** the platform adapter SHALL translate the sender to `CommonSender`
- **THEN** the platform adapter SHALL delegate completion to the common command dispatcher

#### Scenario: Shared permission handling
- **WHEN** a command node declares a required permission
- **THEN** the common command dispatcher SHALL enforce the permission before invoking the command handler

#### Scenario: Shared async policy
- **WHEN** a command node declares asynchronous execution
- **THEN** the common command dispatcher SHALL schedule the handler away from the platform main-thread request path

### Requirement: Profile-specific command names and aliases
The system SHALL resolve command names and aliases from the selected `commands.yml` runtime profile while keeping command availability code-defined.

#### Scenario: Proxy aliases are selected on proxy runtime
- **WHEN** a proxy runtime registers a supported command node
- **THEN** the system SHALL use that node's configured name and aliases from the `proxy` profile section

#### Scenario: Backend canonical aliases are selected
- **WHEN** a backend runtime runs in `standalone` or `master` mode
- **THEN** the system SHALL use supported command names and aliases from the `backend.canonical` profile section

#### Scenario: Backend slave aliases are selected
- **WHEN** a backend runtime runs in `slave` mode
- **THEN** the system SHALL use supported command names and aliases from the `backend.slave` profile section

#### Scenario: Unsupported configured node is ignored
- **WHEN** `commands.yml` contains aliases for a command node that is not supported by the selected runtime profile
- **THEN** the system SHALL NOT register that unsupported command node

### Requirement: Recent player completion cache
The system SHALL provide player-name completions from cache-only recent player suggestion data.

#### Scenario: Completion reads recent cache
- **WHEN** a command completion request needs player name suggestions
- **THEN** the completion path SHALL read online and recent player names from memory
- **THEN** the completion path SHALL NOT query database-backed storage directly

#### Scenario: Recent identities refresh asynchronously
- **WHEN** a storage-owning instance starts or reloads
- **THEN** the system SHALL asynchronously refresh recent player identities for completion caching

#### Scenario: Recent player without time history is suggested
- **WHEN** a stored player identity has `last_seen` within the configured recent-player window and no time history
- **THEN** the player name SHALL be eligible for command completion suggestions

#### Scenario: Default recent player window
- **WHEN** no recent-player completion window is configured
- **THEN** the system SHALL use a 30 day recent-player window

#### Scenario: Online players are suggested immediately
- **WHEN** an online player is visible to the current runtime
- **THEN** the player name SHALL be eligible for command completion suggestions without waiting for a storage refresh

### Requirement: Player suggestion filtering foundation
The system SHALL represent recent player suggestion entries with enough metadata to support future player-specific filters.

#### Scenario: Suggestion entry includes identity metadata
- **WHEN** the recent player suggestion cache stores an entry
- **THEN** the entry SHALL include player UUID and latest known player name

#### Scenario: Suggestion entry includes recency metadata
- **WHEN** the recent player suggestion cache stores an entry from storage
- **THEN** the entry SHALL include the stored last-seen timestamp when available
