## Why

Admin storage maintenance needs a stable backend contract before user-facing commands are added, otherwise future server/world transfer and delete commands would either leak database details into command code or break custom storage integrations. Localization files also need a cleaner long-term layout and key naming convention so new command feedback can be added consistently.

## What Changes

- Add an admin-only storage maintenance capability that can preview and execute storage-type transfers, server-to-server transfers, world-to-world transfers, and scoped server/world deletions.
- Keep the existing runtime `UnifiedStorage` API compatible by exposing maintenance operations through a separate optional admin contract instead of adding mandatory methods to `UnifiedStorage`.
- Require destructive or merging operations to produce a preview and require explicit confirmation before execution.
- Move bundled and generated localization files into a `language/` folder.
- Rename localization keys to camelCase path segments instead of underscore-style or other non-camel-case key segments.
- Preserve user-facing behavior through migration or compatibility handling so existing installations can be updated.

## Capabilities

### New Capabilities
- `admin-storage-maintenance`: Admin storage maintenance primitives for previewing and applying transfer/delete operations before commands are wired to them.

### Modified Capabilities
- `command-architecture`: Admin command architecture must be prepared to host storage maintenance subcommands later without mixing player mutation behavior back into local runtime admin actions.
- `configuration-management`: Localization file location and key naming requirements change for human-managed language files.
- `unified-storage-system`: Storage contracts must remain runtime-safe while allowing optional admin maintenance behavior for database-backed storage.

## Impact

- Affected code: storage API/package contracts, database storage helpers, admin command scaffolding, localization loading, bundled language resources, config/file update paths, and tests.
- API impact: no breaking change to `UnifiedStorage`; new optional admin maintenance contract and result/preview types.
- Data impact: no schema migration is expected for transfer/delete behavior because existing server/world/session/adjustment references are sufficient.
- Operational impact: future commands can call the maintenance contract asynchronously and require confirmation for merges or deletions.
