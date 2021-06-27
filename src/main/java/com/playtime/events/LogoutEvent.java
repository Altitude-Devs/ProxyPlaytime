package com.playtime.events;

import com.playtime.maps.Maps;
import com.playtime.util.objects.PlaytimePlayer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;

import java.util.UUID;

public class LogoutEvent {

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        //TODO only run this when someone actually logs out, not when they swap servers maybe i forgot...
        UUID uuid = event.getPlayer().getUniqueId();

        if (!Maps.playtimePlayers.containsKey(uuid)) return;

        PlaytimePlayer playtimePlayer = Maps.playtimePlayers.get(uuid);
        playtimePlayer.updateServerTime(true);
    }
}
