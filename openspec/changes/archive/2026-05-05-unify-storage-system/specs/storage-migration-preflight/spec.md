## MODIFIED Requirements

### Requirement: Storage method configuration key
The system SHALL use `storageMethod` as the runtime configuration key for selecting the storage backend and SHALL construct the unified storage system for the selected backend.

#### Scenario: storageMethod selects SQLite
- **WHEN** the configuration contains `storageMethod: sqlite`
- **THEN** the system SHALL initialize SQLite-backed unified storage

#### Scenario: storageMethod selects remote SQL
- **WHEN** the configuration contains `storageMethod: mysql` or `storageMethod: mariadb`
- **THEN** the system SHALL initialize unified storage backed by the matching remote SQL storage
