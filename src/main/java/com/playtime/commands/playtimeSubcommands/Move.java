package com.playtime.commands.playtimeSubcommands;

import com.playtime.commands.Command;
import com.playtime.commands.SubCommand;
import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.util.Utilities;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Move extends Command implements SubCommand {

    private final ProxyServer proxyServer;

    public Move(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public String getName() {
        return "move";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        if (args.length != 4 || !args[1].matches("[a-zA-Z0-9_]{3,16}") || !args[2].matches("[a-zA-Z0-9_]{3,16}") || args[1].equalsIgnoreCase(args[2])) {
            source.sendMessage(parseMessage(Config.Messages.INVALID_PLAYTIME_MOVE_COMMAND));
            return;
        }
        boolean set;
        if (args[3].equalsIgnoreCase("add")) {
            set = false;
        } else if (args[3].equalsIgnoreCase("set")) {
            set = true;
        } else {
            source.sendMessage(parseMessage(Config.Messages.INVALID_PLAYTIME_MOVE_COMMAND));
            return;
        }
        UUID playerFrom = Utilities.getPlayerUUID(args[1]);
        UUID playerTo = Utilities.getPlayerUUID(args[2]);
        if (playerFrom == null) {
            source.sendMessage(parseMessage(Config.Messages.PLAYER_NOT_FOUND, Placeholder.parsed("<player>", args[1])));
            return;
        }
        if (playerTo == null) {
            source.sendMessage(parseMessage(Config.Messages.PLAYER_NOT_FOUND, Placeholder.parsed("<player>", args[2])));
            return;
        }
        boolean success = Queries.movePlaytime(playerFrom, playerTo, set);
        if (success)
            source.sendMessage(parseMessage(Config.Messages.MOVED_PLAYTIME));
        else
            source.sendMessage(parseMessage(Config.Messages.FAILED_MOVED_PLAYTIME,
                    Placeholder.parsed("<playerFrom>", Utilities.getPlayerName(playerFrom)),
                    Placeholder.parsed("<playerTo>", Utilities.getPlayerName(playerTo))));
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        if (args.length == 2 || args.length == 3)
            return proxyServer.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .collect(Collectors.toList());
        else if (args.length == 4)
            return List.of("add", "set");

        return List.of();
    }

    @Override
    public String getHelpMessage() {
        return null; //TODO help message
    }
}
