## Why

Players and admins can now query totals by player, server, and world, but they cannot answer time-bounded questions such as "how much did this player play in the last eight months" or "how much did they play between four weeks ago and three days ago." Adding a configurable time-range flag makes `/loritime` useful for audits, competitions, and period-based reporting without adding new commands.

## What Changes

- Add an optional `time:<range>` flag to `/loritime`, with `t:<range>` accepted as a short alias.
- Suggest the long `time:` prefix in tab completion, but do not suggest `t:` or any values after `time:`/`t:`.
- Parse range values through the existing configurable `TimeParser`, so unit aliases keep the server's configured/localized meaning.
- Support single-duration ranges such as `time:8mo` as "from now back to 8 months ago."
- Support near-to-far ranges such as `time:3d-4w` as "from 4 weeks ago up to 3 days ago."
- Reject invalid, empty, unparsable, duplicate, zero/negative, or reversed time ranges.
- Count persisted session time only for the part of each session that overlaps the requested range.
- Count manual adjustments in ranged lookups when their `created_at` is inside the requested range and their scope matches the requested lookup scope.
- Include active in-memory session time only when the active session overlaps the requested range.
- **BREAKING**: Extend storage/API time query contracts to support optional time ranges; no legacy API compatibility layer is required.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-architecture`: `/loritime` parsing, validation, completion, and output semantics gain optional time-range filtering.
- `unified-storage-system`: unified storage and active-session accumulation gain time-range-aware total queries for sessions and manual adjustments.

## Impact

- `LoriTimeCommand`, lookup parsing, and lookup completions.
- Localization usage/output strings for time-ranged lookup feedback.
- Public/internal time query contracts such as `TimeQueryStorage`, `UnifiedStorage`, `AccumulatingTimeStorage`, and `LoriTimeService`.
- Database query helpers for session duration clipping and adjustment `created_at` filtering.
- Tests for parsing, command execution, completion, active sessions, database storage, and public API behavior.
