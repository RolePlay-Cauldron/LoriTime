## ADDED Requirements

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

## MODIFIED Requirements

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
