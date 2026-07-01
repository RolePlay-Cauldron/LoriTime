## ADDED Requirements

### Requirement: Inactive player history cleanup
The system SHALL provide a disabled-by-default storage cleanup feature that removes stored time history for inactive players while preserving player identity.

#### Scenario: Cleanup is disabled by default
- **WHEN** the plugin starts with default configuration
- **THEN** inactive player history cleanup SHALL NOT run

#### Scenario: Cleanup uses storageCleanup configuration
- **WHEN** inactive player history cleanup is configured
- **THEN** the system SHALL read its settings from `storageCleanup.*`

#### Scenario: Cleanup selects inactive players from player activity
- **WHEN** inactive player history cleanup runs with a configured inactivity threshold
- **THEN** the system SHALL select players whose player table activity timestamp is older than the configured threshold

#### Scenario: Cleanup deletes only history
- **WHEN** inactive player history cleanup removes data for a selected player
- **THEN** the system SHALL delete that player's persisted session rows and manual adjustment rows
- **THEN** the system SHALL preserve that player's identity row, UUID, name, and activity metadata

#### Scenario: Cleanup skips active players
- **WHEN** a player's activity timestamp is newer than or equal to the configured inactivity threshold
- **THEN** the system SHALL NOT delete that player's session rows or manual adjustment rows
