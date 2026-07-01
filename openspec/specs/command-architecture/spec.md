# command-architecture Specification

## Purpose
Defines shared command registration, dispatch, runtime profile selection, alias resolution, and player completion behavior across LoriTime platforms.
## Requirements
### Requirement: Runtime command profiles
The system SHALL register commands from code-defined runtime profiles based on platform family and storage responsibility.

#### Scenario: Proxy registers runtime and canonical commands
- **WHEN** a Velocity instance runs in a storage-owning mode
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
- **WHEN** a sender executes a global, server-scoped, or world-scoped time lookup command on a proxy storage owner or backend canonical runtime
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

### Requirement: Common command dispatcher
The system SHALL route native platform command execution and completion through a common command dispatcher.

#### Scenario: Platform adapter delegates execution
- **WHEN** a native Paper-compatible, or Velocity command execution event occurs
- **THEN** the platform adapter SHALL translate the sender to `CommonSender`
- **THEN** the platform adapter SHALL delegate execution to the common command dispatcher

#### Scenario: Platform adapter delegates completion
- **WHEN** a native Paper-compatible, or Velocity completion request occurs
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

### Requirement: Scoped time lookup command
The canonical time lookup command SHALL support explicit global, server, and world scoped lookup forms using flag-style scope arguments.

#### Scenario: Global own time lookup remains default
- **WHEN** a player executes the time lookup command without scope arguments
- **THEN** the command SHALL query that player's global total time
- **THEN** the command SHALL require the existing global self lookup permission

#### Scenario: Global other-player lookup remains default
- **WHEN** a sender executes the time lookup command with a player argument and no scope arguments
- **THEN** the command SHALL query that player's global total time
- **THEN** the command SHALL require the existing other-player global lookup permission when the target is not the sender

#### Scenario: Server own time lookup
- **WHEN** a player executes the time lookup command with `server:<server>` or `s:<server>`
- **THEN** the command SHALL query that player's total time for the specified server
- **THEN** the command SHALL require `loritime.see.server`

#### Scenario: Server other-player lookup
- **WHEN** a sender executes the time lookup command with a player argument and `server:<server>` or `s:<server>`
- **THEN** the command SHALL query the target player's total time for the specified server
- **THEN** the command SHALL require `loritime.see.server.other` when the target is not the sender

#### Scenario: World own time lookup with explicit server
- **WHEN** a player executes the time lookup command with `world:<world>` or `w:<world>` and `server:<server>` or `s:<server>`
- **THEN** the command SHALL query that player's total time for the specified server and world
- **THEN** the command SHALL require `loritime.see.world`

#### Scenario: World other-player lookup with explicit server
- **WHEN** a sender executes the time lookup command with a player argument, `world:<world>` or `w:<world>`, and `server:<server>` or `s:<server>`
- **THEN** the command SHALL query the target player's total time for the specified server and world
- **THEN** the command SHALL require `loritime.see.world.other` when the target is not the sender

#### Scenario: World lookup resolves omitted server on proxy runtime
- **WHEN** a sender executes the time lookup command with `world:<world>` or `w:<world>` and no server flag on a proxy runtime
- **THEN** the command SHALL resolve the server from the relevant online player's current backend server
- **THEN** the command SHALL query the player's total time for the resolved server and requested world

#### Scenario: World lookup resolves omitted server on Paper/Folia canonical runtime
- **WHEN** a sender executes the time lookup command with `world:<world>` or `w:<world>` and no server flag on a Paper/Folia standalone or master runtime
- **THEN** the command SHALL resolve the server from configured `server.name` with the shared default fallback
- **THEN** the command SHALL query the player's total time for the resolved server and requested world

#### Scenario: World lookup without resolvable server fails
- **WHEN** a sender executes a world-scoped time lookup without a server flag and the runtime cannot resolve a canonical server from live or local runtime context
- **THEN** the command SHALL reject the lookup instead of deriving a server from database history

#### Scenario: Scoped lookup accepts flags independent of order
- **WHEN** a sender executes the time lookup command with one optional player token and valid scope flags in any order
- **THEN** the command SHALL parse the same target player and scope regardless of whether the player token appears before or after the scope flags

#### Scenario: Positional scoped lookup is rejected
- **WHEN** a sender executes the time lookup command using the legacy positional scoped forms `server <server>` or `world <server> <world>`
- **THEN** the command SHALL reject the request with usage feedback

### Requirement: Scoped modify command
The canonical modify command SHALL support optional server and world scopes for add, set, and reset operations using flag-style scope arguments.

#### Scenario: Modify command without scope remains global
- **WHEN** a sender executes a modify add, set, or reset operation without scope arguments
- **THEN** the command SHALL apply the operation to the player's global time scope

#### Scenario: Modify command with server scope
- **WHEN** a sender executes a modify add, set, or reset operation with `server:<server>` or `s:<server>`
- **THEN** the command SHALL apply the operation to the player's server time scope

