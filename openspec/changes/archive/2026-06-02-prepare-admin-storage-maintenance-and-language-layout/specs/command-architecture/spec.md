## ADDED Requirements

### Requirement: Admin storage maintenance command preparation
The admin command architecture SHALL be prepared to host storage maintenance actions without exposing them through player mutation commands.

#### Scenario: Maintenance actions remain admin-scoped
- **WHEN** storage maintenance commands are added in a later change
- **THEN** they SHALL be exposed through the admin command or an admin command subtree
- **THEN** they SHALL NOT be exposed through the modify command

#### Scenario: Maintenance commands require storage owner
- **WHEN** a future storage maintenance command is executed on a runtime that does not own canonical storage
- **THEN** the command SHALL reject the operation before attempting maintenance storage access

#### Scenario: Maintenance commands use preview flow
- **WHEN** a future storage maintenance command would transfer, merge, or delete data
- **THEN** the command SHALL use the admin storage maintenance preview and confirmation flow
- **THEN** the command SHALL NOT directly manipulate database tables

#### Scenario: Maintenance command execution is asynchronous
- **WHEN** a future storage maintenance command previews or applies storage work
- **THEN** the command SHALL execute storage work away from platform main-thread command paths
