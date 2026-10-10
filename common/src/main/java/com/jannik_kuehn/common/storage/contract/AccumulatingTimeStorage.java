package com.jannik_kuehn.common.storage.contract;

import com.github.roleplaycauldron.spellbook.core.logger.WrappedLogger;
import com.jannik_kuehn.common.api.storage.TimeRange;
import com.jannik_kuehn.common.api.storage.TimeScope;
import com.jannik_kuehn.common.exception.StorageException;
import com.jannik_kuehn.common.platform.PlatformEventDispatcher;
import com.jannik_kuehn.common.storage.model.AfkPeriod;
import com.jannik_kuehn.common.storage.model.AfkPeriodEndReason;
import com.jannik_kuehn.common.storage.model.ManualTimeAdjustment;
import com.jannik_kuehn.common.storage.model.PersistedPlayerSession;
import com.jannik_kuehn.common.storage.model.PlayerSessionChunk;
import com.jannik_kuehn.common.storage.model.PlayerSessionContext;
import com.jannik_kuehn.common.storage.model.RecentPlayerIdentity;
import com.jannik_kuehn.common.storage.model.StatisticsRequest;
import com.jannik_kuehn.common.storage.model.StatisticsSnapshot;
import com.jannik_kuehn.common.storage.model.TimeEntryReason;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Unified storage decorator that keeps active sessions in memory while persisting session rows.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.CouplingBetweenObjects"})
public class AccumulatingTimeStorage implements UnifiedStorage, TimeAccumulator, StatisticsStorage {
    /**
     * Number of bounded per-player lock stripes.
     */
    private static final int SESSION_LOCK_STRIPES = 64;

    /**
     * Logger for accumulator operations.
     */
    private final WrappedLogger log;

    /**
     * Backing storage that owns persistence.
     */
    private final UnifiedStorage storage;

    /**
     * Supplies the platform event dispatcher.
     */
    private final Supplier<PlatformEventDispatcher> eventDispatcher;

    /**
     * Active persisted sessions keyed by player UUID.
     */
    private final ConcurrentMap<UUID, PersistedPlayerSession> onlineSessions = new ConcurrentHashMap<>();

    /**
     * Tracking tags of online players, kept across AFK pauses of a session.
     */
    private final ConcurrentMap<UUID, Map<String, String>> playerTags = new ConcurrentHashMap<>();

    /**
     * Bounded locks that serialize persistence for a single player.
     */
    private final ReentrantLock[] sessionLocks = createSessionLocks();

    /**
     * Creates a new accumulating storage wrapper.
     *
     * @param log         the logger.
     * @param timeStorage the backing storage.
     */
    public AccumulatingTimeStorage(final WrappedLogger log, final UnifiedStorage timeStorage) {
        this(log, timeStorage, () -> PlatformEventDispatcher.NOOP);
    }

    /**
     * Creates an accumulating storage that reports session and adjustment changes to the platform.
     *
     * @param log             the logger.
     * @param timeStorage     the backing storage.
     * @param eventDispatcher supplier of the current platform event dispatcher.
     */
    public AccumulatingTimeStorage(final WrappedLogger log, final UnifiedStorage timeStorage,
                                   final Supplier<PlatformEventDispatcher> eventDispatcher) {
        this.log = log;
        this.storage = Objects.requireNonNull(timeStorage);
        this.eventDispatcher = Objects.requireNonNull(eventDispatcher);
    }

    @Override
    public Optional<UUID> getUuid(final String playerName) throws StorageException {
        return storage.getUuid(playerName);
    }

    @Override
    public Optional<String> getName(final UUID uniqueId) throws StorageException {
        return storage.getName(uniqueId);
    }

    @Override
    public void setPlayerName(final UUID uniqueId, final String name) throws StorageException {
        storage.setPlayerName(uniqueId, name);
    }

    @Override
    public void setPlayerNames(final Map<UUID, String> entries) throws StorageException {
        storage.setPlayerNames(entries);
    }

    @Override
    public Set<String> getNameEntries() throws StorageException {
        return storage.getNameEntries();
    }

