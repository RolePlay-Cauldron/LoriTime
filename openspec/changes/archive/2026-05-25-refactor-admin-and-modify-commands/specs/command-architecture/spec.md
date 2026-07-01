## ADDED Requirements

### Requirement: Dedicated modify command
The system SHALL expose canonical player time mutation and user deletion behavior through a dedicated modify command.

#### Scenario: Modify command mutates player time on canonical runtime
- **WHEN** a sender executes a modify command on a proxy storage owner or backend canonical runtime
- **THEN** the command SHALL support adding or modifying player time
- **THEN** the command SHALL support setting player time
- **THEN** the command SHALL support resetting player time

#### Scenario: Modify command deletes users on canonical runtime
- **WHEN** a sender executes the delete-user modify action on a proxy storage owner or backend canonical runtime
- **THEN** the command SHALL delete the canonical user data using the existing delete-user behavior

#### Scenario: Modify command unavailable on backend slave
- **WHEN** a backend instance runs in `slave` mode
- **THEN** the system SHALL NOT register the modify command

#### Scenario: Modify command provides player completions
- **WHEN** a modify command completion request needs player name suggestions
- **THEN** the completion path SHALL use the shared recent player completion cache
- **THEN** the completion path SHALL NOT query database-backed storage directly

## MODIFIED Requirements

### Requirement: Runtime command profiles
The system SHALL register commands from code-defined runtime profiles based on platform family and storage responsibility.

#### Scenario: Proxy registers runtime and canonical commands
- **WHEN** a Velocity or Bungee instance runs in a storage-owning mode
- **THEN** the system SHALL register the admin command for local runtime administration
- **THEN** the system SHALL register canonical read commands
- **THEN** the system SHALL register the modify command for canonical storage mutations

#### Scenario: Backend canonical registers full command set
- **WHEN** a backend instance runs in `standalone` or `master` mode
- **THEN** the system SHALL register the admin command for local runtime administration
- **THEN** the system SHALL register canonical read commands
- **THEN** the system SHALL register the modify command for canonical storage mutations

#### Scenario: Backend slave registers local-safe command set
- **WHEN** a backend instance runs in `slave` mode
- **THEN** the system SHALL register the admin command for local runtime administration
- **THEN** the system SHALL NOT register canonical data commands
- **THEN** the system SHALL NOT register the modify command

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

#### Scenario: Player mutation unavailable through admin
- **WHEN** a sender executes the admin command
- **THEN** the admin command SHALL NOT expose player time mutation actions
- **THEN** the admin command SHALL NOT expose delete-user actions

### Requirement: Local operational commands
Local operational commands SHALL operate only on the plugin instance that receives the command and SHALL be exposed through the admin command.

#### Scenario: Admin reload affects current instance
- **WHEN** a sender executes the admin reload action on an instance
- **THEN** the system SHALL reload that instance's local LoriTime runtime
- **THEN** the system SHALL NOT automatically reload other proxy or backend instances

#### Scenario: Admin debug affects current instance
- **WHEN** a sender executes the admin debug action on an instance
- **THEN** the system SHALL toggle debug behavior only for that local instance

#### Scenario: Admin info reports current instance
- **WHEN** a sender executes the admin info action on an instance
- **THEN** the system SHALL report information for that local instance

#### Scenario: Admin update affects current instance
- **WHEN** a sender executes the admin update action on an instance
- **THEN** the system SHALL run update behavior for that local LoriTime runtime

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

#### Scenario: Local command node is not registered
- **WHEN** any runtime profile registers commands
- **THEN** the system SHALL NOT register a dedicated local command node

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

#### Scenario: Shared command helpers
- **WHEN** command classes need common subcommand routing, completion filtering, localization, player lookup, or time parsing behavior
- **THEN** the implementation SHALL use shared command internals or helper services instead of duplicating equivalent logic in each command class
