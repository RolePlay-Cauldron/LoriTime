package com.jannik_kuehn.loritimevelocity.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * The tracking tags of a player changed.
 *
 * @param playerId     the player UUID
 * @param previousTags the tags before the change
 * @param tags         the tags after the change
 * @param at           when the tags changed
 */
public record LoriTimeTrackingTagChangeEvent(UUID playerId, Map<String, String> previousTags,
                                             Map<String, String> tags, Instant at) {
    /**
     * Creates the event with immutable tag maps.
     *
     * @param playerId     the player UUID
     * @param previousTags the tags before the change
     * @param tags         the tags after the change
     * @param at           when the tags changed
     */
    public LoriTimeTrackingTagChangeEvent {
        previousTags = Map.copyOf(previousTags);
        tags = Map.copyOf(tags);
    }
}
