## 1. Scope Model And API

- [x] 1.1 Add a common time-scope value type for global, server, and world scopes.
- [x] 1.2 Update time query contracts to accept scoped totals while preserving global default call paths where useful.
- [x] 1.3 Update manual adjustment value types and write contracts to carry explicit scope metadata.
- [x] 1.4 Update public service/API facade methods for scoped time queries and scoped adjustments, accepting API breakage.

## 2. Database Baseline And Storage

- [x] 2.1 Edit the database baseline schema for SQLite, MySQL, and MariaDB so time adjustments store scope type plus nullable server/world references.
- [x] 2.2 Remove or collapse obsolete legacy aggregate schema behavior if it conflicts with the new baseline-only model.
- [x] 2.3 Update server/world table helpers as needed for lookup and reference resolution without creating fake world rows for server scope.
- [x] 2.4 Implement scoped adjustment inserts and scoped adjustment sum queries.
- [x] 2.5 Implement scoped session sum queries for global, server, and world scopes.
- [x] 2.6 Update accumulating storage so active online time contributes only to matching scoped totals.

## 3. Modify Command

- [x] 3.1 Extend modify command parsing for optional `server <server>` and `world <server> <world>` scope suffixes.
- [x] 3.2 Apply scoped add operations by writing scoped signed adjustments.
- [x] 3.3 Apply scoped set operations by computing the matching scoped total and writing the required scoped adjustment.
- [x] 3.4 Apply scoped reset operations by computing the matching scoped total and writing the required scoped adjustment to reach zero.
- [x] 3.5 Update modify command usage, localization, and completions for scoped forms.

## 4. Time Lookup Command

- [x] 4.1 Extend `/loritime` parsing for `server <server> [player]` and `world <server> <world> [player]`.
- [x] 4.2 Enforce `loritime.see.server` and `loritime.see.server.other` for server-scoped lookups.
- [x] 4.3 Enforce `loritime.see.world` and `loritime.see.world.other` for world-scoped lookups.
- [x] 4.4 Preserve existing global self and other-player lookup behavior.
- [x] 4.5 Update lookup command messages, usage text, and completions for scoped forms.

## 5. AFK Context

- [x] 5.1 Ensure AFK time removal can obtain the player's current server/world context from active session or platform context.
- [x] 5.2 Write AFK time-removal adjustments in world scope when current context is available.
- [x] 5.3 Use shared fallback server/world context for AFK removal when specific context is unavailable.
- [x] 5.4 Preserve existing AFK stop-count, kick, and announcement behavior.

## 6. Tests And Documentation

- [x] 6.1 Add storage tests for global, server, and world scoped totals with sessions and adjustments.
- [x] 6.2 Add storage tests proving global adjustments are not included in server/world totals.
- [x] 6.3 Add command tests for scoped lookup parsing, permissions, and other-player behavior.
- [x] 6.4 Add modify command tests for scoped add, set, and reset behavior.
- [x] 6.5 Add AFK tests proving time-removal adjustments preserve world scope.
- [x] 6.6 Update command, storage, migration/breakage, and API documentation.
- [x] 6.7 Run focused common-module tests and cross-platform compile checks.
