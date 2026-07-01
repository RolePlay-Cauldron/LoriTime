## MODIFIED Requirements

### Requirement: Legacy flat-file migration
The system SHALL detect legacy `names.yml` and `time.yml` files and migrate them into SQLite.

#### Scenario: Legacy flat files are queued for startup backup before import
- **WHEN** legacy `names.yml` or `time.yml` files are present
- **THEN** the system SHALL add them to the normal startup backup before writing migrated data to SQLite

#### Scenario: Legacy flat-file data imports into canonical SQLite prefix
- **WHEN** backed-up legacy flat files contain player names and time values
- **THEN** the system SHALL create or use a SQLite 2.0 database using the `loritime` table prefix and import the legacy values into the normalized schema

#### Scenario: Successful flat-file migration is not repeated
- **WHEN** legacy flat files were already migrated successfully
- **THEN** the system SHALL not import the same legacy values again on later startups
