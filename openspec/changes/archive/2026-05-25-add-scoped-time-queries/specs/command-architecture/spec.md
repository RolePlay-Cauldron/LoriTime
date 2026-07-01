## ADDED Requirements

### Requirement: Scoped time lookup command
The canonical time lookup command SHALL support explicit global, server, and world scoped lookup forms.

#### Scenario: Global own time lookup remains default
- **WHEN** a player executes the time lookup command without scope arguments
- **THEN** the command SHALL query that player's global total time
- **THEN** the command SHALL require the existing global self lookup permission

#### Scenario: Global other-player lookup remains default
- **WHEN** a sender executes the time lookup command with a player argument and no scope arguments
- **THEN** the command SHALL query that player's global total time
- **THEN** the command SHALL require the existing other-player global lookup permission when the target is not the sender

#### Scenario: Server own time lookup
- **WHEN** a player executes the time lookup command with `server <server>`
- **THEN** the command SHALL query that player's total time for the specified server
- **THEN** the command SHALL require `loritime.see.server`

#### Scenario: Server other-player lookup
- **WHEN** a sender executes the time lookup command with `server <server> <player>`
- **THEN** the command SHALL query the target player's total time for the specified server
- **THEN** the command SHALL require `loritime.see.server.other` when the target is not the sender

#### Scenario: World own time lookup
- **WHEN** a player executes the time lookup command with `world <server> <world>`
- **THEN** the command SHALL query that player's total time for the specified server and world
- **THEN** the command SHALL require `loritime.see.world`

#### Scenario: World other-player lookup
- **WHEN** a sender executes the time lookup command with `world <server> <world> <player>`
- **THEN** the command SHALL query the target player's total time for the specified server and world
- **THEN** the command SHALL require `loritime.see.world.other` when the target is not the sender

### Requirement: Scoped modify command
The canonical modify command SHALL support optional server and world scopes for add, set, and reset operations.

#### Scenario: Modify command without scope remains global
- **WHEN** a sender executes a modify add, set, or reset operation without scope arguments
- **THEN** the command SHALL apply the operation to the player's global time scope

#### Scenario: Modify command with server scope
- **WHEN** a sender executes a modify add, set, or reset operation with `server <server>`
- **THEN** the command SHALL apply the operation to the player's server time scope

#### Scenario: Modify command with world scope
- **WHEN** a sender executes a modify add, set, or reset operation with `world <server> <world>`
- **THEN** the command SHALL apply the operation to the player's world time scope

#### Scenario: Scoped set preserves target scope semantics
- **WHEN** a sender sets a player's time for a server or world scope
- **THEN** the command SHALL compute and persist the signed adjustment required to make that scoped total equal the requested value
- **THEN** the command SHALL NOT mutate unrelated scopes

#### Scenario: Scoped reset clears target scope semantics
- **WHEN** a sender resets a player's time for a server or world scope
- **THEN** the command SHALL compute and persist the signed adjustment required to make that scoped total equal zero
- **THEN** the command SHALL NOT mutate unrelated scopes

### Requirement: Scoped command completions
Scoped command completion SHALL provide cached or static suggestions without database-backed request-path reads.

#### Scenario: Time lookup suggests scope keywords
- **WHEN** a sender completes the first time lookup argument
- **THEN** the completion path SHALL suggest supported scope keywords according to sender permissions

#### Scenario: Scoped player completion uses cache
- **WHEN** a scoped time or modify command completion request needs player name suggestions
- **THEN** the completion path SHALL use cached player suggestion data
- **THEN** the completion path SHALL NOT query database-backed storage directly

## MODIFIED Requirements

### Requirement: Canonical data commands
Canonical data commands SHALL only be available on runtimes that own or access canonical LoriTime storage.

#### Scenario: Time lookup command on storage owner
- **WHEN** a sender executes a global, server-scoped, or world-scoped time lookup command on a proxy storage owner or backend canonical runtime
- **THEN** the command SHALL use canonical storage behavior

#### Scenario: Top command unavailable on backend slave
- **WHEN** a backend instance runs in `slave` mode
- **THEN** the system SHALL NOT register the top-time command

#### Scenario: Player mutation unavailable through admin
- **WHEN** a sender executes the admin command
- **THEN** the admin command SHALL NOT expose player time mutation actions
- **THEN** the admin command SHALL NOT expose delete-user actions

### Requirement: Dedicated modify command
The system SHALL expose canonical player time mutation and user deletion behavior through a dedicated modify command.

#### Scenario: Modify command mutates player time on canonical runtime
- **WHEN** a sender executes a modify command on a proxy storage owner or backend canonical runtime
- **THEN** the command SHALL support adding or modifying player time globally or for an explicit server or world scope
- **THEN** the command SHALL support setting player time globally or for an explicit server or world scope
- **THEN** the command SHALL support resetting player time globally or for an explicit server or world scope

#### Scenario: Modify command deletes users on canonical runtime
- **WHEN** the sender executes the delete-user modify action on a proxy storage owner or backend canonical runtime
- **THEN** the command SHALL delete the canonical user data using the existing delete-user behavior

#### Scenario: Modify command unavailable on backend slave
- **WHEN** a backend instance runs in `slave` mode
- **THEN** the system SHALL NOT register the modify command

#### Scenario: Modify command provides player completions
- **WHEN** a modify command completion request needs player name suggestions
- **THEN** the completion path SHALL use the shared recent player completion cache
- **THEN** the completion path SHALL NOT query database-backed storage directly
