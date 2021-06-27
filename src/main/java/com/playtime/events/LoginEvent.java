package com.playtime.events;

import com.playtime.Playtime;
import com.playtime.database.Queries;
import com.playtime.maps.Maps;
import com.playtime.util.objects.PlaytimePlayer;
import com.playtime.util.objects.PlaytimeSeen;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import java.util.HashMap;
import java.util.UUID;

public class LoginEvent {

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(uniqueId);

        if (playtimePlayer == null) {
            playtimePlayer = new PlaytimePlayer(uniqueId, new HashMap<>());
        }

        String serverName = event.getServer().getServerInfo().getName();

        if (serverName.isEmpty()) return;

        playtimePlayer.updateServerTime(serverName);
    }
}
