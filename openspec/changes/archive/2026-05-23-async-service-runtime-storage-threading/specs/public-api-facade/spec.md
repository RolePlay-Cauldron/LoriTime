## ADDED Requirements

### Requirement: Public storage-facing facade operations are asynchronous
The public API facade SHALL expose storage-facing player identity lookups, total-time reads, and manual adjustment writes as `CompletableFuture` operations whose blocking storage work is executed asynchronously under LoriTime control.

#### Scenario: Identity lookup returns future
- **WHEN** a caller asks the facade to resolve a known player UUID or latest player name
- **THEN** the facade SHALL return a `CompletableFuture` for the lookup result
- **THEN** the blocking storage lookup SHALL NOT be performed on the caller's platform main-thread path

#### Scenario: Total-time query returns future
- **WHEN** a caller asks the facade for a player's total online time
- **THEN** the facade SHALL return a `CompletableFuture` for the optional duration result
- **THEN** the completed result SHALL preserve the same storage-mode total-time semantics as the public time query operation

#### Scenario: Manual adjustment write returns future
- **WHEN** a caller asks the facade to add a signed manual time adjustment
- **THEN** the facade SHALL return a `CompletableFuture` that completes after the write has been attempted
- **THEN** the blocking storage write SHALL be executed asynchronously under LoriTime control

#### Scenario: Input validation happens before storage scheduling
- **WHEN** a caller supplies invalid facade input that can be validated without storage access
- **THEN** the facade SHALL reject the call before persisting a partial write

## MODIFIED Requirements

### Requirement: Public API error and availability behavior
The public API facade SHALL define deterministic behavior for unavailable plugin state and asynchronous storage failures.

#### Scenario: API requested before initialization
- **WHEN** a caller requests the facade before LoriTime has initialized its API state
- **THEN** the system SHALL return an empty unavailable result or throw a documented state exception
- **THEN** the behavior SHALL be consistent across supported platforms

#### Scenario: Storage operation fails
- **WHEN** an underlying storage operation fails while serving a facade call
- **THEN** the facade future SHALL complete exceptionally with a documented LoriTime API exception shape
- **THEN** the facade SHALL NOT expose raw SQL exceptions as part of the normal public API contract

#### Scenario: Slave mode read cache miss
- **WHEN** a facade read is requested on an instance that relies on delegated or cached storage data
- **THEN** the facade future SHALL complete with the same deterministic fallback behavior as local LoriTime consumers for that storage mode
- **THEN** the behavior SHALL be documented for third-party callers
