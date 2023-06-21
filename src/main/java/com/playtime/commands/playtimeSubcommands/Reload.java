package com.playtime.commands.playtimeSubcommands;

import com.playtime.commands.Command;
import com.playtime.commands.SubCommand;
import com.playtime.config.Config;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class Reload extends Command implements SubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        source.sendMessage(miniMessage.deserialize("<red>Reloading config...</red>"));
        Config.reload();
        source.sendMessage(miniMessage.deserialize("<green>Config reloaded!</green>"));
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        return List.of();
    }

    @Override
    public String getHelpMessage() {
        return null; //TODO help message
    }
}
