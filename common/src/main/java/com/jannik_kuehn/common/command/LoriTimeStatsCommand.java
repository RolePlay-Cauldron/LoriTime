package com.jannik_kuehn.common.command;

import com.jannik_kuehn.common.LoriTimePlugin;
import com.jannik_kuehn.common.api.storage.TimeRange;
import com.jannik_kuehn.common.api.storage.TimeScope;
import com.jannik_kuehn.common.command.core.CommandScopes;
import com.jannik_kuehn.common.config.localization.Localization;
import com.jannik_kuehn.common.exception.StorageException;
import com.jannik_kuehn.common.platform.CommonCommand;
import com.jannik_kuehn.common.platform.CommonSender;
import com.jannik_kuehn.common.storage.model.SessionContextDefaults;
import com.jannik_kuehn.common.storage.model.StatisticsRequest;
import com.jannik_kuehn.common.storage.model.StatisticsSnapshot;
import com.jannik_kuehn.common.utils.TimeUtil;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;

/** Canonical bounded network statistics command. */
@SuppressWarnings({"PMD.CommentRequired", "PMD.CognitiveComplexity", "PMD.CyclomaticComplexity",
        "PMD.LiteralsFirstInComparisons", "PMD.AvoidLiteralsInIfCondition"})
public class LoriTimeStatsCommand implements CommonCommand {
    private static final long DEFAULT_RANGE_SECONDS = 86_400L;

    private static final long DEFAULT_BOUNCE_SECONDS = 180L;

    private static final List<String> VIEWS = List.of("users", "sessions", "usage", "top", "afk", "retention");

    private final LoriTimePlugin plugin;

    private final Localization localization;

    public LoriTimeStatsCommand(final LoriTimePlugin plugin, final Localization localization) {
        this.plugin = plugin;
        this.localization = localization;
    }

    @Override
    public void execute(final CommonSender sender, final String... arguments) {
        final String view = arguments.length > 0 && VIEWS.contains(arguments[0].toLowerCase(Locale.ROOT))
                ? arguments[0].toLowerCase(Locale.ROOT) : "overview";
        final String[] flags = view.equals("overview") ? arguments : Arrays.copyOfRange(arguments, 1, arguments.length);
        final long defaultRange = positiveDuration("stats.default-range", "1d", DEFAULT_RANGE_SECONDS);
        final long bounce = positiveDuration("stats.bounce-threshold", "3m", DEFAULT_BOUNCE_SECONDS);
        final CommandScopes.LookupRequest parsed = CommandScopes.parseLookup(plugin.getParser(), Clock.systemUTC(), flags);
        if (parsed == null || parsed.playerName() != null) {
            send(sender, "commandUsage", Map.of());
            return;
        }
        final TimeRange range = parsed.hasTimeRange() ? parsed.timeRange()
                : TimeRange.between(Instant.now().minusSeconds(defaultRange), Instant.now());
        final String server = parsed.serverName() == null && parsed.worldName() != null
                ? plugin.getConfig().getString("server.name", SessionContextDefaults.SERVER) : parsed.serverName();
        final TimeScope scope = parsed.worldName() != null ? TimeScope.world(server, parsed.worldName())
                : parsed.serverName() != null ? TimeScope.server(parsed.serverName()) : TimeScope.GLOBAL;
        plugin.getStatisticsStorage().ifPresentOrElse(storage -> {
            try {
                render(sender, view, storage.getStatistics(new StatisticsRequest(range, scope, Duration.ofSeconds(bounce))),
                        parsed.timeRangeInput() == null ? plugin.getConfig().getString("stats.default-range", "1d")
                                : parsed.timeRangeInput(), scope);
            } catch (final StorageException ex) {
                send(sender, "error", Map.of());
            }
        }, () -> send(sender, "unsupported", Map.of()));
    }

    private long positiveDuration(final String key, final String fallback, final long secondsFallback) {
        final OptionalLong parsed = plugin.getParser().parseToSeconds(plugin.getConfig().getString(key, fallback));
        return parsed.isPresent() && parsed.getAsLong() > 0L ? parsed.getAsLong() : secondsFallback;
    }

    private void render(final CommonSender sender, final String view, final StatisticsSnapshot snapshot,
                        final String range, final TimeScope scope) {
        final long totalUsageSeconds = snapshot.serverUsage().values().stream().mapToLong(Duration::getSeconds).sum();
        final String usage = snapshot.serverUsage().entrySet().stream().limit(10)
                .map(entry -> entry.getKey() + ": " + format(entry.getValue()) + " ("
                        + percent(totalUsageSeconds == 0L ? 0.0D
                        : (double) entry.getValue().getSeconds() / totalUsageSeconds) + ")")
                .reduce((left, right) -> left + "<newline>" + right).orElse("-");
        final String top = snapshot.topPlayers().stream().limit(10)
                .map(entry -> entry.name() + ": " + format(entry.duration()))
                .reduce((left, right) -> left + "<newline>" + right).orElse("-");
        send(sender, view, Map.ofEntries(
                Map.entry("range", range), Map.entry("scope", scopeLabel(scope)),
                Map.entry("unique", String.valueOf(snapshot.uniqueUsers())),
                Map.entry("new", String.valueOf(snapshot.newUsers())),
                Map.entry("sessions", String.valueOf(snapshot.sessions())),
                Map.entry("playtime", format(snapshot.totalPlayTime())),
                Map.entry("median", format(snapshot.medianSession())),
                Map.entry("longest", format(snapshot.longestSession())),
                Map.entry("peruser", String.format(Locale.ROOT, "%.2f", snapshot.sessionsPerUser())),
                Map.entry("bounces", String.valueOf(snapshot.bounces())),
                Map.entry("bounce", percent(snapshot.bounceRate())),
                Map.entry("peak", String.valueOf(snapshot.peakConcurrent())),
                Map.entry("afkplayers", String.valueOf(snapshot.afkPlayers())),
                Map.entry("afkkicks", String.valueOf(snapshot.afkKicks())),
                Map.entry("afktime", format(snapshot.afkDuration())),
                Map.entry("afkperiods", String.valueOf(snapshot.afkPeriods())),
                Map.entry("retention", percent(snapshot.retentionRate())),
                Map.entry("usage", usage), Map.entry("top", top)));
    }

    private String scopeLabel(final TimeScope scope) {
        return switch (scope.type()) {
            case GLOBAL -> "network";
            case SERVER -> scope.server();
            case WORLD -> scope.server() + "/" + scope.world();
        };
    }

    private String format(final Duration duration) {
        return TimeUtil.formatTime(duration.getSeconds(), localization);
    }

    private String percent(final double ratio) {
        return String.format(Locale.ROOT, "%.1f%%", ratio * 100.0D);
    }

    private void send(final CommonSender sender, final String key, final Map<String, String> replacements) {
        String message = localization.getRawMessage("message.command.stats." + key);
        for (final Map.Entry<String, String> replacement : replacements.entrySet()) {
            message = message.replace("[" + replacement.getKey() + "]", replacement.getValue());
        }
        sender.sendMessage(localization.formatTextComponentWithoutPrefix(message));
    }

    @Override
    public List<String> handleTabComplete(final CommonSender source, final String... args) {
        if (args.length <= 1) {
            return VIEWS.stream().filter(value -> args.length == 0 || value.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }
        return List.of("time:1d", "server:", "world:");
    }

    @Override
    public List<String> getAliases() {
        return List.of("stats", "loritimestats");
    }

    @Override
    public String getCommandName() {
        return "ltstats";
    }
}
