package com.playtime.util.objects;

public class ServerPlaytime {
    String server;
    long playtime;
    long lastSeen;

    public ServerPlaytime(String server, long playtime, long lastSeen) {
        this.server = server;
        this.playtime = playtime;
        this.lastSeen = lastSeen;
    }

    public String getServer() {
        return server;
    }

    public long getPlaytime() {
        return playtime;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void addPlaytime(long time) {
        playtime+=time;
    }
}
