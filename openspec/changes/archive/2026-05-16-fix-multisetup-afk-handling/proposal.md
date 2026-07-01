## Why

AFK handling in multi-setup deployments is currently ambiguous and can fail to enforce configured kick and time-removal behavior. The implementation depends on Paper/Folia slave detection sending AFK plugin messages to a proxy master, but the docs and proxy/platform checks do not make that responsibility reliable.

## What Changes

- Correct proxy platform detection so Bungee is treated as a proxy for AFK scheduling decisions.
- Keep the existing `afk.enabled`, `afk.after`, `afk.removeTime`, `afk.autoKick`, and `afk.repeatCheck` settings; do not split AFK configuration.
- Align multi-setup AFK behavior so Paper/Folia slaves perform idle detection and send AFK state messages, while proxy masters apply canonical side effects such as time adjustment, kick, and announcements.
- Restart the AFK checker on reload when AFK timing or enablement settings are reloaded, so runtime config changes take effect consistently.
- Update AFK documentation to match the actual required multi-setup configuration.

## Capabilities

### New Capabilities

### Modified Capabilities
- `unified-storage-system`: Clarify multi-setup AFK responsibilities and reload behavior for AFK detection and enforcement.

## Impact

- Affected modules: `common` AFK provider/handler, Bungee platform adapter, Paper/Folia slave AFK registration, proxy plugin messaging, AFK documentation.
- No new user-facing AFK settings.
- No storage schema changes.
- No public command changes.
