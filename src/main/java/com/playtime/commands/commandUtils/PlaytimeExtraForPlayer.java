package com.playtime.commands.commandUtils;

import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.util.Utilities;
import com.playtime.util.objects.PlaytimePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;

public class PlaytimeExtraForPlayer {

    public static Component getPlaytime(String playerName) {
        return getPlaytime(playerName, 7);
    }

    public static Component getPlaytime(String playerName, int days) {
        UUID uuid = Utilities.getPlayerUUID(playerName);

        if (uuid != null) return getPlaytime(uuid, days);

        return MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", playerName));
    }

    public static Component getPlaytime(UUID uuid) {
        return getPlaytime(uuid, 7);
    }

    public static Component getPlaytime(UUID uuid, int days) {
        return MiniMessage.get().parse(buildMessage(uuid, days));
    }

    public static Component getPlaytimeWeek(String playerName) {
        return getPlaytimeWeek(playerName, 1);
    }

    public static Component getPlaytimeWeek(String playerName, int weeks) {
        UUID uuid = Utilities.getPlayerUUID(playerName);

        if (uuid != null) return getPlaytimeWeek(uuid, weeks);

        return MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", playerName));
    }

    public static Component getPlaytimeWeek(UUID uuid) {
        return getPlaytimeWeek(uuid, 0);
    }

    public static Component getPlaytimeWeek(UUID uuid, int weeks) {
        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(uuid);

        if (playtimePlayer == null) return MiniMessage.get().parse(Config.Messages.NO_PLAYTIME_STORED.getMessage().replaceAll("%player%", Utilities.getPlayerName(uuid)));

        return MiniMessage.get().parse(buildMessageWeek(playtimePlayer, weeks));
    }

    private static String buildMessage(UUID uuid, int days) {
        String header = Config.Messages.PLAYTIME_EXTENDED_FORMAT_HEADER.getMessage().replaceAll("%time%", "in the last " + days + " day" + (days == 1 ? "" : "s")).replaceAll("<nl>","\n");
        String format = Config.Messages.PLAYTIME_EXTENDED_FORMAT.getMessage().replaceAll("<nl>","\n");
        String footer = Config.Messages.PLAYTIME_EXTENDED_FORMAT_FOOTER.getMessage().replaceAll("<nl>","\n");
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(header.replaceAll("%player%", Utilities.getPlayerName(uuid))).append("\n");

        days+=1;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -days);

        HashMap<String, Long> extraPlaytime = Queries.getExtraPlaytime(uuid, calendar.getTimeInMillis(), new Date().getTime());
        if (extraPlaytime.isEmpty()) return Config.Messages.NO_PLAYTIME_STORED.getMessage().replaceAll("%player%", Utilities.getPlayerName(uuid));

        long totalTime = 0;
        for (Map.Entry<String, Long> entry : extraPlaytime.entrySet()) {
            if(!Config.TRACKED_SERVERS.contains(entry.getKey())) continue;
            Long time = entry.getValue();
            totalTime += time;
            stringBuilder.append(format.replaceAll("%server%", Utilities.capitalize(entry.getKey())).replaceAll("%time%", Utilities.convertTime(time))).append("\n");
        }

        stringBuilder.append(footer.replaceAll("%total%", Utilities.convertTime(totalTime)));

        return stringBuilder.toString();
    }

    private static String buildMessageWeek(PlaytimePlayer playtimePlayer, int i) {
        String replacement;
        switch (i) {
            case 0:
                replacement = "for the current week";
                break;
            case 1:
                replacement =  "last week";
                break;
            default:
                replacement = i + " weeks ago";
                break;
        }
        String header = Config.Messages.PLAYTIME_EXTENDED_FORMAT_HEADER.getMessage().replaceAll("<nl>", "\n").replaceAll("%time%", replacement);
        String format = Config.Messages.PLAYTIME_EXTENDED_FORMAT.getMessage().replaceAll("<nl>", "\n");
        String footer = Config.Messages.PLAYTIME_EXTENDED_FORMAT_FOOTER.getMessage().replaceAll("<nl>", "\n");
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(header.replaceAll("%player%", Utilities.getPlayerName(playtimePlayer.getUuid()))).append("\n");

        Calendar calendar = getCalendarAtStartOfCurrentWeek();
        if (i > 0) calendar.add(Calendar.DATE, -(i * 7));
        long firstDayOfWeek = calendar.getTimeInMillis();
        calendar.add(Calendar.DATE, 7);
        long lastDayOfWeek = calendar.getTimeInMillis();

        long totalTime = 0;
        for (Map.Entry<String, Long> entry : Queries.getExtraPlaytime(playtimePlayer.getUuid(), firstDayOfWeek,  lastDayOfWeek).entrySet()) {
            if(!Config.TRACKED_SERVERS.contains(entry.getKey().toLowerCase())) continue;
            Long time = entry.getValue();
            totalTime += time;
            stringBuilder.append(format.replaceAll("%server%", Utilities.capitalize(entry.getKey())).replaceAll("%time%", Utilities.convertTime(time))).append("\n");
        }

        stringBuilder.append(footer.replaceAll("%total%", Utilities.convertTime(totalTime)));

        return stringBuilder.toString();
    }

    private static Calendar getCalendarAtStartOfCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        return calendar;
    }
}
