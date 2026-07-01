## Why

The current `/loritime` scoped lookup syntax is positional and does not autocomplete server or world values. Server/world scoped lookup should be easier to type, should prefer live runtime context over stale configuration or database state where possible, and should still allow users to manually query historical server/world scopes.

## What Changes

- Replace the scoped `/loritime server <server> [player]` and `/loritime world <server> <world> [player]` lookup forms with flag-style scope arguments.
- Accept long and short flag forms: `world:<world>`, `w:<world>`, `server:<server>`, and `s:<server>`.
- Only autocomplete the long flag prefixes `world:` and `server:`, while still accepting the short aliases when typed manually.
- Add live-only server and world completion for online/runtime context. Completion must not query database-backed storage.
- Resolve omitted server flags from live/runtime context:
  - Proxy runtimes use the relevant online player's current backend server.
  - Paper/Folia standalone or master runtimes use configured `server.name`, falling back to the shared default.
- Continue to execute manually typed server/world lookups against storage, including values that are not currently cached or suggested.
- Preserve global lookup behavior and existing self/other permission semantics.
- **BREAKING**: Remove support for the positional scoped lookup syntax from `/loritime`.

## Capabilities

### New Capabilities
- None

### Modified Capabilities
- `command-architecture`: Change `/loritime` scoped lookup grammar, scoped completion behavior, and omitted-server resolution requirements.

## Impact

- `common` command parsing and completion helpers, especially `LoriTimeCommand` and `CommandScopes`.
- Common runtime abstractions may need live server/world context methods for completions and omitted-server resolution.
- Paper/Folia, Velocity, and Bungee platform adapters may need to expose current backend/local server and known online worlds.
- Command tests and docs need updates for the new flag syntax and removed positional syntax.
