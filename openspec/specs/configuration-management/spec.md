# configuration-management Specification

## Purpose
Defines structured configuration access, versioned human-managed file migrations, template merging, localization updates, and separation from legacy flat data files.
## Requirements
### Requirement: Structured configuration sections
The system SHALL expose a platform-independent configuration section API that represents nested configuration paths without depending on Bukkit, Paper, Bungee, or Velocity APIs.

#### Scenario: Read existing section
- **WHEN** a caller requests a section for an existing nested config path
- **THEN** the system SHALL return a section object scoped to that path

#### Scenario: Missing section
- **WHEN** a caller requests a section for a missing path or a path that contains a scalar value
- **THEN** the system SHALL report that no section exists without throwing a platform-specific exception

#### Scenario: Enumerate section keys
- **WHEN** a caller requests the direct keys of a section
- **THEN** the system SHALL return only the immediate child keys of that section

#### Scenario: Enumerate nested section keys
- **WHEN** a caller requests recursive keys of a section
- **THEN** the system SHALL return nested child paths relative to that section

### Requirement: Typed value access from sections
Configuration sections SHALL provide typed accessors for strings, numbers, booleans, lists, objects, and nested sections using paths relative to the current section.

#### Scenario: Read typed value
- **WHEN** a caller reads a typed value from a section-relative path that exists with a compatible type
- **THEN** the system SHALL return the configured value

#### Scenario: Read value with default
- **WHEN** a caller reads a missing section-relative path with a default value
- **THEN** the system SHALL return the supplied default value

#### Scenario: Read incompatible value with default
- **WHEN** a caller reads a section-relative path whose value is incompatible with the requested type and a default value is supplied
- **THEN** the system SHALL return the supplied default value and avoid a raw class-cast failure

### Requirement: Dot-path compatibility
The system SHALL keep the existing dot-path configuration access surface available while resolving paths against the structured configuration tree.

#### Scenario: Existing dot-path read
- **WHEN** existing code reads `data.poolSettings.maximumPoolSize` through the configuration dot-path API
- **THEN** the system SHALL resolve the value from the nested `data.poolSettings` section

#### Scenario: Existing dot-path write
- **WHEN** existing code writes a scalar value through the configuration dot-path API
- **THEN** the system SHALL update the corresponding nested path in the structured document

#### Scenario: Existing reload
- **WHEN** existing code reloads a configuration file
- **THEN** the system SHALL reload the structured document and keep subsequent dot-path and section reads consistent

### Requirement: Human config update pipeline
The system SHALL update human-managed configuration files through an explicit config update pipeline that loads the current file, applies versioned migrations, overlays preserved values onto the latest bundled template, and writes the result after backup.

#### Scenario: Add missing key
- **WHEN** a bundled config template contains a new key that is missing from the user's config
- **THEN** the system SHALL add the key with the template default value

#### Scenario: Preserve existing value
- **WHEN** a bundled config template contains a key that already exists in the user's config
- **THEN** the system SHALL preserve the user's configured value unless a migration explicitly changes it

#### Scenario: Delete obsolete key
- **WHEN** a config migration marks a key as deleted
- **THEN** the system SHALL omit that key from the updated human-managed config file

#### Scenario: Backup before update
- **WHEN** the system needs to rewrite an existing human-managed config file
- **THEN** the system SHALL add the original file to backup before writing the updated file

### Requirement: Versioned config migrations
The system SHALL support ordered configuration migrations from a detected schema version to the latest schema version.

#### Scenario: Migrate unversioned config
- **WHEN** a config file has no schema version
- **THEN** the system SHALL treat it as the configured legacy baseline version and apply all later migrations in order

#### Scenario: Rename key while preserving value
- **WHEN** a migration renames an old key to a new key and the old key exists
- **THEN** the system SHALL write the old value to the new key and remove the old key

#### Scenario: Move key while preserving value
- **WHEN** a migration moves a key from one section to another and the source key exists
- **THEN** the system SHALL write the existing value to the target path and remove the source path

#### Scenario: Transform value
- **WHEN** a migration defines a value transformation for an existing key
- **THEN** the system SHALL store the transformed value at the migration target path

#### Scenario: Standardize V2 table prefix
- **WHEN** a legacy LoriTime config is migrated to the V2 schema
- **THEN** the system SHALL set `data.tablePrefix` to `loritime` regardless of the legacy `mysql.tablePrefix` value

#### Scenario: Current config version
- **WHEN** a config file already uses the latest schema version
- **THEN** the system SHALL skip migration operations and only ensure template defaults are present where appropriate

### Requirement: Separate config files from data files
The system SHALL distinguish human-managed configuration files from flat data files so config migration behavior is not applied to simple key/value storage data.

#### Scenario: Human config file
- **WHEN** `config.yml` is loaded during startup
- **THEN** the system SHALL use the structured configuration and migration pipeline

