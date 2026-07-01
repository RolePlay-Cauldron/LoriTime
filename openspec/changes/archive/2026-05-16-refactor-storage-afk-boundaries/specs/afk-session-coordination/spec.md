## ADDED Requirements

### Requirement: Explicit AFK transition decisions
The system SHALL derive AFK side effects from explicit transition decisions before executing player messaging, session accumulation changes, time adjustments, or kick handling.

#### Scenario: Player becomes AFK without auto-kick
- **WHEN** a player crosses the configured AFK threshold and is not auto-kicked
- **THEN** the system SHALL create an AFK-start transition for that player
- **THEN** the transition SHALL preserve existing AFK announcement, self-message, time removal, and stop-count permission behavior

#### Scenario: Player resumes from AFK
- **WHEN** a player leaves AFK state without having been kicked
- **THEN** the system SHALL create an AFK-resume transition for that player
- **THEN** the transition SHALL preserve existing AFK resume message and stopped-accumulation restart behavior

#### Scenario: Player is auto-kicked for AFK
- **WHEN** AFK auto-kick applies to a player
- **THEN** the system SHALL create an AFK-kick transition for that player
- **THEN** the transition SHALL mark the next disconnect as AFK-caused before kicking the player
- **THEN** the transition SHALL use kick messaging instead of sending the normal AFK self message

### Requirement: Versioned AFK plugin message protocol
The system SHALL encode slave-to-master AFK state messages with an explicit protocol version and typed transition value, and SHALL NOT accept legacy unversioned string-boolean AFK payloads.

#### Scenario: Master receives supported AFK protocol message
- **WHEN** a master receives an AFK plugin message with a supported protocol version and valid transition value
- **THEN** the master SHALL apply the requested AFK transition for the target online player

#### Scenario: Master receives unsupported AFK protocol version
- **WHEN** a master receives an AFK plugin message with an unsupported protocol version
- **THEN** the master SHALL ignore the message
- **THEN** the master SHALL log a warning that identifies the unsupported AFK protocol version

#### Scenario: Master receives invalid AFK transition value
- **WHEN** a master receives an AFK plugin message with an unknown transition value
- **THEN** the master SHALL ignore the message
- **THEN** the master SHALL log a warning that identifies the invalid AFK transition value

#### Scenario: Master receives legacy unversioned AFK payload
- **WHEN** a master receives an AFK plugin message using the legacy unversioned string-boolean payload
- **THEN** the master SHALL ignore the message
- **THEN** the master SHALL log a warning that the AFK payload is unsupported

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

### Requirement: AFK handling uses shared session context defaults
The system SHALL use shared session context defaults when AFK resume handling needs to restart accumulation without platform-specific context.

#### Scenario: AFK resume restarts stopped accumulation
- **WHEN** a player resumes from AFK after AFK handling stopped session accumulation
- **THEN** the restarted accumulation SHALL use the shared fallback server and world context values unless a more specific platform context is available
- **THEN** the restarted accumulation SHALL NOT hard-code fallback context values inside AFK handling
