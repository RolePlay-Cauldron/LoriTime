## Why

The current command implementation duplicates permission checks, completion filtering, player-name suggestion logic, async dispatch, and platform registration decisions across individual command classes and platform entrypoints. Multi-setup command availability also needs clearer separation so canonical data commands are available where storage is owned, while local operational commands remain available on both proxy and backend instances.

## What Changes

- **BREAKING** Rework the common command API around reusable command definitions, command contexts, completion providers, and registration profiles instead of each command owning all parsing, permissions, completions, and scheduling.
- Add command profiles for proxy runtimes and backend runtimes, with backend profiles split into `canonical` and `slave`.
- Keep full canonical commands available on standalone backend servers and storage-owning backend/proxy instances.
- Keep reload, debug, and info commands local to each instance; reload SHALL NOT trigger remote proxy/backend reloads.
- Add `commands.yml` for command names and aliases, with separate alias sets for proxy and backend profile nodes.
- Add a configurable recent-player completion cache with a default retention window of 30 days.
- Include known recent players in completion even when they have no time history, as long as they have a stored player identity row.
- Update documentation and `CHANGELOG.md` to describe `commands.yml` and the command alias migration.

## Capabilities

### New Capabilities
- `command-architecture`: Defines command registration profiles, command availability, local versus canonical command behavior, command completion behavior, and recent-player suggestion caching.

### Modified Capabilities
- `configuration-management`: Adds the `commands.yml` configuration file and command completion configuration defaults.

## Impact

- Affects common command contracts and command implementations under `common`.
- Affects Paper/Folia-compatible backend registration, Bungee registration, and Velocity registration.
- Affects command alias configuration, default resources, docs, and changelog.
- Adds storage/query support for recent player identities based on stored player rows and `last_seen`.
- Adds tests for command profile selection, command completion, config loading/defaults, and no synchronous storage access during tab completion.
