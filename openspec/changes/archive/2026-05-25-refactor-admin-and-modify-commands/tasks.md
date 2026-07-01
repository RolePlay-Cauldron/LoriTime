## 1. Shared Command Foundations

- [x] 1.1 Inspect existing command classes for duplicated permission checks, subcommand routing, completion filtering, localization, player lookup, and time parsing.
- [x] 1.2 Add or refine shared command internals for subcommand routing and completion so command classes do not need repeated switch/filter boilerplate.
- [x] 1.3 Add or refine shared helpers for localized command responses and usage/error messages.
- [x] 1.4 Add or refine shared helpers for recent-player completions, UUID/name lookup, and parsed time arguments.

## 2. Admin Command Refactor

- [x] 2.1 Move reload behavior from the local command into the admin command.
- [x] 2.2 Move debug behavior into the admin command.
- [x] 2.3 Move info behavior into the admin command.
- [x] 2.4 Keep update behavior in the admin command and align it with the new shared subcommand structure.
- [x] 2.5 Update admin tab completion to include reload, debug, info, update, and any supported runtime-admin subcommands.
- [x] 2.6 Remove the dedicated local command class and local command registration path.

## 3. Modify Command

- [x] 3.1 Add a dedicated modify command for canonical player mutations.
- [x] 3.2 Move add/modify time behavior from admin into the modify command.
- [x] 3.3 Move set time behavior from admin into the modify command.
- [x] 3.4 Move reset time behavior from admin into the modify command.
- [x] 3.5 Move delete-user behavior from admin into the modify command.
- [x] 3.6 Add modify command tab completion using the shared recent-player completion cache without direct storage lookups on the completion path.

## 4. Runtime Profiles and Commands Configuration

- [x] 4.1 Replace the local command node with the modify command node in command node configuration.
- [x] 4.2 Register admin on proxy, backend canonical, and backend slave profiles.
- [x] 4.3 Register modify on proxy and backend canonical profiles only.
- [x] 4.4 Update `commands.yml` defaults so proxy admin uses `plta`.
- [x] 4.5 Update `commands.yml` defaults so backend canonical and backend slave admin use `lta`.
- [x] 4.6 Remove the local command node from bundled `commands.yml`.
- [x] 4.7 Add modify command defaults to canonical runtime profile sections in bundled `commands.yml`.

## 5. Tests and Documentation

- [x] 5.1 Update command architecture tests for the new admin, modify, and slave profile command availability.
- [x] 5.2 Add admin command tests for reload, debug, info, and update routing.
- [x] 5.3 Add modify command tests for add/modify, set, reset, delete-user, permissions, and completion behavior.
- [x] 5.4 Update configuration tests for `plta`, `lta`, removed local node, and modify node defaults.
- [x] 5.5 Update command documentation and migration notes for the breaking command syntax changes.
- [x] 5.6 Add a `CHANGELOG.md` entry describing the admin/modify command split, alias changes, and removal of the local command.

## 6. Verification

- [x] 6.1 Run focused common command and configuration tests.
- [x] 6.2 Compile affected Paper/Folia-compatible, Bungee, and Velocity modules.
- [x] 6.3 Run OpenSpec validation for `refactor-admin-and-modify-commands`.
- [x] 6.4 Run full `mvn verify`.
