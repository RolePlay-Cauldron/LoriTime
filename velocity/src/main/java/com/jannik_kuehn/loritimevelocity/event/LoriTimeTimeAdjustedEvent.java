package com.jannik_kuehn.loritimevelocity.event;

import com.jannik_kuehn.common.api.storage.TimeScope;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * A signed time adjustment was stored for a player, for example by a command, the API or AFK time removal.
 *
 * @param playerId  the adjusted player UUID
 * @param amount    the signed adjustment
 * @param scope     the adjustment scope
 * @param reason    the machine-readable adjustment reason
 * @param actorName the actor display name
 * @param at        when the adjustment was stored
 */
public record LoriTimeTimeAdjustedEvent(UUID playerId, Duration amount, TimeScope scope, String reason,
                                        String actorName, Instant at) {
}
