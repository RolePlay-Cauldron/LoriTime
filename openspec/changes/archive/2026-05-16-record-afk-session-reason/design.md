## Context

The storage model distinguishes session rows from signed adjustment rows. `AFK_ADJUSTMENT` already identifies time removed outside the normal session lifecycle, but session rows currently have no dedicated reason for an active session ending because the player became AFK or was kicked for AFK. Existing code paths can therefore persist AFK-caused stops as `PLAYER_LEAVE`.

AFK handling has two relevant paths:
- Stop-count bypass: the player remains online but accumulation is stopped while AFK.
- Auto-kick: the player is disconnected because AFK kick enforcement is enabled.

Both are AFK-caused session boundaries and should be represented differently from a normal player leave.

## Goals / Non-Goals

**Goals:**
- Add a dedicated session lifecycle reason for AFK-caused session stops.
- Use the AFK session reason when stop-count bypass stops accumulation.
- Use the AFK session reason when AFK kick enforcement causes the player to leave.
- Preserve `AFK_ADJUSTMENT` for adjustment rows that remove time.

**Non-Goals:**
- Change the database schema.
- Rename or repurpose `AFK_ADJUSTMENT`.
- Change AFK permissions or configuration.
- Change how much time is removed for AFK.

## Decisions

1. Add `PLAYER_AFK` to `TimeEntryReason`.

   `PLAYER_AFK` names the session lifecycle event clearly and avoids overloading `AFK_ADJUSTMENT`, which already describes signed manual adjustment rows. The name also follows the existing `PLAYER_JOIN` and `PLAYER_LEAVE` style.

   Alternative considered: use `AFK_ADJUSTMENT` for session rows too. That would blur the difference between session lifecycle and adjustment history.

2. Stop AFK pause sessions with `PLAYER_AFK`.

   `MasteredAfkPlayerHandling` should pass the AFK session reason when it calls the accumulator for stop-count bypass behavior. This keeps players who are still online but not counting time from looking like they left the server.

3. Preserve AFK kick reason through disconnect handling.

   AFK kick enforcement should make the eventual leave/disconnect listener persist the current session with `PLAYER_AFK`, not `PLAYER_LEAVE`. Implementation can use a small in-memory marker on the canonical owner before calling the platform kick API, and consume that marker in leave/disconnect handling.

   Alternative considered: stop accumulation immediately before kicking. That risks duplicate or out-of-order session stop handling when the platform later fires the leave event. A consumed marker keeps the normal disconnect lifecycle authoritative while preserving the AFK reason.

## Risks / Trade-offs

- If an AFK-kicked player disconnect event is never observed, the active session may not be stopped until fallback flush/shutdown behavior -> This is already a general platform lifecycle risk; the marker should be consumed only by the normal disconnect path.
- In-memory AFK kick markers are lost on plugin shutdown before disconnect handling -> The existing shutdown flush will still persist the session, but may not have the AFK reason.
- Custom integrations that assume only existing enum values may need to handle `PLAYER_AFK` -> This is an API enum expansion, not a storage migration.
