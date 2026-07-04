package com.jannik_kuehn.common.storage.model;

import com.jannik_kuehn.common.api.storage.TimeRange;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Request to transfer one player's scoped storage data.
 *
 * @param operation      transfer operation
 * @param mapping        source-to-target mapping
 * @param playerUuid     selected player UUID
 * @param playerName     selected player name, when known
 * @param timeRange      optional row-selection time range
 * @param timeRangeInput original time-range command input, when supplied
 */
public record PlayerStorageTransferRequest(StorageMaintenanceOperation operation,
                                           StorageTransferMapping mapping,
                                           UUID playerUuid,
                                           Optional<String> playerName,
                                           Optional<TimeRange> timeRange,
                                           Optional<String> timeRangeInput) {

    public PlayerStorageTransferRequest {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(mapping, "mapping");
        Objects.requireNonNull(playerUuid, "playerUuid");
        playerName = playerName == null ? Optional.empty() : playerName.filter(value -> !value.isBlank());
        timeRange = timeRange == null ? Optional.empty() : timeRange;
        timeRangeInput = timeRangeInput == null ? Optional.empty() : timeRangeInput.filter(value -> !value.isBlank());
        if (operation != StorageMaintenanceOperation.SERVER_TRANSFER
                && operation != StorageMaintenanceOperation.WORLD_TRANSFER) {
            throw new IllegalArgumentException("operation must be a scoped transfer operation");
        }
        if (operation == StorageMaintenanceOperation.SERVER_TRANSFER
                && mapping.source().type() != StorageMaintenanceScope.Type.SERVER) {
            throw new IllegalArgumentException("server transfer requires server scopes");
        }
        if (operation == StorageMaintenanceOperation.WORLD_TRANSFER
                && mapping.source().type() != StorageMaintenanceScope.Type.WORLD) {
            throw new IllegalArgumentException("world transfer requires world scopes");
        }
    }

    /**
     * Creates a player-scoped server transfer request.
     *
     * @param playerUuid selected player UUID
     * @param playerName selected player name
     * @param source source server
     * @param target target server
     * @param timeRange optional time range
     * @param timeRangeInput optional raw time range input
     * @return transfer request
     */
    public static PlayerStorageTransferRequest serverTransfer(final UUID playerUuid,
                                                              final Optional<String> playerName,
                                                              final StorageMaintenanceScope source,
                                                              final StorageMaintenanceScope target,
                                                              final Optional<TimeRange> timeRange,
                                                              final Optional<String> timeRangeInput) {
        return new PlayerStorageTransferRequest(StorageMaintenanceOperation.SERVER_TRANSFER,
                new StorageTransferMapping(source, target), playerUuid, playerName, timeRange, timeRangeInput);
    }

    /**
     * Creates a player-scoped world transfer request.
     *
     * @param playerUuid selected player UUID
     * @param playerName selected player name
     * @param source source world
     * @param target target world
     * @param timeRange optional time range
     * @param timeRangeInput optional raw time range input
     * @return transfer request
     */
    public static PlayerStorageTransferRequest worldTransfer(final UUID playerUuid,
                                                             final Optional<String> playerName,
                                                             final StorageMaintenanceScope source,
                                                             final StorageMaintenanceScope target,
                                                             final Optional<TimeRange> timeRange,
                                                             final Optional<String> timeRangeInput) {
        return new PlayerStorageTransferRequest(StorageMaintenanceOperation.WORLD_TRANSFER,
                new StorageTransferMapping(source, target), playerUuid, playerName, timeRange, timeRangeInput);
    }
}
