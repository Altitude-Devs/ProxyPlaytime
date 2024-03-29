package com.playtime;

import com.google.inject.Inject;
import com.playtime.commands.PlaytimeCommand;
import com.playtime.commands.SeenCMD;
import com.playtime.config.Config;
import com.playtime.database.DatabaseManager;
import com.playtime.events.LoginEvent;
import com.playtime.events.LogoutEvent;
import com.playtime.events.PluginMessage;
import com.playtime.handlers.ServerHandler;
import com.playtime.task.PlaytimeDataProcessor;
import com.playtime.task.LogoutTracker;
import com.playtime.task.UpdatePlaytimeTop;
import com.playtime.util.objects.PlaytimeTop;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Plugin(id = "proxyplaytime", name = "Proxy Playtime", version = "0.1.0-SNAPSHOT",
        url = "https://alttd.com", description = "Tracks and displays playtime per user", authors = {"Teri"})
public class Playtime { //TODO only track playtime on servers in config
    private final ProxyServer server;
    private final Logger logger;
    private static Playtime instance;
    private LuckPerms luckPerms;
    private final Path dataDirectory;
    ScheduledTask autoSave;
    ScheduledTask logoutTracker;

    private ServerHandler serverHandler;
    private final ChannelIdentifier channelIdentifier = MinecraftChannelIdentifier.from("playtime:playtime");

    @Inject
    public Playtime(ProxyServer server, Logger logger, @DataDirectory Path proxydataDirectory) {
        instance = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = proxydataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Config.init(getDataDirectory());
        //Config.loadConfig();
        serverHandler = new ServerHandler();
        if (DatabaseManager.getConnection() != null)
            DatabaseManager.initiate();


        autoSave = getServer()
                .getScheduler()
                .buildTask(instance, () -> new Thread(new PlaytimeDataProcessor()).start())
                .delay(Config.AUTO_SAVE, TimeUnit.MINUTES)
                .repeat(Config.AUTO_SAVE, TimeUnit.MINUTES)
                .schedule();

        logoutTracker = getServer()
                .getScheduler()
                .buildTask(instance, LogoutTracker::handleQueue)
                .delay(1L, TimeUnit.MINUTES)
                .repeat(1L, TimeUnit.MINUTES)
                .schedule();

        server.getEventManager().register(instance, new LoginEvent());
        server.getEventManager().register(instance, new LogoutEvent());
        server.getEventManager().register(instance, new PluginMessage(channelIdentifier));

        PlaytimeTop playtimeTop = new PlaytimeTop();
        UpdatePlaytimeTop updatePlaytimeTop = new UpdatePlaytimeTop(playtimeTop);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(updatePlaytimeTop, 0, Config.TOP_UPDATE_MINUTES, TimeUnit.MINUTES);
        server.getCommandManager().register("playtime", new PlaytimeCommand(server, playtimeTop), "pt");
        server.getCommandManager().register("seen", new SeenCMD(server));
//        new PlaytimeCMD().createPlaytimeCommand(server);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        getLogger().info("Starting shutdown process...");
        autoSave.cancel();
        logoutTracker.cancel();

        getLogger().info("Saving player data...");
        Thread thread = new Thread(new PlaytimeDataProcessor());
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getLogger().info("Goodbye!");
//        LogoutTracker.handleQueue();
    }

    private File getDataDirectory() {
        return dataDirectory.toFile();
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
