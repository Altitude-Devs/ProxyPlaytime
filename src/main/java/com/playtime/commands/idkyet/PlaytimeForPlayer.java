package com.playtime.commands.idkyet;

import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.util.Utilities;
import com.playtime.util.objects.PlaytimePlayer;
import com.playtime.util.objects.ServerPlaytime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.UUID;

public class PlaytimeForPlayer {

    public static Component getPlaytime(String playerName) {
        UUID uuid = Utilities.getPlayerUUID(playerName);

        if (uuid != null) return getPlaytime(uuid);

        return MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", playerName));
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

        stringBuilder.append(header.replaceAll("%player%", Utilities.getPlayerName(playtimePlayer.getUuid()))).append("\n");

        for (String server: Config.TRACKED_SERVERS) {
            ServerPlaytime serverPlaytime = playtimePlayer.getPlaytimeOnServer(server);
            if(serverPlaytime == null) continue;
            long playtime = serverPlaytime.getPlaytime();
            if (playtime == 0) continue;
            stringBuilder.append(format.replaceAll("%server%", capitalize(server)).replaceAll("%time%", Utilities.convertTime(playtime))).append("\n");
        }

        stringBuilder.append(footer.replaceAll("%total%", Utilities.convertTime(playtimePlayer.getTotalPlaytime())));

        return stringBuilder.toString();
    }

    private static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
