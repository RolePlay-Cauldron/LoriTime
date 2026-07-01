## 1. Public Facade Shape

- [x] 1.1 Choose final facade and accessor names, then add the public facade type under `common.api`.
- [x] 1.2 Add a facade accessor to `LoriTimeAPI` and remove broad internal plugin access from the public API.
- [x] 1.3 Define facade availability behavior for uninitialized API state and cover it with tests.
- [x] 1.4 Define and add any public API exception or result type needed to avoid leaking raw storage exceptions.

## 2. Facade Operations

- [x] 2.1 Implement UUID lookup by player name through the facade.
- [x] 2.2 Implement latest-name lookup by UUID through the facade.
- [x] 2.3 Implement total-time query through the facade using stable public types.
- [x] 2.4 Implement system manual adjustment writes through the facade.
- [x] 2.5 Implement actor-aware manual adjustment writes through the facade.
- [x] 2.6 Validate null, invalid duration, and unsupported input cases without persisting partial writes.

## 3. Compatibility Cleanup

- [x] 3.1 Fix `LoriTimePlayer` equality and hash-code consistency.
- [x] 3.2 Add a correctly named AFK setter and remove the existing typo-preserving method.
- [x] 3.3 Review public `common.api` classes for accidental exposure that should be removed or kept internal.

## 4. Documentation

- [x] 4.1 Rewrite `docs/API.md` around the new facade as the recommended integration path.
- [x] 4.2 Add examples for dependency setup, facade lookup, identity lookup, time query, and manual adjustment.
- [x] 4.3 Document synchronous/threading expectations and storage-mode behavior, including slave-mode fallback behavior.
- [x] 4.4 Document the new public API surface and exclude internal storage access from normal integrations.

## 5. Verification

- [x] 5.1 Add or update unit tests for facade delegation and error handling.
- [x] 5.2 Add or update unit tests for public model compatibility cleanup.
- [x] 5.3 Run targeted common-module tests.
- [x] 5.4 Run a compile or verification command covering affected modules.
