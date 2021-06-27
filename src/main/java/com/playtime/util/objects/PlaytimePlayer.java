package com.playtime.util.objects;

import com.playtime.Playtime;
import com.playtime.config.Config;
import com.playtime.maps.Maps;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlaytimePlayer {
    private final UUID uuid;
    private final HashMap<String, ServerPlaytime> playtimePerServer;
    private long totalPlaytime;
    private String currentServer;
    private Date currentSessionStart;
    private Date lastSavedServerTime;
    private final ConcurrentLinkedQueue<ServerSession> toUpdateSessions;
    private final ConcurrentLinkedQueue<String> toUpdateServers;

    private boolean online = true;
    private boolean playtimeUpdated = false;

    public PlaytimePlayer(UUID uuid, HashMap<String, ServerPlaytime> playtimePerServer) {
        this.uuid = uuid;
        this.playtimePerServer = playtimePerServer;
        this.totalPlaytime = -1;
        this.currentServer = "";
        this.currentSessionStart = new Date();
        this.toUpdateSessions = new ConcurrentLinkedQueue<>();
        this.toUpdateServers = new ConcurrentLinkedQueue<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Date getCurrentSessionStart() {
        return currentSessionStart;
    }

    public void updateServerTime(boolean logout) {
        updateServerTime(new Date());

        if (logout) {
            Playtime.getInstance().getLogger().info("Handling logged out player: " + uuid); //TODO debug
            saveSession();
            currentServer = "";
            online = false;
            Maps.loggedOutPlayers.add(this); //Queue player for removal from cache
        }
    }

    public void updateServerTime(String server) {
        online = true;
        Date currentTime = new Date();

        if (currentServer.isEmpty()) {
            currentServer = server;
            Maps.playtimeSeen.put(uuid, new PlaytimeSeen(uuid, currentServer, System.currentTimeMillis()));
            lastSavedServerTime = currentTime;
            return;
        }

        if (!currentServer.equals(server)) {
            saveSession();
        }

        updateServerTime(currentTime);
        currentServer = server;
    }

    private void updateServerTime(Date currentTime) {
        ServerPlaytime serverPlaytime;
        long unsavedTime = currentTime.getTime() - lastSavedServerTime.getTime();

        Playtime.getInstance().getLogger().info("Updating player time on server: " + currentServer + " for player: " + uuid); //TODO debug
        if (playtimePerServer.containsKey(currentServer)) {
            serverPlaytime = playtimePerServer.get(currentServer);
            serverPlaytime.addPlaytime(unsavedTime);
            serverPlaytime.setLastSeen(System.currentTimeMillis());
        } else {
            serverPlaytime = new ServerPlaytime(currentServer, unsavedTime, System.currentTimeMillis());
        }

        playtimeUpdated=false;
        playtimePerServer.put(currentServer, serverPlaytime);
        if (!toUpdateServers.contains(currentServer)) toUpdateServers.add(currentServer);
        lastSavedServerTime = currentTime;
//        Maps.playtimeSeen.put(uuid, new PlaytimeSeen(uuid, currentServer, System.currentTimeMillis())); //Save last seen in cache
    }

    public ServerPlaytime getPlaytimeOnServer(String server) {
        return playtimePerServer.getOrDefault(server, null);
    }

    public long getTotalPlaytime() {
        if (!playtimeUpdated) {
            totalPlaytime = playtimePerServer.values().stream().filter(p -> Config.TRACKED_SERVERS.contains(p.getServer())).map(ServerPlaytime::getPlaytime).mapToLong(Long::longValue).sum();
            playtimeUpdated = true;
        }
        return totalPlaytime;
    }

    public void saveSession() {
        Date currentTime = new Date();

        toUpdateSessions.add(new ServerSession(currentServer, currentSessionStart.getTime(), currentTime.getTime()));
        Maps.playtimeSeen.put(uuid, new PlaytimeSeen(uuid, currentServer, System.currentTimeMillis()));

        currentSessionStart = currentTime;
    }

    public Date getLastSavedServerTime() {
        return lastSavedServerTime;
    }

    public ConcurrentLinkedQueue<ServerSession> getToUpdateSessions() {
        return toUpdateSessions;
    }

    public ConcurrentLinkedQueue<String> getToUpdateServers() {
        return toUpdateServers;
    }

    public boolean isOnline() {
        return online;
    }
}
