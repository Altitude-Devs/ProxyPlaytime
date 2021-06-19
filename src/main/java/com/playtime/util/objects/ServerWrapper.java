package com.playtime.util.objects;

import com.playtime.config.ServerConfig;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public class ServerWrapper {

    private final RegisteredServer registeredServer;
    private final String serverName;

    private final String displayName;
    private final boolean trackPlayTime;

    public ServerWrapper(RegisteredServer registeredServer, ServerConfig serverConfig) {
        this.registeredServer = registeredServer;
        this.serverName = registeredServer.getServerInfo().getName();

        this.displayName = serverConfig.DISPLAYNAME;
        this.trackPlayTime = serverConfig.TRACKPLAYTIME;
    }

    public RegisteredServer getRegisteredServer() {
        return registeredServer;
    }

    public String serverName() {
        return serverName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isTrackPlayTime() {
        return trackPlayTime;
    }
}
