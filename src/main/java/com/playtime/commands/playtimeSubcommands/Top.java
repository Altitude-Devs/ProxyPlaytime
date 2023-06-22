package com.playtime.commands.playtimeSubcommands;

import com.playtime.commands.Command;
import com.playtime.commands.SubCommand;
import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.util.objects.PlaytimeTop;
import com.playtime.util.objects.TopPlayer;
import com.playtime.util.objects.TopType;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Top extends Command implements SubCommand {

    private final ProxyServer proxyServer;
    private final PlaytimeTop playtimeTop;

    public Top(ProxyServer proxyServer, PlaytimeTop playtimeTop) {
        this.proxyServer = proxyServer;
        this.playtimeTop = playtimeTop;
    }

    @Override
    public String getName() {
        return "top";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        TopType topType;
        if (args.length < 2) {
            topType = TopType.TOTAL;
        } else {
            try {
                topType = TopType.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                source.sendMessage(parseMessage(Config.Messages.INVALID_ARGUMENT, Placeholder.parsed("argument", args[1])));
                return;
            }
        }

        String format = Config.Messages.PLAYTIME_TOP_FORMAT.getMessage().replaceAll("<nl>","\n");
        playtimeTop.sort(topType);
        Component textComponent = parseMessage(Config.Messages.PLAYTIME_TOP_FORMAT_HEADER.getMessage() + "\n", Placeholder.parsed("top_type", convertStringCase(topType.name())));
        textComponent = textComponent.append((Component) playtimeTop.processTop(topType, list -> {
            TextComponent component = Component.empty();
            for (TopPlayer topPlayer : list) {
                component = component.append(parseMessage(format + "\n", Placeholder.parsed("server", topPlayer.getUsername()), Placeholder.parsed("time", topPlayer.getFormattedTime())));
            }
            return component;
        }));
        textComponent = textComponent.append(parseMessage(Config.Messages.PLAYTIME_TOP_FORMAT_FOOTER));
        source.sendMessage(textComponent);
    }

    private String convertStringCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String firstLetter = input.substring(0, 1).toUpperCase();
        String restOfString = input.substring(1).toLowerCase();
        return firstLetter + restOfString;
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        if (args.length == 2)
            return Arrays.stream(TopType.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList());
        return List.of();
    }

    @Override
    public String getHelpMessage() {
        return null; //TODO help message
    }
}
