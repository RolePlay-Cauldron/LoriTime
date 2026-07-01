## Context

`/loritime` currently supports scoped lookup through positional forms:

- `/loritime server <server> [player]`
- `/loritime world <server> <world> [player]`

This syntax is harder to complete because the meaning of each token depends on its position. It also makes world-only lookup awkward even though runtime code often knows the relevant server context from live platform state.

Paper/Folia standalone and master runtimes already write session rows using `server.name`, with fallback `default`. Proxy runtimes write canonical session rows using the live backend server name observed by the proxy. In proxy multi-setup, Paper/Folia slave `server.name` is intentionally not canonical.

## Goals / Non-Goals

**Goals:**

- Make `/loritime` scoped lookup use flag arguments: `world:<world>`, `w:<world>`, `server:<server>`, and `s:<server>`.
- Allow the optional player argument to remain independent of scope argument order.
- Complete only long flag prefixes (`world:` and `server:`), while accepting short aliases manually.
- Provide live-only server and world completions for online/runtime context without database reads on the completion path.
- Resolve omitted server flags from canonical runtime context: proxy backend server for online proxy players, and `server.name` for Paper/Folia standalone or master.
- Keep manually typed server/world values executable even when they are not suggested by completion.

**Non-Goals:**

- Changing `/ltmodify` scoped argument syntax.
- Adding database-backed tab completion.
- Adding persistent world/server suggestion indexes.
- Reintroducing Paper/Folia slave `server.name` as canonical proxy server context.

## Decisions

### Flag Parser

Use a small command-scope parser that classifies tokens as:

- One optional player token.
- Zero or one world flag: `world:<value>` or `w:<value>`.
- Zero or one server flag: `server:<value>` or `s:<value>`.

Any duplicate flag, empty flag value, unknown flag-like token, or second non-flag token is a usage error.

Flag order should not matter:

```text
/loritime Lorias_ world:spawn server:survival
/loritime world:spawn server:survival Lorias_
```

Both forms describe the same target and scope. This keeps the command ergonomic while still unambiguous.

### Scope Resolution

The parsed request maps to storage scopes as follows:

```text
no world, no server  -> global
server only          -> server scope
world + server       -> exact world scope
world only           -> world scope with runtime-resolved server
```

Runtime-resolved server means:

- Proxy runtime: the target player's current backend server, when the target is online.
- Paper/Folia standalone or master: configured `server.name`, falling back to `default`.

If the command cannot resolve a server for `world:<world>` without `server:<server>`, it should fail with usage or a clear error instead of guessing from database history.

### Completion Data

Completion should remain live-only and cache-only:

- Player suggestions come from existing online/recent player completion cache.
- `server:` suggestions on proxies come from currently known backend servers.
- `server:` suggestions on Paper/Folia standalone/master can include the configured `server.name`.
- `world:` suggestions come from currently online/observed world context.
- Completion must not read `loritime_server`, `loritime_world`, or other database-backed storage.

This keeps completion responsive and avoids stale historical entries. Execution still queries storage for manually typed values, so users can query worlds that are not currently online/cached.

### Runtime Context Surface

The common command layer needs enough runtime context to complete and resolve omitted server flags without knowing platform APIs. This can be exposed through `CommonServer` and/or `CommonPlayerSender` methods such as:

- Resolve current server for an online player.
- Return live server completion candidates.
- Return live world completion candidates, optionally filtered by online player and/or server.
- Return the local canonical Paper/Folia server name for standalone/master runtimes.

Exact method names are implementation details, but the data source boundaries are not: proxy current backend server and Paper/Folia `server.name` are canonical; database history is not used for defaulting or completion.

## Risks / Trade-offs

- **Breaking command syntax** -> Update usage messages, docs, and tests; intentionally reject old positional scoped forms.
- **Proxy world suggestions may be incomplete** -> Allow typed non-cached world names to execute against storage.
- **World-only proxy lookup requires an online target** -> Fail clearly when the target/current player has no live backend context.
- **Common runtime API may grow** -> Keep methods narrowly scoped to live command context rather than exposing storage tables or platform internals.
