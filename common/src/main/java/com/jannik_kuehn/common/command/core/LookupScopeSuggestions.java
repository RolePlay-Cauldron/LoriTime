package com.jannik_kuehn.common.command.core;

import com.jannik_kuehn.common.platform.CommonSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Suggests scope and range flag prefixes for lookup commands.
 */
final class LookupScopeSuggestions {

    private LookupScopeSuggestions() {
    }

    /**
     * Suggests lookup flag prefixes.
     *
     * @param source                  command sender
     * @param argument                partially typed argument
     * @param includeTimeRange        true when time range flags should be suggested
     * @param requireScopePermissions true when read permissions should gate suggestions
     * @return matching flag prefix suggestions
     */
    /* default */ static List<String> suggest(final CommonSender source, final String argument,
                                              final boolean includeTimeRange,
                                              final boolean requireScopePermissions) {
        return suggest(source, argument, includeTimeRange, requireScopePermissions, true);
    }

    /* default */ static List<String> suggest(final CommonSender source, final String argument,
                                              final boolean includeTimeRange,
                                              final boolean requireScopePermissions,
                                              final boolean selfLookup) {
        final String lowerArgument = argument.toLowerCase(Locale.ROOT);
        final List<String> suggestions = new ArrayList<>();
        suggestServer(source, lowerArgument, requireScopePermissions, selfLookup, suggestions);
        suggestWorld(source, lowerArgument, requireScopePermissions, selfLookup, suggestions);
        suggestTimeRange(source, lowerArgument, includeTimeRange, requireScopePermissions, selfLookup, suggestions);
        return suggestions;
    }

    private static void suggestServer(final CommonSender source, final String lowerArgument,
                                      final boolean requireScopePermissions,
                                      final boolean selfLookup,
                                      final List<String> suggestions) {
        final String permission = selfLookup ? "loritime.see.server" : "loritime.see.server.other";
        if ((!requireScopePermissions || source.hasPermission(permission))
                && CommandScopes.SERVER_PREFIX.startsWith(lowerArgument)) {
            suggestions.add(CommandScopes.SERVER_PREFIX);
        }
    }

    private static void suggestWorld(final CommonSender source, final String lowerArgument,
                                     final boolean requireScopePermissions,
                                     final boolean selfLookup,
                                     final List<String> suggestions) {
        final String permission = selfLookup ? "loritime.see.world" : "loritime.see.world.other";
        if ((!requireScopePermissions || source.hasPermission(permission))
                && CommandScopes.WORLD_PREFIX.startsWith(lowerArgument)) {
            suggestions.add(CommandScopes.WORLD_PREFIX);
        }
    }

    private static void suggestTimeRange(final CommonSender source, final String lowerArgument,
                                         final boolean includeTimeRange,
                                         final boolean requireScopePermissions,
                                         final boolean selfLookup,
                                         final List<String> suggestions) {
        if (includeTimeRange && canSuggestTimeRange(source, requireScopePermissions, selfLookup)
                && CommandScopes.TIME_PREFIX.startsWith(lowerArgument)) {
            suggestions.add(CommandScopes.TIME_PREFIX);
        }
    }

    private static boolean canSuggestTimeRange(final CommonSender source,
                                               final boolean requireScopePermissions,
                                               final boolean selfLookup) {
        final String permission = selfLookup ? "loritime.see.timerange" : "loritime.see.timerange.other";
        return !requireScopePermissions
                || source.hasPermission(permission);
    }
}
