## Why

PlaceholderAPI requests can be made for offline players, but LoriTime's synchronous placeholder path is intentionally cache-only to avoid database work on render paths. Supporting offline placeholder refresh would require either broad cache preloading or a larger slave/master projection protocol, which adds memory, startup, and multi-setup complexity for a weak fit with the runtime placeholder contract.

## What Changes

- Restrict LoriTime time placeholders to online players.
- Return deterministic fallback values for offline or missing placeholder players.
- Do not trigger storage refreshes or plugin-message refreshes for offline placeholder requests.
- Keep online-player placeholder behavior cache-first with asynchronous refresh.
- Document the online-only placeholder boundary.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `unified-storage-system`: Clarify that synchronous Paper/Folia placeholder time rendering is online-player-only and offline player requests must use fallback values without storage refresh.

## Impact

- Paper/Folia PlaceholderAPI expansion behavior for offline players.
- Placeholder cache refresh behavior in canonical and slave modes.
- Documentation for placeholder limitations and fallback values.
- Tests for online cache refresh and offline no-refresh behavior.
