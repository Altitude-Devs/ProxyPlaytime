package com.playtime.util.objects;

public class ServerPlaytime {
    long playtime;
    long lastSeen;

    public ServerPlaytime(long playtime, long lastSeen) {
        this.playtime = playtime;
        this.lastSeen = lastSeen;
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
