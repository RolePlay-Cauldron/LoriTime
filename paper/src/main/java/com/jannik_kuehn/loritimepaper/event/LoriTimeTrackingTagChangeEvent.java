package com.jannik_kuehn.loritimepaper.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * The tracking tags of a player changed.
 */
public class LoriTimeTrackingTagChangeEvent extends LoriTimeEvent {
    /**
     * Handler list of this event.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The tags before the change.
     */
    private final Map<String, String> previousTags;

    /**
     * The tags after the change.
     */
    private final Map<String, String> tags;

    /**
     * Creates the event.
     *
     * @param playerId     the player UUID
     * @param previousTags the tags before the change
     * @param tags         the tags after the change
     * @param occurredAt           the moment the event happened
     */
    public LoriTimeTrackingTagChangeEvent(final UUID playerId, final Map<String, String> previousTags,
                                          final Map<String, String> tags, final Instant occurredAt) {
        super(playerId, occurredAt);
        this.previousTags = Map.copyOf(previousTags);
        this.tags = Map.copyOf(tags);
    }

    /**
     * Gets the tags before the change.
     *
     * @return the immutable previous tags
     */
    public Map<String, String> getPreviousTags() {
        return previousTags;
    }

    /**
     * Gets the tags after the change.
     *
     * @return the immutable current tags
     */
    public Map<String, String> getTags() {
        return tags;
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
