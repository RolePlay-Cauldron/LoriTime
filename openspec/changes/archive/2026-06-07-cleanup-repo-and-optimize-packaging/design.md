## Context

LoriTime is now a multi-module Maven repository with `config`, `common`, `paper`, and `velocity` modules checked into the same repository. The repository still contains `.gitmodules`, CI checkout steps that request submodules, and a `.github/updateSubmodules.sh` helper for copying shared files into directories that are no longer Git submodules.

The Paper and Velocity release jars are self-contained and currently about 21 MB each. The compiled LoriTime platform jars are small; most release jar size comes from shaded runtime dependencies. SQLite native libraries are the largest payload because `sqlite-jdbc` includes binaries for multiple operating systems and CPU architectures.

## Goals / Non-Goals

**Goals:**

- Remove stale submodule metadata and CI behavior.
- Preserve single-file deployment for Paper and Velocity release jars.
- Keep database drivers bundled in release jars.
- Reduce jar size only where the supported runtime behavior remains explicit and tested.
- Add verification that records jar-size impact and validates runtime behavior for affected storage modes.

**Non-Goals:**

- Splitting database drivers into optional external jars.
- Removing support for a platform accidentally or without documentation.
- Reworking application storage behavior.
- Changing public API or command behavior.

## Decisions

### Remove Submodule Wiring Completely

Remove `.gitmodules`, remove `submodules: true` from repository checkout steps, and delete `.github/updateSubmodules.sh` if no workflow or documentation still references it.

Alternative considered: keep `.gitmodules` as historical metadata. This keeps CI and contributor tooling misleading, so removal is cleaner.

### Keep Release Jars Self-Contained

Paper and Velocity release jars MUST continue to include the runtime dependencies required for normal plugin startup and supported storage backends. This includes database drivers for SQLite, MySQL, and MariaDB.

Alternative considered: publish driver-specific or optional dependency jars. That would reduce the main jar size, but it weakens the deployment model and is out of scope.

### Keep Full SQLite Native Platform Coverage

SQLite native trimming is not used because LoriTime should support as many SQLite platforms as the bundled `sqlite-jdbc` dependency supports. The release jars keep the native binaries shipped by `sqlite-jdbc`.

Alternative considered: remove less common natives to reduce jar size. That would make the jars smaller but would weaken SQLite support on some operating systems and CPU architectures.

### Enable `minimizeJar` with Runtime Smoke Test Gate

`minimizeJar` is enabled for the shaded common artifact to reduce release jar size. Runtime smoke tests must cover Paper startup, Velocity startup, SQLite, MySQL, MariaDB, bStats loading, config/localization loading, and update/version parsing before publishing a release from this configuration. The shade minimizer can remove classes used through reflection, service loading, JDBC drivers, or platform integration, so a successful Maven test run alone is not enough.

Alternative considered: keep minimization disabled until every runtime smoke test is available. The size reduction is valuable enough to enable it now, while keeping the release verification gate explicit.

### Measure Before and After

The implementation should record baseline and optimized jar sizes for `LoriTimePaper.jar` and `LoriTimeVelocity.jar`. The measurement should include at least total jar size and major dependency groups so the optimization result is reviewable.

## Risks / Trade-offs

- SQLite native trimming can remove support for a user platform -> mitigate by documenting the supported native target list before removing entries.
- `minimizeJar` can remove reflectively loaded classes -> mitigate with runtime smoke tests and explicit shade includes/excludes where needed.
- Removing submodule wiring can break forgotten local workflows -> mitigate by searching workflows, scripts, and documentation before deletion.
- Keeping all database drivers bundled limits the maximum possible size reduction -> accepted to preserve simple deployment.

## Migration Plan

1. Remove stale submodule metadata and CI checkout options.
2. Confirm CI can checkout and build without submodule initialization.
3. Record current Paper and Velocity release jar sizes and dependency group sizes.
4. Apply packaging optimizations in small steps, measuring after each step.
5. Run Maven verification and runtime smoke tests before accepting any minimization or native trimming.

Rollback is straightforward: restore the prior Maven shade configuration or native include list if a runtime smoke test fails.

## Open Questions

- Should Alpine Linux containers be officially supported? If yes, keep Linux Musl SQLite natives.
- Should Windows and macOS SQLite natives remain in release jars for local development and small-server use?
- Should less common architectures such as RISC-V, PPC64, 32-bit x86, and ARMv6/v7 be unsupported for SQLite mode?
