## Context

LoriTime currently has two ways to produce canonical session history in a proxy network. A Velocity or Bungee master tracks joins, backend server switches, autoflushes, and disconnects through the accumulator. A Paper/Folia slave can also report completed session chunks to the master through plugin messaging.

That split creates conflicting context. Proxy listeners use the backend server name from the proxy configuration with fallback world `global`, while Paper/Folia slave reporting uses `server.name` and the Bukkit world. When both describe the same elapsed player time, the database receives duplicate or misleading server entries. The slave reporter also sends autoflush as completed chunks, and the master persists them with append-only `persistSession`, so flush creates new rows instead of updating the active row.

## Goals / Non-Goals

**Goals:**

- Make proxy master session tracking the single source of truth for canonical time rows in proxy multi-setup deployments.
- Use the proxy backend server name as the canonical server context.
- Allow Paper/Folia slaves to report the player's current world as context for the master's active session without creating independent time rows.
- Preserve accumulator behavior where autoflush updates an active persisted row and leave/switch closes the correct active row.
- Make tests cover session ordering around join, autoflush, world update, backend switch, and leave.

**Non-Goals:**

- Migrating existing historical rows that already contain mixed server names.
- Adding a new storage mode.
- Making Paper/Folia `server.name` authoritative in proxy multi-setup.
- Replacing the read cache or placeholder behavior.

## Decisions

### Proxy Backend Name Is Canonical Server Context

In proxy multi-setup, the canonical session `server` SHALL be the backend server name observed by Velocity or Bungee. This is the only layer that reliably observes backend server switches.

Alternative considered: let each Paper/Folia slave report its configured `server.name`. This was rejected because it can diverge from the proxy backend name and creates separate server entries for the same backend session.

### Paper/Folia Slave Reports World Context Only

Paper/Folia slave reporting should become a current-world context signal. It should not send completed session chunks with start and stop timestamps for canonical persistence.

The master can use the reported world to enrich the active session context it already owns. If no active proxy-owned session exists for the player, the master should ignore or defer the world update rather than insert a standalone row.

Alternative considered: keep remote completed session messages and deduplicate them on the master. This was rejected because append-only chunks cannot safely model autoflush, leave, or backend switch updates without row identity.

### Autoflush Must Not Split Rows

Autoflush is progress persistence for the active session, not a context transition. Master-owned active sessions should keep a stable row id and update that row on autoflush. Paper/Folia slave autoflush should not create or report canonical session rows.

### Backend Switches Own Row Boundaries

In proxy multi-setup, backend server switches define canonical row boundaries. World changes from a Paper/Folia slave update current context for the active backend session but do not create a separate canonical row by themselves.

## Risks / Trade-offs

- Historical data may still show mixed server names -> This change prevents new bad rows; a separate migration/cleanup proposal can address existing data.
- Paper world updates can arrive before the proxy master has created the active session -> The master should ignore or cache briefly, but must not insert a row without proxy session ownership.
- World context on a row may represent the latest reported world rather than a full per-world timeline -> This matches the desired "one entry per server" model; per-world history would need a different explicit requirement.
- Plugin messaging protocol changes can affect compatibility between old masters and new slaves -> Use a protocol version check or new message type so unsupported messages are ignored safely.
