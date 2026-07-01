## Why

`CommonSender` currently extends `LoriTimePlayer` even though LoriTime also uses it for console and server-backed command senders that have no player UUID. The type model hides that distinction behind null UUIDs and console checks, which makes player identity easier to misuse after the public player model cleanup.

## What Changes

- **BREAKING** separate generic sender behavior from player identity so a `CommonSender` no longer implies `LoriTimePlayer`.
- Introduce explicit common sender roles for player senders and console senders.
- Make player lookups and online-player enumeration return player sender contracts instead of generic sender contracts.
- Stop platform server adapters from also acting as console sender instances.
- Replace sender console/player branching that depends on fake player identity with role-aware sender handling.
- **BREAKING** remove the public facade compatibility requirement that any `CommonSender` can be passed as a `LoriTimePlayer`; only player sender abstractions may satisfy player identity.

## Capabilities

### New Capabilities
- `common-sender-model`: common command sender roles and the player/console identity boundary across supported platform adapters.

### Modified Capabilities
- `public-api-facade`: remove generic sender compatibility with `LoriTimePlayer` while preserving player sender use of the public identity contract.

## Impact

- Common sender and server contracts in `common.api.common`.
- Paper, Bungee, and Velocity command sender and server adapters.
- Commands, AFK handling, updater notifications, plugin messaging, and admin actor audit metadata that currently branch on `isConsole()` or call player identity methods on `CommonSender`.
- Public API specs that currently describe `CommonSender` as a valid public player identity.
