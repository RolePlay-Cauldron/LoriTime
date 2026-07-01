## ADDED Requirements

### Requirement: Optional admin storage maintenance contract
The system SHALL expose admin storage maintenance behavior through a dedicated optional contract separate from the runtime `UnifiedStorage` contract.

#### Scenario: Runtime storage contract remains compatible
- **WHEN** existing custom storage implements `UnifiedStorage`
- **THEN** the system SHALL NOT require that implementation to add storage maintenance methods

#### Scenario: Maintenance unsupported by custom storage
- **WHEN** admin maintenance behavior is requested from a storage implementation that does not provide the maintenance contract
- **THEN** the system SHALL report storage maintenance as unsupported
- **THEN** the system SHALL NOT attempt database-specific casts or direct table access

#### Scenario: Database storage provides maintenance
- **WHEN** the default database-backed storage is loaded on a storage-owning runtime
- **THEN** the system SHALL provide the admin storage maintenance contract for that storage

### Requirement: Preview-first maintenance operations
Admin storage maintenance operations SHALL produce a preview before applying any transfer or delete operation.

#### Scenario: Transfer preview reports impact
- **WHEN** an admin storage transfer is previewed
- **THEN** the preview SHALL include the source scope, target scope, affected session rows, affected adjustment rows, affected player count, and whether target data already exists

#### Scenario: Delete preview reports impact
- **WHEN** an admin storage delete is previewed
- **THEN** the preview SHALL include the target scope, affected session rows, affected adjustment rows, and affected player count

#### Scenario: Confirmation protects execution
- **WHEN** a maintenance operation requires confirmation
- **THEN** execution SHALL require confirmation bound to the previewed operation details
- **THEN** execution SHALL reject confirmation for a different source, target, operation type, or affected-data fingerprint

### Requirement: Storage-type transfer preparation
The system SHALL prepare maintenance behavior for transferring all LoriTime data between supported storage types.

#### Scenario: Target storage must be empty
- **WHEN** all data is transferred from one storage type to another
- **THEN** the target storage SHALL be verified empty before the transfer is applied

#### Scenario: Non-empty target is rejected
- **WHEN** storage-type transfer is requested and the target storage contains LoriTime player, session, adjustment, server, or world data
- **THEN** the transfer SHALL be rejected before writing data

#### Scenario: Full history is preserved
- **WHEN** storage-type transfer is applied
- **THEN** the target storage SHALL receive player identities, session history, manual adjustments, server names, world names, timestamps, reasons, actor metadata, and scopes

### Requirement: Server dataset transfer
The system SHALL support transferring one or more server-scoped datasets from source server names to target server names.

#### Scenario: Transfer server to new target
- **WHEN** a source server dataset is transferred to a target server that does not exist
- **THEN** the system SHALL create the target server
- **THEN** the system SHALL preserve source world names under the target server
- **THEN** the system SHALL move matching session rows and server/world-scoped adjustment rows to the target server context

#### Scenario: Transfer server to existing target requires confirmation
- **WHEN** a source server dataset is transferred to a target server that already contains LoriTime data
- **THEN** the preview SHALL mark the operation as a merge
- **THEN** execution SHALL require explicit confirmation before adding source data to the existing target entries

#### Scenario: Existing target world requires confirmation
- **WHEN** a source server contains a world name that already exists on the target server
- **THEN** the preview SHALL identify the target world collision
- **THEN** execution SHALL require explicit confirmation before moving source world data into the existing target world

#### Scenario: Multiple server transfers are atomic
- **WHEN** multiple server datasets are transferred in one maintenance request
- **THEN** either all requested server transfers SHALL be applied or none SHALL be applied

### Requirement: World dataset transfer
The system SHALL support transferring one or more world-scoped datasets from source server/world pairs to target server/world pairs.

#### Scenario: Transfer world to new target world
- **WHEN** a source world dataset is transferred to a target world that does not exist
- **THEN** the system SHALL create the target server and world when missing
- **THEN** the system SHALL move matching session rows and world-scoped adjustment rows to the target world context

#### Scenario: Transfer world to existing target requires confirmation
- **WHEN** a source world dataset is transferred to a target world that already contains LoriTime data
- **THEN** the preview SHALL mark the operation as a merge
- **THEN** execution SHALL require explicit confirmation before adding source data to the existing target world entries

#### Scenario: Multiple world transfers are atomic
- **WHEN** multiple world datasets are transferred in one maintenance request
- **THEN** either all requested world transfers SHALL be applied or none SHALL be applied

### Requirement: Scoped storage deletion
The system SHALL support deleting LoriTime data for a whole server scope or one world scope.

#### Scenario: Delete server data
- **WHEN** server-scoped data is deleted for a server
- **THEN** the system SHALL delete session rows for worlds on that server
- **THEN** the system SHALL delete server-scoped adjustments for that server
- **THEN** the system SHALL delete world-scoped adjustments for worlds on that server

#### Scenario: Delete world data
- **WHEN** world-scoped data is deleted for one server/world pair
- **THEN** the system SHALL delete session rows for that world
- **THEN** the system SHALL delete world-scoped adjustments for that world
- **THEN** the system SHALL NOT delete server-scoped adjustments for the containing server

#### Scenario: Deletion requires confirmation
- **WHEN** scoped storage deletion would remove at least one session or adjustment row
- **THEN** execution SHALL require explicit confirmation before deleting data

#### Scenario: Player identity cleanup is conservative
- **WHEN** scoped storage data is deleted
- **THEN** the system SHALL NOT delete player identities unless they have no remaining LoriTime session or adjustment data and the maintenance policy explicitly allows orphan cleanup
