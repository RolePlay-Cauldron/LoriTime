package com.jannik_kuehn.common.service;

import java.util.UUID;

/**
 * Forwards tracking tag changes to the master from runtimes without their own canonical storage.
 */
public interface RemoteTagWriter {

    /**
     * Sets or clears a tracking tag of a player that is online on this runtime.
     *
     * @param uniqueId player UUID
     * @param key      namespaced tag key
     * @param value    tag value, or {@code null} to clear the tag
     */
    void changeTag(UUID uniqueId, String key, String value);
}
