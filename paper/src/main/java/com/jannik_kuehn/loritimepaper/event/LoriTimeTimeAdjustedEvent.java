package com.jannik_kuehn.loritimepaper.event;

import com.jannik_kuehn.common.api.storage.TimeScope;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A signed time adjustment was stored for a player, for example by a command, the API or AFK time removal.
 */
public class LoriTimeTimeAdjustedEvent extends LoriTimeEvent {
    /**
     * Handler list of this event.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * the signed adjustment.
     */
    private final Duration amount;

    /**
     * the adjustment scope.
     */
    private final TimeScope scope;

    /**
     * the machine-readable adjustment reason.
     */
    private final String reason;

    /**
     * the actor display name.
     */
    private final String actorName;

    /**
     * Creates the event.
     *
     * @param playerId the player UUID
     * @param amount the signed adjustment
     * @param scope the adjustment scope
     * @param reason the machine-readable adjustment reason
     * @param actorName the actor display name
     * @param at the moment the event happened
     */
    public LoriTimeTimeAdjustedEvent(final UUID playerId, final Duration amount, final TimeScope scope, final String reason, final String actorName, final Instant at) {
        super(playerId, at);
        this.amount = amount;
        this.scope = scope;
        this.reason = reason;
        this.actorName = actorName;
    }

    /**
     * Gets: the signed adjustment.
     *
     * @return the value
     */
    public Duration getAmount() {
        return amount;
    }

    /**
     * Gets: the adjustment scope.
     *
     * @return the value
     */
    public TimeScope getScope() {
        return scope;
    }

    /**
     * Gets: the machine-readable adjustment reason.
     *
     * @return the value
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets: the actor display name.
     *
     * @return the value
     */
    public String getActorName() {
        return actorName;
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