#### Scenario: Localization file
- **WHEN** a localization file is loaded
- **THEN** the system SHALL support structured section and typed value reads without requiring database or storage migrations

#### Scenario: Flat data file
- **WHEN** legacy file-backed data storage reads or writes arbitrary key/value data
- **THEN** the system SHALL avoid applying human config template migrations to that data file

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

### Requirement: Command completion configuration
The main configuration SHALL expose command completion settings that are not aliases.

#### Scenario: Recent player days default
- **WHEN** a config file is created or updated from the bundled template
- **THEN** `config.yml` SHALL contain a command completion recent-player window with a default value of 30 days

#### Scenario: Recent player days read
- **WHEN** the command completion cache refreshes recent stored player identities
- **THEN** the system SHALL use the configured recent-player window from `config.yml`

### Requirement: Language folder layout
The system SHALL store bundled, generated, and server-owned custom localization files in a canonical `language/` folder.

#### Scenario: Missing language folder is created
- **WHEN** LoriTime starts and the `language/` folder is missing
- **THEN** the system SHALL create the folder before loading localization files

#### Scenario: Configured localization loads from language folder
- **WHEN** `general.language` is set to a language id
- **THEN** the system SHALL resolve the configured localization from `language/<language>.yml`

#### Scenario: Bundled localization defaults are created in language folder
- **WHEN** a required bundled localization file is missing
- **THEN** the system SHALL create the bundled localization file inside the `language/` folder

#### Scenario: Custom localization is not copied from resources
- **WHEN** a configured or requested language id is not a bundled language id
- **THEN** the system SHALL NOT attempt to copy `language/<language>.yml` from bundled resources
- **THEN** the custom language SHALL be loaded only if `language/<language>.yml` already exists in the plugin data folder

#### Scenario: Existing root language file migrates
- **WHEN** an existing installation has `<language>.yml` in the plugin data root and no matching `language/<language>.yml`
- **THEN** the system SHALL move or copy the existing file to `language/<language>.yml` using the normal backup/update safeguards

#### Scenario: Language folder is canonical
- **WHEN** a localization file has been migrated or created in `language/`
- **THEN** the system SHALL treat the folder location as canonical for future reloads and updates

### Requirement: Camel case localization keys
Localization message keys SHALL use dot-separated paths whose multi-word path segments are camelCase.

#### Scenario: Bundled language keys use camel case
- **WHEN** bundled localization files are created or updated
- **THEN** their message keys SHALL use camelCase path segments for multi-word names
- **THEN** their message keys SHALL NOT introduce snake_case path segments

#### Scenario: Runtime lookups use canonical keys
- **WHEN** runtime code reads a localization message
- **THEN** it SHALL request the canonical camelCase localization key

#### Scenario: Unknown custom keys are preserved
- **WHEN** a user localization file contains keys that are not part of the bundled localization template
- **THEN** the localization update path SHALL preserve those keys unless a migration explicitly removes them

### Requirement: Versioned localization schema
Localization files SHALL use the current versioned localization schema and SHALL NOT require runtime compatibility with legacy language file schemas.

#### Scenario: Bundled languages use current schema
- **WHEN** bundled language resources are packaged
- **THEN** each bundled language file SHALL contain the current localization schema version
- **THEN** each bundled language file SHALL identify its locale tag
- **THEN** each bundled language file SHALL store runtime messages in the schema-defined message section

#### Scenario: Legacy language schema is rejected
- **WHEN** a language file uses a legacy schema instead of the current localization schema
- **THEN** the localization loader SHALL reject that file as invalid
- **THEN** the localization resolver SHALL continue using configured fallback candidates when available

### Requirement: Known localization loading
The localization resolver SHALL load only required or previously requested language tags from the plugin data `language/` folder during the current localization lifecycle.

#### Scenario: Startup loads required languages
- **WHEN** LoriTime initializes localization
- **THEN** the resolver SHALL attempt to load the configured default language from `language/<language>.yml`
- **THEN** the resolver SHALL attempt to load the hard fallback language from `language/en-us.yml`

#### Scenario: Requested custom language loads from language folder
- **WHEN** runtime message resolution requests a language tag that has not been loaded yet
- **THEN** the resolver SHALL attempt to load only `language/<tag>.yml` from the plugin data folder
- **THEN** the resolver SHALL remember successfully loaded tags for the current localization lifecycle

#### Scenario: Reload uses known language tags
- **WHEN** localization is reloaded
- **THEN** the resolver SHALL reload the configured default language, the hard fallback language, and language tags previously loaded during the current localization lifecycle
- **THEN** the resolver SHALL NOT load unrelated YAML files merely because they exist in `language/`

#### Scenario: Missing custom language falls back
- **WHEN** a requested custom language file does not exist in the plugin data `language/` folder
- **THEN** the resolver SHALL use the configured fallback candidate chain for message resolution
- **THEN** the resolver SHALL report the missing language without creating a non-bundled resource file

