package com.playtime.events;

import com.playtime.maps.Maps;
import com.playtime.util.objects.PlaytimePlayer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;

import java.util.UUID;

public class LogoutEvent {

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        KickedFromServerEvent.ServerKickResult result = event.getResult();
        System.out.println(result.toString()); //TODO debug statement
        UUID uuid = event.getPlayer().getUniqueId();

        if (!Maps.playtimePlayers.containsKey(uuid)) return;

        PlaytimePlayer playtimePlayer = Maps.playtimePlayers.get(uuid);
        playtimePlayer.updateServerTime(true);
    }
}
