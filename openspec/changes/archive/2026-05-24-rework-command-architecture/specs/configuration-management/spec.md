## ADDED Requirements

### Requirement: Commands configuration file
The system SHALL load command names and aliases from a separate human-managed `commands.yml` configuration file.

#### Scenario: Commands config is created
- **WHEN** LoriTime starts and `commands.yml` is missing
- **THEN** the system SHALL create `commands.yml` from the bundled default template

#### Scenario: Commands config reloads
- **WHEN** the local LoriTime runtime is reloaded
- **THEN** the system SHALL reload `commands.yml`
- **THEN** subsequent command registration or command metadata resolution SHALL use the reloaded command configuration

#### Scenario: Commands config remains separate from main config
- **WHEN** command aliases are customized
- **THEN** the system SHALL read those customizations from `commands.yml`
- **THEN** the system SHALL NOT require command alias customization in `config.yml`

### Requirement: Profile-specific command alias configuration
The `commands.yml` file SHALL support separate command name and alias sections for proxy and backend runtime profiles.

#### Scenario: Proxy profile section
- **WHEN** command aliases are configured for proxy runtimes
- **THEN** `commands.yml` SHALL provide a `profiles.proxy` section for supported proxy command nodes

#### Scenario: Backend canonical profile section
- **WHEN** command aliases are configured for backend storage-owning runtimes
- **THEN** `commands.yml` SHALL provide a `profiles.backend.canonical` section for supported backend canonical command nodes

#### Scenario: Backend slave profile section
- **WHEN** command aliases are configured for backend slave runtimes
- **THEN** `commands.yml` SHALL provide a `profiles.backend.slave` section for supported backend slave command nodes

#### Scenario: Backend wording is platform-neutral
- **WHEN** the bundled command configuration describes server-side platform profiles
- **THEN** the profile name SHALL use `backend`
- **THEN** the profile name SHALL NOT use `paper` as the generic server-side profile key

### Requirement: Command completion configuration
The main configuration SHALL expose command completion settings that are not aliases.

#### Scenario: Recent player days default
- **WHEN** a config file is created or updated from the bundled template
- **THEN** `config.yml` SHALL contain a command completion recent-player window with a default value of 30 days

#### Scenario: Recent player days read
- **WHEN** the command completion cache refreshes recent stored player identities
- **THEN** the system SHALL use the configured recent-player window from `config.yml`
