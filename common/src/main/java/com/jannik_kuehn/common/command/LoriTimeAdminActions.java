package com.jannik_kuehn.common.command;

import com.github.roleplaycauldron.spellbook.core.logger.WrappedLogger;
import com.jannik_kuehn.common.LoriTimePlugin;
import com.jannik_kuehn.common.api.storage.TimeRange;
import com.jannik_kuehn.common.command.core.CommandCompletions;
import com.jannik_kuehn.common.command.core.CommandMessages;
import com.jannik_kuehn.common.command.core.CommandScopes;
import com.jannik_kuehn.common.command.core.PlayerNameCompletions;
import com.jannik_kuehn.common.config.localization.Localization;
import com.jannik_kuehn.common.exception.StorageException;
import com.jannik_kuehn.common.platform.CommonPlayerSender;
import com.jannik_kuehn.common.platform.CommonSender;
import com.jannik_kuehn.common.platform.CommonServer;
import com.jannik_kuehn.common.scheduler.PluginTask;
import com.jannik_kuehn.common.storage.contract.AdminStorageMaintenance;
import com.jannik_kuehn.common.storage.model.PlayerStorageTransferRequest;
import com.jannik_kuehn.common.storage.model.StorageDeleteRequest;
import com.jannik_kuehn.common.storage.model.StorageMaintenancePreview;
import com.jannik_kuehn.common.storage.model.StorageMaintenanceResult;
import com.jannik_kuehn.common.storage.model.StorageMaintenanceScope;
import com.jannik_kuehn.common.storage.model.StorageTransferMapping;
import com.jannik_kuehn.common.storage.model.StorageTransferRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Handles runtime administration subcommands.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.CouplingBetweenObjects",
        "PMD.CyclomaticComplexity", "PMD.AvoidCatchingGenericException", "PMD.CognitiveComplexity",
        "PMD.NcssCount", "PMD.NPathComplexity", "PMD.AvoidLiteralsInIfCondition", "PMD.PrematureDeclaration",
        "PMD.AvoidDuplicateLiterals"})
final class LoriTimeAdminActions {

    /**
     * Path to the debug configuration option.
     */
    private static final String DEBUG_CONFIG_PATH = "general.debug";

    /**
     * Command token that confirms transfer application.
     */
    private static final String CONFIRM_TOKEN = "confirm";

    /**
     * Command suggested by clickable confirmation messages.
     */
    private static final String CONFIRM_COMMAND = "/lta confirm";

    /**
     * Time in seconds before a pending admin action expires.
     */
    private static final long CONFIRM_TIMEOUT_SECONDS = 15L;

    /**
     * Target server flag.
     */
    private static final String TARGET_SERVER_PREFIX = "to-server:";

    /**
     * Target world flag.
     */
    private static final String TARGET_WORLD_PREFIX = "to-world:";

    /**
     * Target server short flag.
     */
    private static final String SHORT_TARGET_SERVER_PREFIX = "ts:";

    /**
     * Target world short flag.
     */
    private static final String SHORT_TARGET_WORLD_PREFIX = "tw:";

    /**
     * LoriTime plugin instance.
     */
    private final LoriTimePlugin plugin;

    /**
     * Localization provider.
     */
    private final Localization localization;

    /**
     * Usage callback for invalid arguments.
     */
    private final Consumer<CommonSender> usage;

    /**
     * Logger for admin subcommand diagnostics.
     */
    private final WrappedLogger log;

    /**
     * Pending confirmation actions by sender name.
     */
    private final Map<String, PendingAdminAction> pendingConfirmations;

    /**
     * Whether debug mode is enabled.
     */
    private boolean isDebugging;

    /**
     * Task that automatically disables debug mode after a certain time.
     */
    private PluginTask autoDisableTask;

    /**
     * Creates admin subcommand actions.
     *
     * @param plugin       LoriTime plugin runtime
     * @param localization localization provider
     * @param usage        usage callback for invalid arguments
     */
    /* default */ LoriTimeAdminActions(final LoriTimePlugin plugin, final Localization localization,
                                       final Consumer<CommonSender> usage) {
        this.plugin = plugin;
        this.localization = localization;
        this.usage = usage;
        this.log = plugin.getLoggerFactory().create(LoriTimeAdminCommand.class, "LoriTimeAdminCommand");
        this.isDebugging = plugin.getConfig().getBoolean(DEBUG_CONFIG_PATH);
        this.pendingConfirmations = new ConcurrentHashMap<>();
        plugin.getScheduler().runAsyncOnce(this::autoDisableCheck);
    }

    /**
     * Reloads runtime configuration.
     *
     * @param sender command sender
     * @param args   subcommand arguments
     */
    /* default */ void reload(final CommonSender sender, final String... args) {
        if (hasUnexpectedArgs(sender, args)) {
            return;
        }
        plugin.reload();
        CommandMessages.send(localization, plugin.getLanguageSelector(), sender, "message.command.loritimeadmin.reload.success");
    }

