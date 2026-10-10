package com.jannik_kuehn.common.api;

import com.jannik_kuehn.common.api.storage.TimeRange;
import com.jannik_kuehn.common.api.storage.TimeScope;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Stable public facade for third-party LoriTime integrations.
 */
public interface LoriTimeService {
    /**
     * Actor name used for API adjustments without an explicit actor.
     */
    String API_ACTOR = "API";

    /**
     * Looks up the stored UUID for a player name.
     *
     * @param playerName player name to resolve
     * @return future containing the UUID when LoriTime knows the player
     */
    CompletableFuture<Optional<UUID>> findUuid(String playerName);

    /**
     * Looks up the latest stored player name for a UUID.
     *
     * @param uniqueId player UUID to resolve
     * @return future containing the latest name when LoriTime knows the player
     */
    CompletableFuture<Optional<String>> findName(UUID uniqueId);

    /**
     * Reads a player's global online time.
     *
     * @param uniqueId player UUID
     * @return future containing the online time when LoriTime has stored data
     */
    CompletableFuture<Optional<Duration>> getOnlineTime(UUID uniqueId);

    /**
     * Reads a player's online time inside a scope.
     *
     * @param uniqueId player UUID
     * @param scope time scope to query
     * @return future containing the online time when LoriTime has stored data
     */
    CompletableFuture<Optional<Duration>> getOnlineTime(UUID uniqueId, TimeScope scope);

    /**
     * Reads a player's online time inside a scope and time range.
     *
     * @param uniqueId player UUID
     * @param scope time scope to query
     * @param range time range to query
     * @return future containing the online time when LoriTime has stored data
     */
    CompletableFuture<Optional<Duration>> getOnlineTime(UUID uniqueId, TimeScope scope, TimeRange range);

    /**
     * Reads a player's global online time.
     *
     * @param player player identity
     * @return future containing the online time when LoriTime has stored data
     */
    CompletableFuture<Optional<Duration>> getOnlineTime(LoriTimePlayer player);

    /**
     * Reads a player's online time inside a scope.
     *
     * @param player player identity
     * @param scope time scope to query
     * @return future containing the online time when LoriTime has stored data
     */
    CompletableFuture<Optional<Duration>> getOnlineTime(LoriTimePlayer player, TimeScope scope);

    /**
     * Reads a player's online time inside a scope and time range.
     *
     * @param player player identity
     * @param scope time scope to query
     * @param range time range to query
     * @return future containing the online time when LoriTime has stored data
     */
    CompletableFuture<Optional<Duration>> getOnlineTime(LoriTimePlayer player, TimeScope scope, TimeRange range);

    /**
     * Adds a signed global time adjustment using the default API actor.
     *
     * @param uniqueId player UUID
     * @param amount signed duration to add or remove
     * @return future completed when the adjustment has been stored
     */
    CompletableFuture<Void> addTime(UUID uniqueId, Duration amount);

    /**
     * Adds a signed scoped time adjustment using the default API actor.
     *
     * @param uniqueId player UUID
     * @param amount signed duration to add or remove
     * @param scope adjustment scope
     * @return future completed when the adjustment has been stored
     */
    CompletableFuture<Void> addTime(UUID uniqueId, Duration amount, TimeScope scope);

    /**
     * Adds a signed global time adjustment using the default API actor.
     *
     * @param player player identity
     * @param amount signed duration to add or remove
     * @return future completed when the adjustment has been stored
     */
    CompletableFuture<Void> addTime(LoriTimePlayer player, Duration amount);

    /**
     * Adds a signed scoped time adjustment using the default API actor.
     *
     * @param player player identity
     * @param amount signed duration to add or remove
     * @param scope adjustment scope
     * @return future completed when the adjustment has been stored
     */
    CompletableFuture<Void> addTime(LoriTimePlayer player, Duration amount, TimeScope scope);

    /**
     * Adds a signed global time adjustment with explicit actor metadata.
     *
     * @param uniqueId player UUID
     * @param amount signed duration to add or remove
     * @param actorUuid actor UUID, or null for a system actor
     * @param actorName actor display name
     * @return future completed when the adjustment has been stored
     */
    CompletableFuture<Void> addTime(UUID uniqueId, Duration amount, UUID actorUuid, String actorName);

    /**
     * Adds a signed scoped time adjustment with explicit actor metadata.
     *
     * @param uniqueId player UUID
     * @param amount signed duration to add or remove
     * @param actorUuid actor UUID, or null for a system actor
     * @param actorName actor display name
     * @param scope adjustment scope
     * @return future completed when the adjustment has been stored
     */
    CompletableFuture<Void> addTime(UUID uniqueId, Duration amount, UUID actorUuid, String actorName, TimeScope scope);

    /**
     * Adds a signed global time adjustment with player actor metadata.
     *
     * @param player target player identity
     * @param amount signed duration to add or remove
     * @param actor actor player identity
     * @return future completed when the adjustment has been stored
     */
    CompletableFuture<Void> addTime(LoriTimePlayer player, Duration amount, LoriTimePlayer actor);

    /**
     * Adds a signed scoped time adjustment with player actor metadata.
     *
     * @param player target player identity
     * @param amount signed duration to add or remove
     * @param actor actor player identity
     * @param scope adjustment scope
     * @return future completed when the adjustment has been stored
     */
    CompletableFuture<Void> addTime(LoriTimePlayer player, Duration amount, LoriTimePlayer actor, TimeScope scope);

    /**
     * Tells whether this runtime owns canonical storage and therefore supports every API call.
     * Slave servers only support {@link #getOnlineTime(UUID)} for players currently online on them;
     * all other calls complete exceptionally with a {@link LoriTimeApiException}.
     *
     * @return {@code true} if all API calls are supported on this runtime
     * @since 2.3.0
     */
    default boolean isFullAccess() {
        return true;
    }

    /**
     * Formats a duration the same way LoriTime does in its own messages and placeholders,
     * using the configured language units. Non-positive durations produce an empty string.
     *
     * @param duration duration to format, precise to whole seconds
     * @return the localized, human-readable duration
     * @throws UnsupportedOperationException if the implementation does not support formatting
     * @since 2.3.0
     */
    default String formatDuration(final Duration duration) {
        throw new UnsupportedOperationException("formatDuration is not supported by this LoriTimeService");
    }
}
