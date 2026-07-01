## MODIFIED Requirements

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
