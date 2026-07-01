## Why

LoriTime already records canonical server and world context for sessions, but user-facing time lookups and manual adjustments still operate as global player totals. Server owners need both server-scoped and world-scoped views and adjustments so playtime can support per-server and per-world rewards, moderation, and analytics.

## What Changes

- Add scoped time lookup support for global, server, and world scopes.
- Add explicit `/loritime` server and world lookup forms with separate permissions for server and world visibility.
- **BREAKING** Update the storage/API model so time queries and manual adjustments can target a scope instead of only a player UUID.
- **BREAKING** Update manual adjustment persistence so adjustments can be global, server-scoped, or world-scoped.
- Update modify command behavior so add, set, and reset operations accept optional server or world scope arguments; omitted scope remains the default global behavior.
- Update AFK time removal so removed time is recorded against the player's current world/server context when that context is available.
- Replace the current database baseline schema with scoped adjustment columns instead of adding a legacy compatibility migration.

## Capabilities

### New Capabilities

### Modified Capabilities
- `unified-storage-system`: Time totals, manual adjustments, and AFK adjustment writes become scope-aware across global, server, and world contexts.
- `command-architecture`: Canonical time and modify commands expose scoped arguments, completions, and permissions.
- `afk-session-coordination`: AFK time-removal adjustments preserve the current server/world context when canonical runtime context is available.

## Impact

- Common storage API and public service methods for time queries and adjustments.
- Database baseline schema and table helpers for manual adjustment scope columns.
- `/loritime` command parsing, permissions, localization, and completions.
- `ltmodify` command parsing and scoped set/add/reset behavior.
- AFK handling paths that create signed time-removal adjustments.
- Tests for storage totals, command permissions/parsing, scoped adjustments, and AFK removal context.
