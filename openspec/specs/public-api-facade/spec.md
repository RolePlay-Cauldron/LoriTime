# public-api-facade Specification

## Purpose
TBD - created by archiving change improve-public-api. Update Purpose after archive.
## Requirements
### Requirement: Stable public API facade
The system SHALL expose a focused public API facade interface for third-party plugins without requiring callers to depend on internal plugin, storage lifecycle, active session persistence, or concrete service implementation types.

#### Scenario: Third-party plugin obtains public facade
- **WHEN** LoriTime has completed API initialization
- **THEN** a caller SHALL be able to obtain the public API facade interface through `LoriTimeAPI`
- **THEN** the returned facade SHALL NOT require the caller to cast to `LoriTimePlugin`, `UnifiedStorage`, `DataStorageManager`, `TimeAccumulator`, or a concrete service implementation class

#### Scenario: Broad plugin accessor is not public API
- **WHEN** third-party integrations use the public API entry point
- **THEN** the system SHALL expose the focused facade interface instead of the internal plugin object
- **THEN** the public entry point SHALL NOT expose storage lifecycle, configuration, localization, updater, or accumulator internals

#### Scenario: Service implementation is replaceable
- **WHEN** LoriTime changes the internal implementation of the public facade
- **THEN** third-party integrations typed against the `LoriTimeService` interface SHALL keep the same public method contract
- **THEN** callers SHALL NOT need to reference the concrete implementation type returned by `LoriTimeAPI`

### Requirement: Public identity lookup operations
The public API facade SHALL allow third-party plugins to resolve known player identity data without exposing storage implementation details.

#### Scenario: Lookup UUID by player name
- **WHEN** a caller asks the facade for a known player name
- **THEN** the facade SHALL return the matching player UUID when LoriTime has stored one
- **THEN** the facade SHALL return an empty result when no stored UUID is known

#### Scenario: Lookup latest name by UUID
- **WHEN** a caller asks the facade for a known player UUID
- **THEN** the facade SHALL return the latest stored player name when LoriTime has one
- **THEN** the facade SHALL return an empty result when no stored name is known

### Requirement: Public time query operations
The public API facade SHALL allow third-party plugins to read player total online time using stable public types.

#### Scenario: Query stored player time
- **WHEN** a caller asks the facade for a player's total online time
- **THEN** the facade SHALL return the same total that LoriTime commands and placeholders would report for that player in the current storage mode
- **THEN** the facade SHALL include active accumulated session time when LoriTime's runtime storage includes it

#### Scenario: Query unknown player time
- **WHEN** a caller asks the facade for a player with no stored time
- **THEN** the facade SHALL return an empty result or documented zero-value result consistently
- **THEN** the behavior SHALL be documented in the public API guide

### Requirement: Public player identity model
The system SHALL expose `LoriTimePlayer` as a stable public player identity contract for third-party API consumers.

#### Scenario: Public player exposes only identity
- **WHEN** a third-party plugin uses a `LoriTimePlayer` through the public API
- **THEN** the public contract SHALL expose the player's UUID and latest known name
- **THEN** the public contract SHALL NOT expose AFK state, resume timestamps, sender permissions, message sending, console state, or online state

#### Scenario: Public player reference is immutable
- **WHEN** a third-party plugin receives or creates a public player reference
- **THEN** the reference SHALL provide stable identity data without public mutation methods
- **THEN** facade operations SHALL use the UUID as the canonical player identity

### Requirement: Public manual adjustment operations
The public API facade SHALL allow third-party plugins to add signed manual time adjustments while preserving audit reason and actor metadata.

#### Scenario: Add system manual adjustment
- **WHEN** a caller adds a signed time adjustment without a player actor
- **THEN** the facade SHALL persist the adjustment with a manual adjustment reason
- **THEN** the facade SHALL use a stable system or console actor representation

#### Scenario: Add actor-aware manual adjustment
- **WHEN** a caller adds a signed time adjustment with actor UUID and actor name
- **THEN** the facade SHALL persist the adjustment with the supplied actor metadata
- **THEN** the adjustment SHALL contribute to future total-time queries

#### Scenario: Reject invalid adjustment input
- **WHEN** a caller supplies invalid adjustment input such as a null target player or unsupported duration value
- **THEN** the facade SHALL reject the call with documented validation behavior
- **THEN** the system SHALL NOT persist a partial adjustment

### Requirement: Player-based facade operations
The public API facade SHALL support player-object overloads for player time reads and manual adjustments.

