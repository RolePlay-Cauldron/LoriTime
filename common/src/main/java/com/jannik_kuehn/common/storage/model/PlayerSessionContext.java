package com.jannik_kuehn.common.storage.model;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Context for an active player session.
 *
 * @param uuid        player UUID
 * @param name        latest known player name
 * @param server      server context
 * @param world       world context
 * @param startedAtMs session start timestamp in milliseconds
 * @param tags        namespaced tracking tags active for this session segment
 */
public record PlayerSessionContext(UUID uuid, Optional<String> name, String server, String world, long startedAtMs,
                                   Map<String, String> tags) {

    /**
     * Normalizes the tags to an immutable map.
     *
     * @param uuid        player UUID
     * @param name        latest known player name
     * @param server      server context
     * @param world       world context
     * @param startedAtMs session start timestamp in milliseconds
     * @param tags        tracking tags, null is treated as none
     */
    public PlayerSessionContext {
        tags = tags == null ? Map.of() : Map.copyOf(tags);
    }

    /**
     * Creates an untagged session context.
     *
     * @param uuid        player UUID
     * @param name        latest known player name
     * @param server      server context
     * @param world       world context
     * @param startedAtMs session start timestamp in milliseconds
     */
    public PlayerSessionContext(final UUID uuid, final Optional<String> name, final String server, final String world,
                                final long startedAtMs) {
        this(uuid, name, server, world, startedAtMs, Map.of());
    }

    /**
     * Creates an untagged session context.
     *
     * @param uuid        player UUID
     * @param name        latest known player name
     * @param server      server context
     * @param world       world context
     * @param startedAtMs session start timestamp in milliseconds
     */
    public PlayerSessionContext(final UUID uuid, final String name, final String server, final String world, final long startedAtMs) {
        this(uuid, Optional.ofNullable(name), server, world, startedAtMs, Map.of());
    }

    /**
     * Creates a tagged session context from nullable player name input.
     *
     * @param uuid        player UUID
     * @param name        latest known player name
     * @param server      server context
     * @param world       world context
     * @param startedAtMs session start timestamp in milliseconds
     * @param tags        tracking tags
     */
    public PlayerSessionContext(final UUID uuid, final String name, final String server, final String world,
                                final long startedAtMs, final Map<String, String> tags) {
        this(uuid, Optional.ofNullable(name), server, world, startedAtMs, tags);
    }
}
