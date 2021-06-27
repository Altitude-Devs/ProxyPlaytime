package com.playtime.util;

import com.playtime.Playtime;
import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.model.user.User;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Utilities {

    public static String convertTime(long timeInMillis){
        return convertTime((int) TimeUnit.MILLISECONDS.toMinutes(timeInMillis));
    }

    private static String convertTime(int timeInMinutes) {
        int days = (int) TimeUnit.MINUTES.toDays(timeInMinutes);
        int hours = (int) (TimeUnit.MINUTES.toHours(timeInMinutes) - TimeUnit.DAYS.toHours(days));
        int minutes = (int) (TimeUnit.MINUTES.toMinutes(timeInMinutes) - TimeUnit.HOURS.toMinutes(hours)
                - TimeUnit.DAYS.toMinutes(days));

        StringBuilder stringBuilder = new StringBuilder();

        if (days != 0) {
            stringBuilder.append(days).append(days == 1 ? " day, " : " days, ");
        }
        if (hours != 0) {
            stringBuilder.append(hours).append(hours == 1 ? " hour, " : " hours, ");
        }
        stringBuilder.append(minutes).append(minutes == 1 ? " minute, " : " minutes, ");

        return stringBuilder.substring(0, stringBuilder.length() - 2);
    }

    public static UUID getPlayerUUID(String playerName) {
        Playtime instance = Playtime.getInstance();
        Optional<Player> player = instance.getServer().getPlayer(playerName);

        if (player.isPresent()) return player.get().getUniqueId();

        User user = instance.getLuckPerms().getUserManager().getUser(playerName);

        if (user != null) {
            return user.getUniqueId();
        }

        try {
            return instance.getLuckPerms().getUserManager().lookupUniqueId(playerName).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getPlayerName(UUID uuid) {
        Playtime instance = Playtime.getInstance();
        Optional<Player> player = instance.getServer().getPlayer(uuid);

        if (player.isPresent()) return player.get().getUsername();

        User user = instance.getLuckPerms().getUserManager().getUser(uuid);

        if (user != null) {
            return user.getUsername();
        }

        try {
            return instance.getLuckPerms().getUserManager().lookupUsername(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return uuid.toString();
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

}