#### Scenario: Modify command with world scope and explicit server
- **WHEN** a sender executes a modify add, set, or reset operation with `world:<world>` or `w:<world>` and `server:<server>` or `s:<server>`
- **THEN** the command SHALL apply the operation to the player's world time scope for the specified server and world

#### Scenario: Modify command resolves omitted world server on proxy runtime
- **WHEN** a sender executes a modify add, set, or reset operation with `world:<world>` or `w:<world>` and no server flag on a proxy runtime
- **THEN** the command SHALL resolve the server from the target player's current backend server
- **THEN** the command SHALL apply the operation to the player's world time scope for the resolved server and requested world

#### Scenario: Modify command resolves omitted world server on backend canonical runtime
- **WHEN** a sender executes a modify add, set, or reset operation with `world:<world>` or `w:<world>` and no server flag on a Paper/Folia standalone or master runtime
- **THEN** the command SHALL resolve the server from configured `server.name` with the shared default fallback
- **THEN** the command SHALL apply the operation to the player's world time scope for the resolved server and requested world

#### Scenario: Modify command rejects world scope without resolvable server
- **WHEN** a sender executes a modify add, set, or reset operation with `world:<world>` or `w:<world>` and no server flag and the runtime cannot resolve a canonical server
- **THEN** the command SHALL reject the operation with usage or scope feedback
- **THEN** the command SHALL NOT mutate canonical storage

#### Scenario: Scoped modify accepts flags independent of order
- **WHEN** a sender executes a modify add, set, or reset operation with valid scope flags in any supported order
- **THEN** the command SHALL parse the same target player and scope regardless of scope flag order

#### Scenario: Positional scoped modify is rejected
- **WHEN** a sender executes a modify add, set, or reset operation using positional scoped forms `server <server>` or `world <server> <world>`
- **THEN** the command SHALL reject the operation with usage feedback
- **THEN** the command SHALL NOT mutate canonical storage

#### Scenario: Scoped set preserves target scope semantics
- **WHEN** a sender sets a player's time for a server or world scope
- **THEN** the command SHALL compute and persist the signed adjustment required to make that scoped total equal the requested value
- **THEN** the command SHALL NOT mutate unrelated scopes

#### Scenario: Scoped reset clears target scope semantics
- **WHEN** a sender resets a player's time for a server or world scope
- **THEN** the command SHALL compute and persist the signed adjustment required to make that scoped total equal zero
- **THEN** the command SHALL NOT mutate unrelated scopes

### Requirement: Scoped command completions
Scoped command completion SHALL provide live or cached suggestions without synchronous database-backed request-path reads.

#### Scenario: Time lookup suggests long scope flag prefixes
- **WHEN** a sender completes a time lookup argument where a scope flag may be entered
- **THEN** the completion path SHALL suggest `world:` and `server:` according to sender permissions and already-entered flags
- **THEN** the completion path SHALL NOT suggest `w:` or `s:`

#### Scenario: Modify command suggests long scope flag prefixes
- **WHEN** a sender completes a modify add, set, or reset argument where a scope flag may be entered
- **THEN** the completion path SHALL suggest `world:` and `server:`
- **THEN** the completion path SHALL NOT suggest `w:` or `s:`
- **THEN** the completion path SHALL NOT query database-backed storage synchronously

#### Scenario: Short scope flags are accepted manually
- **WHEN** a sender enters `w:<world>` or `s:<server>` manually
- **THEN** the command SHALL accept those aliases as equivalent to `world:<world>` and `server:<server>`

#### Scenario: Long server completion uses cached and live data
- **WHEN** a sender completes the value portion of `server:`
- **THEN** the completion path SHALL suggest known server names from the in-memory scope suggestion cache and live runtime data
- **THEN** the completion path SHALL NOT query database-backed storage synchronously

#### Scenario: Short server completion uses cached and live data
- **WHEN** a sender completes the value portion of `s:`
- **THEN** the completion path SHALL suggest matching server values prefixed with `s:`
- **THEN** the completion path SHALL NOT query database-backed storage synchronously

#### Scenario: Long world completion uses cached and live data
- **WHEN** a sender completes the value portion of `world:`
- **THEN** the completion path SHALL suggest known world names from the in-memory scope suggestion cache and live runtime data
- **THEN** the completion path SHALL NOT query database-backed storage synchronously

#### Scenario: Short world completion uses cached and live data
- **WHEN** a sender completes the value portion of `w:`
- **THEN** the completion path SHALL suggest matching world values prefixed with `w:`
- **THEN** the completion path SHALL NOT query database-backed storage synchronously

#### Scenario: Scope suggestion cache refreshes asynchronously
- **WHEN** a storage-owning instance starts, reloads, or observes a player on a server/world
- **THEN** the system SHALL refresh or update the in-memory scope suggestion cache with known server and world names
- **THEN** command completion SHALL use that cache instead of blocking on storage

