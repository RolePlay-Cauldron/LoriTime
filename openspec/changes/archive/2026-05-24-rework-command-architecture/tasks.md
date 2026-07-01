## 1. Configuration Foundations

- [x] 1.1 Add bundled `commands.yml` with `profiles.proxy`, `profiles.backend.canonical`, and `profiles.backend.slave` command node defaults.
- [x] 1.2 Load or create `commands.yml` through the human-managed configuration pipeline.
- [x] 1.3 Add command completion configuration to `config.yml` with a default recent-player window of 30 days.
- [x] 1.4 Add configuration access objects or helpers for command names, aliases, profile sections, and completion settings.
- [x] 1.5 Add tests for `commands.yml` creation, reload behavior, backend profile naming, and default completion settings.

## 2. Recent Player Suggestions

- [x] 2.1 Add a recent player identity model containing UUID, latest known name, and last-seen timestamp when available.
- [x] 2.2 Add storage support for querying player identities with `last_seen` inside the configured recent-player window.
- [x] 2.3 Implement a shared recent player suggestion cache that merges online players, runtime-observed names, and asynchronously refreshed stored identities.
- [x] 2.4 Refresh recent player suggestions asynchronously on startup, reload, and player identity updates.
- [x] 2.5 Add tests proving tab completion reads only cache data and does not call database-backed storage on the completion request path.

## 3. Common Command Architecture

- [x] 3.1 Replace the thin `CommonCommand` model with command definitions, command context, command nodes, execution policy, and completion provider abstractions.
- [x] 3.2 Implement a common command dispatcher that enforces permissions and sender constraints before invoking handlers.
- [x] 3.3 Implement shared sync and async execution policy handling in the dispatcher.
- [x] 3.4 Implement shared helper services for localized command messages, completion filtering, player lookup, and time parsing.
- [x] 3.5 Add tests for permission denial, async dispatch, completion filtering, and command handler invocation.

## 4. Runtime Profiles and Platform Registration

- [x] 4.1 Implement code-defined runtime profiles for `proxy`, `backend.canonical`, and `backend.slave`.
- [x] 4.2 Resolve command names and aliases from `commands.yml` for only the command nodes supported by the selected runtime profile.
- [x] 4.3 Refactor Paper/Folia-compatible backend registration to use the common profile registry.
- [x] 4.4 Refactor Bungee registration to use the common profile registry.
- [x] 4.5 Refactor Velocity registration to use the common profile registry.
- [x] 4.6 Add tests for standalone backend, backend master, backend slave, and proxy command availability.

## 5. Command Behavior Migration

- [x] 5.1 Move canonical time lookup behavior into the new command handler structure.
- [x] 5.2 Move top-time behavior into the new command handler structure.
- [x] 5.3 Move admin time mutation behavior into the new command handler structure.
- [x] 5.4 Move local reload behavior into a local operational command node that reloads only the current instance.
- [x] 5.5 Move debug behavior into a local operational command node that affects only the current instance.
- [x] 5.6 Move info behavior into a local operational command node that reports only the current instance.
- [x] 5.7 Move AFK command behavior into the new command handler structure and keep registration gated by AFK availability.

## 6. Documentation and Migration Notes

- [x] 6.1 Update command documentation to describe proxy and backend profile command availability.
- [x] 6.2 Document `commands.yml`, including profile-specific command names and aliases.
- [x] 6.3 Update migration notes for users moving aliases from `config.yml` to `commands.yml`.
- [x] 6.4 Update `CHANGELOG.md` with the introduction of `commands.yml` for custom command alias configuration.

## 7. Verification

- [x] 7.1 Run focused common command architecture and completion tests.
- [x] 7.2 Run focused configuration-management tests.
- [x] 7.3 Run affected platform registration tests or compile affected platform modules.
- [x] 7.4 Run OpenSpec validation for `rework-command-architecture`.
