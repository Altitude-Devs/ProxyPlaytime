package com.playtime;

import com.google.inject.Inject;
import com.playtime.config.Config;
import com.playtime.database.DatabaseManager;
import com.playtime.task.PlaytimeDataProcessor;
import com.playtime.util.LogoutTracker;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

@Plugin(id = "proxyplaytime", name = "Proxy Playtime", version = "0.1.0-SNAPSHOT",
        url = "https://alttd.com", description = "Tracks and displays playtime per user", authors = {"Teri"})
public class Playtime {
    private final ProxyServer server;
    private final Logger logger;
    private static Playtime instance;
    private LuckPerms luckPerms;

    @Inject
    public Playtime(ProxyServer server, Logger logger) {
        instance = this;
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Config.init();
        Config.loadConfig();
        if (DatabaseManager.getConnection() != null)
            DatabaseManager.initiate();


        getServer()
                .getScheduler()
                .buildTask(instance, () -> new Thread(new PlaytimeDataProcessor()).start())
                .delay(Config.AUTO_SAVE, TimeUnit.MINUTES)
                .repeat(Config.AUTO_SAVE, TimeUnit.MINUTES)
                .schedule();

        getServer()
                .getScheduler()
                .buildTask(instance, LogoutTracker::handleQueue)
                .delay(1L, TimeUnit.MINUTES)
                .repeat(1L, TimeUnit.MINUTES)
                .schedule();
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public static Playtime getInstance() {
        return instance;
    }

    public LuckPerms getLuckPerms() {
        if(luckPerms == null)
            luckPerms = LuckPermsProvider.get();
        return luckPerms;
    }
}
