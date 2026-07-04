package com.jannik_kuehn.common.command;

import com.github.roleplaycauldron.spellbook.core.logger.LoggerFactory;
import com.jannik_kuehn.common.LoriTimePlugin;
import com.jannik_kuehn.common.command.completion.RecentPlayerSuggestionCache;
import com.jannik_kuehn.common.command.completion.ScopeSuggestionCache;
import com.jannik_kuehn.common.config.localization.Localization;
import com.jannik_kuehn.common.exception.StorageException;
import com.jannik_kuehn.common.platform.CommonPlayerSender;
import com.jannik_kuehn.common.platform.CommonServer;
import com.jannik_kuehn.common.player.LoriTimePlayerConverter;
import com.jannik_kuehn.common.storage.contract.UnifiedStorage;
import com.jannik_kuehn.common.storage.model.RecentPlayerIdentity;
import com.jannik_kuehn.common.utils.TimeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
class LoriTimeCompletionTest {

    private static final UUID PLAYER_ID = UUID.fromString("44174cf6-e76c-4994-899c-3387284ecd62");

    private static Stream<Arguments> longScopePrefixCompletionArguments() {
        return Stream.of(
                Arguments.of("s", List.of("server:"), "Expected long server prefix completion"),
                Arguments.of("w", List.of("world:"), "Expected long world prefix completion"),
                Arguments.of("s:", List.of(), "Expected short server alias to stay unsuggested")
        );
    }