    /**
     * Toggles debug mode.
     *
     * @param sender command sender
     * @param args   subcommand arguments
     */
    /* default */ void debug(final CommonSender sender, final String... args) {
        if (hasUnexpectedArgs(sender, args)) {
            return;
        }
        changeDebugMode(sender);
        autoDisableCheck();
    }

    /**
     * Sends version information to the command sender.
     *
     * @param sender command sender
     * @param args   subcommand arguments
     */
    /* default */ void info(final CommonSender sender, final String... args) {
        if (hasUnexpectedArgs(sender, args)) {
            return;
        }
        final MiniMessage miniMessage = MiniMessage.builder().build();
        final String serverVersion = "<#A4A4A4>Server version: <#FF3232>" + plugin.getServer().getServerVersion();
        final String pluginVersion = "<#A4A4A4>Plugin version: <#FF3232>" + plugin.getServer().getPluginVersion();

        sender.sendMessage(localization.formatTextComponent("Version Information"));
        sender.sendMessage("");
        sender.sendMessage(miniMessage.deserialize(serverVersion));
        sender.sendMessage(miniMessage.deserialize(pluginVersion));
    }

    /**
     * Runs the updater when an update is available.
     *
     * @param sender command sender
     * @param args   subcommand arguments
     */
    /* default */ void update(final CommonSender sender, final String... args) {
        if (hasUnexpectedArgs(sender, args)) {
            return;
        }
        if (plugin.getUpdater() == null || !plugin.getUpdater().isUpdateAvailable()) {
            sender.sendMessage(localization.formatTextComponent(localization.getRawMessage("message.updater.notFound")));
            return;
        }
        plugin.getUpdater().update(sender);
    }

    /**
     * Previews or applies a player-scoped storage transfer.
     *
     * @param sender command sender
     * @param args   subcommand arguments
     */
    /* default */ void transfer(final CommonSender sender, final String... args) {
        final ParsedTransfer parsed = parseTransfer(args);
        if (parsed == null) {
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.command.loritimeadmin.transfer.usage");
            return;
        }
        if (!plugin.ownsCanonicalStorage()) {
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.storageMaintenance.unsupported");
            return;
        }
        final Optional<AdminStorageMaintenance> optionalMaintenance = plugin.getAdminStorageMaintenance();
        if (optionalMaintenance.isEmpty()) {
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.storageMaintenance.unsupported");
            return;
        }
        try {
            final AdminStorageMaintenance maintenance = optionalMaintenance.get();
            if (parsed.hasPlayer()) {
                previewPlayerTransfer(sender, parsed, maintenance);
                return;
            }
            previewAllPlayerTransfer(sender, parsed, maintenance);
        } catch (final StorageException | RuntimeException ex) {
            log.error("Storage transfer failed.", ex);
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.command.loritimeadmin.transfer.failure");
        }
    }

    /**
     * Previews a scoped storage history delete.
     *
     * @param sender command sender
     * @param args   subcommand arguments
     */
    /* default */ void deleteHistory(final CommonSender sender, final String... args) {
        final Optional<String> defaultServer = resolveDefaultServer(sender);
        final ParsedDeleteHistory parsed = parseDeleteHistory(defaultServer, args);
        if (parsed == null) {
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.command.loritimeadmin.deleteHistory.usage");
            return;
        }
        if (!plugin.ownsCanonicalStorage()) {
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.storageMaintenance.unsupported");
            return;
        }
        final Optional<AdminStorageMaintenance> optionalMaintenance = plugin.getAdminStorageMaintenance();
        if (optionalMaintenance.isEmpty()) {
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.storageMaintenance.unsupported");
            return;
        }
        try {
            final StorageDeleteRequest request = parsed.toRequest(resolvePlayer(sender, parsed), defaultServer);
            if (request == null) {
                return;
            }
            final AdminStorageMaintenance maintenance = optionalMaintenance.get();
            final StorageMaintenancePreview preview = maintenance.previewDelete(request);
            queuePendingAction(sender, "deleteHistory", () -> {
                final StorageMaintenanceResult result = maintenance.applyDelete(request, preview.confirmation());
                sendDeleteHistorySuccess(sender, result);
            });
            sendDeleteHistoryPreview(sender, preview);
        } catch (final StorageException | RuntimeException ex) {
            log.error("Storage deleteHistory failed.", ex);
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.command.loritimeadmin.deleteHistory.failure");
        }
    }

    private Optional<UUID> resolvePlayer(final CommonSender sender, final ParsedDeleteHistory parsed)
            throws StorageException {
        if (!parsed.hasPlayer()) {
            return Optional.empty();
        }
        final Optional<UUID> optionalPlayer = plugin.getStorage().getUuid(parsed.playerName());
        if (optionalPlayer.isEmpty()) {
            sender.sendMessage(localization.formatTextComponent(localization
                    .getRawMessage("message.command.loritimeadmin.missingUuid")
                    .replace("[player]", parsed.playerName())));
        }
        return optionalPlayer;
    }

