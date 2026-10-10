package com.jannik_kuehn.loritimepaper.util;

import com.jannik_kuehn.common.api.storage.TimeScope;
import com.jannik_kuehn.common.platform.PlatformEventDispatcher;
import com.jannik_kuehn.loritimepaper.event.LoriTimeAfkStateChangeEvent;
import com.jannik_kuehn.loritimepaper.event.LoriTimeEvent;
import com.jannik_kuehn.loritimepaper.event.LoriTimeSessionEndEvent;
import com.jannik_kuehn.loritimepaper.event.LoriTimeSessionStartEvent;
import com.jannik_kuehn.loritimepaper.event.LoriTimeTimeAdjustedEvent;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Publishes LoriTime notifications as Bukkit events.
 */
public class PaperEventDispatcher implements PlatformEventDispatcher {

    @Override
    public void sessionStarted(final UUID playerId, final String name, final String server, final String world,
                               final Instant at) {
        call(new LoriTimeSessionStartEvent(playerId, name, server, world, at));
    }

    @Override
    public void sessionEnded(final UUID playerId, final Instant at) {
        call(new LoriTimeSessionEndEvent(playerId, at));
    }

    @Override
    public void afkChanged(final UUID playerId, final boolean afk, final Instant at) {
        call(new LoriTimeAfkStateChangeEvent(playerId, afk, at));
    }

    @Override
    public void timeAdjusted(final UUID playerId, final Duration amount, final TimeScope scope, final String reason,
                             final String actorName, final Instant at) {
        call(new LoriTimeTimeAdjustedEvent(playerId, amount, scope, reason, actorName, at));
    }

    private void call(final LoriTimeEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }
}
