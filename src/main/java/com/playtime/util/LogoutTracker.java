package com.playtime.util;

import com.playtime.config.Config;
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

            if (playtimePlayer.getLastSavedServerTime().after(calendar.getTime())) {
                Maps.playtimePlayers.remove(playtimePlayer.getUuid());
                iterator.remove();
            }
        }
    }
}
