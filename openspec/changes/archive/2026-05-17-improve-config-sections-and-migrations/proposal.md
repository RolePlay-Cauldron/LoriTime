## Why

LoriTime's current configuration layer exposes only flat dot-path access and rewrites YAML as flat key/value data, which makes section-level handling, template updates, key renames, deletions, and value-preserving migrations fragile. A dedicated configuration management capability is needed before more config shape changes accumulate and become harder to migrate safely.

## What Changes

- Add an own configuration section abstraction similar in purpose to Bukkit's `ConfigurationSection`, but independent from Bukkit and usable from common code.
- Support section reads, nested key traversal, typed values, key enumeration, and section existence checks without requiring callers to manually flatten or parse YAML maps.
- Separate human-managed configuration files from flat file data storage so config files can preserve template structure while legacy data storage remains simple.
- Add a versioned config update path that can add keys, rename keys, delete keys, move values, and preserve existing user values during template updates.
- Add validation and fallback behavior for malformed or incompatible config values where startup currently relies on direct casts.
- Keep the existing dot-path getter surface available during migration so runtime callers can move incrementally.

## Capabilities

### New Capabilities
- `configuration-management`: Defines structured configuration loading, section access, value-preserving updates, and versioned file migrations.

### Modified Capabilities
- None.

## Impact

- Affects `common/src/main/java/com/jannik_kuehn/common/config/**`, especially `Configuration`, `YamlConfiguration`, `YamlKeyValueStore`, and `FileManager`.
- Affects startup config loading in `LoriTimePlugin`.
- Affects localization file loading because localization currently uses the same `Configuration` abstraction.
- May require focused tests for YAML section behavior, config migration operations, template merge behavior, reload behavior, and compatibility of existing dot-path getters.
- Does not change database schema or plugin messaging protocol.
