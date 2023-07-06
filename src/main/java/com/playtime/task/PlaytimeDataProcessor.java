package com.playtime.task;

import com.playtime.Playtime;
import com.playtime.database.Queries;
import com.playtime.maps.Maps;
import com.playtime.util.Utilities;
import com.playtime.util.objects.Groups;
import com.playtime.util.objects.PlaytimePlayer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.track.Track;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PlaytimeDataProcessor implements Runnable{

    @Override
    public void run() {
        try {
            CopyOnWriteArrayList<PlaytimePlayer> playtimePlayers = new CopyOnWriteArrayList<>(Maps.playtimePlayers.values());

            for (PlaytimePlayer playtimePlayer : playtimePlayers) {
                if (playtimePlayer == null || !playtimePlayer.isOnline()) continue;
                playtimePlayer.updateServerTime(false);

                autoRank(playtimePlayer);
            }

            Queries.batchUpdatePlaytime();
            Queries.batchUpdateSessions();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void autoRank(PlaytimePlayer playtimePlayer) throws SQLException {
        Player player = getPlayer(playtimePlayer.getUuid());

        if (player == null) { //TODO figure out why this can happen and how to stop it
            Playtime.getInstance().getLogger().warn("Unable to load player " + playtimePlayer.getUuid() + " during auto rank ");
            return;
        }

        User user = getLuckPermsUser(playtimePlayer.getUuid());
        Group ptRank = Utilities.getHighestPlaytimeRank(user);
        if (ptRank == null)
        {
            Playtime.getInstance().getLogger().warn("Unable to find playtime rank for user: " + user.getUsername());
            return;
        }
        String highestPtGroup = ptRank.getName();
        if (!Maps.groups.containsKey(highestPtGroup)) return;

        Groups group = Maps.groups.get(highestPtGroup);

        if (group == null || TimeUnit.MILLISECONDS.toMinutes(playtimePlayer.getTotalPlaytime()) < group.getTimeRequired()) return;

        //Let everyone know the user got ranked up
        player.getCurrentServer().ifPresent(serverConnection -> serverConnection.getServer()
                .sendMessage(MiniMessage.miniMessage().deserialize(group.getBroadcastMessage().replaceAll("<player>", player.getUsername()))));
        //Send the message that they got ranked up
        String titleMessage = group.getPlayerTitleMessage().replaceAll("<player>", player.getUsername());
        String[] splitMessage;
        if (titleMessage.contains("\\n")) {
            splitMessage = titleMessage.split("\\n", 2);
        } else {
            splitMessage = new String[]{titleMessage, ""};
        }
        player.showTitle(Title.title(MiniMessage.miniMessage().deserialize(splitMessage[0]), MiniMessage.miniMessage().deserialize(splitMessage[1])));
        //Update the group
        rankUpPlayer(user, group.getTrack());
    }

    private void rankUpPlayer(User user, String track_name) {
        LuckPerms luckPerms = Playtime.getInstance().getLuckPerms();

        Track track = luckPerms.getTrackManager().getTrack(track_name);

        if (track == null) return;

        ImmutableContextSet contextSet = ImmutableContextSet.builder()
                .add("world", "global")
                .add("server", "global")
                .build();

        track.promote(user, contextSet);
        Playtime.getInstance().getLuckPerms().getUserManager().saveUser(user);
    }

    private User getLuckPermsUser(UUID uuid) {
        LuckPerms luckPerms = Playtime.getInstance().getLuckPerms();
        User user = null;

        try {
            user = luckPerms.getUserManager().loadUser(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return user;
    }

    private Player getPlayer(UUID uuid) {
        Optional<Player> player = Playtime.getInstance().getServer().getPlayer(uuid);
        return player.orElse(null);

    }
}