    @Override
    public List<RecentPlayerIdentity> getRecentPlayerIdentities(final long recentDays) throws StorageException {
        return storage.getRecentPlayerIdentities(recentDays);
    }

    @Override
    public Set<String> getKnownServerNames() throws StorageException {
        return storage.getKnownServerNames();
    }

    @Override
    public Set<String> getKnownWorldNames() throws StorageException {
        return storage.getKnownWorldNames();
    }

    @Override
    public Map<String, Set<String>> getKnownWorldNamesByServer() throws StorageException {
        return storage.getKnownWorldNamesByServer();
    }

    @Override
    public OptionalLong getTime(final UUID uniqueId) throws StorageException {
        return getTime(uniqueId, TimeScope.GLOBAL);
    }

    @Override
    public OptionalLong getTime(final UUID uniqueId, final TimeScope scope) throws StorageException {
        Objects.requireNonNull(scope, "scope");
        final PersistedPlayerSession activeSession = onlineSessions.get(uniqueId);
        final PlayerSessionContext context = activeSession == null ? null : activeSession.context();
        if (context != null && scope.matches(context)) {
            final long accumulatedTime = (System.currentTimeMillis() - activeSession.lastPersistedAtMs()) / 1000L;
            final long storedTime = storage.getTime(uniqueId, scope).orElse(0);
            return OptionalLong.of(accumulatedTime + storedTime);
        } else {
            return storage.getTime(uniqueId, scope);
        }
    }

    @Override
    public OptionalLong getTime(final UUID uniqueId, final TimeScope scope, final TimeRange range) throws StorageException {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(range, "range");
        final OptionalLong storedTime = storage.getTime(uniqueId, scope, range);
        final PersistedPlayerSession activeSession = onlineSessions.get(uniqueId);
        final PlayerSessionContext context = activeSession == null ? null : activeSession.context();
        if (context != null && scope.matches(context)) {
            final long accumulatedTime = range.overlapSeconds(activeSession.lastPersistedAtMs(), System.currentTimeMillis());
            if (accumulatedTime > 0L || storedTime.isPresent()) {
                return OptionalLong.of(accumulatedTime + storedTime.orElse(0L));
            }
        }
        return storedTime;
    }

    @Override
    public OptionalLong getTaggedTime(final UUID uniqueId, final TimeScope scope, final String tagKey,
                                      final String tagValue) throws StorageException {
        Objects.requireNonNull(scope, "scope");
        final OptionalLong storedTime = storage.getTaggedTime(uniqueId, scope, tagKey, tagValue);
        final PersistedPlayerSession activeSession = onlineSessions.get(uniqueId);
        final PlayerSessionContext context = activeSession == null ? null : activeSession.context();
        if (context != null && scope.matches(context) && tagValue.equals(context.tags().get(tagKey))) {
            final long accumulatedTime = Math.max(0L,
                    (System.currentTimeMillis() - activeSession.lastPersistedAtMs()) / 1000L);
            return OptionalLong.of(accumulatedTime + storedTime.orElse(0L));
        }
        return storedTime;
    }

    @Override
    public OptionalLong getTaggedTime(final UUID uniqueId, final TimeScope scope, final TimeRange range,
                                      final String tagKey, final String tagValue) throws StorageException {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(range, "range");
        final OptionalLong storedTime = storage.getTaggedTime(uniqueId, scope, range, tagKey, tagValue);
        final PersistedPlayerSession activeSession = onlineSessions.get(uniqueId);
        final PlayerSessionContext context = activeSession == null ? null : activeSession.context();
        if (context != null && scope.matches(context) && tagValue.equals(context.tags().get(tagKey))) {
            final long accumulatedTime = range.overlapSeconds(activeSession.lastPersistedAtMs(), System.currentTimeMillis());
            if (accumulatedTime > 0L || storedTime.isPresent()) {
                return OptionalLong.of(accumulatedTime + storedTime.orElse(0L));
            }
        }
        return storedTime;
    }

