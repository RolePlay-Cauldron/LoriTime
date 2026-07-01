## 1. Scope Parsing

- [x] 1.1 Refactor shared command scope parsing so server/world flag parsing can be reused by modify commands without changing existing `/loritime` behavior.
- [x] 1.2 Update modify add/set parsing to accept one player, one required time expression, and optional `server:<server>` / `s:<server>` and `world:<world>` / `w:<world>` flags.
- [x] 1.3 Update modify reset parsing to accept one player and optional `server:<server>` / `s:<server>` and `world:<world>` / `w:<world>` flags.
- [x] 1.4 Implement world-only modify server resolution using the same proxy current-server and backend local-server rules used by `/loritime`.
- [x] 1.5 Reject legacy positional modify scopes `server <server>` and `world <server> <world>` with usage or scope feedback before storage mutation.

## 2. Modify Command Behavior

- [x] 2.1 Wire the parsed flag-style scopes into add, set, and reset storage reads and manual adjustment writes.
- [x] 2.2 Preserve global, server, and world mutation semantics for add and set, including non-negative total validation.
- [x] 2.3 Preserve scoped reset semantics by writing the signed adjustment required to bring the scoped total to zero.
- [x] 2.4 Ensure unresolved world-only scope requests do not call canonical storage mutation methods.

## 3. Completions

- [x] 3.1 Update modify completions to suggest player names from cache without database-backed storage reads.
- [x] 3.2 Update modify completions to suggest long scope prefixes `server:` and `world:` where scope flags are valid.
- [x] 3.3 Update modify completions to suggest cached and live server/world values for long and manually typed short flag prefixes.
- [x] 3.4 Ensure duplicate scope flags are not suggested after an equivalent long or short flag is already present.

## 4. Docs and Localization

- [x] 4.1 Update bundled localization usage strings in all language files to show flag-style `/ltmodify` scopes.
- [x] 4.2 Update `docs/Commands.md` to document the new `/ltmodify` scoped syntax and remove positional scope examples.
- [x] 4.3 Search project docs and bundled resources for remaining old positional modify syntax and update any remaining references.

## 5. Tests and Verification

- [x] 5.1 Add parser-level or command-level tests proving flag-style server and world modify scopes are accepted.
- [x] 5.2 Add tests proving world-only modify scope resolves the server on backend canonical and proxy runtimes.
- [x] 5.3 Add tests proving positional modify scopes are rejected and do not mutate storage.
- [x] 5.4 Add completion tests for modify scope prefixes and server/world value suggestions without synchronous storage reads.
- [x] 5.5 Run targeted command tests for the modified behavior and fix failures.
- [x] 5.6 Run `mvn verify` and fix all open build issues before marking implementation complete.
