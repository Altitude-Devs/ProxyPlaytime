package com.playtime.util.objects;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlaytimeSeen {
    private final UUID uuid;
    private String server;
    private Long lastSeen;
    private final ConcurrentHashMap<String, Long> lastSeenUpdate = new ConcurrentHashMap<>();

    public PlaytimeSeen(UUID uuid, String server, Long lastSeen) {
        this.uuid = uuid;
        this.server = server;
        this.lastSeen = lastSeen;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getServer() {
        return server;
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String server, long lastSeen) {
        lastSeenUpdate.put(server, lastSeen);
        this.server = server;
        this.lastSeen = lastSeen;
    }

    public ConcurrentHashMap<String, Long> getLastSeenUpdate() {
        return lastSeenUpdate;
    }
}
