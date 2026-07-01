## MODIFIED Requirements

### Requirement: Profile-specific command alias configuration
The `commands.yml` file SHALL support separate command name and alias sections for proxy and backend runtime profiles.

#### Scenario: Proxy profile section
- **WHEN** command aliases are configured for proxy runtimes
- **THEN** `commands.yml` SHALL provide a `profiles.proxy` section for supported proxy command nodes
- **THEN** the default proxy admin command name SHALL be `plta`

#### Scenario: Backend canonical profile section
- **WHEN** command aliases are configured for backend storage-owning runtimes
- **THEN** `commands.yml` SHALL provide a `profiles.backend.canonical` section for supported backend canonical command nodes
- **THEN** the default backend canonical admin command name SHALL be `lta`

#### Scenario: Backend slave profile section
- **WHEN** command aliases are configured for backend slave runtimes
- **THEN** `commands.yml` SHALL provide a `profiles.backend.slave` section for supported backend slave command nodes
- **THEN** the default backend slave admin command name SHALL be `lta`

#### Scenario: Backend wording is platform-neutral
- **WHEN** the bundled command configuration describes server-side platform profiles
- **THEN** the profile name SHALL use `backend`
- **THEN** the profile name SHALL NOT use `paper` as the generic server-side profile key

#### Scenario: Local command alias section is removed
- **WHEN** `commands.yml` is created from the bundled template
- **THEN** the template SHALL NOT include a local command node

#### Scenario: Modify command alias section is provided
- **WHEN** `commands.yml` is created from the bundled template
- **THEN** canonical runtime profiles SHALL include a modify command node for canonical storage mutation commands
- **THEN** backend slave profiles SHALL NOT include a modify command node
