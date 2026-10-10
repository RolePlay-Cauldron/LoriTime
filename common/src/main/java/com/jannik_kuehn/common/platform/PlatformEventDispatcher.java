package com.jannik_kuehn.common.platform;

import com.jannik_kuehn.common.api.storage.TimeScope;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Bridge from platform-neutral LoriTime code to the native event system of the running platform.
 * Platform modules translate the calls into Paper or Velocity events. The default implementation ignores everything.
 */
public interface PlatformEventDispatcher {

    /**
     * Dispatcher that ignores all notifications.
     */
    PlatformEventDispatcher NOOP = new PlatformEventDispatcher() {
    };

    /**
     * A player started being tracked.
     *
     * @param playerId the player UUID
     * @param name     the player name
     * @param server   the server name
     * @param world    the world name
     * @param at       when the session started
     */
    default void sessionStarted(final UUID playerId, final String name, final String server, final String world,
                                final Instant at) {
        // ignored by default
    }

    /**
     * A player stopped being tracked.
     *
     * @param playerId the player UUID
     * @param at       when tracking stopped
     */
    default void sessionEnded(final UUID playerId, final Instant at) {
        // ignored by default
    }

    /**
     * A player entered or left the AFK state.
     *
     * @param playerId the player UUID
     * @param afk      {@code true} when the player became AFK
     * @param at       when the state changed
     */
    default void afkChanged(final UUID playerId, final boolean afk, final Instant at) {
        // ignored by default
    }

    /**
     * A signed time adjustment was stored.
     *
     * @param playerId  the adjusted player UUID
     * @param amount    the signed adjustment
     * @param scope     the adjustment scope
     * @param reason    the machine-readable reason
     * @param actorName the actor display name
     * @param at        when the adjustment was stored
     */
    default void timeAdjusted(final UUID playerId, final Duration amount, final TimeScope scope, final String reason,
                              final String actorName, final Instant at) {
        // ignored by default
    }
}
