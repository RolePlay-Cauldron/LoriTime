## Context

The current command architecture separates canonical data commands from local operational commands by registering a dedicated local command root. In practice, that makes common operations such as reload harder to use and leaves the admin command responsible for both runtime administration and player time mutation.

The desired model is a smaller set of command roots with clearer responsibilities:

```text
Admin command
  reload
  debug
  info
  update

Modify command
  add / modify
  set
  reset
  deleteUser

Read commands
  time lookup
  top
  afk
```

Proxy and backend runtimes still need distinct alias defaults through `commands.yml`, but local operational behavior should live under admin instead of a separate local root.

## Goals / Non-Goals

**Goals:**
- Remove the dedicated local command class and local command node.
- Move reload, debug, info, and update behavior into the profile-specific admin command.
- Move player time mutation and user deletion behavior into a dedicated modify command.
- Keep modify unavailable on backend slave runtimes.
- Use `plta` as the proxy admin command name and `lta` as the backend admin command name by default.
- Clean up command classes so shared parsing, completion, localization, player lookup, and async execution patterns are reused instead of duplicated.
- Update changelog documentation for the breaking command syntax changes.

**Non-Goals:**
- Preserving old command roots or aliases for compatibility.
- Changing storage semantics for time mutation or delete-user behavior.
- Changing permissions beyond aligning them with the new command responsibilities.
- Changing public API compatibility guarantees; API breakages are acceptable.

## Decisions

### Split command roots by responsibility

Admin should represent runtime administration, while modify should represent canonical storage mutation. This keeps backend slave rules explicit: slaves may administer their local runtime, but they must not mutate canonical player time or delete canonical users.

Alternative considered: keep mutation subcommands under admin and only remove the local root. That improves ergonomics for reload, but admin remains too broad and keeps storage mutation mixed with runtime operations.

### Register admin on every runtime profile

All runtime profiles should register admin because every runtime needs local operational controls. The profile-specific admin command should expose reload, debug, info, and update.

Alternative considered: keep separate debug and info roots. That preserves old syntax, but it keeps the command surface larger than necessary and duplicates local operational command wiring.

### Register modify only on canonical profiles

Proxy and backend canonical profiles should register the modify command. Backend slave profiles should not register it because they do not own canonical storage writes.

Alternative considered: register modify on slaves and forward mutations to the master. That would be a larger behavior change and risks adding a second mutation path before the command architecture is stable.

### Keep alias availability configurable but code-defined

`commands.yml` should still decide names and aliases for supported nodes, while code decides which nodes exist in each runtime profile. The local node should be removed from default configuration and from command-node resolution.

Alternative considered: leave an unused local node in `commands.yml`. That would reduce config churn, but the project explicitly allows breaking changes and no legacy code.

### Build shared command helpers before moving behavior

The refactor should first identify repeated command concerns and move them behind common helpers or command internals. Candidate shared concerns include subcommand routing, argument-count validation, localized usage/error messages, recent-player completions, UUID/name lookup, and time parsing.

Alternative considered: move methods between command classes directly. That would satisfy syntax changes but preserve duplicated and hard-to-maintain command code.

## Risks / Trade-offs

- Existing user command aliases break -> Document the change in `CHANGELOG.md` and rely on `commands.yml` customization for users to choose their own names.
- Admin command may become large again -> Keep admin limited to runtime operations and use shared subcommand dispatch helpers rather than one large switch.
- Modify command may duplicate existing admin mutation logic during migration -> Extract or share mutation helpers before deleting the old admin methods.
- Permission expectations may be unclear -> Preserve existing permission intent: admin runtime operations require admin permission; modify requires admin or a dedicated mutation permission if the existing permission model already supports it.
- Tests may miss platform registration regressions -> Add profile-level command availability tests and compile all affected platform modules.

## Migration Plan

1. Refactor shared command internals and helpers.
2. Introduce the modify command and migrate mutation behavior.
3. Move runtime admin behavior into admin and remove the local command.
4. Update command profile registration and `commands.yml` defaults.
5. Update tests, documentation, and `CHANGELOG.md`.
6. Run focused common tests, affected platform compiles, OpenSpec validation, and full `mvn verify`.
