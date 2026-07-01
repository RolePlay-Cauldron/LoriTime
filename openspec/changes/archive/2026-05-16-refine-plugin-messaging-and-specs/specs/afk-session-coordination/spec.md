## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: AFK plugin message diagnostics are actionable
The system SHALL log AFK plugin-message decode and application failures with enough context to diagnose incompatible or malformed slave messages.

#### Scenario: AFK message fails during decoding
- **WHEN** an AFK plugin message cannot be decoded
- **THEN** the warning or error SHALL identify that the failed message belongs to the AFK plugin-message family

#### Scenario: AFK message fails during side effects
- **WHEN** a supported AFK plugin message is decoded but applying the transition fails
- **THEN** the error SHALL identify the AFK transition and target player UUID when available

### Requirement: AFK coordination spec has current purpose
The AFK coordination specification SHALL describe its current purpose instead of retaining archive placeholder text.

#### Scenario: Developer reads AFK coordination spec
- **WHEN** a developer opens the AFK coordination specification
- **THEN** the purpose SHALL summarize AFK transition decisions, multi-setup AFK messaging, and canonical master-side AFK side effects
