## Context

LoriTime currently has language files in `language/` and a newer `Localization` resolver package, but startup and runtime callers are still shaped around a single selected localization file. The new resolver expects versioned language files with locale metadata and message sections, while bundled language resources still need to be moved to that schema.

The public API facade is currently implemented as a final `LoriTimeService` class returned directly by `LoriTimeAPI`. That exposes the implementation type as the stable API surface, making later implementation swaps a breaking change even when method signatures stay the same.

## Goals / Non-Goals

**Goals:**
- Load configured default and hard fallback localization from the plugin data `language/` folder.
- Allow custom language files in the plugin data `language/` folder without requiring bundled resources.
- Track language tags loaded during the current localization lifecycle and reload only those known tags plus required default/fallback tags.
- Add a player language selection boundary that returns the configured default language for every player/sender for now.
- Migrate bundled localization resources to the new schema and update runtime calls to the new resolver API.
- Make `LoriTimeService` a public interface and move the current behavior into an internal implementation returned by `LoriTimeAPI`.

**Non-Goals:**
- Persist per-player language preferences.
- Read platform/client locale values.
- Preserve legacy localization schema or old localization API compatibility.
- Persist a separate language registry across full server restarts.

## Decisions

### Load Localization by Known Tags, Not Directory Scan

`Localization` will own the set of known loaded language tags for the current lifecycle. Startup loads the configured default tag and `en-us` hard fallback. Runtime calls that request a language tag ensure that tag is loaded from `language/<tag>.yml` before resolving messages. Reload reloads the known tag set, not every YAML file present in the folder.

Alternative considered: load every `language/*.yml` file on startup. This is rejected because a plugin directory may contain draft, invalid, or unused custom localization files that should not affect health or startup behavior.

Alternative considered: persist a loaded-language registry. This is deferred because future per-player language preferences can become the durable source of requested language tags. Until then, configured default plus fallback are enough across restarts.

### Custom Languages Are File-Based

`FileManager` should create/update bundled languages only for known bundled tags. Custom tags are valid only when the matching file already exists in the plugin data `language/` folder. Missing custom default languages degrade to fallback with clear logging instead of attempting to copy a non-existent bundled resource.

Alternative considered: require every configured language to be bundled. This blocks server-owned custom localizations and is the problem this change addresses.

### Break Legacy Localization Schema

Bundled files will be migrated to the new schema with explicit locale metadata and message sections. The loader will not support the old schema at runtime.

Alternative considered: dual-read old and new schemas. This is rejected because the user explicitly prefers breaking the old schema now instead of carrying compatibility code.

### Introduce a Language Selection Boundary

Commands and player-facing message helpers should call a language provider/resolver rather than reading `plugin.getLocalization()` as a single default language object. The initial implementation returns `general.language` for every player and sender.

Alternative considered: add player-language overloads directly to every command method now. This spreads future preference logic through command code and makes later storage/platform decisions harder to isolate.

### Service Interface and Internal Implementation

`LoriTimeService` becomes the public interface containing the existing public methods and constants that are part of the API contract. The current implementation moves to an internal concrete class such as `DefaultLoriTimeService`. `LoriTimeAPI.service()` returns `Optional<LoriTimeService>`.

Alternative considered: keep the final class and add a second interface later. This would force third-party integrations that typed against the class to migrate later, so the break should happen now while API changes are already in scope.

## Risks / Trade-offs

- Existing custom language files fail after update -> Document the new schema and regenerate bundled examples so server owners have a clear migration target.
- Missing configured custom language produces degraded localization -> Always load `en-us` fallback and log the missing configured tag.
- Lazy loading can surface invalid custom language files at message time -> Cache failed load results for the lifecycle or log clearly to avoid repeated noisy failures.
- Service interface conversion breaks callers importing the concrete type -> Keep method signatures stable on the interface and document `LoriTimeAPI.service()` as the supported entry point.

## Migration Plan

1. Convert bundled language resources to the new schema.
2. Update localization startup to ensure the language folder, configured default, and fallback are available.
3. Replace single-file localization construction and old method calls with resolver-based calls.
4. Add the default-only player language provider and route player-facing command messages through it.
5. Convert `LoriTimeService` to an interface and move implementation to a package-private/internal class.
6. Update tests and documentation for new localization schema and API return type.
