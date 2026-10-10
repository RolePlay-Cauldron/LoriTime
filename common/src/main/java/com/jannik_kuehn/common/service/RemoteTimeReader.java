package com.jannik_kuehn.common.service;

import java.util.OptionalLong;
import java.util.UUID;

/**
 * Read-only time source used by runtimes without their own canonical storage, such as slave servers.
 * It only knows the global time of players that are currently online on this runtime.
 */
public interface RemoteTimeReader {

    /**
     * Reads the cached global time of a player without blocking.
     *
     * @param uniqueId player UUID
     * @return cached time in seconds, or empty when the player is not known to this runtime
     */
    OptionalLong getCachedTime(UUID uniqueId);

    /**
     * Requests an asynchronous refresh of the cached time.
     *
     * @param uniqueId player UUID
     */
    void requestRefresh(UUID uniqueId);
}
