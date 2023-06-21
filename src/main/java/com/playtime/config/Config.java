package com.playtime.config;

import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import com.playtime.maps.Maps;
import com.playtime.util.objects.Groups;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

public final class Config {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER = "The main configuration file";

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
            config = configLoader.load(ConfigurationOptions.defaults().setHeader(HEADER));
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
        loadConfig();
    }

    public static void reload() {
        init(new File(CONFIG_FILE.getParent()));
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
        return config.getNode(splitPath(path));
    }

    /**
     * ONLY EDIT ANYTHING BELOW THIS LINE
     **/

    public enum Messages {
        PLAYTIME_FORMAT_HEADER("playtime-format-header", "<white><st>-------</st></white><dark_gray>[<st>--</st></dark_gray><gold> <player>'s playtime:</gold> <dark_gray><st>--</st>]</dark_gray><white><st>-------</white></st>"),
        PLAYTIME_FORMAT("playtime-format", "<gold>%server%</gold>: %time%"),
        PLAYTIME_FORMAT_FOOTER("playtime-format-footer", "<white><st>------------</st></white>", "<gold>Total</gold><white>: %total%</white>"
                , "<white><st>--------------</st></white><dark_gray>[<st>--</st></dark_gray>    <dark_gray><st>--</st>]</dark_gray><white><st>--------------</st></white>"),
        PLAYTIME_EXTENDED_FORMAT_HEADER("playtime-extended-format-header", "<white><st>-------</white></st><dark_gray>[<st>--</st></dark_gray><gold> <player>'s playtime %time%:</gold> <dark_gray><st>--</st>]</dark_gray><white><st>-------</st></white>"),
        PLAYTIME_EXTENDED_FORMAT("playtime-extended-format", "<gold>%server%</gold><white>: %time%</white>"),
        PLAYTIME_EXTENDED_FORMAT_FOOTER("playtime-extended-format-footer", "<white><st>--------------</white></st><dark_gray>[<st>--</st></dark_gray>    <dark_gray><st>--</st>]</dark_gray><white><st>--------------</st></white>"),
        NO_PERMISSION("messages.no-permission","<red>You do not have permission to do that command.</red>"),
        NO_PLAYTIME_SERVER("messages.no-playtime-server", "<red><player> does not have any playtime on %server%.</red>"),
        PLAYER_NOT_FOUND("messages.player-not-found", "<red><player> is not a valid player.</red>"),
        INVALID_SERVER("messages.invalid-server", "<red>%server% server does not exist.</red>"),
        NO_PLAYTIME_STORED("messages.no-playtime-stored", "<red><player> does not have any playtime stored.</red>"),
        INVALID_SET_COMMAND("messages.invalid-set-command", "<red>Invalid Usage. <gold>/playtime set <player> <server> <time></gold></red>"),
        MOVED_PLAYTIME("messages.moved-playtime", "<green><playerFrom>'s time was successfully moved to <playerTo>'s</green>"),
        FAILED_MOVED_PLAYTIME("messages.failed-moved-playtime", "<red>Unable to move <playerFrom>'s time to <playerTo></red>"),
        INVALID_EXTENDED_PLAYTIME_COMMAND("messages.playtime-extended-invalid-command", "<red>Invalid Usage. <gold>/playtime extra <player> <day/week> [amount]</gold></red>"),
        INVALID_PLAYTIME_MOVE_COMMAND("messages.playtime-move-invalid-command", "<red>Invalid Usage. <gold>/playtime move <player to move from> <player to move to> <add/set></gold></red>"),
        INVALID_SEEN_COMMAND("messages.invalid-seen-command", "<red>Invalid Usage. <gold>/seen <player></gold></red>"),
        SEEN_FORMAT("messages.seen-format", "<white>Player <gold><player></gold> has been %online/offline% for %time% on %server%.</white>"),
        SEEN_ONLINE_FORMAT("messages.seen-online-format", "<green>online</green>"),
        SEEN_OFFLINE_FORMAT("messages.seen-offline-format", "<red>offline</red>"),
        SEEN_TIME_NULL("messages.seen-time-null", "<red>No recorded time.</red>"),
        INVALID_PLAYTIME_RESET_COMMAND("messages.invalid-playtime-reset-command", "<red>Invalid Usage. <gold>/playtime reset <player></gold>.</red>"),
        PLAYTIME_RESET_SUCCESS("messages.playtime-reset-success", "<green>Reset <player> playtime.</green>"),
        PLAYTIME_RESET_FAILURE("messages.playtime-reset-failure", "<red>Failed to reset <player> playtime.</red>"),
        PLAYTIME_HELP_WRAPPER("messages.playtime-help-wrapper", "<gold>Playtime help:\n<commands></gold>");

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

    private static void loadSeenServers() { // i'd suggest moving this into a serverwrapper and using the serverconfig;)
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

    private static void loadGroups() { // again a wrapper with groupconfig?
        Maps.groups.clear();

        String path = "groups";
        ConfigurationNode node = getNode(path);
        Map<Object, ? extends ConfigurationNode> childrenMap = node.getChildrenMap();

        for (ConfigurationNode configurationNode : childrenMap.values()) {
            String groupName = (String) configurationNode.getKey();
            String subPath = path + "." + groupName + ".";
            int timeRequired = getInt(subPath + "requirement", -1);
            String track = getString(subPath + "track", "");
            String broadcastMessage = getString(subPath + "broadcast-message", "");
            String playerTitleMessage = getString(subPath + "player-title-message", "");

            Maps.groups.put(groupName, new Groups(groupName, timeRequired, track, broadcastMessage, playerTitleMessage));
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

    public static long AUTO_SAVE = 5;
    public static long AUTO_RANK = 5;
    public static String SERVER_DISPLAY_NAME = "";
    public static int PURGE_OFFLINE_USERS_FROM_CACHE = 5; //Make customizable (time in min)
    public static ArrayList<String> TRACKED_SERVERS = new ArrayList<>();

    private static void loadConfig() {
        AUTO_SAVE = getLong("auto-save", 5L);
        AUTO_RANK = getLong("auto-rank", 5L);
        SERVER_DISPLAY_NAME = getString("server", "");
        TRACKED_SERVERS.clear();
        getList("tracked-servers", Arrays.asList("Server1", "Server2")).forEach(a -> TRACKED_SERVERS.add(a.toLowerCase()));

        loadGroups();
        loadSeenServers();
        loadMessages();
    }
}