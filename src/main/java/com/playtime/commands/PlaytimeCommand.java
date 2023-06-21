package com.playtime.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.playtime.commands.commandUtils.PlaytimeForPlayer;
import com.playtime.commands.playtimeSubcommands.Extra;
import com.playtime.commands.playtimeSubcommands.Move;
import com.playtime.commands.playtimeSubcommands.Reload;
import com.playtime.commands.playtimeSubcommands.Reset;
import com.playtime.config.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PlaytimeCommand extends Command implements SimpleCommand {
    private final List<SubCommand> subCommands;
    private final ProxyServer proxyServer;

    public PlaytimeCommand(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        subCommands = Arrays.asList(
                new Reload(),
                new Reset(proxyServer),
                new Move(proxyServer),
                new Extra(proxyServer));
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        if (!source.hasPermission("playtime.use")) {
            source.sendMessage(parseMessage(Config.Messages.NO_PERMISSION));
            return;
        }

        if (args.length == 0) {
            if (source instanceof Player player) {
                playtimeGet(proxyServer, source, player.getUsername());
            }
            return;
        }

        subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[0]))
                .findFirst()
                .ifPresentOrElse(subCommand -> {
                    if (source.hasPermission(subCommand.getPermission()))
                        subCommand.execute(args, source);
                    else
                        source.sendMessage(parseMessage(Config.Messages.NO_PERMISSION));
                }, () -> {
                    if (!source.hasPermission("playtime.use.other")) {
                        source.sendMessage(parseMessage(Config.Messages.NO_PERMISSION));
                        return;
                    }
                    if (!args[0].matches("[a-zA-Z0-9_]{3,16}")) {
                        source.sendMessage(parseMessage(Config.Messages.PLAYER_NOT_FOUND, Placeholder.parsed("<player>", args[0])));
                        return;
                    }
                    playtimeGet(proxyServer, source, args[0]);
                });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggest = new ArrayList<>();

        if (!invocation.source().hasPermission("party.use"))
            return suggest;
        else if (args.length == 0) {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .forEach(subCommand -> suggest.add(subCommand.getName()));
            proxyServer.getAllPlayers()
                    .forEach(player -> suggest.add(player.getUsername()));
        } else if (args.length == 1) {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .filter(subCommand -> subCommand.getName().startsWith(args[0].toLowerCase()))
                    .forEach(subCommand -> suggest.add(subCommand.getName()));
            proxyServer.getAllPlayers()
                    .forEach(player -> suggest.add(player.getUsername()));
        } else {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[0]))
                    .findFirst()
                    .ifPresent(subCommand -> suggest.addAll(subCommand.suggest(args, invocation.source())));
        }

        if (args.length == 0)
            return suggest;
        else
            return finalizeSuggest(suggest, args[args.length - 1]);
    }

    public List<String> finalizeSuggest(List<String> possibleValues, String remaining) {
        List<String> finalValues = new ArrayList<>();

        for (String str : possibleValues) {
            if (str.toLowerCase().startsWith(remaining.toLowerCase())) {
                finalValues.add(StringArgumentType.escapeIfRequired(str));
            }
        }

        return finalValues;
    }

    public Component getHelpMessage(CommandSource source) { //TODO implement help for each command
        StringBuilder stringBuilder = new StringBuilder();

        subCommands.stream()
                .filter(subCommand -> source.hasPermission(subCommand.getPermission()))
                .forEach(subCommand -> stringBuilder.append(subCommand.getHelpMessage()).append("\n"));
        if (stringBuilder.length() != 0)
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");

        return parseMessage(Config.Messages.PLAYTIME_HELP_WRAPPER,
                Placeholder.component("commands", parseMessage(stringBuilder.toString())));
    }

    public void playtimeGet(ProxyServer proxyServer, CommandSource source, String playerName) {
        Optional<Player> playerOptional = proxyServer.getPlayer(playerName);
        Component message;

        message = playerOptional
                .map(player -> PlaytimeForPlayer.getPlaytime(player.getUniqueId()))
                .orElseGet(() -> PlaytimeForPlayer.getPlaytime(playerName));

        source.sendMessage(message);
    }

}