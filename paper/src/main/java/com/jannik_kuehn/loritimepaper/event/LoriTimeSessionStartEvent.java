package com.jannik_kuehn.loritimepaper.event;

import java.time.Instant;
import java.util.UUID;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A player started being tracked by LoriTime. Server or world switches do not fire this event.
 */
public class LoriTimeSessionStartEvent extends LoriTimeEvent {
    /**
     * Handler list of this event.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * the player name.
     */
    private final String name;

    /**
     * the server name.
     */
    private final String server;

    /**
     * the world name.
     */
    private final String world;

    /**
     * Creates the event.
     *
     * @param playerId the player UUID
     * @param name the player name
     * @param server the server name
     * @param world the world name
     * @param at the moment the event happened
     */
    public LoriTimeSessionStartEvent(final UUID playerId, final String name, final String server, final String world, final Instant at) {
        super(playerId, at);
        this.name = name;
        this.server = server;
        this.world = world;
    }

    /**
     * Gets: the player name.
     *
     * @return the value
     */
    public String getName() {
        return name;
    }

    /**
     * Gets: the server name.
     *
     * @return the value
     */
    public String getServer() {
        return server;
    }

    /**
     * Gets: the world name.
     *
     * @return the value
     */
    public String getWorld() {
        return world;
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
