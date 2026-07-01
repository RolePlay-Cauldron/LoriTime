## 1. Localization Schema and Resources

- [x] 1.1 Convert bundled language resources to the new localization schema with schema version, locale tag, prefix, and message section.
- [x] 1.2 Update bundled localization keys/usages so runtime lookups target the new schema paths.
- [x] 1.3 Update localization documentation to describe the new custom language file schema and migration expectation.

## 2. Localization Loading

- [x] 2.1 Update `FileManager` language handling so bundled languages are created/updated from resources and custom languages are loaded only from existing plugin data files.
- [x] 2.2 Update `LocalizationLoader` to load a requested language tag from `language/<tag>.yml` instead of loading every YAML file in the folder.
- [x] 2.3 Update `Localization` to track successfully loaded language tags for the current lifecycle and reload only known/default/fallback tags.
- [x] 2.4 Ensure configured default and hard fallback localization are loaded during startup and used for fallback resolution.
- [x] 2.5 Add tests for bundled language creation, existing custom language loading, missing custom language fallback, invalid schema rejection, and reload of known tags only.

## 3. Sender Language Boundary

- [x] 3.1 Add a language selection interface for sender/player-facing messages.
- [x] 3.2 Implement the initial selector so every sender/player resolves to the configured default language.
- [x] 3.3 Route command and player-facing message helpers through the language selector and new localization resolver API.
- [x] 3.4 Add tests proving player and console messages use the configured default language through the selector.

## 4. Public Service Interface

- [x] 4.1 Convert `LoriTimeService` into a public interface containing the existing public facade methods.
- [x] 4.2 Move the current service behavior into an internal implementation class.
- [x] 4.3 Update `LoriTimeAPI.service()` to return `Optional<LoriTimeService>` backed by the internal implementation.
- [x] 4.4 Update API tests and documentation to depend on the interface rather than the concrete implementation.

## 5. Verification

- [x] 5.1 Run focused common module tests for localization, command messaging, and public API facade behavior.
- [x] 5.2 Run compile or broader module verification for affected modules.
- [x] 5.3 Run `openspec validate prepare-localization-and-service-api --strict`.
