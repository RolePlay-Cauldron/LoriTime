## Context

LoriTime storage contracts are synchronous blocking contracts. Their default implementation is database-backed and reaches JDBC through `UnifiedDatabaseStorage`, while `AccumulatingTimeStorage` decorates those calls with active-session runtime state. Many runtime call sites already use the plugin scheduler before calling storage, especially command execution, player join/quit listeners, name persistence listeners, AFK loops, and plugin message processing.

The current public facade does not preserve that safety boundary: `LoriTimeService` delegates directly to storage and documentation asks callers to avoid misuse. Synchronous runtime surfaces also need immediate return values. Tab completion currently reads stored names directly, and the Paper PlaceholderAPI expansion can query stored time during placeholder rendering.

Startup migration and storage initialization are different from gameplay runtime access. They are expected to complete before normal ticking work uses LoriTime storage and remain synchronous lifecycle operations in this change.

## Goals / Non-Goals

**Goals:**

- Make storage-facing public `LoriTimeService` calls asynchronous under LoriTime control.
- Keep blocking internal storage and accumulator contracts coherent for existing runtime storage code.
- State and enforce the runtime rule that database-backed storage access does not occur from platform main-thread request/tick paths.
- Move synchronous placeholder and tab completion behavior away from direct database requests.
- Keep storage failure behavior clear for future-based facade calls.

**Non-Goals:**

- Do not convert `UnifiedStorage`, `TimeAccumulator`, or every internal storage call to `CompletableFuture`.
- Do not redesign JDBC providers, database schema, migration flow, or session persistence semantics.
- Do not move startup database migration or storage initialization off the enable lifecycle in this change.
- Do not promise synchronous platform callbacks after a service future completes.

## Decisions

### The public service becomes asynchronous at the facade boundary

Storage-facing facade methods will return `CompletableFuture` results. LoriTime will schedule the blocking storage operation through its async execution path and complete the future with the same domain result that the synchronous service previously returned.

The future boundary applies to identity lookups, total-time reads, and manual adjustment writes. Input validation should happen before scheduling where the input can be validated without storage access; storage failures complete futures exceptionally with the public API exception shape.

Alternative considered: add `Async` variants while keeping synchronous methods. That would leave the recommended API with a misuse path that can block the server thread.

### Internal storage stays blocking and synchronous

`UnifiedStorage`, focused storage contracts, and the accumulator remain blocking internal contracts. Runtime orchestration owns thread placement. This keeps session accumulation and storage implementations coherent without forcing futures through migration, command internals, plugin messaging, and database table helpers.

Alternative considered: future-based storage interfaces. That would encode async execution deeply but would expand scope across every internal storage consumer without solving synchronous platform surfaces that require cached answers.

### Runtime DB-threading rule is explicit

Database-backed storage reads and writes during normal platform runtime must execute from async storage paths or consume cached/non-database state. The rule covers event-driven runtime operations, command request paths, placeholders, and tab completion where database latency can create user-visible lag.

Lifecycle startup migration and storage initialization remain synchronous because they run before normal runtime ticking is expected to use LoriTime storage. Shutdown or reload behavior should be assessed by implementation against runtime thread guarantees, but this change does not require migration initialization to become asynchronous.

Alternative considered: leave threading as documentation guidance. The current code already has call-site drift in synchronous placeholder and completion paths, so the rule needs to be part of the spec and task audit.

### Synchronous completion and placeholder surfaces use cached answers

Tab completion cannot wait for a database query. Name suggestions should use a cache or a non-database source suitable for immediate return. Placeholder rendering should serve cached totals or a deterministic fallback/refresh path instead of querying database-backed storage directly in the render path.

The design can reuse existing slave read cache concepts where they fit, but standalone/master placeholder behavior also needs a non-blocking runtime path. The exact cache owner and refresh trigger are implementation choices as long as placeholder rendering does not issue a runtime database query from the synchronous request path.

Alternative considered: keep direct database reads because PlaceholderAPI already expects synchronous returns. That is the high-lag failure mode this change is intended to remove.

### Future completion does not imply platform-thread affinity

Facade documentation will state that future continuations observe asynchronous completion context unless callers reschedule platform-sensitive work through their platform API. LoriTime will not bounce every completion back to a main thread.

## Risks / Trade-offs

- [Changing facade signatures breaks integrations written against synchronous return values] -> Treat the change as a deliberate public API break and document future-based examples.
- [Async facade work still uses blocking JDBC threads] -> Keep the blocking work on LoriTime async execution paths so database latency does not stall tick/request paths.
- [Caches can return stale or fallback values] -> Specify deterministic placeholder/completion behavior and refresh paths where user-facing values need eventual update.
- [Thread-safety of platform APIs in future continuations is easy to misuse] -> Document that callers must use their platform scheduler for platform-thread-bound actions after completion.
- [Lifecycle and runtime storage access can be confused] -> Keep startup migration/initialization exception explicit in specs and tests focused on runtime paths.

## Migration Plan

1. Change `LoriTimeService` storage-facing method signatures and tests to future-based results.
2. Add a small facade scheduling helper so API methods wrap validation, storage delegation, and exceptional completion consistently.
3. Audit runtime storage call sites against the DB-threading rule.
4. Replace direct DB access from tab completion and placeholder rendering with cached/non-database response paths.
5. Update API and storage documentation, then verify common and platform modules.
