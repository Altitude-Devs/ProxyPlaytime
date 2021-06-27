package com.playtime.database;

import com.playtime.maps.Maps;
import com.playtime.util.objects.PlaytimePlayer;
import com.playtime.util.objects.PlaytimeSeen;
import com.playtime.util.objects.ServerPlaytime;
import com.playtime.util.objects.ServerSession;
import com.velocitypowered.api.proxy.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class Queries {
    public static void batchUpdatePlaytime() {
        String sql = "INSERT INTO playtime " +
                "(uuid, server_name, playtime, last_seen) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE playtime = ?, last_seen = ?;";
        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql);

            for (PlaytimePlayer playtimePlayer : Maps.playtimePlayers.values()) {
                Iterator<String> iterator = playtimePlayer.getToUpdateServers().iterator();

                while (iterator.hasNext()) {
                    String server = iterator.next();
                    ServerPlaytime playtimeOnServer = playtimePlayer.getPlaytimeOnServer(server);

                    if (playtimeOnServer == null) continue;

                    statement.setString(1, playtimePlayer.getUuid().toString());
                    statement.setString(2, server);
                    statement.setLong(3, playtimeOnServer.getPlaytime());
                    statement.setLong(4, playtimeOnServer.getLastSeen());
                    statement.setLong(5, playtimeOnServer.getPlaytime());
                    statement.setLong(6, playtimeOnServer.getLastSeen());

                    statement.addBatch();
                    iterator.remove();
                }
            }

            statement.executeBatch();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void batchUpdateSessions() {
        String sql = "INSERT INTO sessions " +
                "(uuid, server_name, session_start, session_end) " +
                "VALUES (?, ?, ?, ?);";

        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql);
            for (PlaytimePlayer playtimePlayer : Maps.playtimePlayers.values()) {
                Iterator<ServerSession> iterator = playtimePlayer.getToUpdateSessions().iterator();

                while (iterator.hasNext()) {
                    ServerSession serverSession = iterator.next();

                    if (serverSession == null) continue;

                    statement.setString(1, playtimePlayer.getUuid().toString());
                    statement.setString(2, serverSession.getServerName());
                    statement.setLong(3, serverSession.getSessionStart());
                    statement.setLong(4, serverSession.getSessionEnd());

                    statement.addBatch();
                    iterator.remove();
                }
            }

            statement.executeBatch();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static PlaytimePlayer getPlaytimePlayer(UUID uuid) {
        if (Maps.playtimePlayers.containsKey(uuid)) {
            return Maps.playtimePlayers.get(uuid);
        }

        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement("SELECT * FROM playtime WHERE uuid = ?");
            statement.setString(1, uuid.toString());

            HashMap<String, ServerPlaytime> hashMap = new HashMap<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String serverName = resultSet.getString("server_name");
                hashMap.put(serverName, new ServerPlaytime(serverName, resultSet.getLong("playtime"), resultSet.getLong("last_seen")));
            }

            PlaytimePlayer playtimePlayer = new PlaytimePlayer(uuid, hashMap);
            Maps.playtimePlayers.put(uuid, playtimePlayer);
            return playtimePlayer;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static PlaytimeSeen getLastSeen(Player player) {
        UUID uuid = player.getUniqueId();
        if (Maps.playtimeSeen.containsKey(uuid)) {
            return Maps.playtimeSeen.get(uuid);
        }

        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement("SELECT server, last_seen FROM playtime WHERE uuid = ? ORDER BY last_seen DESC LIMIT 1");
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                PlaytimeSeen playtimeSeen = new PlaytimeSeen(uuid, resultSet.getString("server"), resultSet.getLong("last_seen"));
                Maps.playtimeSeen.put(uuid, playtimeSeen);
                return playtimeSeen;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static void updatePlaytime(PlaytimePlayer playtimePlayer) {
        String sql = "INSERT INTO playtime " +
                "(uuid, server_name, playtime, last_seen) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE playtime = ?, last_seen = ?;";
        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql);

            Iterator<String> iterator = playtimePlayer.getToUpdateServers().iterator();

            while (iterator.hasNext()) {
                String server = iterator.next();
                ServerPlaytime playtimeOnServer = playtimePlayer.getPlaytimeOnServer(server);

                if (playtimeOnServer == null) continue;

                statement.setString(1, playtimePlayer.getUuid().toString());
                statement.setString(2, server);
                statement.setLong(3, playtimeOnServer.getPlaytime());
                statement.setLong(4, playtimeOnServer.getLastSeen());
                statement.setLong(5, playtimeOnServer.getPlaytime());
                statement.setLong(6, playtimeOnServer.getLastSeen());

                statement.addBatch();
                iterator.remove();
            }

            statement.executeBatch();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void updateSessions(PlaytimePlayer playtimePlayer) {
        String sql = "INSERT INTO sessions " +
                "(uuid, server_name, session_start, session_end) " +
                "VALUES (?, ?, ?, ?);";

        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql);

            Iterator<ServerSession> iterator = playtimePlayer.getToUpdateSessions().iterator();

            while (iterator.hasNext()) {
                ServerSession serverSession = iterator.next();

                if (serverSession == null) continue;

                statement.setString(1, playtimePlayer.getUuid().toString());
                statement.setString(2, serverSession.getServerName());
                statement.setLong(3, serverSession.getSessionStart());
                statement.setLong(4, serverSession.getSessionEnd());

                statement.addBatch();
                iterator.remove();
            }

            statement.executeBatch();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static long getExtraPlaytime(String server, UUID uuid, long min, long max) {
        String sql = "SELECT session_start, session_end FROM sessions " +
                "WHERE uuid = ? AND server_name = ? AND session_start > ? AND session_end < ?";

        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql);

            statement.setString(1, uuid.toString());
            statement.setString(2, server);
            statement.setLong(3, min);
            statement.setLong(4, max);

            return calculateSessionTime(statement.executeQuery());

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    private static long calculateSessionTime(ResultSet resultSet) throws SQLException {
        long totalSessionTime = 0;
        while (resultSet.next()) {
            totalSessionTime += resultSet.getLong("session_end") - resultSet.getLong("session_start");
        }
        return totalSessionTime;
    }
}
