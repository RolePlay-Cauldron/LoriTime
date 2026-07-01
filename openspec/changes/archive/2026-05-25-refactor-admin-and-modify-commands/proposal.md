## Why

The current command split puts local runtime actions behind a separate local command while admin also mixes runtime operations with canonical player time mutations. This makes day-to-day usage awkward and keeps unrelated command responsibilities coupled.

## What Changes

- **BREAKING** Remove the dedicated local command root and move local runtime actions back under the profile-specific admin command.
- **BREAKING** Introduce a dedicated modify command for canonical player storage mutations: add/modify time, set time, reset time, and delete user.
- **BREAKING** Move debug, info, reload, and update behavior into the admin command.
- **BREAKING** Update default `commands.yml` aliases so proxy admin uses `plta` and backend/local admin uses `lta`.
- Keep canonical mutation commands unavailable on backend slave runtimes.
- Clean up command implementations to use the shared command internals consistently and reduce duplicated completion, permission, sender, localization, and player lookup code.
- Update `CHANGELOG.md` with the command root and alias changes.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `command-architecture`: Refine command profile registration, admin command responsibilities, dedicated modify command behavior, and command cleanup expectations.
- `configuration-management`: Update `commands.yml` profile defaults and remove the local command node from command alias configuration.

## Impact

- Affects common command classes, command registration profiles, command node configuration, default `commands.yml`, and command-related tests.
- Affects Paper/Folia-compatible, Bungee, and Velocity command registration through the shared profile registry.
- Affects user-facing command syntax and alias defaults.
- May remove or rename public command-related classes and APIs; legacy compatibility is not required.
