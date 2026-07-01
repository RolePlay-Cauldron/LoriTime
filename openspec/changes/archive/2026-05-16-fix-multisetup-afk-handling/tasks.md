## 1. Platform Responsibility Fixes

- [x] 1.1 Change Bungee platform capability reporting so `BungeeServer.isProxy()` returns `true`.
- [x] 1.2 Verify proxy AFK providers do not start the repeated idle detection loop on Velocity or Bungee.
- [x] 1.3 Verify Paper/Folia slave mode still registers AFK plugin messaging, `PaperSlavedAfkHandling`, activity listeners, and `/afk` when `afk.enabled` is true.

## 2. Reload Behavior

- [x] 2.1 Update plugin reload handling so initialized AFK providers reload values and restart AFK checking.
- [x] 2.2 Ensure restarting the checker is idempotent and does not leave duplicate scheduled AFK tasks.
- [x] 2.3 Confirm `afk.after`, `afk.repeatCheck`, and `afk.enabled` changes are reflected after reload where AFK classes were initialized at startup.

## 3. Multi-Setup AFK Enforcement

- [x] 3.1 Verify Paper/Folia slave AFK detection sends `loritime:afk` messages with player UUID, state, and time-to-remove data.
- [x] 3.2 Verify proxy master message handling applies `MasteredAfkPlayerHandling` for online players.
- [x] 3.3 Verify AFK time removal persists an `AFK_ADJUSTMENT` and auto-kick uses proxy-level kick APIs.

## 4. Documentation and Tests

- [x] 4.1 Update AFK documentation and default config comments to state that proxy multi-setup AFK requires `afk.enabled: true` on the proxy master and Paper/Folia slaves.
- [x] 4.2 Add or update tests for Bungee proxy capability reporting and AFK scheduler restart behavior.
- [x] 4.3 Run focused tests for common AFK/reload behavior and compile affected platform modules.
