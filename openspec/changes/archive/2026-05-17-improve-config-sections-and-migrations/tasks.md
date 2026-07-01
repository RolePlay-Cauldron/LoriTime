## 1. Model And API

- [x] 1.1 Add a platform-independent configuration section interface for section paths, direct keys, recursive keys, typed values, nested sections, and existence checks.
- [x] 1.2 Add a structured configuration document abstraction that stores YAML as nested maps/lists instead of a flattened dot-path map.
- [x] 1.3 Implement dot-path traversal helpers for reading, writing, removing, moving, and checking nested paths.
- [x] 1.4 Define compatibility behavior for existing `Configuration` methods, including whether `getAll()` remains flattened or gains a separate structured alternative.

## 2. YAML Loading And Persistence

- [x] 2.1 Implement structured YAML loading for human-managed config and localization files.
- [x] 2.2 Implement structured YAML persistence for human-managed config files without flattening nested sections.
- [x] 2.3 Keep or introduce a separate flat YAML key/value store for legacy file-backed data storage use cases.
- [x] 2.4 Update `FileManager` entry points so callers can explicitly load human config files versus flat data files.

## 3. Config Migration Pipeline

- [x] 3.1 Add config schema metadata for bundled `config.yml`, including latest schema version and legacy baseline behavior for unversioned files.
- [x] 3.2 Add ordered config migration support with operations for add, rename, move, delete, value transform, and validation fallback.
- [x] 3.3 Replace key-set comparison based config updates with the versioned migration and template merge pipeline.
- [x] 3.4 Ensure existing user values are preserved when overlaying migrated values onto the latest bundled template.
- [x] 3.5 Ensure original config files are backed up before any migration or template rewrite writes to disk.

## 4. Runtime Integration

- [x] 4.1 Wire structured config loading into `LoriTimePlugin` startup for `config.yml`.
- [x] 4.2 Wire structured loading into localization file loading without requiring localization-specific migrations.
- [x] 4.3 Reimplement existing dot-path getters and setters over the structured document so current runtime callers keep working.
- [x] 4.4 Move selected nested config call sites to section access where it improves clarity, such as command aliases or database pool settings.
- [x] 4.5 Keep legacy file storage and storage migration imports from accidentally using human config template migrations.

## 5. Validation And Error Handling

- [x] 5.1 Add typed getter behavior for missing values, incompatible values, numeric conversions, and supplied defaults.
- [x] 5.2 Add logging for invalid config values that fall back to defaults.
- [x] 5.3 Add clear startup errors for unreadable or malformed YAML that cannot be recovered through defaults.
- [x] 5.4 Decide and implement handling for unknown custom keys during template rewrites.

## 6. Tests

- [x] 6.1 Add unit tests for section lookup, missing sections, direct keys, recursive keys, and section-relative paths.
- [x] 6.2 Add unit tests for typed section getters with valid values, missing defaults, incompatible values, and numeric widening.
- [x] 6.3 Add unit tests proving existing dot-path getters, setters, remove, reload, and contains behavior still work.
- [x] 6.4 Add unit tests for migration operations: add, rename, move, delete, transform, and unversioned baseline migration.
- [x] 6.5 Add tests for template merge behavior preserving user values and adding new defaults.
- [x] 6.6 Add tests proving human config migrations are not applied to flat data storage files.
- [x] 6.7 Run focused common-module tests for configuration, storage file provider compatibility, localization, and startup-adjacent config usage.
