## REMOVED Requirements

### Requirement: Sender compatibility with public player identity
**Reason**: Generic command senders include console actors and server-backed sender implementations that are not players and cannot truthfully satisfy the non-null LoriTime player identity contract.

**Migration**: Use the player sender role when a platform sender must also be passed as `LoriTimePlayer`. Keep generic `CommonSender` handling for sender permissions and messages only.
