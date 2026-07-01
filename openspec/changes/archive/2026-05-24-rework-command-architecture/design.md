## Context

LoriTime currently exposes command behavior through a small `CommonCommand` interface while individual command classes own parsing, permission checks, async scheduling, message formatting, tab completion, aliases, and storage calls. Platform entrypoints also hard-code which commands are registered for Paper/Folia-compatible backends, Bungee, and Velocity.

The multi-setup model needs clearer separation between commands that operate on canonical storage data and commands that operate on the local plugin instance. Standalone backend servers and storage-owning master instances can run canonical commands such as time lookup, top list, and admin time edits. Slave backend instances should keep local operational commands such as reload, debug, info, and AFK where applicable, without exposing canonical storage commands.

Tab completion must remain synchronous and cache-only. The existing runtime name cache is useful but only reflects players observed during the current runtime. The normalized player table already tracks `last_seen`, so startup and refresh paths can populate a recent-player cache asynchronously without querying storage during completion requests.

## Goals / Non-Goals

**Goals:**
- Introduce a common command architecture with reusable command definitions, command contexts, permission handling, scheduling policy, and completion providers.
- Centralize command registration policy using runtime profiles instead of duplicating command availability decisions in each platform entrypoint.
- Support separate proxy and backend command aliases through `commands.yml`.
- Use `backend.canonical` and `backend.slave` profile sections so the config stays platform-neutral for Paper, Folia, and future backend platforms.
- Keep reload, debug, and info as local instance commands. Reload affects only the instance that receives the command.
- Populate recent-player completions from cached online players, runtime-observed names, and asynchronously refreshed recent stored identities.
- Include recent stored player identities even when the player has no time history.
- Preserve the existing requirement that tab completion must not query database-backed storage on the request path.

**Non-Goals:**
- Add a remote reload or command broadcast protocol.
- Make command availability fully user-configurable. Runtime profile policy remains code-defined so users cannot enable commands that cannot work on a given instance.
- Add a third-party command framework dependency unless implementation proves the lightweight command model insufficient.
- Preserve the current command API as source-compatible. API breakage is acceptable for this change.

## Decisions

### Use Runtime Profiles for Command Availability

Command registration will be selected from runtime profiles:

```
proxy
  canonical commands + local commands

backend.canonical
  canonical commands + local commands

backend.slave
  local commands + AFK when enabled
```

`canonical` means the instance owns or can access canonical storage and may execute data commands. This avoids overloading `standalone`, because backend `standalone` and backend `master` both need the same command set.

Alternative considered: name the backend section `standalone`. This was rejected because it would misrepresent backend master mode, which also owns canonical storage.

### Keep Command Availability Code-Defined, Alias Configuration User-Defined

`commands.yml` will configure command names and aliases for supported command nodes under profile sections. The code will still decide which command nodes are valid for each runtime profile.

This prevents broken states such as enabling `lttop` on a slave backend that does not own canonical storage.

Alternative considered: make `commands.yml` define complete command availability. This was rejected because it would turn runtime correctness into user configuration and make error handling more complex.

### Split Command Roots by Responsibility

Canonical data commands and local operational commands should be separate command nodes. The local node owns reload-like behavior, while admin data commands own storage mutations.

This keeps reload semantics clear: a reload command always reloads only the current instance. Users who want to reload proxy and backend instances must execute the command on each instance individually.

Alternative considered: keep reload under the canonical admin command everywhere. This was rejected because backend slaves need local reload without exposing canonical admin operations.

### Add a Lightweight Common Command Model

The common layer should model commands as definitions and nodes:

- command id
- root name and aliases loaded for the selected profile
- required permission
- execution policy, such as sync or async
- sender constraints
- children or subcommands
- execution handler
- completion provider

Platform adapters should only translate native senders to `CommonSender`, register native command roots, and delegate execution/completion into the common command dispatcher.

Alternative considered: adopt a platform command framework. This was deferred because LoriTime already needs a common platform abstraction and only needs a focused command tree, not a broad dependency.

### Add a Recent Player Suggestion Service

Player suggestions should move out of individual commands into a shared service. The service will merge:

- online players from `CommonServer`
- runtime-observed player names
- recent stored player identities refreshed asynchronously

The completion request path reads only memory. Storage refreshes occur during startup/reload and on player identity updates.

The storage/query contract should return recent player identities, not just names, so future player-specific filters can use UUID, name, and last-seen metadata.

Alternative considered: keep `Set<String> getKnownPlayerNames()` as the only source. This was rejected because it cannot support recent offline players after restart and does not carry enough metadata for future filters.

## Risks / Trade-offs

- Existing aliases may conflict across proxy and backend profile sections -> Validate duplicates per runtime profile and log actionable warnings.
- Moving command names to `commands.yml` may surprise users who previously edited `config.yml` -> Migrate or seed defaults, document the new file, and update `CHANGELOG.md`.
- Recent-player startup refresh could be expensive on very large databases -> Filter by configurable day window, default to 30 days, and run refresh asynchronously.
- Completion cache can be stale until refresh or player join -> Treat suggestions as best-effort only; command execution still validates against storage asynchronously.
- Command API breakage can affect tests and internal callers -> Update common tests around behavior rather than preserving the old interface.

## Migration Plan

1. Add bundled `commands.yml` with proxy and backend profile defaults.
2. Add command completion configuration to `config.yml` with `recentPlayersDays: 30`.
3. Load or create `commands.yml` through the human-managed configuration pipeline.
4. Replace platform hard-coded command lists with common profile selection and native adapter registration.
5. Move current command behavior into command handlers, helpers, and completion providers.
6. Add storage support for recent player identity lookup by `last_seen`.
7. Update docs and `CHANGELOG.md` to announce `commands.yml` and profile-specific alias configuration.
