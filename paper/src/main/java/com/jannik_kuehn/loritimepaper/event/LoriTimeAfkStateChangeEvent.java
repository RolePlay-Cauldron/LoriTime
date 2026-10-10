package com.jannik_kuehn.loritimepaper.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * A player entered or left the AFK state.
 */
public class LoriTimeAfkStateChangeEvent extends LoriTimeEvent {
    /**
     * Handler list of this event.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * true when the player became AFK, false when they resumed.
     */
    private final boolean afk;

    /**
     * Creates the event.
     *
     * @param playerId the player UUID
     * @param afk true when the player became AFK, false when they resumed
     * @param occurredAt the moment the event happened
     */
    public LoriTimeAfkStateChangeEvent(final UUID playerId, final boolean afk, final Instant occurredAt) {
        super(playerId, occurredAt);
        this.afk = afk;
    }

    /**
     * Gets whether the player became AFK.
     *
     * @return {@code true} if the player became AFK, {@code false} if they resumed
     */
    public boolean isAfk() {
        return afk;
    }

    /**
     * Gets the handler list.
     *
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
