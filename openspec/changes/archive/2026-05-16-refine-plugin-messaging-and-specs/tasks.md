## 1. Messaging Protocol Model

- [x] 1.1 Identify all current storage and AFK plugin-message operations, payload shapes, and protocol-version checks.
- [x] 1.2 Introduce explicit storage message operation parsing for supported storage operations such as read, manual write, stale session rejection, world context update, and world switch.
- [x] 1.3 Keep the existing supported storage wire payloads compatible unless a version bump is explicitly required and documented.
- [x] 1.4 Review AFK message protocol helpers and centralize typed AFK transition parsing where it reduces duplication without reintroducing legacy payload support.
- [x] 1.5 Add or update tests for supported storage operations, unknown storage operations, unsupported storage versions, malformed storage payloads, supported AFK transitions, invalid AFK transitions, unsupported AFK versions, and malformed AFK payloads.

## 2. Diagnostics and Logging

- [x] 2.1 Improve storage plugin-message warning and error text so it identifies message family, operation, protocol version when available, and failure context.
- [x] 2.2 Improve AFK plugin-message warning and error text so it identifies message family, transition when available, target UUID when available, and failure context.
- [x] 2.3 Replace misleading generic storage error messages such as operation-independent "could not add time" text with operation-specific diagnostics.
- [x] 2.4 Keep normal ignored state, such as orphan world context messages, out of warning-level logs unless it indicates an invalid payload or protocol mismatch.

## 3. Documentation and Specs

- [x] 3.1 Correct `DataStorageManager` custom storage injection JavaDoc/comments so injected storage close and reload ownership matches current implementation.
- [x] 3.2 Update `openspec/specs/unified-storage-system/spec.md` purpose text.
- [x] 3.3 Update unified storage requirements for `world` context updates versus `world_switch` session splitting.
- [x] 3.4 Update unified storage requirements for typed storage plugin-message operations and diagnostics.
- [x] 3.5 Update `openspec/specs/afk-session-coordination/spec.md` purpose text.
- [x] 3.6 Update AFK coordination requirements for improved AFK plugin-message diagnostics.

## 4. Verification

- [x] 4.1 Run focused common plugin messaging tests.
- [x] 4.2 Run focused storage accumulation tests that cover world context update and world switch semantics.
- [x] 4.3 Run `mvn -pl common pmd:check`.
- [x] 4.4 Run `mvn verify`.
- [x] 4.5 Run `openspec validate refine-plugin-messaging-and-specs --strict`.
