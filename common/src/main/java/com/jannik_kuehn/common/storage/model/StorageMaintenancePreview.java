package com.jannik_kuehn.common.storage.model;

import com.jannik_kuehn.common.api.storage.TimeRange;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Preview of a maintenance operation.
 *
 * @param operation            operation kind
 * @param mappings             transfer mappings, if any
 * @param deleteScope          delete scope, if any
 * @param affectedSessions     affected session rows
 * @param affectedAdjustments  affected adjustment rows
 * @param affectedPlayers      affected player count
 * @param targetDataExists     true when target storage/scope already contains data
 * @param targetCollisions     target collision labels
 * @param confirmationRequired true when execution requires explicit confirmation
 * @param playerUuid           selected player UUID for player-scoped operations
 * @param playerName           selected player name for player-scoped operations
 * @param timeRange            optional row-selection time range
 * @param timeRangeInput       original time range input
 * @param fingerprint          operation fingerprint for confirmation
 */
public record StorageMaintenancePreview(StorageMaintenanceOperation operation,
                                        List<StorageTransferMapping> mappings,
                                        StorageMaintenanceScope deleteScope,
                                        long affectedSessions,
                                        long affectedAdjustments,
                                        long affectedPlayers,
                                        boolean targetDataExists,
                                        List<String> targetCollisions,
                                        boolean confirmationRequired,
                                        Optional<UUID> playerUuid,
                                        Optional<String> playerName,
                                        Optional<TimeRange> timeRange,
                                        Optional<String> timeRangeInput,
                                        String fingerprint) {

    public StorageMaintenancePreview {
        Objects.requireNonNull(operation, "operation");
        mappings = mappings == null ? List.of() : List.copyOf(mappings);
        targetCollisions = targetCollisions == null ? List.of() : List.copyOf(targetCollisions);
        playerUuid = playerUuid == null ? Optional.empty() : playerUuid;
        playerName = playerName == null ? Optional.empty() : playerName.filter(value -> !value.isBlank());
        timeRange = timeRange == null ? Optional.empty() : timeRange;
        timeRangeInput = timeRangeInput == null ? Optional.empty() : timeRangeInput.filter(value -> !value.isBlank());
        Objects.requireNonNull(fingerprint, "fingerprint");
    }

    /**
     * Creates a non-player maintenance preview.
     *
     * @param operation            operation kind
     * @param mappings             transfer mappings
     * @param deleteScope          delete scope
     * @param affectedSessions     affected session rows
     * @param affectedAdjustments  affected adjustment rows
     * @param affectedPlayers      affected player count
     * @param targetDataExists     whether target data exists
     * @param targetCollisions     target collision labels
     * @param confirmationRequired whether confirmation is required
     * @param fingerprint          preview fingerprint
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public StorageMaintenancePreview(final StorageMaintenanceOperation operation,
                                     final List<StorageTransferMapping> mappings,
                                     final StorageMaintenanceScope deleteScope,
                                     final long affectedSessions,
                                     final long affectedAdjustments,
                                     final long affectedPlayers,
                                     final boolean targetDataExists,
                                     final List<String> targetCollisions,
                                     final boolean confirmationRequired,
                                     final String fingerprint) {
        this(operation, mappings, deleteScope, affectedSessions, affectedAdjustments, affectedPlayers,
                targetDataExists, targetCollisions, confirmationRequired, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), fingerprint);
    }

    /**
     * Returns a confirmation for this preview.
     *
     * @return confirmation token
     */
    public StorageMaintenanceConfirmation confirmation() {
        return new StorageMaintenanceConfirmation(fingerprint);
    }
}