    private void previewPlayerTransfer(final CommonSender sender,
                                       final ParsedTransfer parsed,
                                       final AdminStorageMaintenance maintenance) throws StorageException {
        final Optional<UUID> optionalPlayer = plugin.getStorage().getUuid(parsed.playerName());
        if (optionalPlayer.isEmpty()) {
            sender.sendMessage(localization.formatTextComponent(localization
                    .getRawMessage("message.command.loritimeadmin.missingUuid")
                    .replace("[player]", parsed.playerName())));
            return;
        }
        final PlayerStorageTransferRequest request = parsed.toPlayerRequest(optionalPlayer.get(),
                resolveDefaultServer(sender));
        if (request == null) {
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.command.loritimeadmin.transfer.usage");
            return;
        }
        final StorageMaintenancePreview preview = maintenance.previewPlayerTransfer(request);
        queuePendingAction(sender, "transfer", () -> {
            final StorageMaintenanceResult result = maintenance.applyPlayerTransfer(request, preview.confirmation());
            sendTransferSuccess(sender, result);
        });
        sendTransferPreview(sender, preview);
    }

    private void previewAllPlayerTransfer(final CommonSender sender,
                                          final ParsedTransfer parsed,
                                          final AdminStorageMaintenance maintenance) throws StorageException {
        final StorageTransferRequest request = parsed.toStorageRequest(resolveDefaultServer(sender));
        if (request == null) {
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.command.loritimeadmin.transfer.usage");
            return;
        }
        final StorageMaintenancePreview preview = maintenance.previewTransfer(request);
        queuePendingAction(sender, "transfer", () -> {
            final StorageMaintenanceResult result = maintenance.applyTransfer(request, preview.confirmation());
            sendTransferSuccess(sender, result);
        });
        sendTransferPreview(sender, preview);
    }

