package com.playtime.commands.playtimeSubcommands;

import com.playtime.commands.Command;
import com.playtime.commands.SubCommand;
import com.playtime.commands.commandUtils.PlaytimeExtraForPlayer;
import com.playtime.config.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Extra extends Command implements SubCommand {

    ProxyServer proxyServer;

    public Extra(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public String getName() {
        return "extra";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        if (args.length == 1 && source instanceof Player) {
            source.sendMessage(PlaytimeExtraForPlayer.getPlaytime(((Player) source).getUniqueId()));
            return;
        }
        if (args.length < 3) {
            source.sendMessage(MiniMessage.miniMessage().deserialize(Config.Messages.INVALID_EXTENDED_PLAYTIME_COMMAND.getMessage()));
            return;
        }
        if (!args[1].matches("[a-zA-Z0-9_]{3,16}")) {
            source.sendMessage(MiniMessage.miniMessage().deserialize(Config.Messages.INVALID_EXTENDED_PLAYTIME_COMMAND.getMessage()));
            return;
        }
        if (args.length == 4 && !args[3].matches("[0-9]{1,3}")) {
            source.sendMessage(MiniMessage.miniMessage().deserialize(Config.Messages.INVALID_EXTENDED_PLAYTIME_COMMAND.getMessage()));
            return;
        }
        int days = 0;
        if (args.length > 3) {
            try {
                days = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                source.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid number.</red>"));
                return;
            }
            if (days < 0) {
                source.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid number.</red>"));
                return;
            }
        }
        switch (args[2].toLowerCase()) {
            case "day" -> playtimeExtraDay(args, source, days);
            case "week" -> playtimeExtraWeek(args, source, days);
            default -> source.sendMessage(MiniMessage.miniMessage().deserialize(Config.Messages.INVALID_EXTENDED_PLAYTIME_COMMAND.getMessage()));
        }
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        if (args.length == 2)
            return proxyServer.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .collect(Collectors.toList());
        else if (args.length == 3)
            return List.of("day", "week");
        else if (args.length == 4)
            return IntStream.rangeClosed(0, 7)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toList());
        return List.of();
    }

    @Override
    public String getHelpMessage() {
        return null; //TODO help message
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
}
