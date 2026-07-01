## ADDED Requirements

### Requirement: AFK session stop reason
The system SHALL persist active session stops caused by AFK handling with a dedicated AFK session reason that is distinct from normal player leave and AFK adjustment rows.

#### Scenario: Stop-count bypass stops active session as AFK
- **WHEN** a player becomes AFK and AFK handling stops online-time accumulation because of the configured bypass behavior
- **THEN** the active session SHALL be stopped with the AFK session reason
- **THEN** the active session SHALL NOT be stopped with `PLAYER_LEAVE`

#### Scenario: AFK kick stops active session as AFK
- **WHEN** a player is kicked by AFK auto-kick enforcement
- **THEN** the active session SHALL be stopped with the AFK session reason
- **THEN** the active session SHALL NOT be stopped with `PLAYER_LEAVE`

#### Scenario: AFK adjustment remains adjustment reason
- **WHEN** AFK handling removes counted online time through a signed adjustment
- **THEN** the adjustment row SHALL continue to use `AFK_ADJUSTMENT`
- **THEN** the session row SHALL use the AFK session reason only when the active session lifecycle is stopped because of AFK handling

#### Scenario: Normal leave remains player leave
- **WHEN** a player disconnects without an AFK-caused stop or AFK kick marker
- **THEN** the active session SHALL be stopped with `PLAYER_LEAVE`
