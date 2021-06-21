package com.playtime.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.playtime.commands.idkyet.PlaytimeForPlayer;
import com.playtime.config.Config;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class PlaytimeCMD {

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
        Component playtime;

        if (playerOptional.isPresent()) {
            playtime = PlaytimeForPlayer.getPlaytime(playerOptional.get().getUniqueId());
        } else {
            playtime = PlaytimeForPlayer.getPlaytime(playerName);
        }

        source.sendMessage(playtime);
    }
}
