## 1. Runtime Scope Context

- [x] 1.1 Add common runtime methods or helper contracts for resolving a player's current canonical server without database access.
- [x] 1.2 Add common runtime methods or helper contracts for live server and world completion candidates.
- [x] 1.3 Implement Paper/Folia runtime context using configured `server.name` with shared default fallback and online Bukkit worlds.
- [x] 1.4 Implement Velocity runtime context using online players' current backend servers and live observed/available server data.
- [x] 1.5 Implement Bungee runtime context using online players' current backend servers and live observed/available server data.

## 2. Flag Parser

- [x] 2.1 Replace `/loritime` positional scoped parsing with flag parsing for `world:`, `w:`, `server:`, and `s:`.
- [x] 2.2 Support one optional player token independent of flag order.
- [x] 2.3 Reject duplicate flags, empty flag values, unknown flag-like tokens, multiple player tokens, and legacy positional scoped forms.
- [x] 2.4 Resolve scope to global, server, explicit world, or runtime-defaulted world according to the design.
- [x] 2.5 Preserve existing global self and other-player lookup behavior.

## 3. Completion

- [x] 3.1 Update `/loritime` completion to suggest only long scope prefixes `world:` and `server:`.
- [x] 3.2 Keep short aliases `w:` and `s:` absent from prefix suggestions while autocompleting their value portions.
- [x] 3.3 Add server value completion from cached storage names and live runtime data without synchronous storage reads.
- [x] 3.4 Add world value completion from cached storage names and live/online runtime data without synchronous storage reads.
- [x] 3.5 Preserve cached player-name completion for scoped and unscoped lookup forms.
- [x] 3.6 Refresh server/world completion cache asynchronously and update it from newly observed runtime scopes.

## 4. Storage Execution Semantics

- [x] 4.1 Ensure manually typed non-cached server values still execute against canonical storage.
- [x] 4.2 Ensure manually typed non-cached world values still execute against canonical storage.
- [x] 4.3 Return scoped no-data feedback when storage has no matching scoped time for a known player.
- [x] 4.4 Reject world-only lookup when no runtime server can be resolved instead of deriving server from database history.

## 5. Tests And Documentation

- [x] 5.1 Add parser tests for global, player, long flag, short flag, mixed-order flag, and invalid duplicate/legacy forms.
- [x] 5.2 Add command execution tests for server scope, explicit world scope, Paper/Folia `server.name` defaulted world scope, and proxy current-server defaulted world scope.
- [x] 5.3 Add completion tests proving only `world:` and `server:` prefixes are suggested and short aliases are not.
- [x] 5.4 Add completion tests proving server/world suggestions come from live runtime context and not database-backed storage.
- [x] 5.5 Update command usage localization and command documentation for the new flag syntax.
- [x] 5.6 Run focused common command tests, platform compile checks, and `mvn verify`.
