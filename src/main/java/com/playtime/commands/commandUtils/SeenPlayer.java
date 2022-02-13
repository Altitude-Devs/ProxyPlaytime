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

    public static Component getLastSeen(UUID uuid, Player player) {
        return player != null ? getOnlineSeen(uuid, player) : getOfflineSeen(uuid);
    }

    private static Component getOnlineSeen(String playerName, Player player) {
        UUID uuid = player.getUniqueId();
        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(uuid);

        if (playtimePlayer == null) return MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", playerName));

        return getOnlineSeen(playtimePlayer, player);
    }

    private static Component getOnlineSeen(UUID uuid, Player player) {
        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(uuid);

        if (playtimePlayer == null) return Component.empty();

        return getOnlineSeen(playtimePlayer, player);
    }

    private static Component getOnlineSeen(PlaytimePlayer playtimePlayer, Player player) {

        Date currentSessionStart = playtimePlayer.getCurrentSessionStart();
        String passedTime;

        if (currentSessionStart == null) {
            PlaytimeSeen lastSeen = Queries.getLastSeen(playtimePlayer.getUuid());
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

        return getOfflineSeen(uuid);
    }

    private static Component getOfflineSeen(UUID uuid) {
        if (uuid == null) return Component.empty();

        PlaytimeSeen lastSeen = Queries.getLastSeen(uuid);

        if (lastSeen == null || lastSeen.getLastSeen() == 0) return MiniMessage.get().parse(Config.Messages.SEEN_TIME_NULL.getMessage());

        return MiniMessage.get().parse(Config.Messages.SEEN_FORMAT.getMessage()
                .replaceAll("%player%", Utilities.getPlayerName(uuid))
                .replaceAll("%online/offline%", Config.Messages.SEEN_OFFLINE_FORMAT.getMessage())
                .replaceAll("%time%", getPassedTime(lastSeen.getLastSeen()))
                .replaceAll("%server%", lastSeen.getServer()));
    }
}
