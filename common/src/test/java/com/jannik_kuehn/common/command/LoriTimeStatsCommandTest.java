package com.jannik_kuehn.common.command;

import com.github.roleplaycauldron.spellbook.core.logger.LoggerFactory;
import com.jannik_kuehn.common.LoriTimePlugin;
import com.jannik_kuehn.common.api.storage.TimeScope;
import com.jannik_kuehn.common.config.Configuration;
import com.jannik_kuehn.common.config.localization.Localization;
import com.jannik_kuehn.common.platform.CommonSender;
import com.jannik_kuehn.common.storage.contract.StatisticsStorage;
import com.jannik_kuehn.common.storage.model.StatisticsRequest;
import com.jannik_kuehn.common.storage.model.StatisticsSnapshot;
import com.jannik_kuehn.common.utils.TimeParser;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings({"PMD.UnitTestAssertionsShouldIncludeMessage", "PMD.UnitTestContainsTooManyAsserts"})
class LoriTimeStatsCommandTest {
    private static final Instant OBSERVED = Instant.parse("2024-04-17T15:30:00Z");

    private LoriTimePlugin plugin;

    private Configuration config;

    private Localization localization;

    private StatisticsStorage storage;

    private CommonSender sender;

    @BeforeEach
    void setUp() throws Exception {
        plugin = mock(LoriTimePlugin.class);
        config = mock(Configuration.class);
        localization = mock(Localization.class);
        storage = mock(StatisticsStorage.class);
        sender = mock(CommonSender.class);
        when(plugin.getLoggerFactory()).thenReturn(new LoggerFactory(Logger.getLogger("test")));
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getParser()).thenReturn(new TimeParser.Builder()
                .addUnit(60L, "m").addUnit(86_400L, "d").addUnit(604_800L, "w").build());
        when(plugin.getStatisticsStorage()).thenReturn(Optional.of(storage));
        when(config.getString("stats.default-range", "calendar:today")).thenReturn("calendar:today");
        when(config.getString("stats.bounce-threshold", "3m")).thenReturn("3m");
        when(config.getString("stats.calendar-time-zone", "system")).thenReturn("Europe/Berlin");
        when(localization.getRawMessage(anyString())).thenReturn("[range]");
        when(localization.formatTextComponentWithoutPrefix(anyString())).thenReturn(Component.text("stats"));
        when(storage.getStatistics(any())).thenReturn(emptySnapshot());
    }

    @Test
    void calendarCommandUsesCalendarRangeScopeAndObservationInstant() throws Exception {
        final LoriTimeStatsCommand command = command();

        command.execute(sender, "calendar:this-week", "server:survival");

        final ArgumentCaptor<StatisticsRequest> request = ArgumentCaptor.forClass(StatisticsRequest.class);
        verify(storage).getStatistics(request.capture());
        assertEquals(Instant.parse("2024-04-14T22:00:00Z"), request.getValue().range().startInclusive());
        assertEquals(OBSERVED, request.getValue().range().endExclusive());
        assertEquals(OBSERVED, request.getValue().observedAt());
        assertEquals(TimeScope.server("survival"), request.getValue().scope());
    }

    @Test
    void completedCalendarSliceUsesLocalUnitBoundaries() throws Exception {
        command().execute(sender, "calendar:2d-3d");

        final ArgumentCaptor<StatisticsRequest> request = ArgumentCaptor.forClass(StatisticsRequest.class);
        verify(storage).getStatistics(request.capture());
        assertEquals(Instant.parse("2024-04-14T22:00:00Z"), request.getValue().range().startInclusive());
        assertEquals(Instant.parse("2024-04-16T22:00:00Z"), request.getValue().range().endExclusive());
        assertEquals(OBSERVED, request.getValue().observedAt());
    }

    @Test
    void defaultRangeUsesCurrentCalendarDay() throws Exception {
        command().execute(sender);

        final ArgumentCaptor<StatisticsRequest> request = ArgumentCaptor.forClass(StatisticsRequest.class);
        verify(storage).getStatistics(request.capture());
        assertEquals(Instant.parse("2024-04-16T22:00:00Z"), request.getValue().range().startInclusive());
        assertEquals(OBSERVED, request.getValue().range().endExclusive());
    }

    @Test
    void conflictingAndMalformedCalendarFlagsRenderUsageWithoutQuery() {
        command().execute(sender, "time:1d", "calendar:today");
        command().execute(sender, "calendar:0w");

        verifyNoInteractions(storage);
        verify(sender, times(2)).sendMessage(any(Component.class));
    }

    @Test
    void completionIncludesCalendarSelectors() {
        final List<String> root = command().handleTabComplete(sender, "calendar:");

        assertTrue(root.contains("calendar:today"));
        assertTrue(root.contains("calendar:this-week"));
        assertTrue(root.contains("calendar:this-month"));
        assertTrue(root.contains("calendar:2mo"));
        assertTrue(root.contains("calendar:2d-3d"));
    }

    @Test
    void subsequentRequestUsesReloadedTimezoneConfiguration() throws Exception {
        when(config.getString("stats.calendar-time-zone", "system")).thenReturn("UTC", "Europe/Berlin");
        final LoriTimeStatsCommand command = command();

        command.execute(sender, "calendar:today");
        command.execute(sender, "calendar:today");

        final ArgumentCaptor<StatisticsRequest> requests = ArgumentCaptor.forClass(StatisticsRequest.class);
        verify(storage, times(2)).getStatistics(requests.capture());
        assertEquals(Instant.parse("2024-04-17T00:00:00Z"),
                requests.getAllValues().get(0).range().startInclusive());
        assertEquals(Instant.parse("2024-04-16T22:00:00Z"),
                requests.getAllValues().get(1).range().startInclusive());
    }

    private LoriTimeStatsCommand command() {
        return new LoriTimeStatsCommand(plugin, localization, Clock.fixed(OBSERVED, ZoneOffset.UTC));
    }

    private StatisticsSnapshot emptySnapshot() {
        return new StatisticsSnapshot(0, 0, 0, Duration.ZERO, Duration.ZERO, Duration.ZERO, 0.0D,
                0, 0.0D, 0, 0, 0, Duration.ZERO, 0, 0.0D, Map.of(), Map.of(), List.of());
    }
}
