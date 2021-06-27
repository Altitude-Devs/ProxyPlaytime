package com.playtime.task;

import com.playtime.Playtime;
import com.playtime.database.Queries;
import com.playtime.maps.Maps;
import com.playtime.util.objects.Groups;
import com.playtime.util.objects.PlaytimePlayer;
import com.playtime.util.objects.PlaytimeSeen;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.track.Track;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PlaytimeDataProcessor implements Runnable{

    @Override
    public void run() {
        try {
            CopyOnWriteArrayList<PlaytimePlayer> playtimePlayers = new CopyOnWriteArrayList<>(Maps.playtimePlayers.values());

            for (PlaytimePlayer playtimePlayer : playtimePlayers) {
                if (!playtimePlayer.isOnline()) continue;
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

        if (player == null) {
            Playtime.getInstance().getLogger().warn("Unable to load player " + playtimePlayer.getUuid() + " during auto rank ");
            return;
        }

        User user = getLuckPermsUser(playtimePlayer.getUuid());
        String primaryGroup = user.getPrimaryGroup();
        if (!Maps.groups.containsKey(primaryGroup)) return;

        Groups group = Maps.groups.get(primaryGroup);

        if (TimeUnit.MILLISECONDS.toMinutes(playtimePlayer.getTotalPlaytime()) < group.getTimeRequired()) return;

        //Let everyone know the user got ranked up
        player.getCurrentServer().ifPresent(serverConnection -> serverConnection.getServer()
                .sendMessage(MiniMessage.get().parse(group.getBroadcastMessage().replaceAll("%player%", player.getUsername()))));
        //Send the message that they got ranked up
        String[] splitMessage = group.getBroadcastMessage().replaceAll("%player%", player.getUsername()).split("\\n", 2);
        player.showTitle(Title.title(MiniMessage.get().parse(splitMessage[0]), MiniMessage.get().parse(splitMessage[1])));
        //Update the group
        setGroup(user, group);
    }

    private void setGroup(User user, Groups group) {
        LuckPerms luckPerms = Playtime.getInstance().getLuckPerms();
        Track aDefault = luckPerms.getTrackManager().getTrack("default");
        if (aDefault == null) return;

        Group group1 = luckPerms.getGroupManager().getGroup(group.getGroupName());
        if (group1 == null) return;

        String next = aDefault.getNext(Objects.requireNonNull(group1));
        if (next == null) return;

        user.setPrimaryGroup(next);
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
        if (player.isEmpty()) return null;

        return player.get();
    }
}
