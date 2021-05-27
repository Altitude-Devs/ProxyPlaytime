package com.playtime.commands.idkyet;

import com.playtime.Playtime;
import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.util.Utilities;
import com.playtime.util.objects.PlaytimePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public class PlaytimeForPlayer {

    public static Component getPlaytime(String playerName) {
        UUID uuid = getPlayerUUID(playerName);
        return getPlaytime(uuid);
    }

    public static Component getPlaytime(UUID uuid) {
        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(uuid);

        if (playtimePlayer == null) {
            return MiniMessage.get().parse(Config.Messages.NO_PLAYTIME_STORED.getMessage());
        }

        return MiniMessage.get().parse(buildMessage(playtimePlayer));
    }

    private static String buildMessage(PlaytimePlayer playtimePlayer) {
        String header = Config.Messages.PLAYTIME_FORMAT_HEADER.getMessage();
        String format = Config.Messages.PLAYTIME_FORMAT.getMessage();
        String footer = Config.Messages.PLAYTIME_FORMAT_FOOTER.getMessage();
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(header.replaceAll("%player%", getPlayerName(playtimePlayer.getUuid())));

        for (String server: Config.TRACKED_SERVERS) {
            long playtime = playtimePlayer.getPlaytimeOnServer(server).getPlaytime();
            if (playtime == 0) continue;
            stringBuilder.append(format.replaceAll("%server%", server).replaceAll("%time%", Utilities.convertTime(playtime)));
        }

        stringBuilder.append(footer.replaceAll("%total%", String.valueOf(playtimePlayer.getTotalPlaytime())));

        return stringBuilder.toString();
    }

    private static UUID getPlayerUUID(String playerName) {
        Playtime instance = Playtime.getInstance();
        if (instance.getServer().getPlayer(playerName).isPresent()) {
            instance.getServer().getPlayer(playerName).get().getUniqueId();
        }

        User user = instance.getLuckPerms().getUserManager().getUser(playerName);
        if (user != null) {
            return user.getUniqueId();
        }
        return null;
    }

    private static String getPlayerName(UUID uuid) {
        Playtime instance = Playtime.getInstance();
        if (instance.getServer().getPlayer(uuid).isPresent()) {
            return instance.getServer().getPlayer(uuid).get().getUsername();
        }
        User user = instance.getLuckPerms().getUserManager().getUser(uuid);
        if (user != null) {
            return user.getUsername();
        }
        return uuid.toString();
    }
}
