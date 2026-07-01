## Why

The `/ltmodify` scoped syntax still uses positional `server <server>` and `world <server> <world>` suffixes, while `/lt` already uses flag-style `server:<server>` and `world:<world>` parsing. Aligning both commands removes an awkward command shape and makes scoped reads and scoped mutations predictable.

## What Changes

- Change `/ltmodify add`, `/ltmodify set`, and `/ltmodify reset` to accept `server:<server>` / `s:<server>` and `world:<world>` / `w:<world>` scope flags.
- Make modify scope flags order-independent where practical, matching `/lt` behavior.
- Resolve a world-only modify scope through the same server resolution rules used by `/lt`: target player's current backend on proxy runtimes or the local configured server on backend canonical runtimes.
- Update command completions so modify suggests long scope flag prefixes and live or cached server/world values in the same style as `/lt`.
- Update bundled usage text and user-facing docs to show the flag-style modify syntax.
- **BREAKING**: Remove support for legacy positional modify scopes: `server <server>` and `world <server> <world>`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-architecture`: Update scoped modify command syntax, parsing, completion, and world-scope server resolution requirements.

## Impact

- Affected code: `CommandScopes`, `LookupScopeParser` or equivalent shared scope parsing helpers, `LoriTimeModifyCommand`, and command completion helpers.
- Affected tests: scoped modify execution, parser rejection for legacy positional syntax, modify tab completion, and full build verification.
- Affected docs/resources: bundled localization usage strings and project command documentation that mention `/ltmodify` scope arguments.
- No storage schema, public API, or dependency changes are expected.
