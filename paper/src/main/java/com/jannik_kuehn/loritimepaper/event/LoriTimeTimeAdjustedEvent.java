package com.jannik_kuehn.loritimepaper.event;

import com.jannik_kuehn.common.api.storage.TimeScope;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * A signed time adjustment was stored for a player, for example by a command, the API or AFK time removal.
 */
@SuppressWarnings("PMD.DataClass")
public class LoriTimeTimeAdjustedEvent extends LoriTimeEvent {
    /**
     * Handler list of this event.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The signed adjustment.
     */
    private final Duration amount;

    /**
     * The adjustment scope.
     */
    private final TimeScope scope;

    /**
     * The machine-readable adjustment reason.
     */
    private final String reason;

    /**
     * The actor display name.
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
     * @param occurredAt the moment the event happened
     */
    public LoriTimeTimeAdjustedEvent(final UUID playerId, final Duration amount, final TimeScope scope, final String reason, final String actorName, final Instant occurredAt) {
        super(playerId, occurredAt);
        this.amount = amount;
        this.scope = scope;
        this.reason = reason;
        this.actorName = actorName;
    }

    /**
     * Gets the signed adjustment.
     *
     * @return the signed adjustment
     */
    public Duration getAmount() {
        return amount;
    }

    /**
     * Gets the adjustment scope.
     *
     * @return the adjustment scope
     */
    public TimeScope getScope() {
        return scope;
    }

    /**
     * Gets the machine-readable adjustment reason.
     *
     * @return the machine-readable adjustment reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets the actor display name.
     *
     * @return the actor display name
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
