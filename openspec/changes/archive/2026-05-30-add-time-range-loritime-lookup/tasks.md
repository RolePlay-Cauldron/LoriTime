## 1. Time Range Model And Parsing

- [x] 1.1 Add a first-class time range value for inclusive-start/exclusive-end lookup windows.
- [x] 1.2 Add parser support for `time:` and `t:` lookup flags in the shared lookup parser.
- [x] 1.3 Parse single-duration values through the configured `TimeParser` as `now - duration` through `now`.
- [x] 1.4 Parse near-to-far range values through the configured `TimeParser` as `now - far` through `now - near`.
- [x] 1.5 Reject duplicate, empty, unparsable, zero, negative, and reversed time-range flags.
- [x] 1.6 Add parser tests for valid single-duration, valid near-to-far, mixed-order flags, duplicate flags, and invalid ranges.

## 2. Command Execution And Completion

- [x] 2.1 Extend `LoriTimeCommand` to pass the optional range into storage lookups.
- [x] 2.2 Preserve existing permission checks and player/scope resolution with ranged lookups.
- [x] 2.3 Add ranged no-data feedback and output text that includes the requested range when useful.
- [x] 2.4 Update localization usage and messages for all bundled languages.
- [x] 2.5 Suggest `time:` as a long lookup prefix according to already-entered flags.
- [x] 2.6 Keep `t:` accepted but unsuggested, and return no suggestions for values after `time:` or `t:`.
- [x] 2.7 Add command execution and completion tests for ranged global, server, and world lookups.

## 3. Storage API Contracts

- [x] 3.1 Extend focused storage query contracts with ranged total methods.
- [x] 3.2 Extend `UnifiedStorage` and `AccumulatingTimeStorage` to support ranged totals.
- [x] 3.3 Extend `LoriTimeService` with ranged public API lookup methods.
- [x] 3.4 Update fake/test storage implementations for the new contract.
- [x] 3.5 Update API documentation for ranged lookup behavior and API breakage.

## 4. Database Range Queries

- [x] 4.1 Add range-aware session sum queries for global, server, and world scopes.
- [x] 4.2 Clip persisted session durations to range boundaries instead of counting whole overlapping sessions.
- [x] 4.3 Add range-aware manual adjustment sum queries using `created_at` filtering.
- [x] 4.4 Preserve existing unbounded query behavior when no range is supplied.
- [x] 4.5 Add database tests for clipped sessions, excluded non-overlap sessions, ranged adjustments, and scoped ranged totals.

## 5. Active Session Range Handling

- [x] 5.1 Add active-session overlap calculation for ranged accumulator queries.
- [x] 5.2 Ensure current active time contributes to ranges ending at now, such as `time:8mo`.
- [x] 5.3 Ensure current active time does not contribute to historical ranges, such as `time:3d-4w`.
- [x] 5.4 Add accumulator tests for matching, clipped, and excluded active-session ranges.

## 6. Verification

- [x] 6.1 Run focused common command and storage tests.
- [x] 6.2 Run platform compile checks if shared contracts require adapter updates.
- [x] 6.3 Run `mvn verify` and fix PMD findings without suppressions where practical.
