## 1. Repository Cleanup

- [x] 1.1 Confirm no actual Git submodules are configured or required by the repository.
- [x] 1.2 Remove stale `.gitmodules` from the repository.
- [x] 1.3 Remove `submodules: true` from GitHub Actions checkout steps that no longer need it.
- [x] 1.4 Delete `.github/updateSubmodules.sh` if no remaining workflow or documentation references it.
- [x] 1.5 Review `.gitignore` for generated build/report outputs and update only if tracked noise remains.

## 2. Packaging Baseline

- [x] 2.1 Build current Paper and Velocity release jars from a clean target state.
- [x] 2.2 Record baseline sizes for `paper/target/LoriTimePaper.jar` and `velocity/target/LoriTimeVelocity.jar`.
- [x] 2.3 Record major bundled dependency groups, including SQLite natives, MySQL, MariaDB, Guava, bStats, and LoriTime common classes.
- [x] 2.4 Identify dependencies currently shaded despite being provided by Paper, Velocity, or the plugin runtime.

## 3. Self-Contained Jar Optimization

- [x] 3.1 Preserve bundled SQLite, MySQL, and MariaDB driver classes in Paper and Velocity release jars.
- [x] 3.2 Adjust Maven dependency scopes or shade includes to avoid bundling platform-provided dependencies unless a private relocated copy is required.
- [x] 3.3 Document that SQLite keeps the native binaries shipped by `sqlite-jdbc` for broad platform support.
- [x] 3.4 Keep all bundled SQLite native binaries for broad platform support.
- [x] 3.5 Enable `minimizeJar` after local evaluation and keep runtime smoke tests as the release gate.
- [x] 3.6 Record optimized Paper and Velocity jar sizes after each accepted packaging change.

## 4. Verification

- [x] 4.1 Run Maven verification for affected modules.
- [ ] 4.2 Smoke test Paper plugin startup with the optimized jar.
- [ ] 4.3 Smoke test Velocity plugin startup with the optimized jar.
- [x] 4.4 Smoke test SQLite storage mode on each supported SQLite native target available to the maintainer.
- [ ] 4.5 Smoke test MySQL and MariaDB storage modes.
- [x] 4.6 Verify config and localization resources load from the optimized jars.
- [ ] 4.7 Verify updater/version parsing and bStats loading still work after any minimization.
- [x] 4.8 Compare final jar sizes against the baseline and document the result.
