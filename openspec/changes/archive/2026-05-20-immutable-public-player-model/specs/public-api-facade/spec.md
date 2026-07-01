## ADDED Requirements

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

### Requirement: Sender compatibility with public player identity
The system SHALL allow internal sender/player abstractions to be used wherever the public player identity contract is accepted without exposing sender behavior through the public contract.

#### Scenario: Common sender is accepted as player identity
- **WHEN** internal LoriTime code passes a `CommonSender` to a facade or helper method that accepts `LoriTimePlayer`
- **THEN** the sender SHALL satisfy the public player identity contract
- **THEN** consumers typed as `LoriTimePlayer` SHALL only rely on UUID and name identity methods

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
