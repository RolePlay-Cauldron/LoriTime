## MODIFIED Requirements

### Requirement: Stable public API facade
The system SHALL expose a focused public API facade interface for third-party plugins without requiring callers to depend on internal plugin, storage lifecycle, active session persistence, or concrete service implementation types.

#### Scenario: Third-party plugin obtains public facade
- **WHEN** LoriTime has completed API initialization
- **THEN** a caller SHALL be able to obtain the public API facade interface through `LoriTimeAPI`
- **THEN** the returned facade SHALL NOT require the caller to cast to `LoriTimePlugin`, `UnifiedStorage`, `DataStorageManager`, `TimeAccumulator`, or a concrete service implementation class

#### Scenario: Broad plugin accessor is not public API
- **WHEN** third-party integrations use the public API entry point
- **THEN** the system SHALL expose the focused facade interface instead of the internal plugin object
- **THEN** the public entry point SHALL NOT expose storage lifecycle, configuration, localization, updater, or accumulator internals

#### Scenario: Service implementation is replaceable
- **WHEN** LoriTime changes the internal implementation of the public facade
- **THEN** third-party integrations typed against the `LoriTimeService` interface SHALL keep the same public method contract
- **THEN** callers SHALL NOT need to reference the concrete implementation type returned by `LoriTimeAPI`
