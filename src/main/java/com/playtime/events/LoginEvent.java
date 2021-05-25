package com.playtime.events;

import com.playtime.database.Queries;
import com.playtime.util.objects.PlaytimePlayer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import java.util.HashMap;

public class LoginEvent {

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {

        Player player = event.getPlayer();
        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(player);
        if (playtimePlayer == null) {
            playtimePlayer = new PlaytimePlayer(player.getUniqueId(), new HashMap<>());
        }
        String serverName = event.getServer().getServerInfo().getName();
        if (serverName.isEmpty()) return;
        playtimePlayer.updateServerTime(serverName);
        if (event.getPreviousServer().isPresent()) {
            playtimePlayer.startNewSession(serverName);
        }
    }
}
