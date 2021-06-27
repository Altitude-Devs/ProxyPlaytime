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
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlaytimeCMD implements Command {
    ProxyServer proxyServer;

    public PlaytimeCMD(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void execute(@NonNull CommandSource source, String @NotNull [] args) {
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
                if ((args.length >= 2 && args.length <= 3) && !args[1].matches("[a-zA-Z0-9_]{3,16}")) {
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.INVALID_EXTENDED_PLAYTIME_COMMAND.getMessage()));
                    break;
                }
                if (args.length == 3 && !args[2].matches("[0-9]{1,3}")) { //TODO cast to int and check if below config value (config value should be whatever the max time to store in db is for sessions
                    source.sendMessage(MiniMessage.get().parse(Config.Messages.INVALID_EXTENDED_PLAYTIME_COMMAND.getMessage()));
                    break;
                }

                String playerName = args[1];

                Optional<Player> playerOptional = proxyServer.getPlayer(playerName);
                Component playtime;

                if (playerOptional.isPresent()) {
                    if (args.length == 2) {
                        playtime = PlaytimeExtraForPlayer.getPlaytime(playerOptional.get().getUniqueId());
                    } else {
                        playtime = PlaytimeExtraForPlayer.getPlaytime(playerOptional.get().getUniqueId(), Integer.parseInt(args[2]));
                    }
                } else {
                    if (args.length == 2) {
                        playtime = PlaytimeExtraForPlayer.getPlaytime(playerName);
                    } else {
                        playtime = PlaytimeExtraForPlayer.getPlaytime(playerName, Integer.parseInt(args[2]));
                    }
                }

                source.sendMessage(playtime);

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

    @Override
    public List<String> suggest(@NonNull CommandSource source, String @NotNull [] currentArgs) {
        if (!source.hasPermission("playtime.use")) return null;
        switch (currentArgs.length) {
            case 0:
            case 1: {
                List<String> possibleValues = new ArrayList<>();

                if (source.hasPermission("playtime.move")) possibleValues.add("move");
                if (source.hasPermission("playtime.reload")) possibleValues.add("reload");
                if (source.hasPermission("playtime.extra")) possibleValues.add("extra");

                if (!source.hasPermission("playtime.use.other")) return possibleValues;

                for (Player player : proxyServer.getAllPlayers()) {
                    possibleValues.add(player.getGameProfile().getName());
                }

                if (currentArgs.length  == 0) return possibleValues;

                return finalizeSuggest(possibleValues, currentArgs[0].toLowerCase());
            }
            case 2: {
                switch (currentArgs[0].toLowerCase()) {
                    case "move":
                        if (!source.hasPermission("playtime.move")) return null;
                    case "extra":
                        if (currentArgs[0].equalsIgnoreCase("extra") && !source.hasPermission("playtime.extra")) return null;

                        List<String> possibleValues = new ArrayList<>();

                        for (Player player : proxyServer.getAllPlayers()) {
                            possibleValues.add(player.getGameProfile().getName());
                        }

                        return finalizeSuggest(possibleValues, currentArgs[1].toLowerCase());
                }
            }
            case 3: {
                if (currentArgs[0].equalsIgnoreCase("move") && source.hasPermission("playtime.move")) {
                    List<String> possibleValues = new ArrayList<>();

                    for (Player player : proxyServer.getAllPlayers()) {
                        possibleValues.add(player.getGameProfile().getName());
                    }

                    return finalizeSuggest(possibleValues, currentArgs[2].toLowerCase());
                }
            }
            case 4: {
                if (currentArgs[0].equalsIgnoreCase("move") && source.hasPermission("playtime.move")) {
                    List<String> possibleValues = new ArrayList<>();
                    possibleValues.add("add");
                    possibleValues.add("set");

                    return finalizeSuggest(possibleValues, currentArgs[3]);
                }
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

    public void createPlaytimeCommand(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> playtimeCommand = LiteralArgumentBuilder
                .<CommandSource>literal("playtime")
                .requires(ctx -> ctx.hasPermission("playtime.use"))
                .then(LiteralArgumentBuilder.literal("reload"))
                        .requires(ctx -> ctx.hasPermission("playtime.reload"))
                        .executes(context -> {
                            MiniMessage miniMessage = MiniMessage.get();
                            context.getSource().sendMessage(miniMessage.parse("<red>Reloading config...</red>"));
                            Config.reload();
                            context.getSource().sendMessage(miniMessage.parse("<green>Config reloaded!</green>"));
                            return 1;
                        })
                .then(LiteralArgumentBuilder.literal("set"))
                        .requires(ctx -> ctx.hasPermission("playtime.set"))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    Collection<String> possibleValues = new ArrayList<>();
                                    for (Player player : proxyServer.getAllPlayers()) {
                                        possibleValues.add(player.getGameProfile().getName());
                                    }
                                    if (possibleValues.isEmpty()) return Suggestions.empty();
                                    String remaining = builder.getRemaining().toLowerCase();
                                    for (String str : possibleValues) {
                                        if (str.toLowerCase().startsWith(remaining)) {
                                            builder.suggest(StringArgumentType.escapeIfRequired(str));
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    return 1;
                                })
                        )
                .then(LiteralArgumentBuilder.literal("extra"))
                .requires(ctx -> ctx.hasPermission("playtime.extra"))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    Collection<String> possibleValues = new ArrayList<>();
                                    for (Player player : proxyServer.getAllPlayers()) {
                                        possibleValues.add(player.getGameProfile().getName());
                                    }
                                    if (possibleValues.isEmpty()) return Suggestions.empty();
                                    String remaining = builder.getRemaining().toLowerCase();
                                    for (String str : possibleValues) {
                                        if (str.toLowerCase().startsWith(remaining)) {
                                            builder.suggest(StringArgumentType.escapeIfRequired(str));
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    return 1;
                                })
                        )
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                        .requires(ctx -> ctx.hasPermission("playtime.use.other"))
                        .suggests((context, builder) -> {
                            Collection<String> possibleValues = new ArrayList<>();
                            for (Player player : proxyServer.getAllPlayers()) {
                                possibleValues.add(player.getGameProfile().getName());
                            }
                            if (possibleValues.isEmpty()) return Suggestions.empty();
                            String remaining = builder.getRemaining().toLowerCase();
                            for (String str : possibleValues) {
                                if (str.toLowerCase().startsWith(remaining)) {
                                    builder.suggest(StringArgumentType.escapeIfRequired(str));
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            playtimeGet(proxyServer, context.getSource(), context.getArgument("player", String.class));
                            return 1;
                        })
                )
                .requires(commandSource -> commandSource instanceof Player)
                .executes(context -> {
                    Player player = (Player) context.getSource();
                    playtimeGet(proxyServer, context.getSource(), player.getUsername());
                    return 1;
                })
                .build();

        BrigadierCommand brigadierCommand = new BrigadierCommand(playtimeCommand);

        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);

        metaBuilder.aliases("playtime");
        metaBuilder.aliases("pt");

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
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
