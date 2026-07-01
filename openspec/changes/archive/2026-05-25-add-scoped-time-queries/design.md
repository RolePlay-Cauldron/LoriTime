## Context

LoriTime's normalized session storage already records player time against canonical server and world context through `time -> world -> server`. Runtime commands and public storage contracts still expose totals as global player values, and manual adjustments currently have no server or world context. AFK time removal also writes a signed adjustment without preserving the context that caused the removal.

This change is allowed to break API and database compatibility. The database baseline should be updated directly instead of adding a compatibility migration for existing normalized databases.

## Goals / Non-Goals

**Goals:**
- Represent time query and adjustment scope explicitly as global, server, or world.
- Allow `/loritime` to query scoped totals with separate permissions for server and world lookups.
- Allow modify add, set, and reset operations to target global, server, or world scope.
- Include session time, manual adjustments, and active online time consistently for the requested scope.
- Preserve AFK time-removal context by writing AFK adjustments against the current world/server when available.
- Update the database baseline schema directly for scoped adjustments.

**Non-Goals:**
- Preserving compatibility with existing normalized database contents.
- Adding per-server or per-world top lists in this change.
- Adding a legacy migration path for older scoped or unscoped adjustment rows.
- Changing how session rows observe server/world context beyond what is needed for scoped reads and AFK adjustments.

## Decisions

### Use an explicit time scope value

Introduce a common scope representation with three modes:

```text
GLOBAL
SERVER(server)
WORLD(server, world)
```

Storage read and adjustment APIs should accept this scope rather than adding separate method families for every query shape. This keeps command, service, and storage behavior aligned and makes future scoped features easier to add.

Alternative considered: overload methods such as `getTime(uuid)`, `getTime(uuid, server)`, and `getTime(uuid, server, world)`. That is simple at first, but it spreads scope semantics across method signatures and makes manual adjustments harder to keep consistent.

### Store adjustment scope directly

Manual adjustment rows should store scope directly:

```text
scope_type
server_id NULL
world_id NULL
```

Global adjustments use `scope_type = GLOBAL` and null scope references. Server adjustments use `SERVER` plus `server_id`. World adjustments use `WORLD` plus `world_id`. This avoids fake worlds for server-level adjustments and lets totals include only adjustments that match the requested scope.

Alternative considered: store only `world_id` and represent server adjustments with a synthetic world. That makes queries less clear and turns a scope concept into a data convention.

### Scoped total semantics

Global totals include all session rows and all adjustments for the player. Server totals include session rows on that server, server-scoped adjustments for that server, and world-scoped adjustments for worlds on that server. World totals include session rows for that exact server/world and world-scoped adjustments for that exact world.

Global adjustments do not get distributed into server or world totals. This keeps explicit global admin changes global and prevents a server/world lookup from inventing allocation rules.

### Default command scope remains global

Existing unsuffixed lookup and modify commands keep global behavior:

```text
/loritime [player]
/ltmodify add|set|reset ...
```

Scoped behavior is explicit:

```text
/loritime server <server> [player]
/loritime world <server> <world> [player]
/ltmodify add <player> <time> [server <server> | world <server> <world>]
/ltmodify set <player> <time> [server <server> | world <server> <world>]
/ltmodify reset <player> [server <server> | world <server> <world>]
```

This avoids ambiguity between player names, server names, and world names.

### Permission model separates scope and target

Use existing global permissions for global lookups, and add scoped permissions:

```text
loritime.see.server
loritime.see.server.other
loritime.see.world
loritime.see.world.other
```

Modify command permissions remain under the existing modify/admin permission surface unless implementation finds an established finer-grained pattern already in use.

### AFK removal writes current context

AFK time removal should create a signed `AFK_ADJUSTMENT` in the current world scope when the runtime knows the player's canonical server and world. If specific context is unavailable, it should fall back through the same session context defaults used elsewhere. This makes AFK removal visible in world/server totals without adding special query rules.

### Replace the schema baseline

Because compatibility is not required, update the current schema baseline directly so the adjustment table includes scope columns and indexes. Do not add a new migration only to transform existing unscoped adjustment rows.

## Risks / Trade-offs

- [Existing databases are incompatible] -> Document that this change edits the baseline schema and does not provide a compatibility migration.
- [Global totals may not equal sum of all server totals] -> Keep this intentional because global adjustments are not allocated to server/world scopes.
- [Server/world names can collide with player names] -> Require explicit `server` and `world` command keywords.
- [AFK removal may lack exact world context on proxy-only deployments] -> Use current known context when available and shared fallback values otherwise.
- [Query complexity increases] -> Keep scope matching in storage/table helpers and cover global, server, world, and active-session overlay behavior with focused tests.
