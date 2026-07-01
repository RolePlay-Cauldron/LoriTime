## MODIFIED Requirements

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

### Requirement: Scoped command completions
Scoped command completion SHALL provide live or cached suggestions without synchronous database-backed request-path reads.

#### Scenario: Time lookup suggests long scope flag prefixes
- **WHEN** a sender completes a time lookup argument where a scope flag may be entered
- **THEN** the completion path SHALL suggest `world:` and `server:` according to sender permissions and already-entered flags
- **THEN** the completion path SHALL NOT suggest `w:` or `s:`

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
- **WHEN** a sender executes a time lookup with a syntactically valid server or world value that was not suggested by completion
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
