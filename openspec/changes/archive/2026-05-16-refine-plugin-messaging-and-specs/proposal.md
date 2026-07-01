## Why

The storage and AFK flows now have the right ownership boundaries, but plugin messaging still mixes protocol concerns, string commands, decoding, and side effects in one class. The specs also lag behind the current behavior around world context reporting and the external storage ownership documentation is misleading.

## What Changes

- Refine plugin messaging around explicit protocol message types instead of ad hoc string command handling where practical.
- Keep existing wire compatibility for currently supported versioned messages unless a deliberate protocol-version bump is required.
- Improve storage and AFK plugin-message diagnostics so warnings and errors identify the message family, operation, protocol version, and failure context.
- Correct external storage injection documentation so it matches the actual close/reload ownership behavior, or explicitly document the intended behavior if implementation keeps the current lifecycle.
- Update OpenSpec specs to describe current world-context versus world-switch semantics, versioned AFK behavior, and plugin-message diagnostics.
- Fill in missing spec purposes for the touched specs.

Out of scope:
- AFK resume will not be changed to remember the last concrete server/world context.
- Accumulator transition ordering and atomicity will not be redesigned in this change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `unified-storage-system`: clarify storage plugin-message semantics, world context versus world switch behavior, diagnostics, and external storage lifecycle documentation.
- `afk-session-coordination`: clarify versioned AFK plugin-message diagnostics and supported payload expectations.

## Impact

- `common/src/main/java/com/jannik_kuehn/common/module/messaging/PluginMessaging.java`
- AFK and storage plugin-message protocol helper classes, if introduced
- Paper slave message emitters for AFK and world reporting, if message construction is centralized
- `DataStorageManager` JavaDoc/comments around injected storage lifecycle
- `openspec/specs/unified-storage-system/spec.md`
- `openspec/specs/afk-session-coordination/spec.md`
- Focused tests for plugin-message decoding, unsupported versions, malformed payloads, and logging/error context where testable
