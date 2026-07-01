# afk-session-coordination Specification

## Purpose
Defines AFK transition decisions, versioned multi-setup AFK messaging, and canonical master-side AFK side effects.
## Requirements
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

### Requirement: Versioned AFK plugin message protocol
The system SHALL encode slave-to-master AFK state messages with an explicit protocol version and typed transition value, and SHALL NOT accept legacy unversioned string-boolean AFK payloads.

#### Scenario: Master receives supported AFK protocol message
- **WHEN** a master receives an AFK plugin message with a supported protocol version and valid transition value
- **THEN** the master SHALL apply the requested AFK transition for the target online player

#### Scenario: Master receives unsupported AFK protocol version
- **WHEN** a master receives an AFK plugin message with an unsupported protocol version
- **THEN** the master SHALL ignore the message
- **THEN** the master SHALL log a warning that identifies the AFK message family and unsupported AFK protocol version

#### Scenario: Master receives invalid AFK transition value
- **WHEN** a master receives an AFK plugin message with an unknown transition value
- **THEN** the master SHALL ignore the message
- **THEN** the master SHALL log a warning that identifies the invalid AFK transition value

#### Scenario: Master receives legacy unversioned AFK payload
- **WHEN** a master receives an AFK plugin message using the legacy unversioned string-boolean payload
- **THEN** the master SHALL ignore the message
- **THEN** the master SHALL log a warning that the AFK payload is malformed or unsupported

#### Scenario: Master receives malformed AFK payload
- **WHEN** a master receives an AFK plugin message whose supported-version payload is incomplete
- **THEN** the master SHALL ignore the message
- **THEN** the master SHALL log a warning that identifies the malformed AFK payload

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

### Requirement: AFK handling uses shared session context defaults
The system SHALL use shared session context defaults when AFK resume handling needs to restart accumulation without platform-specific context.

#### Scenario: AFK resume restarts stopped accumulation
- **WHEN** a player resumes from AFK after AFK handling stopped session accumulation
- **THEN** the restarted accumulation SHALL use the shared fallback server and world context values unless a more specific platform context is available
- **THEN** the restarted accumulation SHALL NOT hard-code fallback context values inside AFK handling

### Requirement: AFK plugin message diagnostics are actionable
The system SHALL log AFK plugin-message decode and application failures with enough context to diagnose incompatible or malformed slave messages.

#### Scenario: AFK message fails during decoding
- **WHEN** an AFK plugin message cannot be decoded
- **THEN** the warning or error SHALL identify that the failed message belongs to the AFK plugin-message family

#### Scenario: AFK message fails during side effects
- **WHEN** a supported AFK plugin message is decoded but applying the transition fails
- **THEN** the error SHALL identify the AFK transition and target player UUID when available

