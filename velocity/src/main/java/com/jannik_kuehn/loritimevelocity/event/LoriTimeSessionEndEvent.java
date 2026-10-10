package com.jannik_kuehn.loritimevelocity.event;

import java.time.Instant;
import java.util.UUID;

/**
 * LoriTime stopped tracking a player, because they left or counting stopped, for example while AFK.
 *
 * @param playerId the player UUID
 * @param occurredAt       when tracking stopped
 */
public record LoriTimeSessionEndEvent(UUID playerId, Instant occurredAt) {
}
