## Context

The current configuration implementation loads YAML through SnakeYAML, flattens nested maps into dot-path keys, and writes the flat map back to disk. That makes basic runtime lookups convenient, but it loses the distinction between sections and scalar values. It also makes human configuration files hard to update cleanly because the updater can only compare key sets and copy old values into a new resource map.

`Configuration` is currently used for two different jobs: human-managed plugin files such as `config.yml` and language files, and simple flat-file data access through `FileStorageProvider`. Human config needs structure, template order, defaults, migrations, validation, and readable output. Data storage mostly needs durable key/value reads and writes. Keeping both concerns behind one flat model makes config evolution harder than it needs to be.

## Goals / Non-Goals

**Goals:**
- Introduce a platform-independent configuration section API for common code.
- Preserve existing dot-path access while enabling section-oriented access for new code.
- Keep human config files structured and template-oriented instead of rewriting them as flat maps.
- Provide versioned config migrations for key additions, renames, moves, deletions, and value transformations.
- Preserve user values when updating to a newer bundled config template.
- Add enough validation/fallback behavior to avoid startup failures from simple type mismatches.
- Keep config loading usable for localization files.

**Non-Goals:**
- Replacing the database migration system.
- Changing plugin messaging protocol.
- Rewriting all existing config call sites in one step.
- Guaranteeing preservation of arbitrary user-written comments in old config files.
- Changing the user-facing meaning of existing config keys unless a migration explicitly defines it.

## Decisions

### Model config as a section tree

Create a common configuration document model backed by nested maps/lists and expose a `ConfigSection` style API. A section represents a path in the document and can read direct child keys, nested sections, scalar values, lists, and full paths.

Alternative considered: keep only the flat dot-path map and add helper methods that filter prefixes. That is cheaper initially, but it cannot reliably distinguish a real section from a scalar key prefix and continues to write poor YAML output.

### Keep dot-path getters as compatibility methods

The existing `Configuration` getter style should remain available so runtime code can migrate incrementally. Internally, dot paths should traverse the section tree instead of indexing a flattened map.

Alternative considered: introduce a new API and update every caller at once. That would make the end state cleaner, but it increases blast radius across storage, updater, commands, AFK, localization, and platform modules.

### Separate human config from flat data storage

Human-managed config files should use the new structured config document and migration pipeline. Legacy/simple data storage can keep a key/value-oriented YAML store because comments, template order, and section APIs are not central there.

Alternative considered: upgrade `YamlKeyValueStore` to handle every use case. That keeps fewer classes, but preserves the current ambiguity where config files and data files have conflicting persistence needs.

### Use template-first config rewriting

For `config.yml` updates, load the latest bundled resource as the canonical layout, apply migrated user values onto that layout, and write the result. This preserves current template order and bundled comments where the YAML writer supports them, while removing deleted keys predictably.

Alternative considered: preserve the user's exact YAML file and edit nodes in place. That keeps custom comments better, but requires a more capable YAML comment model and makes key moves/deletions more fragile.

### Use explicit schema versions for migrations

Add a config schema version value and run ordered migrations from the detected version to the latest version. Older files without a schema version should be treated as a known legacy baseline.

Migration operations should cover:
- add missing key with default value
- rename key while preserving value
- move key while preserving value
- delete obsolete key
- transform a value
- normalize enum-like strings
- validate value type and fall back to default when invalid

Alternative considered: infer migrations from key presence and resource key comparisons. That works only for simple additions and cannot reliably express renames, deletions, or changed value meaning.

### Prefer local abstractions over Bukkit APIs

The section API should be owned by common code and not depend on Bukkit's `ConfigurationSection`, so it remains usable on Paper, Bungee, and Velocity.

Alternative considered: wrap Bukkit/Paper configuration objects where available. That would bind the common module to a platform shape and would not help proxy platforms consistently.

## Risks / Trade-offs

- Migration bugs can rewrite user config incorrectly -> Always back up the file before applying template rewrites or versioned migrations.
- Template-first rewriting may drop custom user comments -> Document this trade-off and rely on backups; preserve values rather than arbitrary comments.
- Existing code may rely on flat `getAll()` behavior -> Define whether `getAll()` returns flattened values or structured data, and provide explicit methods for both if needed.
- Direct casts can still fail if typed getters are too strict -> Typed getters should handle numeric widening and invalid values consistently.
- Localization files may not need versioned migrations -> Allow schemas without migrations or treat localization as structured read/reload only.
- Data storage behavior could regress if forced through the human config path -> Keep data storage persistence separate from human config management.

## Migration Plan

1. Introduce the section/document API and tests without changing existing runtime behavior.
2. Reimplement dot-path getters over the structured document model.
3. Add a config schema definition for `config.yml` with a latest version and no-op baseline migration for current files.
4. Replace key-set update logic in `FileManager` with the schema migration and template merge pipeline for human config files.
5. Keep simple YAML key/value storage available for legacy file data and `FileStorageProvider`.
6. Move selected runtime call sites to section access where it improves clarity, especially command aliases and nested database pool settings.

Rollback is code-level as long as migrations are value-preserving and backed up. If a deployed version rewrites config files, operators can restore the generated backup before downgrading.

## Open Questions

- Should `configSchemaVersion` be a visible top-level key, or an internal metadata key such as `_schema.config`?
- Should unknown custom keys be dropped during template rewrite, appended to the end, or preserved only when they are under a dedicated custom section?
- Do language files need their own schema/version migration path, or only structured section reading and reload support?
- Should `getAll()` keep the historical flattened behavior for compatibility, with a new method for structured output?
