package com.playtime.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.playtime.commands.commandUtils.PlaytimeExtraForPlayer;
import com.playtime.commands.commandUtils.PlaytimeForPlayer;
import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.util.Utilities;
import com.velocitypowered.api.command.*;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlaytimeCMD implements SimpleCommand {
    ProxyServer proxyServer;

    public PlaytimeCMD(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        if (!source.hasPermission("playtime.use")) {
            source.sendMessage(MiniMessage.get().parse(Config.Messages.NO_PERMISSION.getMessage()));
            return;
        }

        if (args.length == 0) {
            Player player = (Player) source;
            playtimeGet(proxyServer, source, player.getUsername());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!source.hasPermission("playtime.use.other")) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.NO_PERMISSION.getMessage()));
                    break;
                }
                MiniMessage miniMessage = MiniMessage.get();
                source.sendMessage(miniMessage.parse("<red>Reloading config...</red>"));
                Config.reload();
                source.sendMessage(miniMessage.parse("<green>Config reloaded!</green>"));
                break;
            case "move":
                if (!source.hasPermission("playtime.move")) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.NO_PERMISSION.getMessage()));
                    break;
                }

                if (args.length != 4 || !args[1].matches("[a-zA-Z0-9_]{3,16}") || !args[2].matches("[a-zA-Z0-9_]{3,16}") || args[1].equalsIgnoreCase(args[2])) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.INVALID_PLAYTIME_MOVE_COMMAND.getMessage()));
                    break;
                }
                boolean set;
                if (args[3].equalsIgnoreCase("add")) {
                    set = false;
                } else if (args[3].equalsIgnoreCase("set")) {
                    set = true;
                } else {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.INVALID_PLAYTIME_MOVE_COMMAND.getMessage()));
                    break;
                }

                UUID playerFrom = Utilities.getPlayerUUID(args[1]);
                UUID playerTo = Utilities.getPlayerUUID(args[2]);

                if (playerFrom == null) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", args[1])));
                    return;
                }
                if (playerTo == null) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", args[2])));
                    return;
                }

                boolean success = Queries.movePlaytime(playerFrom, playerTo, set);

                source.sendMessage(MiniMessage.get().parse(
                        (success ? Config.Messages.MOVED_PLAYTIME.getMessage() : Config.Messages.FAILED_MOVED_PLAYTIME.getMessage())
                                .replaceAll("%playerFrom%", Utilities.getPlayerName(playerFrom))
                                .replaceAll("%playerTo%", Utilities.getPlayerName(playerTo))));
                break;
            case "extra":
                if (!source.hasPermission("playtime.extra")) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.NO_PERMISSION.getMessage()));
                    break;
                }
                if (args.length == 1 && source instanceof Player) {
                    source.sendMessage(PlaytimeExtraForPlayer.getPlaytime(((Player) source).getUniqueId()));
                    return;
                }
                if (args.length < 3) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.INVALID_EXTENDED_PLAYTIME_COMMAND.getMessage()));
                    break;
                }
                if (!args[1].matches("[a-zA-Z0-9_]{3,16}")) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.INVALID_EXTENDED_PLAYTIME_COMMAND.getMessage()));
                    break;
                }
                if (args.length == 4 && !args[3].matches("[0-9]{1,3}")) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.INVALID_EXTENDED_PLAYTIME_COMMAND.getMessage()));
                    break;
                }

                int days = 0;
                if (args.length > 3) {
                    try {
                        days = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        source.sendMessage(MiniMessage.get().parse("<red>Invalid number.</red>"));
                        return;
                    }
                    if (days < 0) {
                        source.sendMessage(MiniMessage.get().parse("<red>Invalid number.</red>"));
                        return;
                    }
                }

                switch (args[2].toLowerCase()) {
                    case "day":
                        playtimeExtraDay(args, source, days);
                        break;
                    case "week":
                        playtimeExtraWeek(args, source, days);
                        break;
                    default:
                        source.sendMessage(MiniMessage.get().parse(Config.Messages.INVALID_EXTENDED_PLAYTIME_COMMAND.getMessage()));
                        break;
                }

                break;
            default:
                if (!source.hasPermission("playtime.use.other")) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.NO_PERMISSION.getMessage()));
                    return;
                }
                if (!args[0].matches("[a-zA-Z0-9_]{3,16}")) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", args[0])));
                    return;
                }
                playtimeGet(proxyServer, source, args[0]);
        }
    }

    private void playtimeExtraWeek(String[] args, CommandSource source, int days) {
        String playerName = args[1];

        Optional<Player> playerOptional = proxyServer.getPlayer(playerName);
        Component playtime;

        if (playerOptional.isPresent()) {
            if (args.length == 3)
                playtime = PlaytimeExtraForPlayer.getPlaytimeWeek(playerOptional.get().getUniqueId());
            else
                playtime = PlaytimeExtraForPlayer.getPlaytimeWeek(playerOptional.get().getUniqueId(), days);
        } else {
            if (args.length == 3)
                playtime = PlaytimeExtraForPlayer.getPlaytimeWeek(playerName);
            else
                playtime = PlaytimeExtraForPlayer.getPlaytimeWeek(playerName, days);
        }

        source.sendMessage(playtime);
    }

    private void playtimeExtraDay(String[] args, CommandSource source, int days) {
        String playerName = args[1];

        Optional<Player> playerOptional = proxyServer.getPlayer(playerName);
        Component playtime;

        if (playerOptional.isPresent()) {
            if (args.length == 3) {
                playtime = PlaytimeExtraForPlayer.getPlaytime(playerOptional.get().getUniqueId());
            } else {
                playtime = PlaytimeExtraForPlayer.getPlaytime(playerOptional.get().getUniqueId(), Integer.parseInt(args[3]));
            }
        } else {
            if (args.length == 3) {
                playtime = PlaytimeExtraForPlayer.getPlaytime(playerName);
            } else {
                playtime = PlaytimeExtraForPlayer.getPlaytime(playerName, Integer.parseInt(args[3]));
            }
        }

        source.sendMessage(playtime);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        List<String> possibleValues = new ArrayList<>();

        if (!source.hasPermission("playtime.use")) return possibleValues;

        String current = args.length > 0 ? args[args.length - 1] : "";

        switch (args.length) {
            case 0:
            case 1: {
                if (source.hasPermission("playtime.move")) possibleValues.add("move");
                if (source.hasPermission("playtime.reload")) possibleValues.add("reload");
                if (source.hasPermission("playtime.extra")) possibleValues.add("extra");

                if (!source.hasPermission("playtime.use.other")) return possibleValues;

                for (Player player : proxyServer.getAllPlayers()) {
                    possibleValues.add(player.getGameProfile().getName());
                }

                if (args.length  == 0) return possibleValues;
                break;
            }
            case 2: {
                switch (args[0].toLowerCase()) {
                    case "move":
                        if (!source.hasPermission("playtime.move")) break;
                    case "extra":
                        if (args[0].equalsIgnoreCase("extra") && !source.hasPermission("playtime.extra")) break;

                        for (Player player : proxyServer.getAllPlayers()) {
                            possibleValues.add(player.getGameProfile().getName());
                        }
                }
                break;
            }
            case 3: {
                switch (args[0].toLowerCase()) {
                    case "move": {
                        if (!source.hasPermission("playtime.move")) break;

                        for (Player player : proxyServer.getAllPlayers()) {
                            possibleValues.add(player.getGameProfile().getName());
                        }

                        return finalizeSuggest(possibleValues, args[2].toLowerCase());
                    }
                    case "extra": {
                        if (!source.hasPermission("playtime.extra")) break;

                        possibleValues.add("day");
                        possibleValues.add("week");
                    }
                }
                break;
            }
            case 4: {
                if (args[0].equalsIgnoreCase("move") && source.hasPermission("playtime.move")) {
                    possibleValues.add("add");
                    possibleValues.add("set");
                }
                if (args[0].equalsIgnoreCase("extra") && source.hasPermission("playtime.extra")) {
                    if (args[2].equalsIgnoreCase("week")) possibleValues.add("0");
                    for (int i = 1; i <= 7; i++) {
                        possibleValues.add(String.valueOf(i));
                    }
                }
                break;
            }
        }
        return finalizeSuggest(possibleValues, current.toLowerCase());
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

    public void playtimeGet(ProxyServer proxyServer, CommandSource source, String playerName) {
        Optional<Player> playerOptional = proxyServer.getPlayer(playerName);
        Component message;

        if (playerOptional.isPresent()) {
            message = PlaytimeForPlayer.getPlaytime(playerOptional.get().getUniqueId());
        } else {
            message = PlaytimeForPlayer.getPlaytime(playerName);
        }

        source.sendMessage(message);
    }
}
