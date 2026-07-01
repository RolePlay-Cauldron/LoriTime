## ADDED Requirements

### Requirement: Time-ranged lookup command
The canonical time lookup command SHALL support an optional time-range flag that filters the returned total to a bounded history window.

#### Scenario: Single duration time range filters from now
- **WHEN** a sender executes `/loritime` with `time:<duration>` or `t:<duration>`
- **THEN** the command SHALL parse `<duration>` with the configured `TimeParser`
- **THEN** the command SHALL query only the time window from the parsed duration ago up to the command execution time

#### Scenario: Near-to-far time range filters historical interval
- **WHEN** a sender executes `/loritime` with `time:<near>-<far>` or `t:<near>-<far>`
- **THEN** the command SHALL parse both durations with the configured `TimeParser`
- **THEN** the command SHALL query only the time window from `<far>` ago up to `<near>` ago

#### Scenario: Time range combines with scope flags
- **WHEN** a sender executes `/loritime` with a valid time-range flag and any valid combination of player, server, and world lookup arguments
- **THEN** the command SHALL apply the requested time window to the resolved player and scope
- **THEN** the command SHALL preserve the existing scope permission checks

#### Scenario: Time range accepts flags independent of order
- **WHEN** a sender executes `/loritime` with one optional player token, valid scope flags, and a valid time-range flag in any order
- **THEN** the command SHALL parse the same target player, scope, and time range regardless of argument order

#### Scenario: Duplicate time range is rejected
- **WHEN** a sender executes `/loritime` with more than one `time:` or `t:` flag
- **THEN** the command SHALL reject the request with usage feedback

#### Scenario: Invalid time range is rejected
- **WHEN** a sender executes `/loritime` with an empty, unparsable, zero, negative, or reversed time-range value
- **THEN** the command SHALL reject the request with usage feedback
- **THEN** the command SHALL NOT query canonical storage

#### Scenario: Time-ranged no-data feedback distinguishes missing range data
- **WHEN** a sender executes a time-ranged lookup for a known player and storage has no matching time in the requested window
- **THEN** the command SHALL report that the player has no tracked time for the requested range and scope
- **THEN** the command SHALL NOT report that the player has never played globally

### Requirement: Time-range lookup completions
Scoped command completion SHALL expose the long time-range flag prefix while leaving custom range values unsuggested.

#### Scenario: Time lookup suggests long time flag prefix
- **WHEN** a sender completes a time lookup argument where a time-range flag may be entered
- **THEN** the completion path SHALL suggest `time:` according to already-entered flags
- **THEN** the completion path SHALL NOT suggest `t:`

#### Scenario: Time range values are not suggested
- **WHEN** a sender completes the value portion of `time:` or `t:`
- **THEN** the completion path SHALL return no range value suggestions
- **THEN** the completion path SHALL NOT query database-backed storage

#### Scenario: Time flag is not suggested after it is already present
- **WHEN** a sender completes a time lookup after entering a valid `time:` or `t:` flag
- **THEN** the completion path SHALL NOT suggest another `time:` flag
