package com.playtime.config;

import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import com.playtime.maps.Maps;
import com.playtime.util.objects.Groups;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public final class Config {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER = "";

    private static File CONFIG_FILE;
    public static ConfigurationNode config;
    public static YAMLConfigurationLoader configLoader;

    static int version;
    static boolean verbose;

    public static void init(File path) {
        CONFIG_FILE = new File(path, "config.yml");

        configLoader = YAMLConfigurationLoader.builder()
                .setFile(CONFIG_FILE)
                .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                .build();
        if (!CONFIG_FILE.getParentFile().exists()) {
            if (!CONFIG_FILE.getParentFile().mkdirs()) {
                return;
            }
        }
        if (!CONFIG_FILE.exists()) {
            try {
                if (!CONFIG_FILE.createNewFile()) {
                    return;
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        try {
            config = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        configLoader.getDefaultOptions().setHeader(HEADER);
        configLoader.getDefaultOptions().withShouldCopyDefaults(true);

        verbose = getBoolean("verbose", true);
        version = getInt("config-version", 1);

        readConfig(Config.class, null);
        try {
            configLoader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readConfig(Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (InvocationTargetException | IllegalAccessException ex) {
                        throw Throwables.propagate(ex.getCause());
                    }
                }
            }
        }
        try {
            configLoader.save(config);
        } catch (IOException ex) {
            throw Throwables.propagate(ex.getCause());
        }
    }

    public static void saveConfig() {
        try {
            configLoader.save(config);
        } catch (IOException ex) {
            throw Throwables.propagate(ex.getCause());
        }
    }

    private static Object[] splitPath(String key) {
        return PATH_PATTERN.split(key);
    }

    private static void set(String path, Object def) {
        if (config.getNode(splitPath(path)).isVirtual())
            config.getNode(splitPath(path)).setValue(def);
    }

    private static void setString(String path, String def) {
        try {
            if (config.getNode(splitPath(path)).isVirtual())
                config.getNode(splitPath(path)).setValue(TypeToken.of(String.class), def);
        } catch (ObjectMappingException ex) {
        }
    }

    private static boolean getBoolean(String path, boolean def) {
        set(path, def);
        return config.getNode(splitPath(path)).getBoolean(def);
    }

    private static double getDouble(String path, double def) {
        set(path, def);
        return config.getNode(splitPath(path)).getDouble(def);
    }

    private static int getInt(String path, int def) {
        set(path, def);
        return config.getNode(splitPath(path)).getInt(def);
    }

    private static String getString(String path, String def) {
        setString(path, def);
        return config.getNode(splitPath(path)).getString(def);
    }

    private static Long getLong(String path, Long def) {
        set(path, def);
        return config.getNode(splitPath(path)).getLong(def);
    }

    private static <T> List<String> getList(String path, T def) {
        try {
            set(path, def);
            return config.getNode(splitPath(path)).getList(TypeToken.of(String.class));
        } catch (ObjectMappingException ex) {
        }
        return new ArrayList<>();
    }

    private static ConfigurationNode getNode(String path) {
//        if (config.getNode(splitPath(path)).isVirtual()) {
//            new RegexConfig("Dummy");
//        }
        return config.getNode(splitPath(path));
    }

    /**
     * ONLY EDIT ANYTHING BELOW THIS LINE
     **/

    public enum Messages {
        PLAYTIME_FORMAT_HEADER("playtime-format-header", "&f&m-------&8[&m--&6 %player% playtime: &8&m--&8]&f&m-------"),
        PLAYTIME_FORMAT("playtime-format", "&6%server%: %time%"),
        PLAYTIME_FORMAT_FOOTER("playtime-format-footer", "&f&m------------", "&6Total: &f%total%", "&f&m--------------&8&m[--&r    &8&m--]&f&m--------------"),
        PLAYTIME_EXTENDED_FORMAT_HEADER("playtime-extended-format-header", "&f&m-------&8[&m--&6 %player% playtime: &8&m--&8]&f&m-------"),
        PLAYTIME_EXTENDED_FORMAT("playtime-extended-format", "&6%type%: %time%"),
        PLAYTIME_EXTENDED_FORMAT_FOOTER("playtime-extended-format-footer", "&f&m--------------&8&m[--&r    &8&m--]&f&m--------------"),
        NO_PERMISSION("messages.no-permission","&cYou do not have permission to do that command."),
        NO_PLAYTIME_SERVER("messages.no-playtime-server", "&c%player% does not have any playtime on %server%."),
        PLAYER_NOT_FOUND("messages.player-not-found", "&c%player% is not a valid player."),
        INVALID_SERVER("messages.invalid-server", "&c%server% server does not exist."),
        NO_PLAYTIME_STORED("messages.no-playtime-stored", "&c%player% does not have any playtime stored in Plan."),
        INVALID_SET_COMMAND("messages.invalid-set-command", "&cInvalid Usage. /playtime set <player> <server> <time>"),
        PLAYER_TIME_CHANGE("messages.player-time-change", "&a%player%'s time was successfully changed for %server%", "&aOldtime ➜ %oldtime%", "&aNewTime ➜ %newtime%"),
        INVALID_SEEN_COMMAND("messages.invalid-seen-command", "&cInvalid Usage. /seen <player>"),
        SEEN_FORMAT("messages.seen-format", "&fPlayer &6%player% &fhas been %online/offline% &ffor %time% on %server%."),
        SEEN_ONLINE_FORMAT("messages.seen-online-format", "&aonline"),
        SEEN_OFFLINE_FORMAT("messages.seen-offline-format", "&coffline"),
        SEEN_TIME_NULL("messages.seen-time-null", "&cNo recorded time.");

        private final String key;
        private String message;

        Messages(String key, String... message) {
            this.key = key;
            this.message = String.join("\n", message);
        }

        public String getMessage() {
            return message;
        }
    }

    private static void loadMessages() {
        for (Messages messages : Messages.values()) {
            messages.message = getString(messages.key, messages.message);
        }
    }

    private static void loadSeenServers() {
        Maps.seenServers.clear();

        String path = "seen-servers-format";
        ConfigurationNode node = getNode(path);
        List<? extends ConfigurationNode> childrenList = node.getChildrenList();

        for (ConfigurationNode configurationNode : childrenList) {
            String server = (String) configurationNode.getKey();
            String serverDisplayName = getString(path + "." + server, server);

            Maps.seenServers.put(server, serverDisplayName);
        }
    }

    private static void loadGroups() {
        Maps.groups.clear();

        String path = "groups";
        ConfigurationNode node = getNode(path);
        List<? extends ConfigurationNode> childrenList = node.getChildrenList();

        for (ConfigurationNode configurationNode : childrenList) {
            String groupName = (String) configurationNode.getKey();
            String subPath = path + "." + groupName + ".";
            int timeRequired = getInt(subPath + "requirement", -1);
            String rankUpCommand = getString(subPath + "rank-up-command", "");
            String broadcastMessage = getString(subPath + "broadcast-message", "");
            String playerTitleMessage = getString(subPath + "player-title-message", "");

            Maps.groups.put(groupName, new Groups(groupName, timeRequired, rankUpCommand, broadcastMessage, playerTitleMessage));
        }
    }

    public static String DRIVER = "mysql";
    public static String IP = "0.0.0.0";
    public static String PORT = "3306";
    public static String DATABASE = "database";
    public static String USERNAME = "root";
    public static String PASSWORD = "root";

    private static void database() {
        DRIVER = getString("database.driver", DRIVER);
        IP = getString("database.ip", IP);
        PORT = getString("database.port", PORT);
        DATABASE = getString("database.name", DATABASE);
        USERNAME = getString("database.username", USERNAME);
        PASSWORD = getString("database.password", PASSWORD);
    }

    public static boolean TRACK_TIME = false;
    public static long AUTO_SAVE = 5;
    public static long AUTO_RANK = 5;
    public static String SERVER_DISPLAY_NAME = "";
    public static int PURGE_OFFLINE_USERS_FROM_CACHE = 5; //Make customizable (time in min)

    public static void loadConfig() {
        loadMessages();
        loadSeenServers();
        loadGroups();
        database();

        TRACK_TIME = getBoolean("tracktime", false);
        AUTO_SAVE = getLong("auto-save", 1L);
        AUTO_RANK = getLong("auto-rank", 1L);
        SERVER_DISPLAY_NAME = getString("server", "");
    }
}