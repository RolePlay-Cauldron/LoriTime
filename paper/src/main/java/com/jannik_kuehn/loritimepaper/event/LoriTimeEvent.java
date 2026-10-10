package com.jannik_kuehn.loritimepaper.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class of all LoriTime Bukkit events.
 */
public abstract class LoriTimeEvent extends Event {
    /**
     * The player the event is about.
     */
    private final UUID playerId;

    /**
     * When the event happened.
     */
    private final Instant at;

    /**
     * Creates the event. It is asynchronous when created off the primary thread.
     *
     * @param playerId the player UUID
     * @param at       when the event happened
     */
    protected LoriTimeEvent(final UUID playerId, final Instant at) {
        super(!Bukkit.isPrimaryThread());
        this.playerId = playerId;
        this.at = at;
    }

    /**
     * Gets the player this event is about.
     *
     * @return the player UUID
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Gets the moment the event happened.
     *
     * @return the event instant
     */
    public Instant getAt() {
        return at;
    }
}
