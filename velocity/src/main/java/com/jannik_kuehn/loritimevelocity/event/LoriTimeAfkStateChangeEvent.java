package com.jannik_kuehn.loritimevelocity.event;

import java.time.Instant;
import java.util.UUID;

/**
 * A player entered or left the AFK state.
 *
 * @param playerId the player UUID
 * @param afk      {@code true} when the player became AFK, {@code false} when they resumed
 * @param occurredAt       when the state changed
 */
public record LoriTimeAfkStateChangeEvent(UUID playerId, boolean afk, Instant occurredAt) {
}
