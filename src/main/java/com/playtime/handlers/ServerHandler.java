package com.playtime.handlers;

import com.playtime.Playtime;
import com.playtime.config.ServerConfig;
import com.playtime.util.objects.ServerWrapper;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.ArrayList;
import java.util.List;

public class ServerHandler {

    private Playtime plugin;

    private static List<ServerWrapper> servers;

    public ServerHandler() {
        plugin = Playtime.getInstance();
        initialize();
    }

    public void cleanup() { // for use on /reload?
        servers.clear();
        initialize();
    }

    public void initialize() {
        servers = new ArrayList<>();

        for (RegisteredServer registeredServer : plugin.getServer().getAllServers()) {
            servers.add(new ServerWrapper(registeredServer, new ServerConfig(registeredServer.getServerInfo().getName())));
        }
    }

    public ServerWrapper getWrapper(String serverName) {
        for(ServerWrapper wrapper : servers) {
            if(wrapper.serverName().equalsIgnoreCase(serverName)) {
                return wrapper;
            }
        }
        return null;
    }
}
