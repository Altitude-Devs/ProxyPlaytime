package com.playtime.events;

import com.playtime.Playtime;
import com.playtime.database.Queries;
import com.playtime.maps.Maps;
import com.playtime.util.Utilities;
import com.playtime.util.objects.Groups;
import com.playtime.util.objects.PlaytimePlayer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LoginEvent {

    private final LuckPerms luckPerms = Playtime.getInstance().getLuckPerms();

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(uuid);

        if (playtimePlayer == null)
            playtimePlayer = new PlaytimePlayer(uuid, new HashMap<>());
        else
        { //only run this if the player isn't new
            User user = luckPerms.getUserManager().getUser(uuid);
            if (user == null)
                Playtime.getInstance().getLogger().warn("Unable to find user for " + event.getPlayer().getUsername() + ", can't add playtime ranks");
            else
            {
                if (Utilities.getHighestPlaytimeRank(user) == null)
                    addMaxPtRankForUser(user, playtimePlayer);
            }
        }

        String serverName = event.getServer().getServerInfo().getName();

        if (serverName.isEmpty()) return;

        playtimePlayer.updateServerTime(serverName);
    }

    private void addMaxPtRankForUser(User user, PlaytimePlayer playtimePlayer) {
        long totalPlaytime = TimeUnit.MILLISECONDS.toMinutes(playtimePlayer.getTotalPlaytime());
        Groups groups = Maps.groups.values().stream()
                .filter(group -> group.getTimeRequired() > totalPlaytime)
                .min(Comparator.comparing(Groups::getTimeRequired))
                .orElse(Maps.groups.values().stream()
                        .max(Comparator.comparing(Groups::getTimeRequired))
                        .orElse(null));
            if (groups == null) {
                Playtime.getInstance().getLogger().warn("Unable to find playtime ranks");
                return;
            }
        Group group = luckPerms.getGroupManager().getGroup(groups.getGroupName());
        if (group == null)
        {
            Playtime.getInstance().getLogger().warn("Unable to find playtime ranks");
            return;
        }
        user.data().add(InheritanceNode.builder(group).build());
        luckPerms.getUserManager().saveUser(user);
    }
}
