package com.playtime.commands.idkyet;

import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.util.Utilities;
import com.playtime.util.objects.PlaytimeSeen;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Date;
import java.util.UUID;

public class SeenPlayer {

    private static String getPassedTime(Long time) {
        return Utilities.convertTime(new Date().getTime() - time);
    }

    public static Component getLastSeen(String playerName, boolean online) {
        UUID uuid = Utilities.getPlayerUUID(playerName);

        if (uuid == null) return MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", playerName));

        PlaytimeSeen lastSeen = Queries.getLastSeen(uuid);

        if (lastSeen == null || lastSeen.getLastSeen() == 0) return MiniMessage.get().parse(Config.Messages.SEEN_TIME_NULL.getMessage());

        System.out.println(lastSeen.getLastSeen());

        return MiniMessage.get().parse(Config.Messages.SEEN_FORMAT.getMessage()
                .replaceAll("%player%", Utilities.getPlayerName(uuid))
                .replaceAll("%online/offline%", online ? Config.Messages.SEEN_ONLINE_FORMAT.getMessage() : Config.Messages.SEEN_OFFLINE_FORMAT.getMessage())
                .replaceAll("%time%", getPassedTime(lastSeen.getLastSeen()))
                .replaceAll("%server%", lastSeen.getServer()));
    }
}
