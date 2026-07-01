## Context

LoriTime currently exposes its public entry point through `LoriTimeAPI.get()`, which returns the internal `LoriTimePlugin` instance. That object is useful for platform modules, but it exposes broad internals to third-party plugin authors: configuration, localization, storage lifecycle, active session accumulation, player conversion, and updater state.

The recent storage work created focused runtime contracts such as `UnifiedStorage`, `TimeQueryStorage`, `TimeAdjustmentStorage`, and `TimeAccumulator`. Those contracts are valuable inside LoriTime and for advanced storage integrations, but they are still too low-level for normal plugins that only want to read online time or add an audited manual adjustment.

## Goals / Non-Goals

**Goals:**

- Provide a small stable public facade for common third-party integrations.
- Keep existing storage and accumulator contracts available for internal use.
- Make breaking API cleanup where old public access exposes internals or unused typo-preserving methods.
- Define predictable API availability and failure behavior.
- Document the recommended integration path with examples.

**Non-Goals:**

- Replace or redesign the unified storage implementation.
- Preserve source or binary compatibility for old public API entry points.
- Add a Maven or Gradle publication pipeline unless the implementation scope explicitly expands later.
- Add a network or REST API.
- Change how player session rows are persisted.

## Decisions

### Add a facade instead of changing `LoriTimePlugin`

Introduce a public facade owned by the `common.api` package, tentatively named `LoriTimeService` or `LoriTimePublicApi`. The facade delegates to `LoriTimePlugin` and `UnifiedStorage`, but callers do not receive direct access to storage lifecycle methods or session row controls.

Alternative considered: add convenience methods directly to `LoriTimePlugin`. That would improve ergonomics but continue treating the whole plugin object as the public API surface.

### Replace broad plugin access with a facade accessor

Remove the old broad plugin accessor and expose `LoriTimeAPI.service()` returning the facade. If the plugin is unavailable, the accessor returns `Optional.empty()` so callers can handle missing or uninitialized LoriTime deterministically.

Alternative considered: keep `LoriTimeAPI.get()` and document it as legacy. That keeps internals reachable and contradicts the goal of a major public API overhaul.

### Use standard Java types at the public boundary

The facade should accept `UUID`, `String`, `Duration`, and small API-owned records/enums where useful. It should avoid raw epoch milliseconds, session IDs, `PlayerSessionContext`, `PlayerSessionChunk`, `SQLException`, and storage implementation types.

Alternative considered: mirror `UnifiedStorage` exactly. That would reduce implementation work but would not solve the accidental exposure problem.

### Make write operations actor-aware

Manual adjustments should support actor metadata so API writes retain the same audit quality as command-driven writes. The facade can provide a simple overload for system adjustments and a richer overload that accepts actor UUID/name.

Alternative considered: only expose `addTime(UUID, Duration)`. That is convenient, but it loses important audit context.

### Document threading and storage-mode behavior

Storage calls may perform database work or plugin-message-backed reads depending on mode. Documentation should state whether facade methods are synchronous, whether callers should use async scheduling, and how slave-mode cache misses behave.

Alternative considered: introduce only asynchronous methods. That would be safer for consumers but larger in scope and may not fit existing command/placeholder code patterns.

## Risks / Trade-offs

- Public facade becomes another API to maintain -> keep it intentionally small and backed by tests.
- Removing broad access can break old integrations -> acceptable for this major API overhaul and keeps the new API boundary clean.
- `Duration` improves clarity but storage uses seconds -> specify truncation or rejection behavior for sub-second durations.
- Synchronous facade calls can be misused on server threads -> document threading expectations and consider async variants if implementation shows meaningful blocking risk.
- Naming may be hard to change later -> choose a neutral facade name before implementation and avoid exposing storage-specific wording.

## Migration Plan

1. Add the facade and new accessor.
2. Update documentation to show the facade path as the default integration method.
3. Remove old unused broad accessors and typo-preserving aliases instead of deprecating them.
4. Update internal call sites to use the corrected public model names.
5. Run full verification across all modules.

## Open Questions

- Should the facade accessor return `Optional<LoriTimeService>` or throw a documented exception when LoriTime is not initialized?
- Should the first facade version include async methods, or should synchronous methods plus documentation be enough?
- Should custom storage injection get a dedicated public extension API in a future change?