    /**
     * Applies the pending administrative action for a sender.
     *
     * @param sender command sender
     * @param args   subcommand arguments
     */
    /* default */ void confirm(final CommonSender sender, final String... args) {
        if (hasUnexpectedArgs(sender, args)) {
            return;
        }
        final PendingAdminAction pending = pendingConfirmations.remove(senderKey(sender));
        if (pending == null) {
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.command.loritimeadmin.confirm.none");
            return;
        }
        if (pending.timeoutTask() != null) {
            pending.timeoutTask().cancel();
        }
        if (System.currentTimeMillis() > pending.expiresAtMillis()) {
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.command.loritimeadmin.confirm.expired");
            return;
        }
        try {
            pending.action().run();
        } catch (final StorageException | RuntimeException ex) {
            log.error("Confirmed admin action failed.", ex);
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender,
                    "message.command.loritimeadmin.confirm.failure");
        }
    }

    /**
     * Completes transfer command arguments.
     *
     * @param sender command sender
     * @param args   current transfer arguments
     * @return completions
     */
    /* default */ List<String> completeTransfer(final CommonSender sender, final String... args) {
        if (!sender.hasPermission("loritime.admin")) {
            return List.of();
        }
        final String argument = args.length == 0 ? "" : args[args.length - 1];
        final String lowerArgument = argument.toLowerCase(Locale.ROOT);
        if (args.length <= 1 && !argument.contains(":")) {
            return PlayerNameCompletions.suggest(plugin, argument);
        }
        if (lowerArgument.startsWith(CommandScopes.SERVER_PREFIX)) {
            return prefixedScopeValues(sender, CommandScopes.SERVER_PREFIX, argument, true, false, args);
        }
        if (lowerArgument.startsWith(CommandScopes.SHORT_SERVER_PREFIX)) {
            return prefixedScopeValues(sender, CommandScopes.SHORT_SERVER_PREFIX, argument, true, false, args);
        }
        if (lowerArgument.startsWith(CommandScopes.WORLD_PREFIX)) {
            return prefixedScopeValues(sender, CommandScopes.WORLD_PREFIX, argument, false, false, args);
        }
        if (lowerArgument.startsWith(CommandScopes.SHORT_WORLD_PREFIX)) {
            return prefixedScopeValues(sender, CommandScopes.SHORT_WORLD_PREFIX, argument, false, false, args);
        }
        if (lowerArgument.startsWith(TARGET_SERVER_PREFIX)) {
            return prefixedScopeValues(sender, TARGET_SERVER_PREFIX, argument, true, true, args);
        }
        if (lowerArgument.startsWith(SHORT_TARGET_SERVER_PREFIX)) {
            return prefixedScopeValues(sender, SHORT_TARGET_SERVER_PREFIX, argument, true, true, args);
        }
        if (lowerArgument.startsWith(TARGET_WORLD_PREFIX)) {
            return prefixedScopeValues(sender, TARGET_WORLD_PREFIX, argument, false, true, args);
        }
        if (lowerArgument.startsWith(SHORT_TARGET_WORLD_PREFIX)) {
            return prefixedScopeValues(sender, SHORT_TARGET_WORLD_PREFIX, argument, false, true, args);
        }
        if (lowerArgument.startsWith(CommandScopes.TIME_PREFIX) || lowerArgument.startsWith(CommandScopes.SHORT_TIME_PREFIX)) {
            return List.of();
        }
        final List<String> suggestions = new ArrayList<>();
        final List<String> previousArgs = args.length == 0 ? List.of() : Arrays.asList(Arrays.copyOf(args, args.length - 1));
        addMissingFlagSuggestion(suggestions, previousArgs, CommandScopes.SERVER_PREFIX);
        addMissingFlagSuggestion(suggestions, previousArgs, CommandScopes.WORLD_PREFIX);
        addMissingFlagSuggestion(suggestions, previousArgs, TARGET_SERVER_PREFIX);
        addMissingFlagSuggestion(suggestions, previousArgs, TARGET_WORLD_PREFIX);
        addMissingFlagSuggestion(suggestions, previousArgs, CommandScopes.TIME_PREFIX);
        return CommandCompletions.startsWith(suggestions, argument);
    }

    /**
     * Completes deleteHistory command arguments.
     *
     * @param sender command sender
     * @param args   current deleteHistory arguments
     * @return completions
     */
    /* default */ List<String> completeDeleteHistory(final CommonSender sender, final String... args) {
        if (!sender.hasPermission("loritime.admin")) {
            return List.of();
        }
        final String argument = args.length == 0 ? "" : args[args.length - 1];
        final String lowerArgument = argument.toLowerCase(Locale.ROOT);
        if (args.length <= 1 && !argument.contains(":")) {
            return PlayerNameCompletions.suggest(plugin, argument);
        }
        if (lowerArgument.startsWith(CommandScopes.SERVER_PREFIX)) {
            return prefixedScopeValues(sender, CommandScopes.SERVER_PREFIX, argument, true, false, args);
        }
        if (lowerArgument.startsWith(CommandScopes.SHORT_SERVER_PREFIX)) {
            return prefixedScopeValues(sender, CommandScopes.SHORT_SERVER_PREFIX, argument, true, false, args);
        }
        if (lowerArgument.startsWith(CommandScopes.WORLD_PREFIX)) {
            return prefixedScopeValues(sender, CommandScopes.WORLD_PREFIX, argument, false, false, args);
        }
        if (lowerArgument.startsWith(CommandScopes.SHORT_WORLD_PREFIX)) {
            return prefixedScopeValues(sender, CommandScopes.SHORT_WORLD_PREFIX, argument, false, false, args);
        }
        if (lowerArgument.startsWith(CommandScopes.TIME_PREFIX) || lowerArgument.startsWith(CommandScopes.SHORT_TIME_PREFIX)) {
            return List.of();
        }
        final List<String> suggestions = new ArrayList<>();
        final List<String> previousArgs = args.length == 0 ? List.of() : Arrays.asList(Arrays.copyOf(args, args.length - 1));
        addMissingFlagSuggestion(suggestions, previousArgs, CommandScopes.SERVER_PREFIX);
        addMissingFlagSuggestion(suggestions, previousArgs, CommandScopes.WORLD_PREFIX);
        addMissingFlagSuggestion(suggestions, previousArgs, CommandScopes.TIME_PREFIX);
        return CommandCompletions.startsWith(suggestions, argument);
    }

    private ParsedDeleteHistory parseDeleteHistory(final Optional<String> defaultServer, final String... args) {
        if (args.length < 1) {
            return null;
        }
        String playerName = null;
        String sourceServer = null;
        String sourceWorld = null;
        String timeRangeInput = null;
        for (final String argument : args) {
            final String lowerArgument = argument.toLowerCase(Locale.ROOT);
            if (CONFIRM_TOKEN.equalsIgnoreCase(argument)) {
                return null;
            } else if (lowerArgument.startsWith(CommandScopes.SERVER_PREFIX)) {
                sourceServer = assignOnce(sourceServer, argument, CommandScopes.SERVER_PREFIX.length());
                if (sourceServer == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(CommandScopes.SHORT_SERVER_PREFIX)) {
                sourceServer = assignOnce(sourceServer, argument, CommandScopes.SHORT_SERVER_PREFIX.length());
                if (sourceServer == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(CommandScopes.WORLD_PREFIX)) {
                sourceWorld = assignOnce(sourceWorld, argument, CommandScopes.WORLD_PREFIX.length());
                if (sourceWorld == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(CommandScopes.SHORT_WORLD_PREFIX)) {
                sourceWorld = assignOnce(sourceWorld, argument, CommandScopes.SHORT_WORLD_PREFIX.length());
                if (sourceWorld == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(CommandScopes.TIME_PREFIX)) {
                timeRangeInput = assignOnce(timeRangeInput, argument, CommandScopes.TIME_PREFIX.length());
                if (timeRangeInput == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(CommandScopes.SHORT_TIME_PREFIX)) {
                timeRangeInput = assignOnce(timeRangeInput, argument, CommandScopes.SHORT_TIME_PREFIX.length());
                if (timeRangeInput == null) {
                    return null;
                }
            } else if (!argument.contains(":") && playerName == null) {
                playerName = argument;
            } else {
                return null;
            }
        }
        if (sourceServer == null && defaultServer.isEmpty()) {
            return null;
        }
        final Optional<TimeRange> timeRange = timeRangeInput == null
                ? Optional.empty()
                : parseTimeRange(timeRangeInput);
        if (timeRangeInput != null && timeRange.isEmpty()) {
            return null;
        }
        return new ParsedDeleteHistory(playerName, sourceServer, sourceWorld,
                timeRange, Optional.ofNullable(timeRangeInput));
    }

    private ParsedTransfer parseTransfer(final String... args) {
        if (args.length < 1) {
            return null;
        }
        String playerName = null;
        String sourceServer = null;
        String sourceWorld = null;
        String targetServer = null;
        String targetWorld = null;
        String timeRangeInput = null;
        for (final String argument : args) {
            final String lowerArgument = argument.toLowerCase(Locale.ROOT);
            if (CONFIRM_TOKEN.equalsIgnoreCase(argument)) {
                return null;
            } else if (lowerArgument.startsWith(CommandScopes.SERVER_PREFIX)) {
                sourceServer = assignOnce(sourceServer, argument, CommandScopes.SERVER_PREFIX.length());
                if (sourceServer == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(CommandScopes.SHORT_SERVER_PREFIX)) {
                sourceServer = assignOnce(sourceServer, argument, CommandScopes.SHORT_SERVER_PREFIX.length());
                if (sourceServer == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(CommandScopes.WORLD_PREFIX)) {
                sourceWorld = assignOnce(sourceWorld, argument, CommandScopes.WORLD_PREFIX.length());
                if (sourceWorld == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(CommandScopes.SHORT_WORLD_PREFIX)) {
                sourceWorld = assignOnce(sourceWorld, argument, CommandScopes.SHORT_WORLD_PREFIX.length());
                if (sourceWorld == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(TARGET_SERVER_PREFIX)) {
                targetServer = assignOnce(targetServer, argument, TARGET_SERVER_PREFIX.length());
                if (targetServer == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(SHORT_TARGET_SERVER_PREFIX)) {
                targetServer = assignOnce(targetServer, argument, SHORT_TARGET_SERVER_PREFIX.length());
                if (targetServer == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(TARGET_WORLD_PREFIX)) {
                targetWorld = assignOnce(targetWorld, argument, TARGET_WORLD_PREFIX.length());
                if (targetWorld == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(SHORT_TARGET_WORLD_PREFIX)) {
                targetWorld = assignOnce(targetWorld, argument, SHORT_TARGET_WORLD_PREFIX.length());
                if (targetWorld == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(CommandScopes.TIME_PREFIX)) {
                timeRangeInput = assignOnce(timeRangeInput, argument, CommandScopes.TIME_PREFIX.length());
                if (timeRangeInput == null) {
                    return null;
                }
            } else if (lowerArgument.startsWith(CommandScopes.SHORT_TIME_PREFIX)) {
                timeRangeInput = assignOnce(timeRangeInput, argument, CommandScopes.SHORT_TIME_PREFIX.length());
                if (timeRangeInput == null) {
                    return null;
                }
            } else if (!argument.contains(":") && playerName == null) {
                playerName = argument;
            } else {
                return null;
            }
        }
        if (playerName == null && timeRangeInput != null) {
            return null;
        }
        final Optional<TimeRange> timeRange = timeRangeInput == null
                ? Optional.empty()
                : parseTimeRange(timeRangeInput);
        if (timeRangeInput != null && timeRange.isEmpty()) {
            return null;
        }
        if (sourceWorld == null) {
            if (sourceServer == null || targetServer == null || targetWorld != null) {
                return null;
            }
            return ParsedTransfer.server(playerName, sourceServer, targetServer, timeRange, Optional.ofNullable(timeRangeInput),
                    false);
        }
        final String resolvedTargetServer = targetServer == null ? sourceServer : targetServer;
        if (targetWorld == null) {
            return null;
        }
        return ParsedTransfer.world(playerName, sourceServer, sourceWorld, resolvedTargetServer, targetWorld,
                timeRange, Optional.ofNullable(timeRangeInput), false);
    }

    private Optional<String> resolveDefaultServer(final CommonSender sender) {
        final CommonServer server = plugin.getServer();
        if (sender instanceof final CommonPlayerSender playerSender && playerSender.getUniqueId() != null) {
            return server.getCurrentServer(playerSender.getUniqueId())
                    .or(server::getLocalServerName);
        }
        return Optional.empty();
    }

    private String assignOnce(final String current, final String argument, final int prefixLength) {
        if (current != null || argument.length() == prefixLength) {
            return null;
        }
        return argument.substring(prefixLength);
    }

    private Optional<TimeRange> parseTimeRange(final String input) {
        final String[] parts = input.split("-", -1);
        if (parts.length == 1) {
            final OptionalLong duration = parsePositiveDuration(parts[0]);
            return duration.isEmpty() ? Optional.empty() : Optional.of(range(0L, duration.getAsLong()));
        }
        if (parts.length == 2) {
            final OptionalLong near = parsePositiveDuration(parts[0]);
            final OptionalLong far = parsePositiveDuration(parts[1]);
            if (near.isEmpty() || far.isEmpty() || near.getAsLong() >= far.getAsLong()) {
                return Optional.empty();
            }
            return Optional.of(range(near.getAsLong(), far.getAsLong()));
        }
        return Optional.empty();
    }

    private OptionalLong parsePositiveDuration(final String input) {
        final OptionalLong duration = plugin.getParser() == null ? OptionalLong.empty() : plugin.getParser().parseToSeconds(input);
        return duration.isPresent() && duration.getAsLong() > 0L ? duration : OptionalLong.empty();
    }

    private TimeRange range(final long nearSeconds, final long farSeconds) {
        final Instant now = Instant.now(Clock.systemUTC());
        return TimeRange.between(now.minusSeconds(farSeconds),
                now.minusSeconds(nearSeconds));
    }

    private void sendTransferPreview(final CommonSender sender, final StorageMaintenancePreview preview) {
        final Component previewMessage = localization.formatTextComponent(localization
                .getRawMessage("message.command.loritimeadmin.transfer.preview")
                .replace("[player]", preview.playerName()
                        .orElse(preview.playerUuid().map(UUID::toString).orElse("all players")))
                .replace("[source]", sourceLabel(preview))
                .replace("[target]", targetLabel(preview))
                .replace("[sessions]", Long.toString(preview.affectedSessions()))
                .replace("[adjustments]", Long.toString(preview.affectedAdjustments()))
                .replace("[players]", Long.toString(preview.affectedPlayers()))
                .replace("[range]", preview.timeRangeInput().orElse("all"))
                .replace("[merge]", Boolean.toString(preview.targetDataExists())));
        sender.sendMessage(previewMessage.append(Component.text(" [Confirm]")
                .clickEvent(ClickEvent.suggestCommand(CONFIRM_COMMAND))
                .hoverEvent(HoverEvent.showText(Component.text("Suggest " + CONFIRM_COMMAND)))));
        sender.sendMessage(localization.formatTextComponent(localization
                .getRawMessage("message.command.loritimeadmin.transfer.warning")));
    }

    private void sendTransferSuccess(final CommonSender sender, final StorageMaintenanceResult result) {
        sender.sendMessage(localization.formatTextComponent(localization
                .getRawMessage("message.command.loritimeadmin.transfer.success")
                .replace("[sessions]", Long.toString(result.affectedSessions()))
                .replace("[adjustments]", Long.toString(result.affectedAdjustments()))
                .replace("[players]", Long.toString(result.affectedPlayers()))));
    }

    private void sendDeleteHistoryPreview(final CommonSender sender, final StorageMaintenancePreview preview) {
        final Component previewMessage = localization.formatTextComponent(localization
                .getRawMessage("message.command.loritimeadmin.deleteHistory.preview")
                .replace("[player]", preview.playerName()
                        .orElse(preview.playerUuid().map(UUID::toString).orElse("all players")))
                .replace("[scope]", preview.deleteScope() == null ? "?" : scopeLabel(preview.deleteScope()))
                .replace("[sessions]", Long.toString(preview.affectedSessions()))
                .replace("[adjustments]", Long.toString(preview.affectedAdjustments()))
                .replace("[players]", Long.toString(preview.affectedPlayers()))
                .replace("[range]", preview.timeRangeInput().orElse("all")));
        sender.sendMessage(previewMessage.append(Component.text(" [Confirm]")
                .clickEvent(ClickEvent.suggestCommand(CONFIRM_COMMAND))
                .hoverEvent(HoverEvent.showText(Component.text("Suggest " + CONFIRM_COMMAND)))));
        sender.sendMessage(localization.formatTextComponent(localization
                .getRawMessage("message.command.loritimeadmin.deleteHistory.warning")));
    }

    private void sendDeleteHistorySuccess(final CommonSender sender, final StorageMaintenanceResult result) {
        sender.sendMessage(localization.formatTextComponent(localization
                .getRawMessage("message.command.loritimeadmin.deleteHistory.success")
                .replace("[sessions]", Long.toString(result.affectedSessions()))
                .replace("[adjustments]", Long.toString(result.affectedAdjustments()))
                .replace("[players]", Long.toString(result.affectedPlayers()))));
    }

    private String sourceLabel(final StorageMaintenancePreview preview) {
        return preview.mappings().isEmpty() ? "?" : scopeLabel(preview.mappings().get(0).source());
    }

    private String targetLabel(final StorageMaintenancePreview preview) {
        return preview.mappings().isEmpty() ? "?" : scopeLabel(preview.mappings().get(0).target());
    }

    private String scopeLabel(final StorageMaintenanceScope scope) {
        return switch (scope.type()) {
            case STORAGE -> "storage";
            case SERVER -> scope.server();
            case WORLD -> scope.server() + "/" + scope.world();
        };
    }

    private List<String> prefixedScopeValues(final CommonSender sender,
                                             final String prefix,
                                             final String argument,
                                             final boolean server,
                                             final boolean target,
                                             final String... args) {
        final String valuePrefix = argument.substring(prefix.length());
        final List<String> values = server
                ? plugin.getScopeSuggestionCache().suggestServers(plugin.getServer().getLiveServerNames(), valuePrefix)
                : prefixedWorldValues(sender, valuePrefix, target, args);
        return values.stream().map(value -> prefix + value).toList();
    }

    private List<String> prefixedWorldValues(final CommonSender sender,
                                             final String valuePrefix,
                                             final boolean target,
                                             final String... args) {
        final Optional<String> serverName = resolveCompletionServer(sender, target, args);
        if (serverName.isEmpty()) {
            return List.of();
        }
        final Optional<UUID> sourceUniqueId = sender instanceof CommonPlayerSender playerSender
                ? Optional.ofNullable(playerSender.getUniqueId())
                : Optional.empty();
        return plugin.getScopeSuggestionCache().suggestWorlds(serverName.get(),
                plugin.getServer().getLiveWorldNames(serverName, sourceUniqueId), valuePrefix);
    }

    private Optional<String> resolveCompletionServer(final CommonSender sender,
                                                     final boolean target,
                                                     final String... args) {
        final Optional<String> explicitServer = target
                ? findFlagValue(args, TARGET_SERVER_PREFIX, SHORT_TARGET_SERVER_PREFIX)
                        .or(() -> findFlagValue(args, CommandScopes.SERVER_PREFIX, CommandScopes.SHORT_SERVER_PREFIX))
                : findFlagValue(args, CommandScopes.SERVER_PREFIX, CommandScopes.SHORT_SERVER_PREFIX);
        if (explicitServer.isPresent()) {
            return explicitServer;
        }
        if (sender instanceof final CommonPlayerSender playerSender && playerSender.getUniqueId() != null) {
            return plugin.getServer().getCurrentServer(playerSender.getUniqueId())
                    .or(plugin.getServer()::getLocalServerName);
        }
        return plugin.getServer().getLocalServerName();
    }

    private Optional<String> findFlagValue(final String[] args, final String longPrefix, final String shortPrefix) {
        return Arrays.stream(args)
                .filter(argument -> argument.regionMatches(true, 0, longPrefix, 0, longPrefix.length())
                        || argument.regionMatches(true, 0, shortPrefix, 0, shortPrefix.length()))
                .filter(argument -> argument.length() > argument.indexOf(':') + 1)
                .map(argument -> argument.substring(argument.indexOf(':') + 1))
                .findFirst();
    }

    private void addMissingFlagSuggestion(final List<String> suggestions,
                                          final List<String> previousArgs,
                                          final String flag) {
        if (previousArgs.stream().noneMatch(value -> value.regionMatches(true, 0, flag, 0, flag.length()))) {
            suggestions.add(flag);
        }
    }

    private boolean hasUnexpectedArgs(final CommonSender sender, final String... args) {
        if (args.length == 0) {
            return false;
        }
        usage.accept(sender);
        return true;
    }

    private void queuePendingAction(final CommonSender sender,
                                    final String description,
                                    final ConfirmableAdminAction action) {
        final String key = senderKey(sender);
        final PendingAdminAction previous = pendingConfirmations.remove(key);
        if (previous != null && previous.timeoutTask() != null) {
            previous.timeoutTask().cancel();
        }
        final long expiresAt = System.currentTimeMillis() + CONFIRM_TIMEOUT_SECONDS * 1_000L;
        final PluginTask timeoutTask = plugin.getScheduler().runAsyncOnceLater(CONFIRM_TIMEOUT_SECONDS,
                () -> expirePendingAction(key, expiresAt));
        pendingConfirmations.put(key, new PendingAdminAction(description, expiresAt, timeoutTask, action));
    }

    private void expirePendingAction(final String key, final long expiresAt) {
        final PendingAdminAction pending = pendingConfirmations.get(key);
        if (pending != null && pending.expiresAtMillis() == expiresAt) {
            pendingConfirmations.remove(key);
        }
    }

    private String senderKey(final CommonSender sender) {
        return sender.getName().toLowerCase(Locale.ROOT);
    }

    private void changeDebugMode(final CommonSender sender) {
        final boolean configValue = plugin.getConfig().getBoolean(DEBUG_CONFIG_PATH);
        if (configValue) {
            if (autoDisableTask != null) {
                autoDisableTask.cancel();
            }
            stopDebugging();
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender, "message.command.debug.disabled");
        } else {
            startDebugging();
            CommandMessages.send(localization, plugin.getLanguageSelector(), sender, "message.command.debug.enabled");
        }
    }

    private void startDebugging() {
        isDebugging = true;
        plugin.getConfig().setTemporaryValue(DEBUG_CONFIG_PATH, true);
        log.info("Debug mode has been enabled.");
    }

    private void stopDebugging() {
        isDebugging = false;
        plugin.getConfig().setTemporaryValue(DEBUG_CONFIG_PATH, false);
        log.info("Debug mode has been disabled.");
    }

    private void autoDisableCheck() {
        final int configTimeToDisable = plugin.getConfig().getInt("general.debugAutoDisableTime", 30);
        if (configTimeToDisable <= 0) {
            log.debug("Debug mode will not be disabled automatically.");
            return;
        }
        final long timeToDisable = configTimeToDisable * 60L;
        autoDisableTask = plugin.getScheduler().runAsyncOnceLater(timeToDisable, () -> {
            if (isDebugging) {
                log.debug("Auto disabling the debug mode.");
                stopDebugging();
            }
        });
    }

    /**
     * Action that can be executed after a sender confirms it.
     */
    @FunctionalInterface
    private interface ConfirmableAdminAction {
        /**
         * Runs the confirmed action.
         *
         * @throws StorageException when storage mutation fails
         */
        void run() throws StorageException;
    }

    private record ParsedTransfer(String playerName,
                                  String sourceServer,
                                  String sourceWorld,
                                  String targetServer,
                                  String targetWorld,
                                  Optional<TimeRange> timeRange,
                                  Optional<String> timeRangeInput,
                                  boolean confirm) {

        private static ParsedTransfer server(final String playerName,
                                             final String sourceServer,
                                             final String targetServer,
                                             final Optional<TimeRange> timeRange,
                                             final Optional<String> timeRangeInput,
                                             final boolean confirm) {
            return new ParsedTransfer(playerName, sourceServer, null, targetServer, null,
                    timeRange, timeRangeInput, confirm);
        }

        private static ParsedTransfer world(final String playerName,
                                            final String sourceServer,
                                            final String sourceWorld,
                                            final String targetServer,
                                            final String targetWorld,
                                            final Optional<TimeRange> timeRange,
                                            final Optional<String> timeRangeInput,
                                            final boolean confirm) {
            return new ParsedTransfer(playerName, sourceServer, sourceWorld, targetServer, targetWorld,
                    timeRange, timeRangeInput, confirm);
        }

        private boolean hasPlayer() {
            return playerName != null;
        }

        private PlayerStorageTransferRequest toPlayerRequest(final UUID playerUuid,
                                                             final Optional<String> defaultServer) {
            if (sourceWorld == null) {
                final StorageMaintenanceScope source = StorageMaintenanceScope.server(sourceServer);
                final StorageMaintenanceScope target = StorageMaintenanceScope.server(targetServer);
                return PlayerStorageTransferRequest.serverTransfer(playerUuid, Optional.of(playerName), source, target,
                        timeRange, timeRangeInput);
            }
            final StorageMaintenanceScope resolvedSource = resolveWorldScope(sourceServer, sourceWorld, defaultServer);
            final StorageMaintenanceScope resolvedTarget = resolveWorldScope(targetServer, targetWorld, defaultServer);
            if (resolvedSource == null || resolvedTarget == null) {
                return null;
            }
            return PlayerStorageTransferRequest.worldTransfer(playerUuid, Optional.of(playerName), resolvedSource, resolvedTarget,
                    timeRange, timeRangeInput);
        }

        private StorageTransferRequest toStorageRequest(final Optional<String> defaultServer) {
            if (sourceWorld == null) {
                final StorageMaintenanceScope source = StorageMaintenanceScope.server(sourceServer);
                final StorageMaintenanceScope target = StorageMaintenanceScope.server(targetServer);
                return StorageTransferRequest.serverTransfer(List.of(new StorageTransferMapping(source, target)));
            }
            final StorageMaintenanceScope resolvedSource = resolveWorldScope(sourceServer, sourceWorld, defaultServer);
            final StorageMaintenanceScope resolvedTarget = resolveWorldScope(targetServer, targetWorld, defaultServer);
            if (resolvedSource == null || resolvedTarget == null) {
                return null;
            }
            return StorageTransferRequest.worldTransfer(List.of(new StorageTransferMapping(resolvedSource, resolvedTarget)));
        }

        private StorageMaintenanceScope resolveWorldScope(final String server,
                                                          final String world,
                                                          final Optional<String> defaultServer) {
            if (server != null) {
                return StorageMaintenanceScope.world(server, world);
            }
            return defaultServer.map(resolvedServer -> StorageMaintenanceScope.world(resolvedServer, world)).orElse(null);
        }
    }

    private record ParsedDeleteHistory(String playerName,
                                       String sourceServer,
                                       String sourceWorld,
                                       Optional<TimeRange> timeRange,
                                       Optional<String> timeRangeInput) {

        private boolean hasPlayer() {
            return playerName != null;
        }

        private StorageDeleteRequest toRequest(final Optional<UUID> playerUuid,
                                               final Optional<String> defaultServer) {
            if (hasPlayer() && playerUuid.isEmpty()) {
                return null;
            }
            final String resolvedServer = sourceServer == null ? defaultServer.orElse(null) : sourceServer;
            if (resolvedServer == null) {
                return null;
            }
            final StorageMaintenanceScope scope = sourceWorld == null
                    ? StorageMaintenanceScope.server(resolvedServer)
                    : StorageMaintenanceScope.world(resolvedServer, sourceWorld);
            if (hasPlayer()) {
                return StorageDeleteRequest.player(scope, playerUuid.get(), Optional.of(playerName),
                        timeRange, timeRangeInput);
            }
            return StorageDeleteRequest.allPlayers(scope, timeRange, timeRangeInput);
        }
    }

    private record PendingAdminAction(String description,
                                      long expiresAtMillis,
                                      PluginTask timeoutTask,
                                      ConfirmableAdminAction action) {
    }
}
