package com.playtime.util.objects;

public class ServerSession {
    String serverName;
    long sessionStart;
    long sessionEnd;

    public ServerSession(String serverName, long sessionStart, long sessionEnd) {
        this.serverName = serverName;
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
    }

    public String getServerName() {
        return serverName;
    }

    public long getSessionStart() {
        return sessionStart;
    }

    public long getSessionEnd() {
        return sessionEnd;
    }
}
