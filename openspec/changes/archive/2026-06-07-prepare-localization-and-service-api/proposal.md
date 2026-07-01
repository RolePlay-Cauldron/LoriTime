## Why

Localization is moving from one selected YAML file to a resolver that can support custom languages and future per-player language preferences. The public API facade also needs an interface boundary so LoriTime can change its service implementation without changing the API methods that third-party integrations call.

## What Changes

- **BREAKING** Replace the legacy single-file localization API usage with the new `Localization` resolver API.
- **BREAKING** Migrate bundled language files to the new schema format and do not keep legacy language schema compatibility.
- Support custom language files from the plugin data `language/` folder without requiring them to be bundled resources.
- Load only languages that are needed now or have already been loaded in the current localization lifecycle, instead of blindly loading every YAML file in `language/`.
- Introduce a language selection interface for player-facing messages that currently returns the configured default language for every player.
- Preserve a future path for per-player language preferences without changing command APIs again.
- **BREAKING** Convert `LoriTimeService` into a public interface and move the current implementation behind it.
- Return the `LoriTimeService` interface from `LoriTimeAPI` instead of exposing the concrete implementation class.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `configuration-management`: Localization files use the new schema, custom language files are loaded from the canonical language folder when requested or previously loaded, and startup ensures default/fallback localization is available.
- `common-sender-model`: Player-facing message localization is routed through a language selection boundary that currently resolves every sender/player to the configured default language.
- `public-api-facade`: The public service contract becomes an interface returned by `LoriTimeAPI`, with the current behavior provided by an internal implementation.

## Impact

- Affected code: `common/src/main/java/com/jannik_kuehn/common/config/localization/**`, `FileManager`, bundled `common/src/main/resources/language/*.yml`, command/message helpers, sender-facing message paths, `LoriTimePlugin` localization startup/reload, `LoriTimeService`, `LoriTimeAPI`, and API tests.
- Affected docs: localization documentation and public API documentation should reflect the new language schema and service interface.
- Compatibility: Existing custom language files using the legacy schema must be migrated by server owners or regenerated; the change intentionally does not preserve the old localization API/schema.
