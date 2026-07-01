## Why

The public API needs a player object that can identify the exact player for time queries and adjustments without exposing mutable AFK state, sender behavior, or platform runtime details. The current `LoriTimePlayer` mixes public identity with internal tracking state, which makes third-party API usage less clear and harder to keep stable.

## What Changes

- **BREAKING** Convert `LoriTimePlayer` into a public immutable identity contract that exposes only stable player identity data.
- Add a public immutable implementation for plugin-created player references.
- Make internal sender abstractions usable where a public `LoriTimePlayer` is accepted by having `CommonSender` extend the public player contract.
- Move or rename the current mutable AFK/session tracking model so it is clearly internal runtime state.
- Add `LoriTimeService` overloads that accept `LoriTimePlayer` for time queries and manual time adjustments.
- Update public API documentation to describe the player model, exact identity behavior, and the sender/runtime separation.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `public-api-facade`: Adds a stable public player identity model and facade operations that accept it without exposing internal sender or AFK/session state.

## Impact

- Affected API classes under `common/src/main/java/com/jannik_kuehn/common/api`.
- Affected internal common/platform abstractions where `CommonSender` and mutable LoriTime player tracking are used.
- Affected facade tests and public model tests.
- Affected documentation in `docs/API.md`.
