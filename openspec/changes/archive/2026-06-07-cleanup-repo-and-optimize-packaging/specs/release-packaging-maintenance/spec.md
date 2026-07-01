## ADDED Requirements

### Requirement: Repository contains no stale submodule contract
The repository SHALL NOT advertise Maven module directories as Git submodules when those directories are checked in as normal repository content.

#### Scenario: No submodules are configured
- **WHEN** maintainers inspect repository metadata and CI checkout configuration
- **THEN** `.gitmodules` and submodule checkout options are absent unless an actual Git submodule is present

#### Scenario: Obsolete submodule helper is removed
- **WHEN** a helper script exists only to synchronize files into former submodule directories
- **THEN** the helper script is removed or replaced by normal repository-level configuration

### Requirement: Release jars are self-contained
Paper and Velocity release jars SHALL include the runtime dependencies needed for plugin startup and supported storage backends without requiring users to install separate LoriTime dependency jars.

#### Scenario: Database drivers remain bundled
- **WHEN** a Paper or Velocity release jar is built
- **THEN** the jar contains the supported SQLite, MySQL, and MariaDB driver classes needed by LoriTime storage configuration

#### Scenario: Platform APIs are not bundled unnecessarily
- **WHEN** a dependency is provided by the target server platform or plugin environment
- **THEN** the release jar excludes that dependency unless LoriTime requires a relocated private copy

### Requirement: SQLite native binaries remain broadly bundled
The build SHALL keep the SQLite native binaries shipped by `sqlite-jdbc` unless LoriTime explicitly changes its SQLite platform support policy.

#### Scenario: Broad native coverage is retained
- **WHEN** a Paper or Velocity release jar is built
- **THEN** SQLite native binaries for the platforms shipped by `sqlite-jdbc` remain bundled

#### Scenario: SQLite driver entry point is retained
- **WHEN** a release jar is built with shade minimization enabled
- **THEN** `org.sqlite.JDBC` remains present so SQLite mode can load the JDBC driver

### Requirement: Jar minimization requires runtime validation
The build MAY enable shade minimization for release artifacts, but release validation SHALL include runtime smoke tests for plugin paths that depend on shaded classes.

#### Scenario: Minimization is enabled
- **WHEN** `minimizeJar` is enabled for a release artifact
- **THEN** verification includes Paper startup, Velocity startup, SQLite storage, MySQL storage, MariaDB storage, config/localization loading, updater/version parsing, and bStats loading

#### Scenario: Runtime validation fails
- **WHEN** any required runtime smoke test fails after minimization
- **THEN** minimization is reverted or constrained with explicit includes before the optimization is accepted

### Requirement: Packaging changes report size impact
Packaging optimization changes SHALL include before-and-after jar-size measurements for Paper and Velocity release artifacts.

#### Scenario: Packaging optimization is reviewed
- **WHEN** a change modifies shading, dependency scope, native trimming, or minimization
- **THEN** the reviewable output includes total release jar size before and after the change

#### Scenario: Major dependency payload is investigated
- **WHEN** a release jar remains larger than expected
- **THEN** maintainers can identify the largest bundled dependency groups from the build or documented measurement command
