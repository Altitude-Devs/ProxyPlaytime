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

public class Reset extends Command implements SubCommand {

    private final ProxyServer proxyServer;

    public Reset(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        if (args.length != 2) {
            source.sendMessage(parseMessage(Config.Messages.INVALID_PLAYTIME_RESET_COMMAND));
            return;
        }
        UUID uuid = Utilities.getPlayerUUID(args[1]);
        if (uuid == null) {
            source.sendMessage(parseMessage(Config.Messages.PLAYER_NOT_FOUND, Placeholder.parsed("<player>", args[1])));
            return;
        }
        if (Queries.resetPlaytime(uuid))
            source.sendMessage(parseMessage(Config.Messages.PLAYTIME_RESET_SUCCESS, Placeholder.parsed("<player>", args[1])));
        else
            source.sendMessage(parseMessage(Config.Messages.PLAYTIME_RESET_FAILURE, Placeholder.parsed("<player>", args[1])));
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        if (args.length == 2)
            return proxyServer.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .collect(Collectors.toList());
        return List.of();
    }

    @Override
    public String getHelpMessage() {
        return null; //TODO help message
    }
}
