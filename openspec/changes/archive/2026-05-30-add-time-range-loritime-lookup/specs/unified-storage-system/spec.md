## ADDED Requirements

### Requirement: Time-ranged storage totals
The unified storage service SHALL support player time totals filtered by an optional time range while preserving existing global, server, and world scope semantics.

#### Scenario: Unbounded total remains unchanged
- **WHEN** runtime code queries a player's total time without a time range
- **THEN** the unified storage service SHALL preserve the existing unbounded total behavior

#### Scenario: Global ranged total includes matching data
- **WHEN** runtime code queries a player's global total time with a time range
- **THEN** the unified storage service SHALL include overlapping persisted session duration for that player within the range
- **THEN** the unified storage service SHALL include signed manual adjustments for that player whose `created_at` is inside the range regardless of adjustment scope

#### Scenario: Server ranged total includes matching server data
- **WHEN** runtime code queries a player's server total time with a time range
- **THEN** the unified storage service SHALL include overlapping persisted session duration for that player on that server within the range
- **THEN** the unified storage service SHALL include server-scoped adjustments for that server whose `created_at` is inside the range
- **THEN** the unified storage service SHALL include world-scoped adjustments for worlds on that server whose `created_at` is inside the range
- **THEN** the unified storage service SHALL NOT include global adjustments

#### Scenario: World ranged total includes exact world data
- **WHEN** runtime code queries a player's world total time with a time range
- **THEN** the unified storage service SHALL include overlapping persisted session duration for that player in that exact server and world within the range
- **THEN** the unified storage service SHALL include world-scoped adjustments for that exact server and world whose `created_at` is inside the range
- **THEN** the unified storage service SHALL NOT include global or server-scoped adjustments

#### Scenario: Persisted sessions are clipped to range boundaries
- **WHEN** a persisted session starts before the requested range and ends inside or after the requested range
- **THEN** the unified storage service SHALL count only the portion of the session that overlaps the requested range

#### Scenario: Non-overlapping persisted sessions are excluded
- **WHEN** a persisted session has no overlap with the requested time range
- **THEN** the unified storage service SHALL exclude that session from the ranged total
- **THEN** that excluded session SHALL NOT cause an otherwise empty ranged query to return a zero-valued hit

#### Scenario: Active session contributes to matching ranged total
- **WHEN** runtime code queries a player's ranged total while that player has an active session
- **THEN** the active session elapsed time SHALL contribute only when the active session context matches the requested scope and overlaps the requested range
- **THEN** only the overlapping active session duration SHALL be counted

#### Scenario: Active session outside ranged total is excluded
- **WHEN** runtime code queries a player's ranged total and the player's active session does not overlap the requested range
- **THEN** the active session SHALL NOT contribute to the result

### Requirement: Time range API contract
Runtime and public API time query contracts SHALL expose time-range-aware lookup methods.

#### Scenario: Storage exposes ranged lookup
- **WHEN** runtime code needs a player total for a scope and time range
- **THEN** the focused storage query contract SHALL provide a ranged lookup method
- **THEN** implementations SHALL combine matching session durations and matching manual adjustments for that range

#### Scenario: Public service exposes ranged lookup
- **WHEN** API consumers need a player total for a scope and time range
- **THEN** the public LoriTime service SHALL provide a ranged lookup method returning an optional duration asynchronously

#### Scenario: Invalid time range is not accepted by storage API
- **WHEN** runtime or API code attempts to construct or pass an invalid time range
- **THEN** the system SHALL reject the invalid range before issuing storage queries
