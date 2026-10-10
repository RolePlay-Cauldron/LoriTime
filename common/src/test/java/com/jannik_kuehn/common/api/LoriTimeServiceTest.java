package com.jannik_kuehn.common.api;

import com.jannik_kuehn.common.LoriTimePlugin;
import com.jannik_kuehn.common.api.storage.TimeRange;
import com.jannik_kuehn.common.api.storage.TimeScope;
import com.jannik_kuehn.common.config.localization.Localization;
import com.jannik_kuehn.common.exception.StorageException;
import com.jannik_kuehn.common.platform.CommonPlayerSender;
import com.jannik_kuehn.common.platform.CommonServer;
import com.jannik_kuehn.common.scheduler.PluginScheduler;
import com.jannik_kuehn.common.scheduler.PluginTask;
import com.jannik_kuehn.common.service.LoriTimeServiceImpl;
import com.jannik_kuehn.common.service.RemoteTagWriter;
import com.jannik_kuehn.common.service.RemoteTimeReader;
import com.jannik_kuehn.common.storage.contract.TimeAccumulator;
import com.jannik_kuehn.common.storage.contract.UnifiedStorage;
import com.jannik_kuehn.common.storage.model.ManualTimeAdjustment;
import com.jannik_kuehn.common.storage.model.TimeEntryReason;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ResourceLock("LoriTimeAPI")
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidAccessibilityAlteration"})
class LoriTimeServiceTest {

    private static final UUID PLAYER_ID = UUID.fromString("44174cf6-e76c-4994-899c-3387284ecd62");

    private static final UUID ACTOR_ID = UUID.fromString("22fc9749-1470-4998-b74f-22add1f4dbb3");

    private LoriTimePlugin plugin;

    private UnifiedStorage storage;

