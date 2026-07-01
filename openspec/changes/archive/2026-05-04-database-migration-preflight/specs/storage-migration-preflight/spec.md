## ADDED Requirements

### Requirement: SQL startup path detection
The system SHALL inspect the configured SQL database before invoking the database updater.

#### Scenario: Versioned database starts normally
- **WHEN** the configured database contains the LoriTime version table
- **THEN** the system SHALL run the normal database update path without running first-startup queries

#### Scenario: Legacy SQL database is detected
- **WHEN** the configured database has no LoriTime version table and contains a valid LoriTime 1.x aggregate table
- **THEN** the system SHALL create the version table, record version `1`, and run normal database updates

#### Scenario: Fresh SQL database is detected
- **WHEN** the configured database has no LoriTime version table and no valid LoriTime 1.x aggregate table
- **THEN** the system SHALL run first-startup database initialization

### Requirement: Legacy SQL migration routing
The system SHALL route LoriTime 1.x SQL databases through the versioned migration path so version `2` migration queries transform legacy aggregate data into the normalized schema.

#### Scenario: Legacy aggregate time migrates to fallback scope
- **WHEN** a LoriTime 1.x SQL aggregate table contains player UUID, name, and time data
- **THEN** the system SHALL migrate the data into the normalized player/server/world/time tables using the configured fallback server and world

#### Scenario: Legacy SQL migration is not run for fresh installs
- **WHEN** a fresh database is initialized
- **THEN** the system SHALL create the normalized schema without executing legacy aggregate copy or drop statements

### Requirement: Legacy flat-file migration
The system SHALL detect legacy `names.yml` and `time.yml` files and migrate them into SQLite.

#### Scenario: Legacy flat files are backed up before import
- **WHEN** legacy `names.yml` or `time.yml` files are present
- **THEN** the system SHALL back up the files before writing migrated data to SQLite

#### Scenario: Legacy flat-file data imports into SQLite
- **WHEN** backed-up legacy flat files contain player names and time values
- **THEN** the system SHALL create or use a SQLite 2.0 database and import the legacy values into the normalized schema

#### Scenario: Backup failure aborts flat-file migration
- **WHEN** backing up legacy flat files fails
- **THEN** the system SHALL abort the flat-file migration without importing partial data

#### Scenario: Successful flat-file migration is not repeated
- **WHEN** legacy flat files were already migrated successfully
- **THEN** the system SHALL not import the same legacy values again on later startups

### Requirement: Storage method configuration key
The system SHALL use `storageMethod` as the runtime configuration key for selecting the storage backend.

#### Scenario: storageMethod selects SQLite
- **WHEN** the configuration contains `storageMethod: sqlite`
- **THEN** the system SHALL initialize SQLite storage

#### Scenario: storageMethod selects remote SQL
- **WHEN** the configuration contains `storageMethod: mysql` or `storageMethod: mariadb`
- **THEN** the system SHALL initialize the matching remote SQL storage
