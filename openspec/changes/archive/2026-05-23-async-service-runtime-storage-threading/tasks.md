## 1. Async Public Service

- [x] 1.1 Convert storage-facing `LoriTimeService` identity lookup, time query, and manual adjustment methods to `CompletableFuture` return types.
- [x] 1.2 Add a facade async execution helper that schedules blocking storage delegation and wraps storage failures with the public API exception shape.
- [x] 1.3 Preserve pre-storage input validation and player overload semantics after the future-based signature break.

## 2. Runtime Database Threading Audit

- [x] 2.1 Audit runtime storage and accumulator call sites for database-backed work on platform main-thread request or tick paths.
- [x] 2.2 Keep existing async listener, command, AFK, plugin messaging, and cache-flush paths aligned with the explicit runtime DB-threading rule.
- [x] 2.3 Document the lifecycle exception for synchronous startup migration and storage initialization without converting internal storage contracts to futures.

## 3. Synchronous Runtime Surfaces

- [x] 3.1 Replace stored-name database lookups in command tab completion with cached or non-database suggestion data.
- [x] 3.2 Replace Paper/Folia placeholder render-path database total queries with cached or non-database time data and deterministic fallback behavior.
- [x] 3.3 Add refresh/update paths needed to keep completion and placeholder caches useful without blocking their synchronous request paths.

## 4. Documentation And Verification

- [x] 4.1 Update API documentation and examples for future-based `LoriTimeService` calls, future failure handling, and caller thread-affinity expectations.
- [x] 4.2 Add or update service tests for async results, validation behavior, and exceptional completion on storage failures.
- [x] 4.3 Add or update runtime/platform tests covering non-database tab completion and placeholder render behavior.
- [x] 4.4 Run focused common and platform verification for service API changes and runtime cache/threading paths.
