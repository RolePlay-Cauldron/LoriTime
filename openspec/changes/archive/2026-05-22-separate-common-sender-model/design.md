## Context

The recent public player cleanup narrowed `LoriTimePlayer` to UUID and latest-known player name. The common platform layer still models command actors through `CommonSender`, and that contract currently extends `LoriTimePlayer` for all senders. Platform console wrappers and platform server adapters therefore satisfy a player contract with null UUIDs and a `CONSOLE` name even though they are not players.

Command and runtime code currently recovers the distinction through `isConsole()` checks before using player-only data. `CommonServer` player lookup and online enumeration also return generic sender types even though those methods can only produce players.

## Goals / Non-Goals

**Goals:**

- Make generic command sender behavior independent from player identity.
- Represent player senders and console senders with explicit common contracts.
- Keep player sender adapters usable wherever LoriTime needs both messaging/permission behavior and a `LoriTimePlayer`.
- Make platform server adapters server services only instead of reusing them as console sender instances.
- Update call sites to branch on sender role before reading player-only data.

**Non-Goals:**

- Do not change the public `LoriTimePlayer` identity fields.
- Do not redesign storage or `LoriTimeService` threading in this sender-focused change.
- Do not keep compatibility shims that preserve fake UUID access for console senders.
- Do not add new sender roles that are not represented by current platform behavior.

## Decisions

### Generic sender contract has no player or console identity

`CommonSender` will keep behavior shared by all command actors, such as permission checks, messages, and sender display metadata. It will no longer extend `LoriTimePlayer` and will no longer expose methods whose meaning depends on whether the sender is a player.

Alternative considered: keep `CommonSender` generic but retain nullable `getUniqueId()` and `isConsole()`. That keeps current call sites shorter, but preserves the invalid player identity state this change is meant to remove.

### Player and console roles are explicit contracts

The common layer will expose a player sender contract that combines `CommonSender` with `LoriTimePlayer`, and a console sender contract for console-only behavior. Player sender adapters implement the player role. Console command wrappers implement the console role.

Role-aware sender code can use concrete common sender contracts or type checks to select player UUID audit metadata, player self-target behavior, and console actor metadata. Console role handling preserves the stable console audit label without pretending a console has a player UUID.

Alternative considered: use only `CommonPlayerSender` and infer any non-player sender as console. The current supported platforms expose console as the relevant non-player sender, so an explicit console contract documents that behavior and removes boolean console state from the generic sender contract.

### Server adapters stop doubling as console senders

`CommonServer` implementations will provide server operations only. Console senders will be created through command/platform sender adapters where sender behavior is needed, and server console messaging continues through the server console messaging operation.

Alternative considered: make each server adapter implement the new console sender contract. That would remove fake UUIDs, but it would keep one object responsible for both server services and command actor behavior.

### Player-returning server methods use player sender types

`CommonServer#getPlayer` and `CommonServer#getOnlinePlayers` will return player sender contracts. Callers that receive values from those methods no longer need to defend against console results, and player converter/AFK code gets a truthful player identity source.

Alternative considered: keep generic return types while documenting that results are players. That loses type information at exactly the boundary where the platform lookup already knows the result is a player.

### Breaking cleanup is preferred over compatibility aliases

This change removes sender methods and inheritance that no longer match the domain model. Call sites will be migrated directly instead of adding deprecated aliases or nullable bridges.

## Risks / Trade-offs

- [Commands currently branch on `isConsole()` and read UUID from `CommonSender`] -> Update those paths around explicit player and console roles and add focused tests for player self-target and console audit behavior.
- [Platform wrappers currently mix player and non-player sender adaptation] -> Keep platform player adapters and console adapters separate so their contracts match the native source type.
- [Sender display metadata and player identity names overlap] -> Keep player sender name semantics aligned with `LoriTimePlayer` and make generic sender display semantics explicit in the common sender contract.
- [Changing server adapter responsibilities touches all supported platforms] -> Update Paper, Bungee, and Velocity server tests/adapters together and keep `sendMessageToConsole` as the server-side console output path.
