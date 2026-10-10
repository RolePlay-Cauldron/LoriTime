package com.jannik_kuehn.loritimepaper.util;

import com.jannik_kuehn.common.api.storage.TimeScope;
import com.jannik_kuehn.common.platform.PlatformEventDispatcher;
import com.jannik_kuehn.loritimepaper.event.LoriTimeAfkStateChangeEvent;
import com.jannik_kuehn.loritimepaper.event.LoriTimeEvent;
import com.jannik_kuehn.loritimepaper.event.LoriTimeSessionEndEvent;
import com.jannik_kuehn.loritimepaper.event.LoriTimeSessionStartEvent;
import com.jannik_kuehn.loritimepaper.event.LoriTimeTimeAdjustedEvent;
import com.jannik_kuehn.loritimepaper.event.LoriTimeTrackingTagChangeEvent;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Publishes LoriTime notifications as Bukkit events.
 */
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class PaperEventDispatcher implements PlatformEventDispatcher {

    @Override
    public void sessionStarted(final UUID playerId, final String name, final String server, final String world,
                               final Instant occurredAt) {
        call(new LoriTimeSessionStartEvent(playerId, name, server, world, occurredAt));
    }

    @Override
    public void sessionEnded(final UUID playerId, final Instant occurredAt) {
        call(new LoriTimeSessionEndEvent(playerId, occurredAt));
    }

    @Override
    public void afkChanged(final UUID playerId, final boolean afk, final Instant occurredAt) {
        call(new LoriTimeAfkStateChangeEvent(playerId, afk, occurredAt));
    }

    @Override
    public void timeAdjusted(final UUID playerId, final Duration amount, final TimeScope scope, final String reason,
                             final String actorName, final Instant occurredAt) {
        call(new LoriTimeTimeAdjustedEvent(playerId, amount, scope, reason, actorName, occurredAt));
    }

    @Override
    public void trackingTagsChanged(final UUID playerId, final Map<String, String> previousTags,
                                    final Map<String, String> tags, final Instant occurredAt) {
        call(new LoriTimeTrackingTagChangeEvent(playerId, previousTags, tags, occurredAt));
    }

    private void call(final LoriTimeEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }
}
