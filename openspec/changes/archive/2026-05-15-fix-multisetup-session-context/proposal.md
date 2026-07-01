## Why

Multi-setup session rows currently mix proxy backend server names with Paper slave configured server names, producing duplicate server entries for the same player session. Paper slave autoflush also sends completed session chunks that the master appends as new rows, so leave and switch updates can target the wrong active row semantics.

## What Changes

- Make proxy masters the authoritative owner of canonical session lifecycle in proxy multi-setup deployments.
- Use the proxy backend server name as the canonical `server` context for multisetup session rows.
- Restrict Paper/Folia slave reporting to current world context updates for the master's active session instead of completed session row writes.
- Prevent slave autoflush from creating canonical time rows.
- Ensure backend server switches and player leaves update the master-owned active row for the current backend session.
- **BREAKING**: In proxy multi-setup mode, Paper/Folia `server.name` will no longer create canonical server entries.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `unified-storage-system`: Clarify proxy master ownership of canonical session rows and change Paper/Folia slave session reporting into world context reporting.

## Impact

- Affects Velocity and Bungee session listeners, Paper/Folia slave session reporting, plugin messaging for storage/session context, and accumulator/session persistence tests.
- Existing databases may contain historical mixed server entries; this change prevents new duplicates but does not migrate old rows.
