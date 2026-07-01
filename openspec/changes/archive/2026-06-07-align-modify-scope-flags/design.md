## Context

`/loritime` already parses scope arguments as self-describing flags: `server:<server>`, `s:<server>`, `world:<world>`, and `w:<world>`. The parser accepts one optional player token and valid flags in any order, and world-only lookups resolve the server from runtime context.

`/ltmodify` uses the same storage scopes but still parses a positional suffix: `server <server>` or `world <server> <world>`. That makes mutation syntax less ergonomic than lookup syntax and keeps separate parsing/completion logic for the same scope concept.

## Goals / Non-Goals

**Goals:**

- Make `add`, `set`, and `reset` in `/ltmodify` use the same server/world flag syntax as `/loritime`.
- Share parsing and completion behavior where that reduces duplicate command logic.
- Preserve existing global, server, and world storage mutation semantics.
- Update bundled usage text and command documentation to match the new syntax.
- Run `mvn verify` at the end of implementation and fix all build issues found.

**Non-Goals:**

- Preserve legacy positional modify scope syntax.
- Change storage schema, history rows, or public API behavior.
- Add time-range filtering to modify commands.
- Change permissions for modify commands.

## Decisions

### Reuse the lookup scope model for modify scopes

Modify should parse scope flags through a shared parser or shared parser primitives rather than maintaining positional suffix parsing in `CommandScopes`.

Rationale: lookup and modify both target the same `TimeScope` concepts. A shared flag parser reduces drift and makes short aliases, duplicate flag rejection, unknown flag rejection, and empty value rejection consistent.

Alternative considered: add a second modify-only flag parser. That would work, but it keeps duplicate rules for the same command language.

### Keep player and time parsing action-specific

`reset` has one target player token plus optional scope flags. `add` and `set` have one target player token, one required time expression, and optional scope flags. Their action-specific parsers should feed the parsed scope flags into existing mutation logic.

Rationale: `/loritime` has an optional player and optional time-range flag, while `/ltmodify` has required player/time positions for mutation actions. Sharing only the scope flag behavior keeps the parser clear without forcing all command grammars into one abstraction.

Alternative considered: parse every modify argument in any order. That would make it harder to distinguish a player name from multi-token time expressions and is not needed to solve the current usability problem.

### World-only modify scopes use lookup server resolution

When modify receives `world:<world>` without a server flag, it should resolve the server the same way `/loritime` does: target player's current backend on proxy runtimes, or the configured local server on backend canonical runtimes.

Rationale: this is the biggest practical improvement over the current `world <server> <world>` shape and keeps read/write scope behavior consistent.

Alternative considered: require `server:<server>` for every world-scoped modify operation. That is simpler internally, but it keeps modify less capable than lookup and does not meet the goal of handling world/server parsing the same way.

### Reject legacy positional scopes

The implementation should reject `server <server>` and `world <server> <world>` for modify commands with normal usage feedback.

Rationale: the project does not need backward compatibility for this command shape, and supporting both forms would keep ambiguous parsing paths alive.

Alternative considered: accept both forms temporarily. That lowers immediate migration friction but makes completions, docs, and future maintenance less clear.

## Risks / Trade-offs

- Users with saved instructions or habits for positional modify scopes will see usage feedback until they switch to flags. Mitigation: update bundled localization and `docs/Commands.md` in the same change.
- Multi-token time expressions can conflict with flag parsing if a time token contains a colon. Mitigation: treat only recognized scope flags as scope flags and reject unknown flag-like tokens rather than silently folding them into time.
- World-only modify on proxy runtimes depends on target-player current server availability. Mitigation: match `/loritime` behavior and reject the command when the server cannot be resolved.
- Shared parser changes can affect `/loritime`. Mitigation: keep existing lookup tests and add modify-specific parser/execution coverage before running full `mvn verify`.
