package com.jannik_kuehn.common.storage.model;

import com.jannik_kuehn.common.api.storage.TimeRange;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Request to delete one server or world dataset.
 *
 * @param scope          scope to delete
 * @param playerUuid     selected player UUID, when player-scoped
 * @param playerName     selected player name, when known
 * @param timeRange      optional row-selection time range
 * @param timeRangeInput original time-range command input, when supplied
 */
public record StorageDeleteRequest(StorageMaintenanceScope scope,
                                   Optional<UUID> playerUuid,
                                   Optional<String> playerName,
                                   Optional<TimeRange> timeRange,
                                   Optional<String> timeRangeInput) {

    public StorageDeleteRequest {
        Objects.requireNonNull(scope, "scope");
        playerUuid = playerUuid == null ? Optional.empty() : playerUuid;
        playerName = playerName == null ? Optional.empty() : playerName.filter(value -> !value.isBlank());
        timeRange = timeRange == null ? Optional.empty() : timeRange;
        timeRangeInput = timeRangeInput == null ? Optional.empty() : timeRangeInput.filter(value -> !value.isBlank());
        if (scope.type() == StorageMaintenanceScope.Type.STORAGE) {
            throw new IllegalArgumentException("delete scope must be server or world");
        }
    }

    /**
     * Creates an all-player delete request without a time range.
     *
     * @param scope target server or world scope
     */
    public StorageDeleteRequest(final StorageMaintenanceScope scope) {
        this(scope, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Creates an all-player scoped delete request.
     *
     * @param scope target server or world scope
     * @param timeRange optional time range
     * @param timeRangeInput optional raw time range input
     * @return delete request
     */
    public static StorageDeleteRequest allPlayers(final StorageMaintenanceScope scope,
                                                  final Optional<TimeRange> timeRange,
                                                  final Optional<String> timeRangeInput) {
        return new StorageDeleteRequest(scope, Optional.empty(), Optional.empty(), timeRange, timeRangeInput);
    }

    /**
     * Creates a player-scoped delete request.
     *
     * @param scope target server or world scope
     * @param playerUuid selected player UUID
     * @param playerName selected player name
     * @param timeRange optional time range
     * @param timeRangeInput optional raw time range input
     * @return delete request
     */
    public static StorageDeleteRequest player(final StorageMaintenanceScope scope,
                                              final UUID playerUuid,
                                              final Optional<String> playerName,
                                              final Optional<TimeRange> timeRange,
                                              final Optional<String> timeRangeInput) {
        return new StorageDeleteRequest(scope, Optional.of(playerUuid), playerName, timeRange, timeRangeInput);
    }
}
