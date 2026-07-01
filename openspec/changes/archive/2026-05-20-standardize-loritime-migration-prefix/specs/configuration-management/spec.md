## MODIFIED Requirements

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
