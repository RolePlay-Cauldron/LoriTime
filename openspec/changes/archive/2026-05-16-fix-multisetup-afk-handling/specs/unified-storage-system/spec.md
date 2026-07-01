## ADDED Requirements

### Requirement: Multi-setup AFK handling
The system SHALL keep AFK detection and AFK side effects coordinated across multi-setup instances while using the existing AFK configuration keys.

#### Scenario: Paper or Folia slave detects AFK
- **WHEN** a Paper or Folia-compatible instance runs in `slave` mode with `afk.enabled` enabled
- **THEN** the instance SHALL observe local player activity and run AFK idle detection
- **THEN** the instance SHALL send AFK state changes to the master over the configured AFK plugin messaging channel

#### Scenario: Proxy master applies AFK side effects
- **WHEN** a Velocity or Bungee instance runs in `master` mode with `afk.enabled` enabled and receives a valid AFK state message for an online player
- **THEN** the proxy master SHALL apply configured AFK side effects including time adjustment, kick handling, and announcements

#### Scenario: Proxy master does not run idle detection
- **WHEN** a Velocity or Bungee instance runs as a proxy
- **THEN** the instance SHALL NOT run the repeated AFK idle detection loop

#### Scenario: Existing AFK settings remain authoritative
- **WHEN** AFK handling is configured
- **THEN** the system SHALL use the existing `afk.enabled`, `afk.after`, `afk.removeTime`, `afk.autoKick`, and `afk.repeatCheck` settings
- **THEN** the system SHALL NOT require separate detection or enforcement AFK settings

#### Scenario: AFK reload refreshes scheduling
- **WHEN** runtime configuration is reloaded and the AFK feature has been initialized
- **THEN** the system SHALL reload AFK configuration values and restart AFK idle detection according to the current settings
