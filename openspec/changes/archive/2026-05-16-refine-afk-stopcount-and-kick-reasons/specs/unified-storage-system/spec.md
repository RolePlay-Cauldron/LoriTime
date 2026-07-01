## ADDED Requirements

### Requirement: AFK state messages are independent from stop-count policy
The system SHALL send AFK state messages for non-kicked AFK state transitions independently from whether AFK handling stops online-time accumulation.

#### Scenario: Non-kicked AFK player receives AFK start message
- **WHEN** a player becomes AFK and is not kicked by AFK auto-kick enforcement
- **THEN** the player SHALL receive the AFK self message
- **THEN** online players with the AFK announce permission SHALL receive the AFK announce message

#### Scenario: Stop-count bypass does not suppress AFK start message
- **WHEN** a player becomes AFK and has the stop-count bypass permission
- **THEN** the player SHALL receive the AFK self message
- **THEN** online players with the AFK announce permission SHALL receive the AFK announce message

#### Scenario: AFK resume message is sent for non-kicked AFK resume
- **WHEN** a non-kicked AFK player resumes from AFK
- **THEN** the player SHALL receive the AFK resume self message
- **THEN** online players with the AFK announce permission SHALL receive the AFK resume announce message

#### Scenario: AFK kick uses kick messaging instead of AFK start message
- **WHEN** a player is kicked by AFK auto-kick enforcement
- **THEN** the player SHALL receive the configured AFK kick message through the kick operation
- **THEN** online players with the AFK kick announce permission SHALL receive the AFK kick announce message
- **THEN** the player SHALL NOT receive the AFK self message as a separate chat message

## MODIFIED Requirements

### Requirement: AFK session stop reason
The system SHALL persist active session stops caused by AFK handling with dedicated AFK session reasons that are distinct from normal player leave and AFK adjustment rows.

#### Scenario: AFK without stop-count bypass stops active session as AFK
- **WHEN** a player becomes AFK and does not have the stop-count bypass permission
- **THEN** the active session SHALL be stopped with `PLAYER_AFK`
- **THEN** the active session SHALL NOT be stopped with `PLAYER_LEAVE`

#### Scenario: Stop-count bypass keeps active session counting
- **WHEN** a player becomes AFK and has the stop-count bypass permission
- **THEN** the active session SHALL continue counting
- **THEN** the active session SHALL NOT be stopped with `PLAYER_AFK`
- **THEN** the active session SHALL NOT be stopped with `PLAYER_AFK_KICK`

#### Scenario: AFK kick stops active session with AFK kick reason
- **WHEN** a player is kicked by AFK auto-kick enforcement
- **THEN** the active session SHALL be stopped with `PLAYER_AFK_KICK`
- **THEN** the active session SHALL NOT be stopped with `PLAYER_AFK`
- **THEN** the active session SHALL NOT be stopped with `PLAYER_LEAVE`

#### Scenario: AFK adjustment remains adjustment reason
- **WHEN** AFK handling removes counted online time through a signed adjustment
- **THEN** the adjustment row SHALL continue to use `AFK_ADJUSTMENT`
- **THEN** the session row SHALL use an AFK session reason only when the active session lifecycle is stopped because of AFK handling

#### Scenario: Normal leave remains player leave
- **WHEN** a player disconnects without an AFK-caused stop or AFK kick marker
- **THEN** the active session SHALL be stopped with `PLAYER_LEAVE`
