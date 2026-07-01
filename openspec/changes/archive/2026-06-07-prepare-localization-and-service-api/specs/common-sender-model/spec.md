## ADDED Requirements

### Requirement: Player language selection boundary
The system SHALL route player-facing localization through a language selection boundary that can later resolve per-player language preferences without changing command APIs.

#### Scenario: Player sender uses configured default language
- **WHEN** a player sender receives a localized LoriTime message
- **THEN** the language selection boundary SHALL resolve the sender language to the configured default language
- **THEN** the localized message SHALL be formatted through the localization resolver using that language tag

#### Scenario: Console sender uses configured default language
- **WHEN** a console sender receives a localized LoriTime message
- **THEN** the language selection boundary SHALL resolve the sender language to the configured default language
- **THEN** the localized message SHALL be formatted through the localization resolver using that language tag

#### Scenario: Future preference source is isolated
- **WHEN** LoriTime later adds per-player language preferences
- **THEN** command handlers SHALL be able to keep their message-sending APIs stable
- **THEN** preference lookup changes SHALL be isolated behind the language selection boundary
