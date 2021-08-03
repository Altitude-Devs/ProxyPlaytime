package com.playtime.commands.commandUtils;

import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.util.Utilities;
import com.playtime.util.objects.PlaytimePlayer;
import com.playtime.util.objects.PlaytimeSeen;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Date;
import java.util.UUID;

public class SeenPlayer {

    private static String getPassedTime(Long time) {
        return Utilities.convertTime(new Date().getTime() - time);
    }

    public static Component getLastSeen(String playerName, Player player) {
        return player != null ? getOnlineSeen(playerName, player) : getOfflineSeen(playerName);
    }

    private static Component getOnlineSeen(String playerName, Player player) {
        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(player.getUniqueId());

        if (playtimePlayer == null) return MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", playerName));

        Date currentSessionStart = playtimePlayer.getCurrentSessionStart();
        String passedTime;

        if (currentSessionStart == null) {
            PlaytimeSeen lastSeen = Queries.getLastSeen(player.getUniqueId());
            if (lastSeen == null || lastSeen.getLastSeen() == 0) return MiniMessage.get().parse(Config.Messages.SEEN_TIME_NULL.getMessage());
            passedTime = getPassedTime(lastSeen.getLastSeen());
        } else {
            passedTime = getPassedTime(currentSessionStart.getTime());
        }

        return MiniMessage.get().parse(Config.Messages.SEEN_FORMAT.getMessage()
                .replaceAll("%player%", player.getUsername())
                .replaceAll("%online/offline%", Config.Messages.SEEN_ONLINE_FORMAT.getMessage())
                .replaceAll("%time%", passedTime)
                .replaceAll("%server%", player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Unknown"));
    }

    private static Component getOfflineSeen(String playerName) {
        UUID uuid = Utilities.getPlayerUUID(playerName);

        if (uuid == null) return MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", playerName));

        PlaytimeSeen lastSeen = Queries.getLastSeen(uuid);

        if (lastSeen == null || lastSeen.getLastSeen() == 0) return MiniMessage.get().parse(Config.Messages.SEEN_TIME_NULL.getMessage());

        return MiniMessage.get().parse(Config.Messages.SEEN_FORMAT.getMessage()
                .replaceAll("%player%", Utilities.getPlayerName(uuid))
                .replaceAll("%online/offline%", Config.Messages.SEEN_OFFLINE_FORMAT.getMessage())
                .replaceAll("%time%", getPassedTime(lastSeen.getLastSeen()))
                .replaceAll("%server%", lastSeen.getServer()));
    }
}
