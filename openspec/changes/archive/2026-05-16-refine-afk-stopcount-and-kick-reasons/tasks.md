## 1. AFK Reason Model

- [x] 1.1 Add `PLAYER_AFK_KICK` to `TimeEntryReason`.
- [x] 1.2 Update database reason persistence and display/parsing paths so `PLAYER_AFK_KICK` round-trips like other reasons.
- [x] 1.3 Update storage and migration-related tests that enumerate or assert known reasons.

## 2. AFK Messaging and Accumulation

- [x] 2.1 Refactor mastered AFK handling so AFK start messages are sent for every non-kicked AFK transition.
- [x] 2.2 Keep `loritime.afk.bypass.stopCount` limited to whether accumulation stops while AFK.
- [x] 2.3 Refactor resume handling so resume messages are sent for non-kicked AFK resumes, but accumulation restarts only when it was stopped.
- [x] 2.4 Preserve AFK kick behavior so kicked players receive the kick message and kick announce, not an extra AFK self message.

## 3. AFK Kick Session Reason

- [x] 3.1 Update AFK kick marker consumption so AFK-kicked disconnects stop active sessions with `PLAYER_AFK_KICK`.
- [x] 3.2 Ensure non-kick AFK stop-count behavior stops active sessions with `PLAYER_AFK`.
- [x] 3.3 Ensure normal disconnects without AFK markers still use `PLAYER_LEAVE`.

## 4. Tests and Documentation

- [x] 4.1 Add focused AFK handling tests for AFK start/resume messages with and without stop-count bypass.
- [x] 4.2 Add platform or common listener tests for AFK kick reason persistence where the disconnect marker is consumed.
- [x] 4.3 Update AFK documentation and V1-to-V2 migration notes for the final stop-count and reason behavior.
- [x] 4.4 Run focused common-module AFK/storage tests and relevant platform listener tests.
