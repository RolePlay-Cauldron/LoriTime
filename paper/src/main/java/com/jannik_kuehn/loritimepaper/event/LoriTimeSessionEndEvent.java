package com.jannik_kuehn.loritimepaper.event;

import java.time.Instant;
import java.util.UUID;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * LoriTime stopped tracking a player, because they left or counting stopped, for example while AFK.
 */
public class LoriTimeSessionEndEvent extends LoriTimeEvent {
    /**
     * Handler list of this event.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates the event.
     *
     * @param playerId the player UUID
     * @param at the moment the event happened
     */
    public LoriTimeSessionEndEvent(final UUID playerId, final Instant at) {
        super(playerId, at);
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