    @Test
    void loriTimeTabCompletionUsesCachedAndOnlineNamesWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        final CommonPlayerSender onlinePlayer = mock(CommonPlayerSender.class);
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of("Lorias_"));
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[]{onlinePlayer});
        when(onlinePlayer.getName()).thenReturn("OnlineUser");
        when(context.source.hasPermission("loritime.see.other")).thenReturn(true);

        final List<String> completions = new LoriTimeCommand(context.plugin, context.localization).handleTabComplete(context.source, "L");

        assertEquals(List.of("Lorias_"), completions, "Expected tab completion to use cached player names");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void modifyTabCompletionUsesCachedAndOnlineNamesWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        final CommonPlayerSender onlinePlayer = mock(CommonPlayerSender.class);
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of("Lorias_"));
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[]{onlinePlayer});
        when(onlinePlayer.getName()).thenReturn("OnlineUser");
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);

        final List<String> completions = new LoriTimeModifyCommand(context.plugin, context.localization,
                mock(TimeParser.class)).handleTabComplete(context.source, "modify", "L");

        assertEquals(List.of("Lorias_"), completions, "Expected modify tab completion to use cached player names");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void modifyTabCompletionSuggestsLongScopePrefixes() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of());
        when(context.plugin.getRecentPlayerSuggestionCache()).thenReturn(null);
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[0]);
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);

        final List<String> completions = new LoriTimeModifyCommand(context.plugin, context.localization,
                mock(TimeParser.class)).handleTabComplete(context.source, "add", "Lorias_", "12", "s");

        assertEquals(List.of("server:"), completions, "Expected modify tab completion to suggest long server prefix");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void modifyTabCompletionUsesLiveServerCandidatesWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);
        when(context.server.getLiveServerNames()).thenReturn(List.of("survival", "creative"));

        final List<String> completions = new LoriTimeModifyCommand(context.plugin, context.localization,
                mock(TimeParser.class)).handleTabComplete(context.source, "set", "Lorias_", "12", "server:su");

        assertEquals(List.of("server:survival"), completions,
                "Expected modify server flag value completion from live runtime context");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void modifyTabCompletionUsesLiveWorldCandidatesWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);
        when(context.server.getLiveWorldNames(Optional.of("survival"), Optional.empty()))
                .thenReturn(List.of("world", "world_nether"));

        final List<String> completions = new LoriTimeModifyCommand(context.plugin, context.localization,
                mock(TimeParser.class)).handleTabComplete(context.source, "set", "Lorias_", "12",
                "server:survival", "world:wo");

        assertEquals(List.of("world:world", "world:world_nether"), completions,
                "Expected modify world flag value completion from live runtime context");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void modifyTabCompletionUsesCachedServerCandidatesForShortFlagsWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        context.scopeCache.replaceStoredNames(Set.of("survival", "creative"),
                Map.of("survival", Set.of("world", "world_nether")));
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);
        when(context.server.getLiveServerNames()).thenReturn(List.of());

        final LoriTimeModifyCommand command = new LoriTimeModifyCommand(context.plugin, context.localization,
                mock(TimeParser.class));

        assertEquals(List.of("s:survival"),
                command.handleTabComplete(context.source, "add", "Lorias_", "12", "s:su"),
                "Expected modify short server flag values from the cached scope names");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void modifyTabCompletionUsesCachedWorldCandidatesForShortFlagsWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        context.scopeCache.replaceStoredNames(Set.of("survival", "creative"),
                Map.of("survival", Set.of("world", "world_nether")));
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);
        when(context.server.getLiveWorldNames(Optional.of("survival"), Optional.empty())).thenReturn(List.of());

        final LoriTimeModifyCommand command = new LoriTimeModifyCommand(context.plugin, context.localization,
                mock(TimeParser.class));

        assertEquals(List.of("w:world", "w:world_nether"),
                command.handleTabComplete(context.source, "add", "Lorias_", "12", "s:survival", "w:wo"),
                "Expected modify short world flag values from the cached scope names");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void modifyTabCompletionDoesNotSuggestDuplicateScopeFlags() {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of());
        when(context.plugin.getRecentPlayerSuggestionCache()).thenReturn(null);
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[0]);

        final List<String> completions = new LoriTimeModifyCommand(context.plugin, context.localization,
                mock(TimeParser.class)).handleTabComplete(context.source, "reset", "Lorias_", "s:survival", "s");

        assertEquals(List.of(), completions, "Expected duplicate modify server flag to stay unsuggested");
    }

    @Test
    void loriTimeTabCompletionUsesRecentPlayerSuggestionCache() throws StorageException {
        final CompletionContext context = new CompletionContext();
        final RecentPlayerSuggestionCache cache = new RecentPlayerSuggestionCache();
        cache.replaceRecentIdentities(List.of(new RecentPlayerIdentity(PLAYER_ID, "Lorias_", Optional.empty())));
        when(context.plugin.getRecentPlayerSuggestionCache()).thenReturn(cache);
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[0]);
        when(context.source.hasPermission("loritime.see.other")).thenReturn(true);

        final List<String> completions = new LoriTimeCommand(context.plugin, context.localization).handleTabComplete(context.source, "L");

        assertEquals(List.of("Lorias_"), completions, "Expected tab completion to use the recent player cache");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @ParameterizedTest
    @MethodSource("longScopePrefixCompletionArguments")
    void loriTimeTabCompletionSuggestsOnlyLongScopePrefixes(final String argument,
                                                            final List<String> expected,
                                                            final String message) {
        final CompletionContext context = new CompletionContext();
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of());
        when(context.plugin.getRecentPlayerSuggestionCache()).thenReturn(null);
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[0]);
        when(context.source.hasPermission("loritime.see.server")).thenReturn(true);
        when(context.source.hasPermission("loritime.see.world")).thenReturn(true);

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(expected, command.handleTabComplete(context.source, argument), message);
    }

    @Test
    void loriTimeTabCompletionUsesLiveServerCandidatesWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.see.server")).thenReturn(true);
        when(context.server.getLiveServerNames()).thenReturn(List.of("survival", "creative"));

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of("server:survival"), command.handleTabComplete(context.source, "server:su"),
                "Expected server flag value completion from live runtime context");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void loriTimeTabCompletionUsesLiveWorldCandidatesWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.see.world")).thenReturn(true);
        when(context.server.getLiveWorldNames(Optional.of("survival"), Optional.empty()))
                .thenReturn(List.of("world", "world_nether"));

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of("world:world", "world:world_nether"),
                command.handleTabComplete(context.source, "server:survival", "world:wo"),
                "Expected world flag value completion from live runtime context");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void loriTimeTabCompletionUsesCachedServerCandidatesForShortFlags() throws StorageException {
        final CompletionContext context = new CompletionContext();
        context.scopeCache.replaceStoredNames(Set.of("survival", "creative"),
                Map.of("survival", Set.of("world", "world_nether")));
        when(context.source.hasPermission("loritime.see.server")).thenReturn(true);
        when(context.server.getLiveServerNames()).thenReturn(List.of());

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of("s:survival"), command.handleTabComplete(context.source, "s:su"),
                "Expected short server flag values from the cached scope names");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void loriTimeTabCompletionUsesCachedWorldCandidatesForShortFlags() throws StorageException {
        final CompletionContext context = new CompletionContext();
        context.scopeCache.replaceStoredNames(Set.of("survival", "creative"),
                Map.of("survival", Set.of("world", "world_nether")));
        when(context.source.hasPermission("loritime.see.world")).thenReturn(true);
        when(context.server.getLiveWorldNames(Optional.of("survival"), Optional.empty())).thenReturn(List.of());

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of("w:world", "w:world_nether"), command.handleTabComplete(context.source, "s:survival", "w:wo"),
                "Expected short world flag values from the cached scope names");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void loriTimeTabCompletionHidesOtherPlayerServerPrefixWithoutOtherPermission() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.see.server")).thenReturn(true);

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of(), command.handleTabComplete(context.source, "Lorias_", "s"),
                "Expected other-player server prefix to require other server permission");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void loriTimeTabCompletionHidesOtherPlayerServerValuesWithoutOtherPermission() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.see.server")).thenReturn(true);
        when(context.server.getLiveServerNames()).thenReturn(List.of("survival", "creative"));

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of(), command.handleTabComplete(context.source, "Lorias_", "server:su"),
                "Expected other-player server values to require other server permission");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void loriTimeTabCompletionSuggestsOtherPlayerServerScopeWithOtherPermission() {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.see.server.other")).thenReturn(true);
        when(context.server.getLiveServerNames()).thenReturn(List.of("survival", "creative"));

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of("server:"), command.handleTabComplete(context.source, "Lorias_", "s"),
                "Expected other-player server prefix with other server permission");
        assertEquals(List.of("server:survival"), command.handleTabComplete(context.source, "Lorias_", "server:su"),
                "Expected other-player server values with other server permission");
    }

    @Test
    void loriTimeTabCompletionHidesOtherPlayerWorldPrefixWithoutOtherPermission() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.see.world")).thenReturn(true);

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of(), command.handleTabComplete(context.source, "Lorias_", "w"),
                "Expected other-player world prefix to require other world permission");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void loriTimeTabCompletionHidesOtherPlayerWorldValuesWithoutOtherPermission() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.see.world")).thenReturn(true);
        when(context.server.getLiveWorldNames(Optional.of("survival"), Optional.empty()))
                .thenReturn(List.of("world", "world_nether"));

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of(), command.handleTabComplete(context.source, "Lorias_", "server:survival", "world:wo"),
                "Expected other-player world values to require other world permission");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void loriTimeTabCompletionSuggestsOtherPlayerWorldScopeWithOtherPermission() {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.see.world.other")).thenReturn(true);
        when(context.server.getLiveWorldNames(Optional.of("survival"), Optional.empty()))
                .thenReturn(List.of("world", "world_nether"));

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of("world:"), command.handleTabComplete(context.source, "Lorias_", "w"),
                "Expected other-player world prefix with other world permission");
        assertEquals(List.of("world:world", "world:world_nether"),
                command.handleTabComplete(context.source, "Lorias_", "server:survival", "world:wo"),
                "Expected other-player world values with other world permission");
    }

    @Test
    void loriTimeTabCompletionSuggestsLongTimePrefixOnly() {
        final CompletionContext context = new CompletionContext();
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of());
        when(context.plugin.getRecentPlayerSuggestionCache()).thenReturn(null);
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[0]);
        when(context.source.hasPermission("loritime.see.timerange")).thenReturn(true);

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of("time:"), command.handleTabComplete(context.source, "t"),
                "Expected long time prefix completion");
    }

    @Test
    void loriTimeTabCompletionDoesNotSuggestTimePrefixWithoutTimeRangePermission() {
        final CompletionContext context = new CompletionContext();
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of());
        when(context.plugin.getRecentPlayerSuggestionCache()).thenReturn(null);
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[0]);

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of(), command.handleTabComplete(context.source, "t"),
                "Expected time prefix to require ranged lookup permission");
    }

    @Test
    void loriTimeTabCompletionUsesOtherTimeRangePermissionForOtherPlayerLookup() {
        final CompletionContext context = new CompletionContext();
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of());
        when(context.plugin.getRecentPlayerSuggestionCache()).thenReturn(null);
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[0]);
        when(context.source.hasPermission("loritime.see.timerange")).thenReturn(true);

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of(), command.handleTabComplete(context.source, "Lorias_", "t"),
                "Expected other-player time range prefix to ignore self ranged permission");

        when(context.source.hasPermission("loritime.see.timerange.other")).thenReturn(true);
        assertEquals(List.of("time:"), command.handleTabComplete(context.source, "Lorias_", "t"),
                "Expected other-player time range prefix with other ranged permission");
    }

    @Test
    void loriTimeTabCompletionDoesNotSuggestTimeValuesOrDuplicateTimeFlag() {
        final CompletionContext context = new CompletionContext();
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of());
        when(context.plugin.getRecentPlayerSuggestionCache()).thenReturn(null);
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[0]);

        final LoriTimeCommand command = new LoriTimeCommand(context.plugin, context.localization);

        assertEquals(List.of(), command.handleTabComplete(context.source, "time:3"),
                "Expected no custom time range value suggestions");
        assertEquals(List.of(), command.handleTabComplete(context.source, "t:3d", "t"),
                "Expected no duplicate time flag suggestion");
    }

    @Test
    void modifyTabCompletionKeepsScopePrefixSuggestionsWithoutReadPermissions() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);

        final LoriTimeModifyCommand command = new LoriTimeModifyCommand(context.plugin, context.localization,
                mock(TimeParser.class));

        assertEquals(List.of("server:"), command.handleTabComplete(context.source, "add", "Lorias_", "12", "s"),
                "Expected admin modify prefix completion without read scope permissions");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void modifyTabCompletionKeepsScopeValueSuggestionsWithoutReadPermissions() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);
        when(context.server.getLiveServerNames()).thenReturn(List.of("survival", "creative"));

        final LoriTimeModifyCommand command = new LoriTimeModifyCommand(context.plugin, context.localization,
                mock(TimeParser.class));

        assertEquals(List.of("server:survival"), command.handleTabComplete(context.source, "add", "Lorias_", "12", "server:su"),
                "Expected admin modify value completion without read scope permissions");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void adminTransferCompletionUsesCachedPlayerNamesWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of("Lorias_"));
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[0]);

        final List<String> completions = new LoriTimeAdminCommand(context.plugin, context.localization)
                .handleTabComplete(context.source, "transfer", "L");

        assertEquals(List.of("Lorias_"), completions, "Expected transfer player completion from cache");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void adminTransferCompletionUsesLiveAndCachedScopeCandidatesWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        context.scopeCache.replaceStoredNames(Set.of("cached-survival"),
                Map.of("cached-survival", Set.of("cached-world")));
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);
        when(context.source.getUniqueId()).thenReturn(PLAYER_ID);
        when(context.server.getLiveServerNames()).thenReturn(List.of("survival", "creative"));
        when(context.server.getCurrentServer(PLAYER_ID)).thenReturn(Optional.of("cached-survival"));
        when(context.server.getLiveWorldNames(Optional.of("cached-survival"), Optional.of(PLAYER_ID))).thenReturn(List.of());

        final LoriTimeAdminCommand command = new LoriTimeAdminCommand(context.plugin, context.localization);

        assertEquals(List.of("server:survival"),
                command.handleTabComplete(context.source, "transfer", "Lorias_", "server:su"),
                "Expected source server values from live runtime context");
        assertEquals(List.of("to-server:survival"),
                command.handleTabComplete(context.source, "transfer", "Lorias_", "to-server:su"),
                "Expected target server values from live runtime context");
        assertEquals(List.of("to-world:cached-world"),
                command.handleTabComplete(context.source, "transfer", "Lorias_", "to-world:cached"),
                "Expected target world values from cached scope names");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void adminTransferCompletionSuggestsMissingFlagsAndConfirm() {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);

        final LoriTimeAdminCommand command = new LoriTimeAdminCommand(context.plugin, context.localization);

        assertEquals(List.of("world:"),
                command.handleTabComplete(context.source, "transfer", "Lorias_", "server:survival", "w"),
                "Expected remaining transfer flags matching the typed prefix");
        assertEquals(List.of("to-server:", "to-world:"),
                command.handleTabComplete(context.source, "transfer", "Lorias_", "server:survival", "to"),
                "Expected target transfer flags matching the typed prefix");
        assertEquals(List.of(),
                command.handleTabComplete(context.source, "transfer", "Lorias_", "server:survival", "c"),
                "Expected confirm to stay a top-level admin subcommand");
        assertEquals(List.of(),
                command.handleTabComplete(context.source, "transfer", "Lorias_", "time:3d", "time"),
                "Expected duplicate time flag to stay unsuggested");
    }

    @Test
    void adminDeleteHistoryCompletionUsesCachedPlayerAndScopeCandidatesWithoutStorageLookup() throws StorageException {
        final CompletionContext context = new CompletionContext();
        context.scopeCache.replaceStoredNames(Set.of("cached-survival"),
                Map.of("survival", Set.of("cached-world")));
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);
        when(context.plugin.getKnownPlayerNames()).thenReturn(Set.of("Lorias_"));
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[0]);
        when(context.server.getLiveServerNames()).thenReturn(List.of("survival"));
        when(context.server.getLiveWorldNames(Optional.of("survival"), Optional.empty())).thenReturn(List.of());

        final LoriTimeAdminCommand command = new LoriTimeAdminCommand(context.plugin, context.localization);

        assertEquals(List.of("Lorias_"), command.handleTabComplete(context.source, "deleteHistory", "L"),
                "Expected deleteHistory player completion from cache");
        assertEquals(List.of("server:survival"),
                command.handleTabComplete(context.source, "deleteHistory", "Lorias_", "server:su"),
                "Expected deleteHistory server values from live runtime context");
        assertEquals(List.of("world:cached-world"),
                command.handleTabComplete(context.source, "deleteHistory", "Lorias_", "server:survival", "world:cached"),
                "Expected deleteHistory world values from cached scope names");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void adminDeleteHistoryCompletionSuggestsMissingFlags() {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);

        final LoriTimeAdminCommand command = new LoriTimeAdminCommand(context.plugin, context.localization);

        assertEquals(List.of("world:"),
                command.handleTabComplete(context.source, "deleteHistory", "Lorias_", "server:survival", "w"),
                "Expected remaining deleteHistory flags matching the typed prefix");
        assertEquals(List.of(),
                command.handleTabComplete(context.source, "deleteHistory", "Lorias_", "time:3d", "time"),
                "Expected duplicate time flag to stay unsuggested");
    }

    @Test
    void adminDeleteHistoryWorldCompletionDefaultsToExecutorCurrentServer() throws StorageException {
        final CompletionContext context = new CompletionContext();
        context.scopeCache.replaceStoredNames(Set.of("survival", "creative"),
                Map.of("survival", Set.of("world"), "creative", Set.of("world", "plots")));
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);
        when(context.source.getUniqueId()).thenReturn(PLAYER_ID);
        when(context.server.getCurrentServer(PLAYER_ID)).thenReturn(Optional.of("creative"));
        when(context.server.getLiveWorldNames(Optional.of("creative"), Optional.of(PLAYER_ID))).thenReturn(List.of());

        final LoriTimeAdminCommand command = new LoriTimeAdminCommand(context.plugin, context.localization);

        assertEquals(List.of("world:world", "world:plots"),
                command.handleTabComplete(context.source, "deleteHistory", "world:"),
                "Expected world completion to use the executor's current server when no server flag is present");
        verifyNoSuggestionStorageLookup(context.storage);
    }

    @Test
    void adminTabCompletionSuggestsConfirmSubcommand() {
        final CompletionContext context = new CompletionContext();
        when(context.source.hasPermission("loritime.admin")).thenReturn(true);

        final List<String> completions = new LoriTimeAdminCommand(context.plugin, context.localization)
                .handleTabComplete(context.source, "c");

        assertEquals(List.of("confirm"), completions, "Expected top-level confirm completion");
    }

    @Test
    void afkTabCompletionDoesNotSuggestPlayerTargets() {
        final CompletionContext context = new CompletionContext();
        final CommonPlayerSender onlinePlayer = mock(CommonPlayerSender.class);
        when(context.server.getOnlinePlayers()).thenReturn(new CommonPlayerSender[]{onlinePlayer});
        when(onlinePlayer.getName()).thenReturn("OnlineUser");

        final LoriTimeAfkCommand command = new LoriTimeAfkCommand(context.plugin, context.localization);

        assertEquals(List.of(), command.handleTabComplete(context.source),
                "Expected AFK completion to stay empty for self-only command");
        assertEquals(List.of(), command.handleTabComplete(context.source, "O"),
                "Expected AFK completion not to suggest player targets");
    }

    private void verifyNoSuggestionStorageLookup(final UnifiedStorage storage) throws StorageException {
        verify(storage, never()).getNameEntries();
        verify(storage, never()).getRecentPlayerIdentities(anyLong());
        verify(storage, never()).getKnownServerNames();
        verify(storage, never()).getKnownWorldNames();
        verify(storage, never()).getKnownWorldNamesByServer();
    }

    private static final class CompletionContext {

        private final LoriTimePlugin plugin = mock(LoriTimePlugin.class);

        private final UnifiedStorage storage = mock(UnifiedStorage.class);

        private final Localization localization = mock(Localization.class);

        private final com.jannik_kuehn.common.config.Configuration config =
                mock(com.jannik_kuehn.common.config.Configuration.class);

        private final CommonServer server = mock(CommonServer.class);

        private final com.jannik_kuehn.common.scheduler.PluginScheduler scheduler =
                mock(com.jannik_kuehn.common.scheduler.PluginScheduler.class);

        private final CommonPlayerSender source = mock(CommonPlayerSender.class);

        private final ScopeSuggestionCache scopeCache = new ScopeSuggestionCache();

        private CompletionContext() {
            when(plugin.getLoggerFactory()).thenReturn(new LoggerFactory(Logger.getLogger("test")));
            when(plugin.getStorage()).thenReturn(storage);
            when(plugin.getLocalization()).thenReturn(localization);
            when(plugin.getConfig()).thenReturn(config);
            when(plugin.getScheduler()).thenReturn(scheduler);
            when(plugin.getServer()).thenReturn(server);
            when(plugin.getPlayerConverter()).thenReturn(mock(LoriTimePlayerConverter.class));
            when(plugin.getScopeSuggestionCache()).thenReturn(scopeCache);
            when(config.getBoolean(anyString())).thenReturn(false);
            when(source.getName()).thenReturn("SourcePlayer");
        }
    }
}
