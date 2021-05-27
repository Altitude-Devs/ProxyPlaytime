package com.playtime.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.playtime.Playtime;
import com.playtime.commands.idkyet.PlaytimeForPlayer;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class PlaytimeCMD {

    public void createPlaytimeCommand(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> playtimeCommand = LiteralArgumentBuilder
                .<CommandSource>literal("playtime")
                .requires(ctx -> ctx.hasPermission("playtime.use")) //TODO permission system? load permissions from config?
                .then(RequiredArgumentBuilder
                        .<CommandSource, String>argument("player", StringArgumentType.word()))
//                        .suggests((context, builder) -> {
//                            Collection<String> possibleValues = new ArrayList<>();
//                            for (Player player : proxyServer.getAllPlayers()) {
//                                possibleValues.add(player.getGameProfile().getName());
//                            }
//                            if(possibleValues.isEmpty()) return Suggestions.empty();
//                            String remaining = builder.getRemaining().toLowerCase();
//                            for (String str : possibleValues) {
//                                if (str.toLowerCase().startsWith(remaining)) {
//                                    builder.suggest(str = StringArgumentType.escapeIfRequired(str));
//                                }
//                            }
//                            return builder.buildFuture();
//                        }))
                .executes(commandContext -> {
                    Optional<Player> playerOptional = proxyServer.getPlayer(commandContext.getArgument("player", String.class));
                    if (playerOptional.isPresent()) {
                        Playtime.getInstance().getLogger().info("Handling default playtime command send by: " + commandContext.getSource().toString()); //TODO debug
                        Component playtime = PlaytimeForPlayer.getPlaytime(playerOptional.get().getUniqueId());
                        commandContext.getSource().sendMessage(playtime);
                    } else {
                        Playtime.getInstance().getLogger().info("Handling default playtime command send by: " + commandContext.getSource().toString()); //TODO debug
                    }
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
}