    @Override
    public void addTime(final UUID uuid, final long additionalTime, final TimeEntryReason reason) throws StorageException {
        storage.addTime(uuid, additionalTime, reason);
    }

    @Override
    public void addTime(final ManualTimeAdjustment adjustment) throws StorageException {
        storage.addTime(adjustment);
        eventDispatcher.get().timeAdjusted(adjustment.playerUuid(), Duration.ofSeconds(adjustment.amountSeconds()),
                adjustment.scope(), adjustment.reason().name(), adjustment.actorName(), Instant.now());
    }

    @Override
    public void addTimes(final Map<UUID, Long> additionalTimes, final TimeEntryReason reason) throws StorageException {
        storage.addTimes(additionalTimes, reason);
    }

    @Override
    public void addAdjustments(final List<ManualTimeAdjustment> adjustments) throws StorageException {
        storage.addAdjustments(adjustments);
    }

    @Override
    public void openAfkPeriod(final UUID playerId, final String playerName, final String server, final String world,
                              final Instant startedAt) throws StorageException {
        statisticsStorage().openAfkPeriod(playerId, playerName, server, world, startedAt);
    }

    @Override
    public void closeAfkPeriod(final UUID playerId, final Instant endedAt, final AfkPeriodEndReason reason)
            throws StorageException {
        statisticsStorage().closeAfkPeriod(playerId, endedAt, reason);
    }

    @Override
    public int recoverOpenAfkPeriods(final Instant endedAt) throws StorageException {
        return statisticsStorage().recoverOpenAfkPeriods(endedAt);
    }

    @Override
    public List<AfkPeriod> getAfkPeriods(final TimeRange range, final TimeScope scope) throws StorageException {
        return statisticsStorage().getAfkPeriods(range, scope);
    }

    @Override
    public StatisticsSnapshot getStatistics(final StatisticsRequest request) throws StorageException {
        checkpointActiveSessions(request.observedAt().toEpochMilli());
        return statisticsStorage().getStatistics(request);
    }

    private StatisticsStorage statisticsStorage() throws StorageException {
        if (storage instanceof final StatisticsStorage statistics) {
            return statistics;
        }
        throw new StorageException("Statistics are not supported by the active storage");
    }

    @Override
    public Map<String, ?> getAllTimeEntries() throws StorageException {
        return storage.getAllTimeEntries();
    }

    @Override
    public long startSession(final PlayerSessionContext context, final TimeEntryReason reason) throws StorageException {
        return storage.startSession(context, reason);
    }

    @Override
    public void updateSession(final long sessionId, final long stoppedAtMs, final TimeEntryReason reason) throws StorageException {
        storage.updateSession(sessionId, stoppedAtMs, reason);
    }

    @Override
    public void updateSessionWorld(final long sessionId, final String server, final String world) throws StorageException {
        storage.updateSessionWorld(sessionId, server, world);
    }

