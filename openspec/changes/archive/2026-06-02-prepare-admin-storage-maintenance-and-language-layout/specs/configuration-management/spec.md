## ADDED Requirements

### Requirement: Language folder layout
The system SHALL store bundled and generated localization files in a `language/` folder.

#### Scenario: Missing language folder is created
- **WHEN** LoriTime starts and the `language/` folder is missing
- **THEN** the system SHALL create the folder before loading the selected localization file

#### Scenario: Selected localization loads from language folder
- **WHEN** `general.language` is set to a language id
- **THEN** the system SHALL load the localization file from `language/<language>.yml`

#### Scenario: Bundled localization defaults are created in language folder
- **WHEN** the selected localization file is missing
- **THEN** the system SHALL create the bundled default localization file inside the `language/` folder

#### Scenario: Existing root language file migrates
- **WHEN** an existing installation has `<language>.yml` in the plugin data root and no matching `language/<language>.yml`
- **THEN** the system SHALL move or copy the existing file to `language/<language>.yml` using the normal backup/update safeguards

#### Scenario: Language folder is canonical
- **WHEN** a localization file has been migrated or created in `language/`
- **THEN** the system SHALL treat the folder location as canonical for future reloads and updates

### Requirement: Camel case localization keys
Localization keys SHALL use dot-separated paths whose multi-word path segments are camelCase.

#### Scenario: Bundled language keys use camel case
- **WHEN** bundled localization files are created or updated
- **THEN** their message keys SHALL use camelCase path segments for multi-word names
- **THEN** their message keys SHALL NOT introduce snake_case path segments

#### Scenario: Runtime lookups use canonical keys
- **WHEN** runtime code reads a localization message
- **THEN** it SHALL request the canonical camelCase localization key

#### Scenario: Legacy key migration preserves values
- **WHEN** a user localization file contains a known legacy key that is renamed to camelCase
- **THEN** the localization update path SHALL preserve the user's value at the canonical camelCase key
- **THEN** the obsolete legacy key SHALL be removed from the updated managed localization file

#### Scenario: Unknown custom keys are preserved
- **WHEN** a user localization file contains keys that are not part of the bundled localization template
- **THEN** the localization update path SHALL preserve those keys unless a migration explicitly removes them
