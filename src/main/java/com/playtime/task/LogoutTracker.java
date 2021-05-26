package com.playtime.task;

import com.playtime.Playtime;
import com.playtime.config.Config;
import com.playtime.database.Queries;
import com.playtime.maps.Maps;
import com.playtime.util.objects.PlaytimePlayer;

import java.util.Calendar;
import java.util.Iterator;

public class LogoutTracker {

    public static void handleQueue() {
        Iterator<PlaytimePlayer> iterator = Maps.loggedOutPlayers.iterator();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, Config.PURGE_OFFLINE_USERS_FROM_CACHE);

        while (iterator.hasNext()) {
            PlaytimePlayer playtimePlayer = iterator.next();
            if (playtimePlayer.isOnline()) {
                iterator.remove();
                return;
            }
            Playtime.getInstance().getLogger().info("Handling logout queue for player: " + playtimePlayer.getUuid()); //TODO debug
            if (playtimePlayer.getLastSavedServerTime().after(calendar.getTime())) {
                Playtime.getInstance().getLogger().info("Removing player: " + playtimePlayer.getUuid() + " from the cache"); //TODO debug
                Maps.playtimePlayers.remove(playtimePlayer.getUuid());
                iterator.remove();
                Queries.updatePlaytime(playtimePlayer);
                Queries.updateSessions(playtimePlayer);
            }
        }
    }
}
