## Context

`LoriTimePlayer` currently sits in the public API package but represents both player identity and LoriTime's mutable runtime tracking state. Internal code uses it for AFK state and resume timestamps, while third-party integrations need a stable object that identifies the exact player for facade operations.

`CommonSender` is an internal platform abstraction with player identity plus permissions, messaging, console, and online-state behavior. Platform player adapters already implement `CommonSender`, so they can naturally satisfy a public player identity contract if the dependency direction is `CommonSender extends LoriTimePlayer`.

## Goals / Non-Goals

**Goals:**

- Make `LoriTimePlayer` a stable public identity model for third-party facade use.
- Keep public player identity immutable from the perspective of API consumers.
- Allow `CommonSender` instances to be passed anywhere a `LoriTimePlayer` is accepted.
- Keep AFK/session tracking mutation internal to LoriTime.
- Add facade overloads that accept `LoriTimePlayer` and use UUID as the canonical target identity.

**Non-Goals:**

- Do not expose permissions, messaging, console state, or online state through the public player API.
- Do not make third-party plugins depend on `CommonSender`.
- Do not add a public session-management or AFK-management API in this change.
- Do not introduce a separate public API artifact in this change.

## Decisions

### Public player as interface

`LoriTimePlayer` will become a public interface exposing only `UUID getUniqueId()` and `String getName()`.

This preserves the existing public name while removing mutable AFK/session behavior from the public contract. An interface is preferable to only a record because internal sender/player adapters can implement it without converting between objects.

Alternative considered: make `LoriTimePlayer` a record. That is simpler for third-party references, but it does not model the internal relationship where `CommonSender` already has the same identity methods.

### Public immutable reference implementation

Add a public immutable implementation, such as `LoriTimePlayerRef`, for API-created references. It will implement `LoriTimePlayer` and store UUID plus latest known name.

This lets third-party callers hold or create player references without implementing the interface themselves.

Alternative considered: expose only factory methods on `LoriTimeService`. Factory-only construction is possible, but a small immutable reference type is clearer in documentation and tests.

### Internal runtime player split

Move the current mutable AFK/session state into an internal runtime model, such as `TrackedLoriTimePlayer` or `RuntimeLoriTimePlayer`, that implements `LoriTimePlayer`.

Internal AFK and session code can keep the current behavior, but mutation methods like `setAfk`, `setLastResumeTime`, and `getLastResumeTime` will no longer be part of the public player contract.

### CommonSender extends LoriTimePlayer

Update `CommonSender` so it extends `LoriTimePlayer`.

This keeps the dependency direction clean:

```
Public API:
  LoriTimePlayer = UUID + name

Internal platform layer:
  CommonSender extends LoriTimePlayer
  + permission checks
  + message sending
  + console/online state
```

### Facade overloads use canonical UUID identity

Add `LoriTimeService` overloads for player-based operations:

- `Optional<Duration> getOnlineTime(LoriTimePlayer player)`
- `void addTime(LoriTimePlayer player, Duration amount)`
- `void addTime(LoriTimePlayer player, Duration amount, LoriTimePlayer actor)`

The facade will validate the player object and use `player.getUniqueId()` as the canonical target identity. Names are metadata and must not determine which account receives time changes.

## Risks / Trade-offs

- **Breaking compile changes for code calling public AFK/session methods on `LoriTimePlayer`** -> The affected behavior was internal runtime state; update internal callers to the renamed runtime model and document the public API boundary.
- **Third-party callers may pass stale names** -> Persist and query by UUID; treat names as display/audit metadata only.
- **Interface equality cannot enforce UUID equality** -> Equality guarantees belong to concrete immutable implementations and internal runtime implementations; facade behavior must not depend on object equality.
- **Package still contains internal abstractions** -> This change improves the player model, but a future package-boundary cleanup should still move storage, scheduler, and platform runtime contracts out of `common.api`.
