## 1. Common Sender Contracts

- [x] 1.1 Refactor `CommonSender` to keep only shared sender behavior and add explicit player sender and console sender roles.
- [x] 1.2 Update `CommonServer` player lookup and online-player enumeration signatures to return player sender roles.
- [x] 1.3 Remove platform server adapters from the sender hierarchy while preserving server console output operations.

## 2. Platform Sender Adapters

- [x] 2.1 Update Paper player and console command sender adapters to implement the explicit sender roles.
- [x] 2.2 Update Bungee player and console command sender adapters to implement the explicit sender roles.
- [x] 2.3 Update Velocity player and console command sender adapters to implement the explicit sender roles.

## 3. Runtime Sender Call Sites

- [x] 3.1 Migrate common commands from console boolean checks and generic sender UUID access to role-aware sender handling.
- [x] 3.2 Migrate AFK, updater, player conversion, plugin messaging, and server dispatch call sites to the player sender return types where player identity is required.
- [x] 3.3 Preserve audited console and player actor metadata for manual adjustments after the sender split.

## 4. Verification

- [x] 4.1 Add or update common tests for player self-target behavior, console-only command behavior, and manual adjustment actor metadata.
- [x] 4.2 Add or update platform adapter tests for server player lookup and console sender separation.
- [x] 4.3 Run focused module tests and compile all supported platform modules after the sender contract break.
