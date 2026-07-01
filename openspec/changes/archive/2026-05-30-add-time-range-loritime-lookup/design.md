## Context

`/loritime` already supports one optional player token plus `server:`/`s:` and `world:`/`w:` scope flags. Storage totals currently combine persisted session durations, matching manual adjustments, and active in-memory session time for global/server/world scopes. The database schema already stores enough timestamp data for this feature: session rows have `join_time` and `leave_time`, while manual adjustment rows have `created_at`.

The new time-range flag is command-facing, but it cuts across parsing, completion, localization, public API contracts, active session accumulation, and database SQL. API breakage is allowed, so implementation can introduce a first-class range type instead of hiding this behind overload-only helper code.

## Goals / Non-Goals

**Goals:**

- Add optional `/loritime` history windows with `time:<range>` and `t:<range>`.
- Reuse the configured `TimeParser` so unit symbols remain configurable and localized.
- Count session rows only for the portion that overlaps the requested window.
- Count manual adjustments by `created_at` when they match both the time window and requested scope.
- Preserve existing unbounded lookup behavior when no time flag is present.
- Keep tab completion deterministic: suggest `time:` as a prefix, accept `t:`, and do not suggest range values.
- Extend internal/public storage APIs cleanly for ranged totals.

**Non-Goals:**

- No new database migration is required because required timestamp columns already exist.
- No legacy API compatibility layer is required.
- No date-calendar syntax such as `2026-05-01..2026-05-25` is included.
- No range support is added to modify commands, top lists, placeholders, or slave read-cache messages in this change.

## Decisions

### Represent ranges as a first-class value

Introduce a small immutable value, for example `TimeRange`, with nullable/optional absence handled by overloads or a separate unbounded constant. A bounded range should store absolute instants or epoch milliseconds:

```text
TimeRange
  fromInclusive
  toExclusive
```

Using absolute instants keeps storage logic deterministic and avoids each table helper calculating "now" independently. Command parsing can create a range using the current clock once per lookup.

Alternative considered: store only parsed offsets such as `nearSeconds` and `farSeconds`. That would push "now" calculation into storage and active-session code, increasing drift and making tests harder.

### Parse command ranges as "ago" offsets

The value after `time:`/`t:` is parsed with the plugin's existing `TimeParser`.

```text
time:8mo    => [now - 8mo, now]
time:3d-4w  => [now - 4w, now - 3d]
```

For two-sided ranges, the left side is the near boundary and the right side is the far boundary. The parser MUST reject empty, unparsable, zero/negative, duplicate, or reversed ranges such as `time:4w-3d`.

Alternative considered: `time:4w-3d` as far-to-near. This reads naturally in some contexts, but it conflicts with the user's requested `3d-4w` shape and is easier to misuse.

### Count only overlapping session duration

Session queries must clip session rows to the requested range:

```text
overlapStart = max(join_time, rangeStart)
overlapEnd   = min(leave_time, rangeEnd)
counted      = max(0, overlapEnd - overlapStart)
```

Rows with no overlap contribute zero and should not make the query look like a hit. For SQL, the table helper can add range-aware variants using dialect-specific timestamp functions where needed. The active accumulator should apply the same overlap calculation in Java for current in-memory sessions.

Alternative considered: include entire rows that overlap the range. That is simpler but would overcount sessions crossing range boundaries, especially around day boundaries.

### Count manual adjustments by created_at

Manual adjustments do not have a session span, so their range membership is determined by `created_at`. The existing scope matching semantics stay intact:

- global lookup includes all adjustment scopes inside the range
- server lookup includes matching server adjustments and matching world adjustments on that server
- world lookup includes only matching world adjustments

Alternative considered: exclude manual adjustments from ranged lookup. The user explicitly wants adjustments to count, and `created_at` is already present and indexed.

### Completion suggests only the prefix

`time:` should be suggested alongside other long scope prefixes when applicable. `t:` remains accepted but unsuggested. Once the user has typed `time:` or `t:`, completion returns no value suggestions because ranges are individual and parser/config dependent.

### API shape can combine overloads and a breaking method

A pragmatic API shape is:

```java
OptionalLong getTime(UUID uniqueId, TimeScope scope);
OptionalLong getTime(UUID uniqueId, TimeScope scope, TimeRange range);
```

Existing global overloads can either remain for convenience or be removed if the codebase prefers a breaking simplification. Public `LoriTimeService` should expose a ranged equivalent returning `CompletableFuture<Optional<Duration>>`.

## Risks / Trade-offs

- SQL date arithmetic differs across SQLite and MySQL/MariaDB -> keep range boundaries as bound timestamps and isolate clipping SQL in table helpers/dialect methods.
- Ranged active-session calculations can be easy to get off by one -> use a consistent inclusive-start/exclusive-end range model and unit tests at boundaries.
- TimeParser uses configured units, so `m` likely means minute and `mo` means month -> document examples with configured unit expectations and avoid hardcoding month aliases.
- Manual adjustments are point-in-time events, not spans -> using `created_at` is auditable and simple, but it means an adjustment correcting old time counts in the range where the admin performed it, not where the original play happened.
- A ranged lookup can return no time even for a known player with lifetime time -> use a dedicated ranged no-data message or extend the scoped no-data message to mention the requested range.
