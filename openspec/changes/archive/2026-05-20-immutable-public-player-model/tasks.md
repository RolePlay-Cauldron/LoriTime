## 1. Public Player Contract

- [x] 1.1 Convert `LoriTimePlayer` into a public identity interface exposing `getUniqueId()` and `getName()`.
- [x] 1.2 Add an immutable public player reference implementation for UUID/name pairs.
- [x] 1.3 Ensure public player model validation rejects null or invalid identity data where objects are constructed or consumed.

## 2. Internal Runtime Model

- [x] 2.1 Move the current mutable AFK/session state into an internal runtime player implementation.
- [x] 2.2 Update `LoriTimePlayerConverter` to cache and return the internal runtime implementation for LoriTime internals.
- [x] 2.3 Update AFK, command, placeholder, messaging, and platform code that needs mutable runtime state to use the internal runtime type.

## 3. Sender Compatibility

- [x] 3.1 Update `CommonSender` to extend the public `LoriTimePlayer` identity contract.
- [x] 3.2 Verify Paper, Bungee, Velocity, and console sender implementations still satisfy the sender contract.
- [x] 3.3 Keep sender-only behavior out of the public `LoriTimePlayer` contract.

## 4. Facade Operations

- [x] 4.1 Add `LoriTimeService` overloads for querying online time by `LoriTimePlayer`.
- [x] 4.2 Add `LoriTimeService` overloads for system manual adjustments by target `LoriTimePlayer`.
- [x] 4.3 Add `LoriTimeService` overloads for actor-aware manual adjustments using target and actor `LoriTimePlayer` objects.
- [x] 4.4 Ensure all player-object facade overloads use UUID as the canonical identity and use names only as metadata.

## 5. Tests and Documentation

- [x] 5.1 Add or update tests for immutable public player references and public contract boundaries.
- [x] 5.2 Add or update tests for `CommonSender` compatibility with player-object facade overloads.
- [x] 5.3 Add or update tests for player-based time query and manual adjustment facade behavior.
- [x] 5.4 Update `docs/API.md` with the public player model, player-object examples, and internal sender separation.
- [x] 5.5 Run targeted common tests and a compile check for affected platform modules.
