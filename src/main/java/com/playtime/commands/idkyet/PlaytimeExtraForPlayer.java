package com.playtime.commands.idkyet;

import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.util.Utilities;
import com.playtime.util.objects.PlaytimePlayer;
import com.playtime.util.objects.ServerPlaytime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlaytimeExtraForPlayer {

    public static Component getPlaytime(String playerName) {
        UUID uuid = Utilities.getPlayerUUID(playerName);

        if (uuid != null) return getPlaytime(uuid);

        return MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", playerName));
    }

    public static Component getPlaytime(UUID uuid) {
        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(uuid);

        if (playtimePlayer == null) return MiniMessage.get().parse(Config.Messages.NO_PLAYTIME_STORED.getMessage().replaceAll("%player%", Utilities.getPlayerName(uuid)));

        return MiniMessage.get().parse(buildMessage(playtimePlayer));
    }

    private static String buildMessage(PlaytimePlayer playtimePlayer) {
        String header = Config.Messages.PLAYTIME_FORMAT_HEADER.getMessage();
        String format = Config.Messages.PLAYTIME_FORMAT.getMessage();
        String footer = Config.Messages.PLAYTIME_FORMAT_FOOTER.getMessage();
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(header.replaceAll("%player%", Utilities.getPlayerName(playtimePlayer.getUuid()))).append("\n");

        for (String server: Config.TRACKED_SERVERS) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -30);
            long time = Queries.getExtraPlaytime(server, playtimePlayer.getUuid(), calendar.getTimeInMillis(),  new Date().getTime());
            if(time == 0) continue;
            stringBuilder.append(format.replaceAll("%server%", Utilities.capitalize(server)).replaceAll("%time%", Utilities.convertTime(time))).append("\n");
        }

        stringBuilder.append(footer.replaceAll("%total%", Utilities.convertTime(playtimePlayer.getTotalPlaytime())));

        return stringBuilder.toString();
    }
}