#### Scenario: Query time by public player
- **WHEN** a caller asks the facade for online time using a `LoriTimePlayer`
- **THEN** the facade SHALL query the same total as the UUID-based operation for that player's UUID
- **THEN** the player's name SHALL NOT affect the target identity of the query

#### Scenario: Add system adjustment by public player
- **WHEN** a caller adds a signed time adjustment using a `LoriTimePlayer`
- **THEN** the facade SHALL persist the adjustment for that player's UUID
- **THEN** the facade SHALL use the same system actor behavior as the UUID-based system adjustment

#### Scenario: Add actor-aware adjustment by public players
- **WHEN** a caller adds a signed time adjustment using a target `LoriTimePlayer` and actor `LoriTimePlayer`
- **THEN** the facade SHALL persist the adjustment for the target player's UUID
- **THEN** the facade SHALL persist the actor player's UUID and name as actor metadata

#### Scenario: Reject invalid player input
- **WHEN** a caller supplies a null player object or a player object with invalid identity data
- **THEN** the facade SHALL reject the call with documented validation behavior
- **THEN** the system SHALL NOT persist a partial adjustment

### Requirement: Public storage-facing facade operations are asynchronous
The public API facade SHALL expose storage-facing player identity lookups, total-time reads, and manual adjustment writes as `CompletableFuture` operations whose blocking storage work is executed asynchronously under LoriTime control.

#### Scenario: Identity lookup returns future
- **WHEN** a caller asks the facade to resolve a known player UUID or latest player name
- **THEN** the facade SHALL return a `CompletableFuture` for the lookup result
- **THEN** the blocking storage lookup SHALL NOT be performed on the caller's platform main-thread path

#### Scenario: Total-time query returns future
- **WHEN** a caller asks the facade for a player's total online time
- **THEN** the facade SHALL return a `CompletableFuture` for the optional duration result
- **THEN** the completed result SHALL preserve the same storage-mode total-time semantics as the public time query operation

#### Scenario: Manual adjustment write returns future
- **WHEN** a caller asks the facade to add a signed manual time adjustment
- **THEN** the facade SHALL return a `CompletableFuture` that completes after the write has been attempted
- **THEN** the blocking storage write SHALL be executed asynchronously under LoriTime control

#### Scenario: Input validation happens before storage scheduling
- **WHEN** a caller supplies invalid facade input that can be validated without storage access
- **THEN** the facade SHALL reject the call before persisting a partial write

### Requirement: Public API error and availability behavior
The public API facade SHALL define deterministic behavior for unavailable plugin state and asynchronous storage failures.

#### Scenario: API requested before initialization
- **WHEN** a caller requests the facade before LoriTime has initialized its API state
- **THEN** the system SHALL return an empty unavailable result or throw a documented state exception
- **THEN** the behavior SHALL be consistent across supported platforms

#### Scenario: Storage operation fails
- **WHEN** an underlying storage operation fails while serving a facade call
- **THEN** the facade future SHALL complete exceptionally with a documented LoriTime API exception shape
- **THEN** the facade SHALL NOT expose raw SQL exceptions as part of the normal public API contract

#### Scenario: Slave mode read cache miss
- **WHEN** a facade read is requested on an instance that relies on delegated or cached storage data
- **THEN** the facade future SHALL complete with the same deterministic fallback behavior as local LoriTime consumers for that storage mode
- **THEN** the behavior SHALL be documented for third-party callers

### Requirement: Public API documentation
The system SHALL document the recommended public API facade as the default third-party integration path.

#### Scenario: Developer reads API documentation
- **WHEN** a developer opens the LoriTime API documentation
- **THEN** the documentation SHALL show how to declare LoriTime as a plugin dependency or soft dependency
- **THEN** the documentation SHALL show how to obtain the public facade
- **THEN** the documentation SHALL include examples for identity lookup, time query, and manual adjustment

#### Scenario: Developer needs compatibility guidance
- **WHEN** a developer reads the public API documentation
- **THEN** the documentation SHALL identify which API surface is stable for third-party use
- **THEN** the documentation SHALL identify legacy or advanced access paths that are not the recommended default
- **THEN** the documentation SHALL state threading and storage-mode expectations

### Requirement: Public model cleanup
The system SHALL correct public model contract issues without keeping unused deprecated aliases.

#### Scenario: Player equality is evaluated
- **WHEN** two public LoriTime player model instances represent the same player UUID
- **THEN** equality and hash code behavior SHALL be consistent with each other

#### Scenario: Typo-preserving method is removed
- **WHEN** a public method has an established typo or awkward name and internal callers can use a corrected name
- **THEN** the system SHALL expose the correctly named method
- **THEN** the system SHALL remove the typo-preserving method instead of deprecating it

