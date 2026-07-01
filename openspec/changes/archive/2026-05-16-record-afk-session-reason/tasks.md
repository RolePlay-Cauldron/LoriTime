## 1. Reason Model

- [x] 1.1 Add a dedicated AFK session lifecycle reason, `PLAYER_AFK`, to `TimeEntryReason`.
- [x] 1.2 Confirm existing storage reason persistence accepts the new enum value without schema changes.

## 2. AFK Stop-Count Handling

- [x] 2.1 Update mastered AFK stop-count handling to stop the active session with `PLAYER_AFK`.
- [x] 2.2 Add or update tests proving stop-count AFK does not persist `PLAYER_LEAVE`.

## 3. AFK Kick Handling

- [x] 3.1 Add a canonical-owner AFK kick marker that is set before AFK auto-kick disconnects the player.
- [x] 3.2 Update Paper, Bungee, and Velocity leave/disconnect accumulation paths to consume the AFK kick marker and use `PLAYER_AFK` instead of `PLAYER_LEAVE`.
- [x] 3.3 Ensure normal non-AFK disconnects continue to use `PLAYER_LEAVE`.
- [x] 3.4 Add or update tests proving AFK-kicked sessions use `PLAYER_AFK` and normal disconnects still use `PLAYER_LEAVE`.

## 4. Verification

- [x] 4.1 Run focused common tests for AFK handling and accumulator reason behavior.
- [x] 4.2 Compile affected Paper, Bungee, and Velocity modules.
- [x] 4.3 Run full `mvn verify`.
