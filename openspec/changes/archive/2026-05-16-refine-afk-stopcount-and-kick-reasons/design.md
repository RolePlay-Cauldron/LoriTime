## Context

AFK side effects currently combine three concerns in one branch: player messaging, active session accumulation, and kick handling. Recent stop-count bypass changes made the permission name correct, but the AFK start message is still only sent in the bypass branch while resume messages are tied to whether accumulation was stopped.

Session persistence also uses one AFK session reason for both "player became AFK and counting stopped" and "player was kicked for being AFK". Those events are operationally different and should be distinguishable in storage history.

## Goals / Non-Goals

**Goals:**

- Make AFK start/resume messages describe AFK state transitions, independent of whether time counting is stopped.
- Keep `loritime.afk.bypass.stopCount` focused on accumulation only.
- Persist AFK auto-kick session stops with a new `PLAYER_AFK_KICK` reason.
- Persist non-kick AFK stop-count session stops with `PLAYER_AFK`.
- Preserve existing kick bypass, time removal bypass, and announcement permissions.

**Non-Goals:**

- Add new commands, configuration keys, or permissions.
- Change AFK detection timing.
- Change the existing `AFK_ADJUSTMENT` reason for removed counted time.
- Migrate historical stored reasons.

## Decisions

### Split AFK state messaging from accumulation policy

When a player enters AFK and is not kicked, the handler should send the self AFK message and public AFK announce before applying the stop-count policy. The stop-count bypass then decides only whether the active session continues counting.

Alternative considered: keep messages only on the bypass branch. That preserves current behavior but makes the user experience depend on a time-accounting permission, which is the source of the bug.

### Track whether accumulation was stopped

The handler should continue tracking players whose accumulation was stopped for AFK. Resume should always send AFK resume state messages for non-kicked AFK resumes, but it should restart accumulation only when the player is in that stopped-for-AFK set.

Alternative considered: always call `startAccumulating` on resume. That creates an unnecessary new session/context switch for players whose time never stopped because of the bypass.

### Add `PLAYER_AFK_KICK` for AFK kick session stops

AFK kick handling already marks the next disconnect as AFK-caused. The disconnect path should use a dedicated `PLAYER_AFK_KICK` reason when that marker is consumed. Non-kick AFK stop-count behavior should continue using `PLAYER_AFK`.

Alternative considered: use `PLAYER_AFK` for both paths and infer kicks from surrounding logs or events. That loses useful persisted history and makes reporting less precise.

## Risks / Trade-offs

- Custom integrations that switch over `TimeEntryReason` may need to handle `PLAYER_AFK_KICK` -> Document the enum expansion and add focused tests around reason persistence.
- Resume messages could appear for a player whose AFK state was toggled manually while no accumulation stop happened -> This is acceptable because resume messaging represents AFK state, not storage state.
- AFK kick reason depends on the existing kick marker being consumed on disconnect -> Add tests for Paper, Bungee, and Velocity disconnect handling where practical.

## Migration Plan

No data migration is required. Historical AFK kicks remain stored with their existing reason. New AFK kick session stops use `PLAYER_AFK_KICK` after the change is deployed.
