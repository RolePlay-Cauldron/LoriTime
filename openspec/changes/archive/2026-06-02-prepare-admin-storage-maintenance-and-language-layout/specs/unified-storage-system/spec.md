## ADDED Requirements

### Requirement: Runtime storage API compatibility for maintenance
Admin storage maintenance support SHALL NOT change the required runtime storage API surface.

#### Scenario: UnifiedStorage does not expose maintenance methods
- **WHEN** this change is implemented
- **THEN** `UnifiedStorage` SHALL remain focused on runtime identity, time query, adjustment, session, and player deletion behavior
- **THEN** storage maintenance transfer and scoped delete methods SHALL NOT be added as required `UnifiedStorage` methods

#### Scenario: Maintenance uses focused storage internals
- **WHEN** database-backed maintenance transfers or deletes scoped storage data
- **THEN** the implementation SHALL use focused maintenance contracts and table helpers
- **THEN** normal runtime callers SHALL continue using existing storage contracts

### Requirement: Maintenance preserves scoped total semantics
Storage maintenance operations SHALL preserve scoped time total behavior for sessions and manual adjustments.

#### Scenario: Server transfer preserves server totals
- **WHEN** a server dataset is transferred to a target server
- **THEN** player totals for the target server SHALL include transferred sessions and matching server/world-scoped adjustments
- **THEN** global player totals SHALL remain unchanged except for the source and target scope labels

#### Scenario: World transfer preserves world totals
- **WHEN** a world dataset is transferred to a target world
- **THEN** player totals for the target world SHALL include transferred sessions and matching world-scoped adjustments
- **THEN** global player totals SHALL remain unchanged except for the source and target scope labels

#### Scenario: Scoped delete removes matching contributions
- **WHEN** server or world data is deleted through maintenance behavior
- **THEN** scoped and global totals SHALL no longer include the deleted session or adjustment rows
- **THEN** unrelated scopes SHALL retain their existing contributions
