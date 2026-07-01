## Why

The current public entry point exposes the full `LoriTimePlugin` object, which makes internal runtime services, storage lifecycle controls, and implementation details look like supported third-party API. A focused public facade would give plugin authors stable read/write operations while preserving the existing storage and session contracts for LoriTime internals.

## What Changes

- Add a stable public API facade for common integration use cases: player identity lookup, total-time reads, and manual time adjustments.
- Keep existing storage and accumulator contracts as internal runtime contracts, but remove broad public access paths that expose them accidentally.
- Define availability and failure behavior for the public entry point so callers can handle LoriTime not being enabled, running in slave mode, or storage failures predictably.
- Add public API documentation with dependency setup, platform notes, examples, threading guidance, and compatibility expectations.
- Correct public model issues where needed, such as `LoriTimePlayer` equality/hash behavior and awkward method naming.
- **BREAKING** Remove unused legacy/deprecated public methods that only existed for compatibility with the old broad API surface.

## Capabilities

### New Capabilities
- `public-api-facade`: Stable third-party plugin API for reading player time data, resolving player identity, and writing manual adjustments without depending on internal storage/session lifecycle details.

### Modified Capabilities

None.

## Impact

- Affected API classes under `common/src/main/java/com/jannik_kuehn/common/api`.
- Affected storage-facing code where facade methods delegate to internal `UnifiedStorage`.
- Affected documentation in `docs/API.md` and possibly README setup guidance.
- Tests should cover facade availability, delegation behavior, error handling, and public model behavior.
