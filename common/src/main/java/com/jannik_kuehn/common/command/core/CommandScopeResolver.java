package com.jannik_kuehn.common.command.core;

import com.jannik_kuehn.common.api.storage.TimeScope;
import com.jannik_kuehn.common.platform.CommonPlayerSender;
import com.jannik_kuehn.common.platform.CommonSender;
import com.jannik_kuehn.common.platform.CommonServer;

import java.util.Optional;

/**
 * Resolves command scope defaults from sender context.
 */
public final class CommandScopeResolver {

    private CommandScopeResolver() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Resolves the default server for command execution.
     *
     * @param server platform server adapter
     * @param sender command sender
     * @return sender current server, local server, or empty when no sender server context exists
     */
    public static Optional<String> executionDefaultServer(final CommonServer server, final CommonSender sender) {
        if (sender instanceof final CommonPlayerSender playerSender && playerSender.getUniqueId() != null) {
            return server.getCurrentServer(playerSender.getUniqueId())
                    .or(server::getLocalServerName);
        }
        return Optional.empty();
    }

    /**
     * Resolves the default server for completions.
     *
     * @param server platform server adapter
     * @param sender command sender
     * @return sender current server, local server, or empty when neither exists
     */
    public static Optional<String> completionDefaultServer(final CommonServer server, final CommonSender sender) {
        if (sender instanceof final CommonPlayerSender playerSender && playerSender.getUniqueId() != null) {
            return server.getCurrentServer(playerSender.getUniqueId())
                    .or(server::getLocalServerName);
        }
        return server.getLocalServerName();
    }

    /**
     * Resolves a time scope from parsed command flags.
     *
     * @param server     platform server adapter
     * @param sender     command sender
     * @param hasServer  true when the command provided a server flag
     * @param serverName parsed server name
     * @param hasWorld   true when the command provided a world flag
     * @param worldName  parsed world name
     * @return resolved time scope
     */
    public static Optional<TimeScope> timeScope(final CommonServer server,
                                                final CommonSender sender,
                                                final boolean hasServer,
                                                final String serverName,
                                                final boolean hasWorld,
                                                final String worldName) {
        if (!hasWorld) {
            return Optional.of(hasServer ? TimeScope.server(serverName) : TimeScope.GLOBAL);
        }
        if (hasServer) {
            return Optional.of(TimeScope.world(serverName, worldName));
        }
        return executionDefaultServer(server, sender).map(defaultServer -> TimeScope.world(defaultServer, worldName));
    }
}