#### Scenario: Manually typed uncached scopes still execute
- **WHEN** a sender executes a time lookup or modify operation with a syntactically valid server or world value that was not suggested by completion
- **THEN** the command SHALL query canonical storage for that scoped total
- **THEN** the command SHALL report no data only when storage has no matching scoped time for the requested player

#### Scenario: Scoped player completion uses cache
- **WHEN** a scoped time or modify command completion request needs player name suggestions
- **THEN** the completion path SHALL use cached player suggestion data
- **THEN** the completion path SHALL NOT query database-backed storage directly

#### Scenario: Scoped no-data feedback distinguishes unknown scoped time
- **WHEN** a sender executes a server-scoped or world-scoped time lookup for a known player and storage has no matching scoped time
- **THEN** the command SHALL report that the player has no tracked time for the requested scope
- **THEN** the command SHALL NOT report that the player has never played globally

### Requirement: Time-ranged lookup command
The canonical time lookup command SHALL support an optional time-range flag that filters the returned total to a bounded history window.

#### Scenario: Single duration time range filters from now
- **WHEN** a sender executes `/loritime` with `time:<duration>` or `t:<duration>`
- **THEN** the command SHALL parse `<duration>` with the configured `TimeParser`
- **THEN** the command SHALL query only the time window from the parsed duration ago up to the command execution time

#### Scenario: Near-to-far time range filters historical interval
- **WHEN** a sender executes `/loritime` with `time:<near>-<far>` or `t:<near>-<far>`
- **THEN** the command SHALL parse both durations with the configured `TimeParser`
- **THEN** the command SHALL query only the time window from `<far>` ago up to `<near>` ago

#### Scenario: Time range combines with scope flags
- **WHEN** a sender executes `/loritime` with a valid time-range flag and any valid combination of player, server, and world lookup arguments
- **THEN** the command SHALL apply the requested time window to the resolved player and scope
- **THEN** the command SHALL preserve the existing scope permission checks
- **THEN** the command SHALL require `loritime.see.timerange` for own lookups using a time-range flag
- **THEN** the command SHALL require `loritime.see.timerange.other` when the target is not the sender

#### Scenario: Time range accepts flags independent of order
- **WHEN** a sender executes `/loritime` with one optional player token, valid scope flags, and a valid time-range flag in any order
- **THEN** the command SHALL parse the same target player, scope, and time range regardless of argument order

#### Scenario: Duplicate time range is rejected
- **WHEN** a sender executes `/loritime` with more than one `time:` or `t:` flag
- **THEN** the command SHALL reject the request with usage feedback

#### Scenario: Invalid time range is rejected
- **WHEN** a sender executes `/loritime` with an empty, unparsable, zero, negative, or reversed time-range value
- **THEN** the command SHALL reject the request with usage feedback
- **THEN** the command SHALL NOT query canonical storage

#### Scenario: Time-ranged no-data feedback distinguishes missing range data
- **WHEN** a sender executes a time-ranged lookup for a known player and storage has no matching time in the requested window
- **THEN** the command SHALL report that the player has no tracked time for the requested range and scope
- **THEN** the command SHALL NOT report that the player has never played globally

### Requirement: Time-range lookup completions
Scoped command completion SHALL expose the long time-range flag prefix while leaving custom range values unsuggested.

#### Scenario: Time lookup suggests long time flag prefix
- **WHEN** a sender completes a time lookup argument where a time-range flag may be entered
- **THEN** the completion path SHALL suggest `time:` according to sender permissions and already-entered flags
- **THEN** the completion path SHALL NOT suggest `t:`

#### Scenario: Time range values are not suggested
- **WHEN** a sender completes the value portion of `time:` or `t:`
- **THEN** the completion path SHALL return no range value suggestions
- **THEN** the completion path SHALL NOT query database-backed storage

#### Scenario: Time flag is not suggested after it is already present
- **WHEN** a sender completes a time lookup after entering a valid `time:` or `t:` flag
- **THEN** the completion path SHALL NOT suggest another `time:` flag

### Requirement: Admin storage maintenance command preparation
The admin command architecture SHALL be prepared to host storage maintenance actions without exposing them through player mutation commands.

#### Scenario: Maintenance actions remain admin-scoped
- **WHEN** storage maintenance commands are added in a later change
- **THEN** they SHALL be exposed through the admin command or an admin command subtree
- **THEN** they SHALL NOT be exposed through the modify command

#### Scenario: Maintenance commands require storage owner
- **WHEN** a future storage maintenance command is executed on a runtime that does not own canonical storage
- **THEN** the command SHALL reject the operation before attempting maintenance storage access

#### Scenario: Maintenance commands use preview flow
- **WHEN** a future storage maintenance command would transfer, merge, or delete data
- **THEN** the command SHALL use the admin storage maintenance preview and confirmation flow
- **THEN** the command SHALL NOT directly manipulate database tables

#### Scenario: Maintenance command execution is asynchronous
- **WHEN** a future storage maintenance command previews or applies storage work
- **THEN** the command SHALL execute storage work away from platform main-thread command paths
