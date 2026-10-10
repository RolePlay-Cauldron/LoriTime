package com.jannik_kuehn.loritimevelocity.util;

import com.jannik_kuehn.common.api.storage.TimeScope;
import com.jannik_kuehn.common.platform.PlatformEventDispatcher;
import com.jannik_kuehn.loritimevelocity.event.LoriTimeAfkStateChangeEvent;
import com.jannik_kuehn.loritimevelocity.event.LoriTimeSessionEndEvent;
import com.jannik_kuehn.loritimevelocity.event.LoriTimeSessionStartEvent;
import com.jannik_kuehn.loritimevelocity.event.LoriTimeTimeAdjustedEvent;
import com.jannik_kuehn.loritimevelocity.event.LoriTimeTrackingTagChangeEvent;
import com.velocitypowered.api.event.EventManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Publishes LoriTime notifications through the Velocity event manager.
 */
public class VelocityEventDispatcher implements PlatformEventDispatcher {
    /**
     * The Velocity event manager.
     */
    private final EventManager eventManager;

    /**
     * Creates the dispatcher.
     *
     * @param eventManager the Velocity event manager
     */
    public VelocityEventDispatcher(final EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void sessionStarted(final UUID playerId, final String name, final String server, final String world,
                               final Instant occurredAt) {
        eventManager.fireAndForget(new LoriTimeSessionStartEvent(playerId, name, server, world, occurredAt));
    }

    @Override
    public void sessionEnded(final UUID playerId, final Instant occurredAt) {
        eventManager.fireAndForget(new LoriTimeSessionEndEvent(playerId, occurredAt));
    }

    @Override
    public void afkChanged(final UUID playerId, final boolean afk, final Instant occurredAt) {
        eventManager.fireAndForget(new LoriTimeAfkStateChangeEvent(playerId, afk, occurredAt));
    }

    @Override
    public void timeAdjusted(final UUID playerId, final Duration amount, final TimeScope scope, final String reason,
                             final String actorName, final Instant occurredAt) {
        eventManager.fireAndForget(new LoriTimeTimeAdjustedEvent(playerId, amount, scope, reason, actorName, occurredAt));
    }

    @Override
    public void trackingTagsChanged(final UUID playerId, final Map<String, String> previousTags,
                                    final Map<String, String> tags, final Instant occurredAt) {
        eventManager.fireAndForget(new LoriTimeTrackingTagChangeEvent(playerId, previousTags, tags, occurredAt));
    }
}
