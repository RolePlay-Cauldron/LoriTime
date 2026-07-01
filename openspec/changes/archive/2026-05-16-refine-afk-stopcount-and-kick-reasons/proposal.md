## Why

AFK messaging and time-counting behavior are currently coupled to the stop-count bypass permission, which can produce confusing UX: a player may see the AFK resume message without ever seeing the AFK start message. AFK session reasons also need to distinguish a normal AFK stop from an AFK auto-kick so history can explain why the session ended.

## What Changes

- Decouple AFK state messages from stop-count behavior.
- Send the AFK self message and AFK announce when a non-kicked player enters AFK, regardless of whether counting is stopped or bypassed.
- Send AFK resume messages when a non-kicked AFK player resumes, while only restarting accumulation when it was actually stopped.
- Keep `loritime.afk.bypass.stopCount` as a true bypass: players with it keep accumulating time while AFK.
- Add a new `PLAYER_AFK_KICK` time entry reason for sessions ended by AFK auto-kick.
- Use `PLAYER_AFK` for sessions stopped because a player became AFK without the stop-count bypass.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `unified-storage-system`: refine AFK side-effect requirements for messages, stop-count bypass behavior, and AFK kick session reasons.

## Impact

- Common AFK handling logic and tests.
- `TimeEntryReason` enum and any reason persistence/display mappings.
- Platform disconnect listeners that translate AFK kick markers into session stop reasons.
- Documentation for AFK permissions and the V1-to-V2 migration guide.
