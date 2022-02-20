package com.playtime.util;

import com.playtime.Playtime;
import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.track.Track;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Utilities {

    private static final LuckPerms luckPerms = Playtime.getInstance().getLuckPerms();

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

    public static Group getHighestPlaytimeRank(User user) {
        Track track = luckPerms.getTrackManager().getTrack("default");
        if (track == null)
        {
            Playtime.getInstance().getLogger().warn("Unable to find track default, can't add playtime ranks");
            return null;
        }
        Collection<Group> inheritedGroups = user.getInheritedGroups(QueryOptions.builder(QueryMode.CONTEXTUAL).flag(Flag.RESOLVE_INHERITANCE, false).build());
        return inheritedGroups.stream()
                .filter(track::containsGroup)
                .max(Comparator.comparing(group -> {
                    OptionalInt weight = group.getWeight();
                    if (weight.isEmpty())
                        return -1;
                    return weight.getAsInt();
                })).orElse(null);
//        GroupManager groupManager = luckPerms.getGroupManager();
//        return user.getDistinctNodes().stream()
//                .filter(NodeType.INHERITANCE::matches)
//                .map(NodeType.INHERITANCE::cast)
//                .map(InheritanceNode::getGroupName)
//                .map(groupManager::getGroup)
//                .filter(Objects::nonNull)
//                .filter(track::containsGroup)
//                .findFirst()
//                .orElse(null);
    }

}
