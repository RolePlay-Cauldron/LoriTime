## 1. Protocol And Contracts

- [x] 1.1 Define a slave-to-master world context message shape that carries player UUID, world name, and observation time without start/stop timestamps.
- [x] 1.2 Update storage plugin-message handling to accept world context updates separately from completed session writes.
- [x] 1.3 Keep existing manual adjustment/write messages functional while rejecting unsupported or stale session protocol messages safely.

## 2. Master Session Ownership

- [x] 2.1 Ensure Velocity master session rows use the proxy backend server name as canonical server context.
- [x] 2.2 Ensure Bungee master session rows use the proxy backend server name as canonical server context.
- [x] 2.3 Ensure proxy master autoflush updates the active persisted session row instead of inserting new rows.
- [x] 2.4 Ensure backend server switch and disconnect close the current master-owned active row.
- [x] 2.5 Apply latest known Paper/Folia world context to active master-owned sessions without creating standalone rows.

## 3. Paper/Folia Slave Reporting

- [x] 3.1 Replace Paper/Folia slave completed session reporting with current-world context reporting.
- [x] 3.2 Stop Paper/Folia slave periodic updates from sending completed session chunks.
- [x] 3.3 Ensure Paper/Folia slave join, world change, periodic update, and leave send only current world context when needed.
- [x] 3.4 Ensure Paper/Folia `server.name` no longer creates canonical server entries in proxy multi-setup.

## 4. Tests

- [x] 4.1 Add plugin messaging tests for world context messages and unsupported session protocol handling.
- [x] 4.2 Add accumulator or integration-style tests for proxy join, autoflush, backend switch, autoflush, and leave producing one row per backend server session.
- [x] 4.3 Add tests proving Paper/Folia slave autoflush does not insert canonical time rows.
- [x] 4.4 Add tests proving Paper/Folia world context updates enrich the active master-owned session and are ignored when no active session exists.
- [x] 4.5 Add regression tests proving Paper/Folia `server.name` does not create duplicate canonical server entries in proxy multi-setup.

## 5. Verification

- [x] 5.1 Run focused common-module tests for plugin messaging, accumulator behavior, and database session persistence.
- [x] 5.2 Compile affected Paper, Velocity, and Bungee modules.
- [x] 5.3 Update relevant storage or multisetup documentation if user-facing setup guidance changes.
