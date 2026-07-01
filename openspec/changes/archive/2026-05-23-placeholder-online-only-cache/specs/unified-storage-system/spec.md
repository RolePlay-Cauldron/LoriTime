## ADDED Requirements

### Requirement: Placeholder time rendering is online-player-only
Paper/Folia time placeholder rendering SHALL only request cache refreshes for online players and SHALL return deterministic fallback values for offline or missing players without touching database-backed storage.

#### Scenario: Online placeholder uses cache refresh path
- **WHEN** a Paper or Folia-compatible instance renders an online-time placeholder for an online player during normal runtime
- **THEN** the placeholder path SHALL read cached or non-database time data
- **THEN** the placeholder path SHALL request an asynchronous refresh for that online player
- **THEN** the placeholder path SHALL NOT query database-backed total time directly

#### Scenario: Offline placeholder uses fallback without refresh
- **WHEN** a Paper or Folia-compatible instance renders an online-time placeholder for an offline player
- **THEN** the placeholder path SHALL return deterministic fallback values based on zero online time
- **THEN** the placeholder path SHALL NOT request an asynchronous storage refresh
- **THEN** the placeholder path SHALL NOT send a plugin-message refresh request

#### Scenario: Missing placeholder player uses fallback without refresh
- **WHEN** a Paper or Folia-compatible instance renders an online-time placeholder without a player object
- **THEN** the placeholder path SHALL return deterministic fallback values based on zero online time
- **THEN** the placeholder path SHALL NOT request an asynchronous storage refresh
- **THEN** the placeholder path SHALL NOT send a plugin-message refresh request
