package com.playtime.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.playtime.commands.commandUtils.SeenPlayer;
import com.playtime.config.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeenCMD implements SimpleCommand {
    ProxyServer proxyServer;

    public SeenCMD(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        if (!source.hasPermission("playtime.seen")) {
            source.sendMessage(MiniMessage.get().parse(Config.Messages.NO_PERMISSION.getMessage()));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(MiniMessage.get().parse(Config.Messages.INVALID_SEEN_COMMAND.getMessage()));
            return;
        }

        if (!args[0].matches("[a-zA-Z0-9_]{3,16}")) {
            source.sendMessage(MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", args[0])));
        }

        String playerName = args[0];

        Optional<Player> playerOptional = proxyServer.getPlayer(playerName);

        source.sendMessage(SeenPlayer.getLastSeen(playerName, playerOptional.orElse(null)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        if (!source.hasPermission("playtime.seen")) return null;
        switch (args.length) {
            case 0:
            case 1: {
                List<String> possibleValues = new ArrayList<>();

                for (Player player : proxyServer.getAllPlayers()) {
                    possibleValues.add(player.getGameProfile().getName());
                }

                if (args.length  == 0) return possibleValues;

                return finalizeSuggest(possibleValues, args[0].toLowerCase());
            }
        }
        return null;
    }

    public List<String> finalizeSuggest(List<String> possibleValues, String remaining) {
        List<String> finalValues = new ArrayList<>();

        for (String str : possibleValues) {
            if (str.toLowerCase().startsWith(remaining)) {
                finalValues.add(StringArgumentType.escapeIfRequired(str));
            }
        }

        return finalValues;
    }
}
