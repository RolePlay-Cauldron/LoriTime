package com.jannik_kuehn.loritimevelocity.event;

import java.time.Instant;
import java.util.UUID;

/**
 * A player started being tracked by LoriTime. Server switches do not fire this event.
 *
 * @param playerId the player UUID
 * @param name     the player name
 * @param server   the server name
 * @param world    the world name
 * @param occurredAt       when the session started
 */
public record LoriTimeSessionStartEvent(UUID playerId, String name, String server, String world, Instant occurredAt) {
}
