## 1. Maintenance Contract

- [x] 1.1 Add an optional admin storage maintenance contract separate from `UnifiedStorage`.
- [x] 1.2 Add request, preview, confirmation, and result value types for storage-type transfer, server transfer, world transfer, and scoped deletion.
- [x] 1.3 Expose maintenance support from default database-backed storage while returning unsupported behavior for storage implementations that do not provide the contract.
- [x] 1.4 Add confirmation fingerprint or token handling that binds execution to the previewed operation details.

## 2. Database Maintenance Implementation

- [x] 2.1 Add table helper queries for counting affected players, session rows, adjustment rows, and target collisions.
- [x] 2.2 Implement server dataset transfer for single and multiple source/target mappings, including target server/world creation.
- [x] 2.3 Implement world dataset transfer for single and multiple source/target mappings, including target server/world creation.
- [x] 2.4 Implement scoped server deletion for matching sessions, server-scoped adjustments, and world-scoped adjustments.
- [x] 2.5 Implement scoped world deletion for matching sessions and world-scoped adjustments without removing containing server adjustments.
- [x] 2.6 Ensure each multi-dataset transfer/delete operation runs atomically inside one transaction.
- [x] 2.7 Add storage-type transfer preparation that rejects non-empty targets before writing and preserves full history when copied.

## 3. Admin Command Preparation

- [x] 3.1 Add admin command/service plumbing that can discover the optional maintenance contract without adding final user-facing subcommands.
- [x] 3.2 Ensure future maintenance command execution paths are asynchronous and storage-owner-only.
- [x] 3.3 Add localized feedback keys for unsupported maintenance, preview summaries, confirmation required, confirmation mismatch, success, and failure.

## 4. Language Layout and Key Migration

- [x] 4.1 Move bundled localization resources into a `language/` folder.
- [x] 4.2 Update localization loading to use `language/<language>.yml` as the canonical path.
- [x] 4.3 Migrate existing root-level `<language>.yml` files into `language/` with backup/update safeguards.
- [x] 4.4 Rename bundled localization keys to camelCase path segments.
- [x] 4.5 Update runtime localization lookups to use canonical camelCase keys.
- [x] 4.6 Add migration or compatibility mappings that preserve values from known legacy keys.
- [x] 4.7 Preserve unknown custom localization keys during updates unless an explicit migration removes them.
- [x] 4.8 Update comments or documentation references that point to the old localization location.

## 5. Verification

- [x] 5.1 Add unit tests for maintenance preview counts, collision detection, confirmation mismatch rejection, and unsupported storage behavior.
- [x] 5.2 Add database tests for server transfer, world transfer, scoped deletion, and unchanged global totals.
- [x] 5.3 Add tests for atomic rollback on failed multi-dataset maintenance operations.
- [x] 5.4 Add tests for language folder creation, root language migration, camelCase key migration, and custom key preservation.
- [x] 5.5 Run focused storage and configuration tests.
- [x] 5.6 Run full Maven verification.