    private LoriTimeService service;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        resetApi();
        plugin = mock(LoriTimePlugin.class);
        storage = mock(UnifiedStorage.class);
        final PluginScheduler scheduler = mock(PluginScheduler.class);
        when(plugin.getStorage()).thenReturn(storage);
        when(plugin.getScheduler()).thenReturn(scheduler);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return mock(PluginTask.class);
        }).when(scheduler).runAsyncOnce(any());
        service = new LoriTimeServiceImpl(plugin);
    }

    @AfterEach
    void tearDown() throws ReflectiveOperationException {
        resetApi();
    }

    @Test
    void serviceAccessorIsEmptyBeforeInitialization() {
        assertTrue(LoriTimeAPI.service().isEmpty(), "Service should be unavailable before initialization");
    }

    @Test
    void serviceAccessorReturnsFacadeAfterInitialization() {
        LoriTimeAPI.setPlugin(plugin);

        assertTrue(LoriTimeAPI.service().isPresent(), "Service should be available after initialization");
    }

    @Test
    void findsUuidByPlayerName() throws StorageException {
        when(storage.getUuid("Lorias_")).thenReturn(Optional.of(PLAYER_ID));

        assertEquals(Optional.of(PLAYER_ID), service.findUuid("Lorias_").join(),
                "Expected facade to return stored UUID");
    }

    @Test
    void findsLatestNameByUuid() throws StorageException {
        when(storage.getName(PLAYER_ID)).thenReturn(Optional.of("Lorias_"));

        assertEquals(Optional.of("Lorias_"), service.findName(PLAYER_ID).join(),
                "Expected facade to return stored name");
    }

    @Test
    void returnsOnlineTimeAsDuration() throws StorageException {
        when(storage.getTime(PLAYER_ID, TimeScope.GLOBAL)).thenReturn(OptionalLong.of(3661));

        assertEquals(Optional.of(Duration.ofSeconds(3661)), service.getOnlineTime(PLAYER_ID).join(),
                "Expected facade to expose time as Duration");
    }

    @Test
    void returnsEmptyOnlineTimeForUnknownPlayer() throws StorageException {
        when(storage.getTime(PLAYER_ID, TimeScope.GLOBAL)).thenReturn(OptionalLong.empty());

        assertEquals(Optional.empty(), service.getOnlineTime(PLAYER_ID).join(), "Unknown time should be empty");
    }

    @Test
    void returnsOnlineTimeByPlayerIdentity() throws StorageException {
        final LoriTimePlayer player = new LoriTimePlayerRef(PLAYER_ID, "Renamed");
        when(storage.getTime(PLAYER_ID, TimeScope.GLOBAL)).thenReturn(OptionalLong.of(120));

        assertEquals(Optional.of(Duration.ofSeconds(120)), service.getOnlineTime(player).join(),
                "Player overload should query by UUID");
    }

    @Test
    void addsSystemManualAdjustment() throws StorageException {
        service.addTime(PLAYER_ID, Duration.ofSeconds(30)).join();

        verify(storage).addTime(new ManualTimeAdjustment(PLAYER_ID, 30L,
                TimeEntryReason.MANUAL_ADJUSTMENT, (UUID) null, LoriTimeService.API_ACTOR));
    }

    @Test
    void addsSystemManualAdjustmentByPlayerIdentity() throws StorageException {
        final LoriTimePlayer player = new LoriTimePlayerRef(PLAYER_ID, "Renamed");

        service.addTime(player, Duration.ofSeconds(30)).join();

        verify(storage).addTime(new ManualTimeAdjustment(PLAYER_ID, 30L,
                TimeEntryReason.MANUAL_ADJUSTMENT, (UUID) null, LoriTimeService.API_ACTOR));
    }

    @Test
    void addsActorAwareManualAdjustment() throws StorageException {
        service.addTime(PLAYER_ID, Duration.ofSeconds(-45), ACTOR_ID, "Admin").join();

        verify(storage).addTime(new ManualTimeAdjustment(PLAYER_ID, -45L,
                TimeEntryReason.MANUAL_ADJUSTMENT, ACTOR_ID, "Admin"));
    }

    @Test
    void addsActorAwareManualAdjustmentByPlayerIdentities() throws StorageException {
        final LoriTimePlayer player = new LoriTimePlayerRef(PLAYER_ID, "Renamed");
        final LoriTimePlayer actor = new LoriTimePlayerRef(ACTOR_ID, "Admin");

        service.addTime(player, Duration.ofSeconds(-45), actor).join();

        verify(storage).addTime(new ManualTimeAdjustment(PLAYER_ID, -45L,
                TimeEntryReason.MANUAL_ADJUSTMENT, ACTOR_ID, "Admin"));
    }

    @Test
    void returnsScopedOnlineTime() throws StorageException {
        when(storage.getTime(PLAYER_ID, TimeScope.world("survival", "world"))).thenReturn(OptionalLong.of(120));

        assertEquals(Optional.of(Duration.ofSeconds(120)),
                service.getOnlineTime(PLAYER_ID, TimeScope.world("survival", "world")).join(),
                "Expected facade to expose scoped time");
    }

    @Test
    void returnsRangedScopedOnlineTime() throws StorageException {
        final TimeRange range = TimeRange.between(Instant.ofEpochSecond(1), Instant.ofEpochSecond(10));
        when(storage.getTime(PLAYER_ID, TimeScope.world("survival", "world"), range)).thenReturn(OptionalLong.of(120));

        assertEquals(Optional.of(Duration.ofSeconds(120)),
                service.getOnlineTime(PLAYER_ID, TimeScope.world("survival", "world"), range).join(),
                "Expected facade to expose ranged scoped time");
    }

    @Test
    void addsScopedManualAdjustment() throws StorageException {
        service.addTime(PLAYER_ID, Duration.ofSeconds(30), TimeScope.server("survival")).join();

        verify(storage).addTime(new ManualTimeAdjustment(PLAYER_ID, 30L,
                TimeEntryReason.MANUAL_ADJUSTMENT, (UUID) null, LoriTimeService.API_ACTOR, TimeScope.server("survival")));
    }

    @Test
    void rejectsSubSecondAdjustment() throws StorageException {
        assertThrows(IllegalArgumentException.class, () -> service.addTime(PLAYER_ID, Duration.ofMillis(500)),
                "Sub-second adjustments should be rejected");

        verify(storage, never()).addTime(any(ManualTimeAdjustment.class));
    }

    @Test
    void rejectsNullInputsBeforeWriting() throws StorageException {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> service.findUuid(null)),
                () -> assertThrows(NullPointerException.class, () -> service.findName(null)),
                () -> assertThrows(NullPointerException.class, () -> service.getOnlineTime((UUID) null)),
                () -> assertThrows(NullPointerException.class, () -> service.getOnlineTime((LoriTimePlayer) null)),
                () -> assertThrows(NullPointerException.class, () -> service.addTime((UUID) null, Duration.ofSeconds(1))),
                () -> assertThrows(NullPointerException.class,
                        () -> service.addTime((LoriTimePlayer) null, Duration.ofSeconds(1))),
                () -> assertThrows(NullPointerException.class, () -> service.addTime(PLAYER_ID, null)),
                () -> assertThrows(NullPointerException.class,
                        () -> service.addTime(PLAYER_ID, Duration.ofSeconds(1), ACTOR_ID, null))
        );
        verify(storage, never()).addTime(any(ManualTimeAdjustment.class));
    }

    @Test
    void rejectsInvalidPlayerIdentityBeforeWriting() throws StorageException {
        final LoriTimePlayer missingName = mock(LoriTimePlayer.class);
        when(missingName.getUniqueId()).thenReturn(PLAYER_ID);
        when(missingName.getName()).thenReturn(" ");

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> service.getOnlineTime(missingName)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.addTime(missingName, Duration.ofSeconds(1))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.addTime(new LoriTimePlayerRef(PLAYER_ID, "Lorias_"),
                                Duration.ofSeconds(1), missingName))
        );
        verify(storage, never()).addTime(any(ManualTimeAdjustment.class));
    }

    @Test
    void wrapsStorageFailures() throws StorageException {
        when(storage.getName(PLAYER_ID)).thenThrow(new StorageException("failure"));

        final CompletionException thrown = assertThrows(CompletionException.class, () -> service.findName(PLAYER_ID).join(),
                "Storage failures should be wrapped in the public API exception");
        assertInstanceOf(LoriTimeApiException.class, thrown.getCause(),
                "Storage failures should be wrapped in the public API exception");
    }

    @Test
    void completesExceptionallyWhenStorageThrowsRuntimeException() throws StorageException {
        when(storage.getName(PLAYER_ID)).thenThrow(new IllegalStateException("boom"));

        final CompletionException thrown = assertThrows(CompletionException.class, () -> service.findName(PLAYER_ID).join(),
                "Runtime failures must complete the future instead of leaving it pending");
        assertInstanceOf(LoriTimeApiException.class, thrown.getCause(),
                "Runtime failures should be wrapped in the public API exception");
    }

    @Test
    void slaveRuntimeReadsCachedGlobalTime() {
        when(plugin.getStorage()).thenReturn(null);
        final RemoteTimeReader reader = mock(RemoteTimeReader.class);
        when(reader.getCachedTime(PLAYER_ID)).thenReturn(OptionalLong.of(90));
        when(plugin.getRemoteTimeReader()).thenReturn(Optional.of(reader));

        assertEquals(Optional.of(Duration.ofSeconds(90)), service.getOnlineTime(PLAYER_ID).join(),
                "Slave runtime should answer from the remote time reader");
        assertFalse(service.isFullAccess(), "Slave runtime has no full API access");
    }

    @Test
    void slaveRuntimeRequestsRefreshForUnknownPlayer() {
        when(plugin.getStorage()).thenReturn(null);
        final RemoteTimeReader reader = mock(RemoteTimeReader.class);
        when(reader.getCachedTime(PLAYER_ID)).thenReturn(OptionalLong.empty());
        when(plugin.getRemoteTimeReader()).thenReturn(Optional.of(reader));

        final CompletionException thrown = assertThrows(CompletionException.class,
                () -> service.getOnlineTime(PLAYER_ID).join(), "Unknown slave player should fail fast");
        assertInstanceOf(LoriTimeApiException.class, thrown.getCause(), "Expected public API exception");
        verify(reader).requestRefresh(PLAYER_ID);
    }

    @Test
    void slaveRuntimeRejectsUnsupportedCalls() {
        when(plugin.getStorage()).thenReturn(null);
        when(plugin.getRemoteTimeReader()).thenReturn(Optional.empty());
        final TimeScope scope = TimeScope.server("survival");

        assertAll(
                () -> assertInstanceOf(LoriTimeApiException.class, assertThrows(CompletionException.class,
                        () -> service.getOnlineTime(PLAYER_ID).join()).getCause()),
                () -> assertInstanceOf(LoriTimeApiException.class, assertThrows(CompletionException.class,
                        () -> service.getOnlineTime(PLAYER_ID, scope).join()).getCause()),
                () -> assertInstanceOf(LoriTimeApiException.class, assertThrows(CompletionException.class,
                        () -> service.findUuid("Lorias_").join()).getCause()),
                () -> assertInstanceOf(LoriTimeApiException.class, assertThrows(CompletionException.class,
                        () -> service.addTime(PLAYER_ID, Duration.ofSeconds(1)).join()).getCause())
        );
    }

    @Test
    void formatsDurationWithConfiguredLocalization() {
        final Localization localization = mock(Localization.class);
        when(localization.getRawMessage("unit.hour.singular")).thenReturn("h");
        when(localization.getRawMessage("unit.minute.plural")).thenReturn("min");
        when(plugin.getLocalization()).thenReturn(localization);

        assertEquals("1 h 2 min", service.formatDuration(Duration.ofSeconds(3720)),
                "Expected facade to format with the configured units");
    }

    @Test
    void fullAccessIsAvailableWithStorage() {
        assertTrue(service.isFullAccess(), "Canonical runtime should report full access");
    }

    @Test
    void setsTrackingTagThroughAccumulatorKeepingExistingTags() throws StorageException {
        final TimeAccumulator accumulator = mock(TimeAccumulator.class);
        when(plugin.getAccumulator()).thenReturn(accumulator);
        when(accumulator.getTrackingTags(PLAYER_ID)).thenReturn(Map.of("quests:class", "Mage"));
        onlinePlayer(true);

        service.setTrackingTag(PLAYER_ID, "rp:character", "Aria").join();

        verify(accumulator).switchTags(eq(PLAYER_ID),
                eq(Map.of("quests:class", "Mage", "rp:character", "Aria")), anyLong());
    }

    @Test
    void clearsTrackingTagThroughAccumulator() throws StorageException {
        final TimeAccumulator accumulator = mock(TimeAccumulator.class);
        when(plugin.getAccumulator()).thenReturn(accumulator);
        when(accumulator.getTrackingTags(PLAYER_ID)).thenReturn(Map.of("rp:character", "Aria"));
        onlinePlayer(true);

        service.clearTrackingTag(PLAYER_ID, "rp:character").join();

        verify(accumulator).switchTags(eq(PLAYER_ID), eq(Map.of()), anyLong());
    }

    @Test
    void rejectsInvalidTrackingTagInput() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.setTrackingTag(PLAYER_ID, "NoNamespace", "Aria")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.setTrackingTag(PLAYER_ID, "rp:character", " ")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.setTrackingTag(PLAYER_ID, "rp:character", "x".repeat(192))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.clearTrackingTag(PLAYER_ID, "UPPER:case")),
                () -> assertThrows(NullPointerException.class,
                        () -> service.setTrackingTag(null, "rp:character", "Aria")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.getOnlineTime(PLAYER_ID, TimeScope.GLOBAL, "bad key", "Aria"))
        );
    }

    @Test
    void failsTrackingTagChangeForOfflinePlayer() throws StorageException {
        final TimeAccumulator accumulator = mock(TimeAccumulator.class);
        when(plugin.getAccumulator()).thenReturn(accumulator);
        onlinePlayer(false);

        final CompletionException thrown = assertThrows(CompletionException.class,
                () -> service.setTrackingTag(PLAYER_ID, "rp:character", "Aria").join());

        assertInstanceOf(LoriTimeApiException.class, thrown.getCause(), "Expected public API exception");
        verify(accumulator, never()).switchTags(any(), any(), anyLong());
    }

    @Test
    void returnsTaggedOnlineTime() throws StorageException {
        when(storage.getTaggedTime(PLAYER_ID, TimeScope.GLOBAL, "rp:character", "Aria"))
                .thenReturn(OptionalLong.of(600));

        assertEquals(Optional.of(Duration.ofSeconds(600)),
                service.getOnlineTime(PLAYER_ID, TimeScope.GLOBAL, "rp:character", "Aria").join(),
                "Expected the tagged time as Duration");
    }

    @Test
    void returnsRangedTaggedOnlineTime() throws StorageException {
        final TimeRange range = TimeRange.between(Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"));
        when(storage.getTaggedTime(PLAYER_ID, TimeScope.GLOBAL, range, "rp:character", "Aria"))
                .thenReturn(OptionalLong.of(120));
        when(storage.getTaggedTime(PLAYER_ID, TimeScope.GLOBAL, range, "rp:character", "Bob"))
                .thenReturn(OptionalLong.empty());

        assertEquals(Optional.of(Duration.ofSeconds(120)),
                service.getOnlineTime(PLAYER_ID, TimeScope.GLOBAL, range, "rp:character", "Aria").join(),
                "Expected the ranged tagged time as Duration");
        assertEquals(Optional.empty(),
                service.getOnlineTime(PLAYER_ID, TimeScope.GLOBAL, range, "rp:character", "Bob").join(),
                "Expected empty when no tagged time exists");
    }

    @Test
    void wrapsTaggedQueryFailures() throws StorageException {
        when(storage.getTaggedTime(PLAYER_ID, TimeScope.GLOBAL, "rp:character", "Aria"))
                .thenThrow(new StorageException("boom"));

        final CompletionException thrown = assertThrows(CompletionException.class,
                () -> service.getOnlineTime(PLAYER_ID, TimeScope.GLOBAL, "rp:character", "Aria").join());

        assertInstanceOf(LoriTimeApiException.class, thrown.getCause(), "Expected public API exception");
    }

    @Test
    void slaveRuntimeForwardsTrackingTagChangesToMaster() {
        when(plugin.getStorage()).thenReturn(null);
        final RemoteTagWriter writer = mock(RemoteTagWriter.class);
        when(plugin.getRemoteTagWriter()).thenReturn(Optional.of(writer));
        onlinePlayer(true);

        service.setTrackingTag(PLAYER_ID, "rp:character", "Aria").join();
        service.clearTrackingTag(PLAYER_ID, "rp:character").join();

        verify(writer).changeTag(PLAYER_ID, "rp:character", "Aria");
        verify(writer).changeTag(PLAYER_ID, "rp:character", null);
    }

    @Test
    void slaveRuntimeWithoutTagWriterRejectsTrackingTagChange() {
        when(plugin.getStorage()).thenReturn(null);
        when(plugin.getRemoteTagWriter()).thenReturn(Optional.empty());
        onlinePlayer(true);

        final CompletionException thrown = assertThrows(CompletionException.class,
                () -> service.setTrackingTag(PLAYER_ID, "rp:character", "Aria").join());

        assertInstanceOf(LoriTimeApiException.class, thrown.getCause(), "Expected public API exception");
    }

    @Test
    void slaveRuntimeRejectsTaggedQueries() {
        when(plugin.getStorage()).thenReturn(null);

        final CompletionException thrown = assertThrows(CompletionException.class,
                () -> service.getOnlineTime(PLAYER_ID, TimeScope.GLOBAL, "rp:character", "Aria").join());

        assertInstanceOf(LoriTimeApiException.class, thrown.getCause(), "Expected public API exception");
    }

    private void onlinePlayer(final boolean online) {
        final CommonServer server = mock(CommonServer.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPlayer(PLAYER_ID)).thenReturn(online
                ? Optional.of(mock(CommonPlayerSender.class)) : Optional.empty());
    }

    private void resetApi() throws ReflectiveOperationException {
        final Field field = LoriTimeAPI.class.getDeclaredField("loriTimePlugin");
        field.setAccessible(true);
        field.set(null, null);
    }
}
