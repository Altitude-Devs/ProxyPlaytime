package com.playtime.util.objects;

import com.playtime.Playtime;
import com.velocitypowered.api.proxy.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TopPlayer {

    private String username;
    private final UUID uuid;
    private final Long millisecondSessionTime;
    private boolean properName;

    public TopPlayer(String username, UUID uuid, Long millisecondSessionTime, boolean properName) {
        this.username = username;
        this.uuid = uuid;
        this.millisecondSessionTime = millisecondSessionTime;
        this.properName = properName;
    }

    public String getFormattedTime() {
        long days = TimeUnit.MILLISECONDS.toDays(millisecondSessionTime);
        long hours = TimeUnit.MILLISECONDS.toHours(millisecondSessionTime) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisecondSessionTime) % 60;

        return (days == 0 ? "" : days + " days, ") +
                (hours == 0 ? "" : hours + " hours, ") +
                (minutes + " minutes");
    }

    public String getUsername() {
        if (properName)
            return username;
        Optional<Player> optionalPlayer = Playtime.getInstance().getServer().getPlayer(uuid);
        if (optionalPlayer.isEmpty())
            return username;
        username = optionalPlayer.get().getUsername();
        properName = true;
        return username;
    }

    public long getSessionTime() {
        return millisecondSessionTime;
    }

}
