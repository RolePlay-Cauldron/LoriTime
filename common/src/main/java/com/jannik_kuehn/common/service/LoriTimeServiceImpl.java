package com.jannik_kuehn.common.service;

import com.jannik_kuehn.common.LoriTimePlugin;
import com.jannik_kuehn.common.api.LoriTimeApiException;
import com.jannik_kuehn.common.api.LoriTimePlayer;
import com.jannik_kuehn.common.api.LoriTimeService;
import com.jannik_kuehn.common.api.storage.TimeRange;
import com.jannik_kuehn.common.api.storage.TimeScope;
import com.jannik_kuehn.common.exception.StorageException;
import com.jannik_kuehn.common.storage.model.ManualTimeAdjustment;
import com.jannik_kuehn.common.storage.contract.UnifiedStorage;
import com.jannik_kuehn.common.storage.model.TimeEntryReason;
import com.jannik_kuehn.common.utils.TimeUtil;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Default public facade implementation.
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class LoriTimeServiceImpl implements LoriTimeService {
    /**
     * Parameter name used for player identity validation.
     */
    private static final String PLAYER_PARAMETER = "player";

    /**
     * Parameter name used for UUID validation.
     */
    private static final String UNIQUE_ID_PARAMETER = "uniqueId";

    /**
     * Actor name used for API adjustments without an explicit actor.
     */
    private final LoriTimePlugin plugin;

    /**
     * Creates a new public API facade.
     *
     * @param plugin the backing LoriTime plugin.
     */
    public LoriTimeServiceImpl(final LoriTimePlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /**
     * Looks up a stored UUID by player name.
     *
     * @param playerName the player name.
     * @return future for the UUID, if LoriTime knows the player.
     */
    @Override
    public CompletableFuture<Optional<UUID>> findUuid(final String playerName) {
        Objects.requireNonNull(playerName, "playerName");
        return supplyAsync("Could not look up UUID for player name " + playerName,
                () -> requireStorage("look up UUIDs").getUuid(playerName));
    }

    /**
     * Looks up the latest stored player name for a UUID.
     *
     * @param uniqueId the player UUID.
     * @return future for the player name, if LoriTime knows the player.
     */
    @Override
    public CompletableFuture<Optional<String>> findName(final UUID uniqueId) {
        Objects.requireNonNull(uniqueId, UNIQUE_ID_PARAMETER);
        return supplyAsync("Could not look up player name for UUID " + uniqueId,
                () -> requireStorage("look up player names").getName(uniqueId));
    }

    /**
     * Returns the current total online time for a player.
     *
     * @param uniqueId the player UUID.
     * @return future for the total online time, if LoriTime has stored time for the player.
     */
    @Override
    public CompletableFuture<Optional<Duration>> getOnlineTime(final UUID uniqueId) {
        return getOnlineTime(uniqueId, TimeScope.GLOBAL);
    }

    /**
     * Returns the current scoped online time for a player.
     *
     * @param uniqueId the player UUID.
     * @param scope    the requested time scope.
     * @return future for the total online time, if LoriTime has stored time for the player in that scope.
     */
    @Override
    public CompletableFuture<Optional<Duration>> getOnlineTime(final UUID uniqueId, final TimeScope scope) {
        Objects.requireNonNull(uniqueId, UNIQUE_ID_PARAMETER);
        Objects.requireNonNull(scope, "scope");
        return supplyAsync("Could not query online time for UUID " + uniqueId, () -> {
            final UnifiedStorage storage = plugin.getStorage();
            final OptionalLong seconds = storage == null
                    ? readRemoteTime(uniqueId, scope)
                    : storage.getTime(uniqueId, scope);
            return seconds.isPresent() ? Optional.of(Duration.ofSeconds(seconds.getAsLong())) : Optional.empty();
        });
    }

    /**
     * Returns the current scoped online time for a player inside a time range.
     *
     * @param uniqueId the player UUID.
     * @param scope    the requested time scope.
     * @param range    the requested time range.
     * @return future for the ranged online time, if LoriTime has matching stored time.
     */
    @Override
    public CompletableFuture<Optional<Duration>> getOnlineTime(final UUID uniqueId,
                                                               final TimeScope scope,
                                                               final TimeRange range) {
        Objects.requireNonNull(uniqueId, UNIQUE_ID_PARAMETER);
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(range, "range");
        return supplyAsync("Could not query online time for UUID " + uniqueId, () -> {
            final OptionalLong seconds = requireStorage("query ranged online time").getTime(uniqueId, scope, range);
            return seconds.isPresent() ? Optional.of(Duration.ofSeconds(seconds.getAsLong())) : Optional.empty();
        });
    }

    /**
     * Returns the current total online time for a player.
     *
     * @param player the player identity.
     * @return future for the total online time, if LoriTime has stored time for the player.
     */
    @Override
    public CompletableFuture<Optional<Duration>> getOnlineTime(final LoriTimePlayer player) {
        return getOnlineTime(validate(player, PLAYER_PARAMETER).getUniqueId());
    }

    /**
     * Returns the current scoped online time for a player.
     *
     * @param player the player identity.
     * @param scope  the requested time scope.
     * @return future for the total online time, if LoriTime has stored time for the player in that scope.
     */
    @Override
    public CompletableFuture<Optional<Duration>> getOnlineTime(final LoriTimePlayer player, final TimeScope scope) {
        return getOnlineTime(validate(player, PLAYER_PARAMETER).getUniqueId(), scope);
    }

    /**
     * Returns the current scoped online time for a player inside a time range.
     *
     * @param player the player identity.
     * @param scope  the requested time scope.
     * @param range  the requested time range.
     * @return future for the ranged online time, if LoriTime has matching stored time.
     */
    @Override
    public CompletableFuture<Optional<Duration>> getOnlineTime(final LoriTimePlayer player,
                                                               final TimeScope scope,
                                                               final TimeRange range) {
        return getOnlineTime(validate(player, PLAYER_PARAMETER).getUniqueId(), scope, range);
    }

    /**
     * Adds a signed manual time adjustment using the stable API actor.
     *
     * @param uniqueId the player UUID.
     * @param amount   the signed amount to add or remove.
     */
    @Override
    public CompletableFuture<Void> addTime(final UUID uniqueId, final Duration amount) {
        return addTime(uniqueId, amount, null, API_ACTOR);
    }

    /**
     * Adds a signed manual time adjustment using the stable API actor for a scope.
     *
     * @param uniqueId the player UUID.
     * @param amount   the signed amount to add or remove.
     * @param scope    the adjustment scope.
     */
    @Override
    public CompletableFuture<Void> addTime(final UUID uniqueId, final Duration amount, final TimeScope scope) {
        return addTime(uniqueId, amount, null, API_ACTOR, scope);
    }

    /**
     * Adds a signed manual time adjustment using the stable API actor.
     *
     * @param player the player identity.
     * @param amount the signed amount to add or remove.
     */
    @Override
    public CompletableFuture<Void> addTime(final LoriTimePlayer player, final Duration amount) {
        return addTime(validate(player, PLAYER_PARAMETER).getUniqueId(), amount);
    }

    /**
     * Adds a signed manual time adjustment using the stable API actor for a scope.
     *
     * @param player the player identity.
     * @param amount the signed amount to add or remove.
     * @param scope  the adjustment scope.
     */
    @Override
    public CompletableFuture<Void> addTime(final LoriTimePlayer player, final Duration amount, final TimeScope scope) {
        return addTime(validate(player, PLAYER_PARAMETER).getUniqueId(), amount, scope);
    }

    /**
     * Adds a signed manual time adjustment with actor metadata.
     *
     * @param uniqueId  the player UUID.
     * @param amount    the signed amount to add or remove.
     * @param actorUuid the actor UUID, or null for system actors.
     * @param actorName the actor display name.
     */
    @Override
    public CompletableFuture<Void> addTime(final UUID uniqueId, final Duration amount, final UUID actorUuid,
                                           final String actorName) {
        return addTime(uniqueId, amount, actorUuid, actorName, TimeScope.GLOBAL);
    }

    /**
     * Adds a signed manual time adjustment with actor metadata and scope.
     *
     * @param uniqueId  the player UUID.
     * @param amount    the signed amount to add or remove.
     * @param actorUuid the actor UUID, or null for system actors.
     * @param actorName the actor display name.
     * @param scope     the adjustment scope.
     */
    @Override
    public CompletableFuture<Void> addTime(final UUID uniqueId, final Duration amount, final UUID actorUuid,
                                           final String actorName, final TimeScope scope) {
        Objects.requireNonNull(uniqueId, UNIQUE_ID_PARAMETER);
        Objects.requireNonNull(actorName, "actorName");
        Objects.requireNonNull(scope, "scope");
        if (actorName.isBlank()) {
            throw new IllegalArgumentException("actorName must not be blank");
        }
        final long seconds = seconds(amount);
        return runAsync("Could not add time adjustment for UUID " + uniqueId, () ->
                requireStorage("add time adjustments").addTime(new ManualTimeAdjustment(uniqueId, seconds,
                        TimeEntryReason.MANUAL_ADJUSTMENT, actorUuid, actorName, scope)));
    }

    /**
     * Adds a signed manual time adjustment with actor player metadata.
     *
     * @param player the target player identity.
     * @param amount the signed amount to add or remove.
     * @param actor  the actor player identity.
     */
    @Override
    public CompletableFuture<Void> addTime(final LoriTimePlayer player, final Duration amount,
                                           final LoriTimePlayer actor) {
        final LoriTimePlayer validPlayer = validate(player, PLAYER_PARAMETER);
        final LoriTimePlayer validActor = validate(actor, "actor");
        return addTime(validPlayer.getUniqueId(), amount, validActor.getUniqueId(), validActor.getName());
    }

    /**
     * Adds a signed manual time adjustment with actor player metadata and scope.
     *
     * @param player the target player identity.
     * @param amount the signed amount to add or remove.
     * @param actor  the actor player identity.
     * @param scope  the adjustment scope.
     */
    @Override
    public CompletableFuture<Void> addTime(final LoriTimePlayer player, final Duration amount,
                                           final LoriTimePlayer actor, final TimeScope scope) {
        final LoriTimePlayer validPlayer = validate(player, PLAYER_PARAMETER);
        final LoriTimePlayer validActor = validate(actor, "actor");
        return addTime(validPlayer.getUniqueId(), amount, validActor.getUniqueId(), validActor.getName(), scope);
    }

    /**
     * Formats a duration using the configured LoriTime language units.
     *
     * @param duration the duration, precise to whole seconds.
     * @return the localized, human-readable duration.
     */
    @Override
    public String formatDuration(final Duration duration) {
        return TimeUtil.formatTime(seconds(duration), plugin.getLocalization());
    }

    /**
     * Tells whether canonical storage is available on this runtime.
     *
     * @return {@code true} if all API calls are supported.
     */
    @Override
    public boolean isFullAccess() {
        return plugin.getStorage() != null;
    }

    private UnifiedStorage requireStorage(final String operation) {
        final UnifiedStorage storage = plugin.getStorage();
        if (storage == null) {
            throw new LoriTimeApiException("Cannot " + operation
                    + ": this server has no canonical LoriTime storage (slave mode)");
        }
        return storage;
    }

    private OptionalLong readRemoteTime(final UUID uniqueId, final TimeScope scope) {
        if (!TimeScope.GLOBAL.equals(scope)) {
            throw new LoriTimeApiException("Cannot query scoped online time: this server has no canonical "
                    + "LoriTime storage (slave mode), only global time is available");
        }
        final RemoteTimeReader reader = plugin.getRemoteTimeReader().orElseThrow(() -> new LoriTimeApiException(
                "LoriTime storage is not available on this server (slave mode, no time reader registered)"));
        final OptionalLong cached = reader.getCachedTime(uniqueId);
        if (cached.isEmpty()) {
            reader.requestRefresh(uniqueId);
            throw new LoriTimeApiException("Online time of " + uniqueId
                    + " is not available on this slave server (only online players are supported)");
        }
        return cached;
    }

    private CompletableFuture<Void> runAsync(final String failureMessage, final StorageRunnable action) {
        return supplyAsync(failureMessage, () -> {
            action.run();
            return null;
        });
    }

    private <T> CompletableFuture<T> supplyAsync(final String failureMessage, final StorageSupplier<T> supplier) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        plugin.getScheduler().runAsyncOnce(() -> {
            try {
                future.complete(supplier.get());
            } catch (final LoriTimeApiException ex) {
                future.completeExceptionally(ex);
            } catch (final StorageException | RuntimeException ex) {
                future.completeExceptionally(new LoriTimeApiException(failureMessage, ex));
            }
        });
        return future;
    }

    private LoriTimePlayer validate(final LoriTimePlayer player, final String parameterName) {
        Objects.requireNonNull(player, parameterName);
        Objects.requireNonNull(player.getUniqueId(), parameterName + ".uniqueId");
        final String name = Objects.requireNonNull(player.getName(), parameterName + ".name");
        if (name.isBlank()) {
            throw new IllegalArgumentException(parameterName + ".name must not be blank");
        }
        return player;
    }

    private long seconds(final Duration amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.getNano() != 0) {
            throw new IllegalArgumentException("amount must be precise to whole seconds");
        }
        return amount.getSeconds();
    }

    /**
     * Storage value supplier that may fail.
     *
     * @param <T> result type
     */
    @FunctionalInterface
    private interface StorageSupplier<T> {
        /**
         * Gets the value from storage.
         *
         * @return the storage result
         * @throws StorageException if storage access fails
         */
        T get() throws StorageException;
    }

    /**
     * Storage action that may fail.
     */
    @FunctionalInterface
    private interface StorageRunnable {
        /**
         * Runs the storage action.
         *
         * @throws StorageException if storage access fails
         */
        void run() throws StorageException;
    }
}
