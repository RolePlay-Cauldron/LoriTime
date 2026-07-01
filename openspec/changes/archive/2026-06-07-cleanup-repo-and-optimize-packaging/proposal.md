## Why

The repository still carries stale submodule metadata even though the Maven modules now live directly in this repository. The release jars are also much larger than the project code itself because the build bundles runtime dependencies, especially SQLite native binaries, without an explicit packaging policy or measured optimization path.

## What Changes

- Remove obsolete submodule wiring from repository metadata and CI checkout steps.
- Retire the helper script that copies shared files into former submodule directories if it is no longer referenced.
- Keep Paper and Velocity release jars self-contained, including database drivers.
- Introduce a measured packaging optimization path that records jar-size baselines, evaluates SQLite native trimming against the supported platform list, and evaluates `minimizeJar` only with runtime smoke testing.
- Document the packaging policy so future dependency changes do not accidentally bloat or break the release jars.

## Capabilities

### New Capabilities

- `release-packaging-maintenance`: Defines repository cleanup and self-contained release jar packaging expectations.

### Modified Capabilities

None.

## Impact

- Repository root metadata: `.gitmodules`, local submodule assumptions, and ignored/generated files if needed.
- GitHub Actions checkout and release artifact workflow.
- Maven shade configuration in root, `common`, `paper`, and `velocity` modules.
- Runtime dependency packaging for SQLite, MySQL, MariaDB, bStats, and shared LoriTime code.
- Verification scope expands to include jar-size reporting and runtime smoke tests for Paper, Velocity, and supported storage modes.
