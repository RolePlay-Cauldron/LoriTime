## Context

The current implementation has already moved toward a unified database-backed storage model and reason-aware active session accumulation. However, runtime code can still reach concrete storage decorators, AFK transition side effects are distributed across provider, handler, messaging, and listener code, and AFK plugin messages use unversioned string status values while storage messages already have protocol versioning.

This change is a refactor with behavior-preservation as a constraint. It should make storage and AFK responsibilities clearer without changing existing configuration keys, database schema, or player-visible AFK outcomes.

## Goals / Non-Goals

**Goals:**
- Keep storage callers dependent on focused contracts such as durable storage, active session accumulation, and adjustment writing instead of concrete implementation classes.
- Make AFK transition handling explicit enough that detection, enforcement, session stop/restart, and kick marking can be tested independently.
- Add a versioned AFK plugin messaging payload that can reject unsupported or malformed messages cleanly.
- Centralize session fallback context values so platform listeners and AFK resume paths use the same defaults.
- Preserve existing storage totals, session reason semantics, AFK messages, permissions, and config keys.

**Non-Goals:**
- No database schema migration.
- No new AFK configuration model.
- No replacement of the current plugin messaging transport.
- No user-facing behavior changes to idle detection, AFK announcements, time removal, auto-kick, or placeholders.

## Decisions

### Use focused interfaces at runtime

Runtime access should move toward small contracts: identity and durable time storage for persistence, `TimeAccumulator` for active sessions, and an adjustment-writing contract for manual/AFK signed adjustments. `AccumulatingTimeStorage` can remain the implementation that composes these behaviors, but callers should not need to depend on that concrete type.

Alternative considered: keep `UnifiedStorage` as the only runtime contract and rely on naming discipline. That leaves the current broad API surface in place and makes it easy for future code to bypass intended responsibility boundaries.

### Keep the storage refactor internal

The refactor should preserve the existing `UnifiedStorage` behavior and database-backed implementation. Compatibility methods can remain temporarily where needed, but new or touched runtime code should prefer focused contracts.

Alternative considered: perform a hard API break and delete broad methods immediately. That would create unnecessary blast radius for a behavior-preserving cleanup.

### Model AFK transitions before executing side effects

AFK handling should distinguish the transition decision from the side effects. A small internal model can describe whether a player is becoming AFK, resuming, being auto-kicked, stopping accumulation, restarting accumulation, or only sending messages. The master handler can then execute side effects from that decision.

Alternative considered: only rename or split existing methods. That improves readability locally but does not make the cross-module AFK state machine much clearer.

### Version AFK plugin messages

AFK slave-to-master messages should include a protocol version and a typed transition value. Unsupported versions should be ignored with a warning. The versioned format should be implemented in a way that can coexist with existing messaging channels.

Alternative considered: reuse the existing unversioned string boolean payload. That keeps the wire format simple but makes future AFK message extensions fragile and inconsistent with storage messaging.

Legacy unversioned AFK string-boolean payloads will not be accepted. Paper, Velocity, and Bungee components must use the new versioned AFK message format together.

### Centralize session context defaults

Fallback values such as `default` server and `global` world should be provided by a shared session context/defaults component or constants owned by the storage/session layer. AFK resume and platform listeners should consume that source instead of hard-coding the same literals independently.

Alternative considered: leave defaults as literals because there are only a few call sites. That is low effort now, but it risks subtle divergence as proxy and Paper context behavior evolves.

## Risks / Trade-offs

- Refactoring contracts may touch many call sites -> Keep changes incremental and preserve compatibility shims until all callers are migrated.
- AFK wire protocol changes can break mixed-version proxy/Paper deployments -> Reject legacy and unsupported AFK payloads explicitly so mixed-version deployments fail predictably instead of applying ambiguous state changes.
- Making AFK transitions explicit could add abstraction without reducing complexity -> Keep the model small and test it through existing AFK behavior scenarios.
- Centralized defaults could hide platform-specific context rules -> Keep defaults limited to fallback values; platform listeners still own observed server/world context.

## Migration Plan

1. Introduce focused contracts and shared session context defaults while keeping current behavior.
2. Migrate runtime callers away from concrete `AccumulatingTimeStorage` access.
3. Add versioned AFK message encode/decode and tests for valid, invalid, and unsupported payloads.
4. Refactor AFK handling to use explicit transition decisions while preserving existing messages, permissions, and session reasons.
5. Remove or deprecate redundant direct concrete access once callers no longer require it.

Rollback is code-level only: because no schema or config migration is planned, reverting the change restores the previous runtime boundaries.
