package com.playtime.commands.commandUtils;

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

        return MiniMessage.miniMessage().deserialize(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("<player>", playerName));
    }

    public static Component getPlaytime(UUID uuid) {
        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(uuid);

        if (playtimePlayer == null) {
            return MiniMessage.miniMessage().deserialize(Config.Messages.NO_PLAYTIME_STORED.getMessage());
        }

        return MiniMessage.miniMessage().deserialize(buildMessage(playtimePlayer));
    }

    private static String buildMessage(PlaytimePlayer playtimePlayer) {
        String header = Config.Messages.PLAYTIME_FORMAT_HEADER.getMessage().replaceAll("<nl>","\n");
        String format = Config.Messages.PLAYTIME_FORMAT.getMessage().replaceAll("<nl>","\n");
        String footer = Config.Messages.PLAYTIME_FORMAT_FOOTER.getMessage().replaceAll("<nl>","\n");
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(header.replaceAll("<player>", Utilities.getPlayerName(playtimePlayer.getUuid()))).append("\n");

        for (String server: Config.TRACKED_SERVERS) {
            ServerPlaytime serverPlaytime = playtimePlayer.getPlaytimeOnServer(server);
            if(serverPlaytime == null) continue;
            long playtime = serverPlaytime.getPlaytime();
            if (playtime == 0) continue;
            stringBuilder.append(format.replaceAll("<server>", Utilities.capitalize(server)).replaceAll("<time>", Utilities.convertTime(playtime))).append("\n");
        }

        stringBuilder.append(footer.replaceAll("<total>", Utilities.convertTime(playtimePlayer.getTotalPlaytime())));

        return stringBuilder.toString();
    }
}
