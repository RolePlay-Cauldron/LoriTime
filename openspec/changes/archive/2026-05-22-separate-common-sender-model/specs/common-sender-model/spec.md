## ADDED Requirements

### Requirement: Generic sender behavior is independent from player identity
The system SHALL model common command sender behavior without requiring every sender to expose a player UUID, player online state, or public player identity contract.

#### Scenario: Console sender is not a player identity
- **WHEN** a platform command is executed by the console
- **THEN** the common sender used for the command SHALL provide sender behavior without satisfying `LoriTimePlayer`
- **THEN** the common sender SHALL NOT expose a fake or nullable player UUID through the generic sender contract

#### Scenario: Shared sender behavior remains available
- **WHEN** a command handles either a player sender or console sender
- **THEN** the common sender contract SHALL provide shared permission and message-sending behavior

### Requirement: Player sender role combines sender behavior and player identity
The system SHALL expose a player sender role for platform actors that are both common senders and LoriTime player identities.

#### Scenario: Platform player sender is accepted as player identity
- **WHEN** a platform adapter represents an online player sender
- **THEN** the adapter SHALL satisfy the player sender role
- **THEN** the player sender SHALL provide the UUID and player name required by `LoriTimePlayer`

#### Scenario: Player-only logic requires player sender role
- **WHEN** runtime code needs sender player identity such as self-target UUID or player actor UUID metadata
- **THEN** the runtime SHALL use the player sender role before reading player identity

### Requirement: Console sender role is explicit
The system SHALL expose a console sender role for platform actors that represent console command execution without player identity.

#### Scenario: Console adjustment actor is role-aware
- **WHEN** a console sender performs an audited manual adjustment
- **THEN** runtime code SHALL use null actor UUID and the stable console actor name
- **THEN** runtime code SHALL NOT obtain that metadata from fake player identity values

#### Scenario: Console-only command behavior is role-aware
- **WHEN** a command rejects a console self-target action
- **THEN** the command SHALL identify the sender through the console sender role

### Requirement: Server player lookup returns player senders
The common server contract SHALL expose player sender results for operations that look up or enumerate online players.

#### Scenario: Player lookup has player type
- **WHEN** runtime code looks up an online player from the common server by UUID or name
- **THEN** a successful lookup SHALL return a player sender role

#### Scenario: Online enumeration has player type
- **WHEN** runtime code enumerates online players from the common server
- **THEN** every returned sender SHALL satisfy the player sender role

### Requirement: Server services are not console senders
Platform server adapters SHALL represent server services separately from console command sender adapters.

#### Scenario: Server adapter exposes console output without sender role
- **WHEN** runtime code sends a message to the platform console through the common server contract
- **THEN** the server adapter SHALL deliver the console message
- **THEN** the server adapter SHALL NOT need to act as a console command sender
