## Why

The recommended `LoriTimeService` facade still performs storage-facing operations synchronously, so third-party integrations can block a server thread by using the supported API path. Internal runtime code already schedules many database operations asynchronously, but the storage threading rule is not explicit and synchronous surfaces such as tab completion and placeholders can still reach database-backed storage from request paths.

## What Changes

- **BREAKING** convert database-facing `LoriTimeService` operations to `CompletableFuture` results and make LoriTime own their asynchronous storage execution.
- Keep internal storage and accumulator contracts synchronous instead of converting every runtime storage API to futures.
- Define a runtime storage threading rule that database-backed reads and writes do not run on platform main-thread request/tick paths.
- Preserve synchronous startup migration and storage initialization where database work runs before normal server ticking.
- Replace database reads in synchronous runtime surfaces such as tab completion and placeholder rendering with cached or non-database response paths.
- Update API documentation and tests for async facade behavior, exceptional completion, storage-mode behavior, and runtime DB-threading expectations.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `public-api-facade`: storage-facing facade operations become asynchronous and expose `CompletableFuture` completion/failure behavior.
- `unified-storage-system`: runtime database-backed storage access gains an explicit off-main-thread rule and synchronous placeholder/tab-completion paths must avoid direct database access.

## Impact

- Public `LoriTimeService` signatures, documentation, and facade tests.
- Scheduler-backed asynchronous execution and API exception wrapping around facade storage delegation.
- Runtime command completion name sources and Paper PlaceholderAPI read paths.
- Storage/runtime specs distinguishing blocking internal storage contracts from platform runtime execution paths.
