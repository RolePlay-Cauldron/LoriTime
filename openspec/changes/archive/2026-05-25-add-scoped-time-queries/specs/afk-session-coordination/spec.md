## MODIFIED Requirements

### Requirement: Explicit AFK transition decisions
The system SHALL derive AFK side effects from explicit transition decisions before executing player messaging, session accumulation changes, scoped time adjustments, or kick handling.

#### Scenario: Player becomes AFK without auto-kick
- **WHEN** a player crosses the configured AFK threshold and is not auto-kicked
- **THEN** the system SHALL create an AFK-start transition for that player
- **THEN** the transition SHALL preserve existing AFK announcement, self-message, scoped time removal, and stop-count permission behavior

#### Scenario: Player resumes from AFK
- **WHEN** a player leaves AFK state without having been kicked
- **THEN** the system SHALL create an AFK-resume transition for that player
- **THEN** the transition SHALL preserve existing AFK resume message and stopped-accumulation restart behavior

#### Scenario: Player is auto-kicked for AFK
- **WHEN** AFK auto-kick applies to a player
- **THEN** the system SHALL create an AFK-kick transition for that player
- **THEN** the transition SHALL mark the next disconnect as AFK-caused before kicking the player
- **THEN** the transition SHALL use kick messaging instead of sending the normal AFK self message

### Requirement: Multi-setup AFK behavior is preserved
The system SHALL preserve existing multi-setup AFK behavior while changing internal boundaries and message encoding.

#### Scenario: Paper slave detects AFK
- **WHEN** a Paper or Folia-compatible slave detects that a player became AFK
- **THEN** the slave SHALL send a versioned AFK-start message to the master
- **THEN** the slave SHALL NOT apply canonical storage side effects locally

#### Scenario: Paper slave detects AFK resume
- **WHEN** a Paper or Folia-compatible slave detects that a player resumed from AFK
- **THEN** the slave SHALL send a versioned AFK-resume message to the master
- **THEN** the slave SHALL NOT restart canonical session accumulation locally

#### Scenario: Proxy master applies AFK transition
- **WHEN** a Velocity or Bungee master receives a valid AFK transition message for an online player
- **THEN** the master SHALL apply the same AFK side effects that a local master-owned AFK transition would apply
- **THEN** time-removal adjustments SHALL use the player's current server and world context when available
