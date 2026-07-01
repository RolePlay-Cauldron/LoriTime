## ADDED Requirements

### Requirement: Runtime database access avoids platform main-thread paths
The system SHALL keep database-backed runtime storage reads and writes off platform main-thread request and tick execution paths while retaining synchronous lifecycle startup migration and storage initialization.

#### Scenario: Runtime database work is scheduled off main thread
- **WHEN** normal runtime code needs to perform a database-backed storage read or write
- **THEN** the storage call SHALL execute from an asynchronous or otherwise non-main-thread runtime path

#### Scenario: Startup migration stays synchronous
- **WHEN** LoriTime enables canonical storage and performs required database migration or storage initialization before normal runtime ticking uses storage
- **THEN** the lifecycle operation MAY remain synchronous

#### Scenario: Blocking storage contracts remain internal
- **WHEN** runtime orchestration uses `UnifiedStorage`, focused storage contracts, or the accumulator
- **THEN** those contracts MAY remain synchronous blocking contracts
- **THEN** runtime thread placement SHALL protect platform main-thread paths from database latency

### Requirement: Synchronous runtime data surfaces avoid database requests
Runtime surfaces that must return immediate synchronous values SHALL not issue database-backed storage operations from their request paths.

#### Scenario: Tab completion avoids stored-name query on request path
- **WHEN** a command tab completion request needs player name suggestions
- **THEN** the completion path SHALL use cached or non-database suggestion data
- **THEN** the completion path SHALL NOT query database-backed stored names directly

#### Scenario: Placeholder render avoids direct database total query
- **WHEN** a Paper or Folia-compatible instance renders an online-time placeholder during normal runtime
- **THEN** the placeholder path SHALL use cached or non-database time data with deterministic fallback behavior
- **THEN** the placeholder path SHALL NOT query database-backed total time directly