    @Override
    public void deletePlayer(final UUID uniqueId) throws StorageException {
        onlineSessions.remove(uniqueId);
        try {
            storage.deletePlayer(uniqueId);
        } catch (final java.sql.SQLException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public int deleteInactiveHistory(final long inactiveDays) throws StorageException {
        return storage.deleteInactiveHistory(inactiveDays);
    }

    @Override
    public void persistSession(final PlayerSessionChunk session) throws StorageException {
        storage.persistSession(session);
    }

    @Override
    public void startAccumulating(final UUID uuid, final String name, final String server,
                                  final String world, final long when)
            throws StorageException {
        withSessionLock(uuid, () -> {
            final PlayerSessionContext context = new PlayerSessionContext(uuid, name, server, world, when,
                    playerTags.getOrDefault(uuid, Map.of()));
            final long sessionId = storage.startSession(context, TimeEntryReason.PLAYER_JOIN);
            final PersistedPlayerSession previous = onlineSessions.put(uuid, new PersistedPlayerSession(sessionId, context, when));
            if (previous != null) {
                storage.updateSession(previous.sessionId(), when, switchReason(previous.context(), context));
            }
        });
        eventDispatcher.get().sessionStarted(uuid, name, server, world, Instant.ofEpochMilli(when));
    }

    @Override
    public void stopAccumulatingAndSaveOnlineTime(final UUID uuid, final long when,
                                                  final TimeEntryReason reason)
            throws StorageException {
        final AtomicBoolean ended = new AtomicBoolean();
        withSessionLock(uuid, () -> {
            if (reason != TimeEntryReason.PLAYER_AFK) {
                playerTags.remove(uuid);
            }
            final PersistedPlayerSession session = onlineSessions.remove(uuid);
            if (session != null) {
                storage.updateSession(session.sessionId(), when, reason);
                ended.set(true);
            }
        });
        if (ended.get()) {
            eventDispatcher.get().sessionEnded(uuid, Instant.ofEpochMilli(when));
        }
    }

    @Override
    public void switchContext(final UUID uuid, final String name, final String server,
                              final String world, final long when)
            throws StorageException {
        withSessionLock(uuid, () -> {
            final PersistedPlayerSession current = onlineSessions.get(uuid);
            final PlayerSessionContext next = new PlayerSessionContext(uuid, name, server, world, when,
                    current == null ? playerTags.getOrDefault(uuid, Map.of()) : current.context().tags());
            if (current != null
                    && current.context().server().equals(server)
                    && current.context().world().equals(world)) {
                return;
            }
            final long sessionId = storage.startSession(next, TimeEntryReason.PLAYER_JOIN);
            final PersistedPlayerSession previous = onlineSessions.put(uuid, new PersistedPlayerSession(sessionId, next, when));
            if (previous != null) {
                storage.updateSession(previous.sessionId(), when, switchReason(previous.context(), next));
            }
        });
    }

    @Override
    public void updateWorldContext(final UUID uuid, final String world, final long observedAtMs)
            throws StorageException {
        withSessionLock(uuid, () -> {
            final PersistedPlayerSession current = onlineSessions.get(uuid);
            if (current == null || current.context().world().equals(world) || observedAtMs < current.context().startedAtMs()) {
                return;
            }
            final PlayerSessionContext previous = current.context();
            final PlayerSessionContext updated = new PlayerSessionContext(previous.uuid(), previous.name(),
                    previous.server(), world, previous.startedAtMs(), previous.tags());
            if (onlineSessions.replace(uuid, current,
                    new PersistedPlayerSession(current.sessionId(), updated, current.lastPersistedAtMs()))) {
                storage.updateSessionWorld(current.sessionId(), updated.server(), updated.world());
            }
        });
    }

    @Override
    public void switchWorldContext(final UUID uuid, final String world, final long observedAtMs)
            throws StorageException {
        withSessionLock(uuid, () -> {
            final PersistedPlayerSession current = onlineSessions.get(uuid);
            if (current == null || current.context().world().equals(world) || observedAtMs < current.context().startedAtMs()) {
                return;
            }
            final PlayerSessionContext previous = current.context();
            final PlayerSessionContext next = new PlayerSessionContext(previous.uuid(), previous.name(),
                    previous.server(), world, observedAtMs, previous.tags());
            final long sessionId = storage.startSession(next, TimeEntryReason.PLAYER_JOIN);
            if (onlineSessions.replace(uuid, current, new PersistedPlayerSession(sessionId, next, observedAtMs))) {
                storage.updateSession(current.sessionId(), observedAtMs, TimeEntryReason.WORLD_SWITCH);
            }
        });
    }

    @Override
    public void switchTags(final UUID uuid, final Map<String, String> tags, final long observedAtMs)
            throws StorageException {
        Objects.requireNonNull(tags, "tags");
        final Map<String, String> next = Map.copyOf(tags);
        final AtomicReference<Map<String, String>> previousTags = new AtomicReference<>(Map.of());
        withSessionLock(uuid, () -> {
            final Map<String, String> replaced = next.isEmpty() ? playerTags.remove(uuid) : playerTags.put(uuid, next);
            previousTags.set(replaced == null ? Map.of() : replaced);
            final PersistedPlayerSession current = onlineSessions.get(uuid);
            if (current == null || current.context().tags().equals(next)) {
                return;
            }
            final long switchedAtMs = Math.max(observedAtMs, current.context().startedAtMs());
            final PlayerSessionContext previous = current.context();
            final PlayerSessionContext updated = new PlayerSessionContext(previous.uuid(), previous.name(),
                    previous.server(), previous.world(), switchedAtMs, next);
            final long sessionId = storage.startSession(updated, TimeEntryReason.PLAYER_JOIN);
            if (onlineSessions.replace(uuid, current, new PersistedPlayerSession(sessionId, updated, switchedAtMs))) {
                storage.updateSession(current.sessionId(), switchedAtMs, TimeEntryReason.TAG_SWITCH);
            }
        });
        if (!previousTags.get().equals(next)) {
            eventDispatcher.get().trackingTagsChanged(uuid, previousTags.get(), next, Instant.ofEpochMilli(observedAtMs));
        }
    }

    @Override
    public Map<String, String> getTrackingTags(final UUID uuid) {
        return playerTags.getOrDefault(uuid, Map.of());
    }

    @Override
    public void flushOnlineTimeCache() throws StorageException {
        if (onlineSessions.isEmpty()) {
            return;
        }
        log.debug("Flushing online time cache");
        checkpointActiveSessions(System.currentTimeMillis());
    }

    private void checkpointActiveSessions(final long observedAtMs) throws StorageException {
        for (final Map.Entry<UUID, PersistedPlayerSession> entry : onlineSessions.entrySet()) {
            final UUID uuid = entry.getKey();
            withSessionLock(uuid, () -> checkpointActiveSession(uuid, observedAtMs));
        }
    }

    @Override
    public Optional<PlayerSessionContext> getActiveSessionContext(final UUID uuid) {
        final PersistedPlayerSession session = onlineSessions.get(uuid);
        return session == null ? Optional.empty() : Optional.of(session.context());
    }

    @Override
    @SuppressWarnings("PMD.UseTryWithResources")
    public void close() throws StorageException {
        try {
            if (!onlineSessions.isEmpty()) {
                final long now = System.currentTimeMillis();
                for (final UUID uuid : List.copyOf(onlineSessions.keySet())) {
                    withSessionLock(uuid, () -> {
                        final PersistedPlayerSession session = onlineSessions.remove(uuid);
                        if (session != null) {
                            storage.updateSession(session.sessionId(), now, TimeEntryReason.SHUTDOWN_FLUSH);
                        }
                    });
                }
            }
        } finally {
            this.storage.close();
        }
    }

    private TimeEntryReason switchReason(final PlayerSessionContext previous, final PlayerSessionContext next) {
        if (!previous.server().equals(next.server())) {
            return TimeEntryReason.SERVER_SWITCH;
        }
        return TimeEntryReason.WORLD_SWITCH;
    }

    private void checkpointActiveSession(final UUID uuid, final long observedAtMs) throws StorageException {
        final PersistedPlayerSession current = onlineSessions.get(uuid);
        if (current != null && observedAtMs > current.lastPersistedAtMs()
                && onlineSessions.replace(uuid, current,
                new PersistedPlayerSession(current.sessionId(), current.context(), observedAtMs))) {
            storage.updateSession(current.sessionId(), observedAtMs, TimeEntryReason.AUTO_FLUSH);
        }
    }

    private void withSessionLock(final UUID uuid, final StorageAction action) throws StorageException {
        final ReentrantLock lock = sessionLocks[(uuid.hashCode() & Integer.MAX_VALUE) % SESSION_LOCK_STRIPES];
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    private ReentrantLock[] createSessionLocks() {
        final ReentrantLock[] locks = new ReentrantLock[SESSION_LOCK_STRIPES];
        for (int index = 0; index < locks.length; index++) {
            locks[index] = new ReentrantLock();
        }
        return locks;
    }

    /**
     * Action that may perform a storage operation while holding a session lock.
     */
    @FunctionalInterface
    private interface StorageAction {
        /**
         * Performs the storage operation.
         */
        void run() throws StorageException;
    }
}
