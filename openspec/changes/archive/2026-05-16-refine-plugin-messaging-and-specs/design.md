## Context

Plugin messaging currently lives mostly in `PluginMessaging`, which serializes arbitrary `Object...` payloads and dispatches storage operations by string values such as `world` and `world_switch`. AFK messaging already has a version constant and typed transition values, but storage messaging does not have an equivalent operation model.

The runtime behavior is intentionally unchanged for AFK resume and accumulator transition ordering. The goal is to make the message layer easier to reason about, easier to test, and clearer when something fails.

## Goals / Non-Goals

**Goals:**

- Introduce explicit message operation models for storage and, where helpful, AFK plugin messaging.
- Keep supported versioned wire formats stable unless a protocol-version bump is intentionally documented.
- Distinguish malformed payloads, unsupported protocol versions, unknown operations, stale legacy session payloads, and storage side-effect failures in logs.
- Clarify the storage specs for `world` as context correction and `world_switch` as session splitting.
- Correct JavaDoc around external storage injection lifecycle.

**Non-Goals:**

- Do not change AFK resume to preserve the last active session context.
- Do not redesign accumulator persistence ordering or transaction semantics.
- Do not reintroduce legacy unversioned AFK payload support.
- Do not make slaves own canonical session lifecycle.

## Decisions

### Keep the current channels but make message operations explicit

The existing channels `loritime:storage` and `loritime:afk` should remain. The current separation is useful: storage messages affect totals/session context, AFK messages affect AFK state transitions.

Storage operations should move from scattered string comparisons to a small protocol model, for example `StorageMessageType` plus parser/serializer helpers. String command values can remain on the wire for compatibility, but should be mapped into a typed operation before side effects run.

Alternative considered: merge AFK and storage messages into one envelope. That would be cleaner long term but creates a wider protocol migration than needed for this change.

### Centralize protocol-version checks per message family

Each message family should check protocol versions in one helper path. Unsupported versions should log the message family, operation when known, and received version.

Alternative considered: keep per-method version checks. That preserves current code shape but continues to duplicate warning text and makes future operation additions easier to implement inconsistently.

### Preserve `world` versus `world_switch` semantics

`world` should remain a context correction/update. It updates the active row's world without creating a new time row. `world_switch` should represent an observed world-change event and split the active session row with `WORLD_SWITCH`.

Alternative considered: make every world report split rows. That would over-count periodic reports and join/leave observations as real transitions.

### Correct external storage lifecycle documentation first

The current implementation closes the injected accumulator through `closeStorages()`. This change should correct JavaDoc/comments to match that behavior unless implementation work discovers an explicit reason to change lifecycle ownership.

Alternative considered: change close behavior to never close injected storage. That is a behavioral change for integrations and should be a separate, deliberate API decision if desired later.

## Risks / Trade-offs

- Wire compatibility risk -> Keep existing channels, operation strings, and payload order unless tests explicitly cover a version bump.
- Logging noise risk -> Use precise warnings for invalid inputs, but avoid logging normal ignored state such as orphan world updates at warning level.
- Over-abstraction risk -> Keep protocol helpers small and local to messaging; do not introduce a full framework for plugin messages.
- Spec drift risk -> Update specs alongside tests for `world` versus `world_switch` and AFK payload rejection.
