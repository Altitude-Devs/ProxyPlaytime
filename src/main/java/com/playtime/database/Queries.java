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

    public static PlaytimeSeen getLastSeen(UUID uuid) {
        if (Maps.playtimeSeen.containsKey(uuid)) {
            return Maps.playtimeSeen.get(uuid);
        }

        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement("SELECT server_name, last_seen FROM playtime WHERE uuid = ? ORDER BY last_seen DESC LIMIT 1");
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                PlaytimeSeen playtimeSeen = new PlaytimeSeen(uuid, resultSet.getString("server_name"), resultSet.getLong("last_seen"));
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

    public static boolean movePlaytime(UUID uuidFrom, UUID uuidTo, boolean set) {
        savePlayerTime(uuidFrom);
        savePlayerTime(uuidTo);

//INSERT INTO playtime (uuid, server_name, playtime, last_seen) VALUES ('14904acd-d538-426c-ac56-3863311b133c', 'valley', 500000, 0) ON DUPLICATE KEY UPDATE playtime = playtime + 50000, last_seen = 0
        String sqlSelect = "SELECT uuid, server_name, playtime, last_seen FROM playtime WHERE uuid = ?";
        String sqlInsert = "INSERT INTO playtime (uuid, server_name, playtime, last_seen) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE playtime = " + (set ? "" : "playtime + ") + "?, last_seen = ?";

        try {
            PreparedStatement statementSelect = DatabaseManager.getConnection().prepareStatement(sqlSelect);
            statementSelect.setString(1, uuidFrom.toString());

            ResultSet resultSet = statementSelect.executeQuery();

            PreparedStatement statementInsert = DatabaseManager.getConnection().prepareStatement(sqlInsert);

            while (resultSet.next()) {
                long playtime = resultSet.getLong("playtime");
                long last_seen = resultSet.getLong("last_seen");
                statementInsert.setString(1, uuidTo.toString());
                statementInsert.setString(2, resultSet.getString("server_name"));
                statementInsert.setLong(3, playtime);
                statementInsert.setLong(4, last_seen);
                statementInsert.setLong(5, playtime);
                statementInsert.setLong(6, last_seen);

                statementInsert.addBatch();
            }

            int[] ints = statementInsert.executeBatch();

            syncPlayerTime(uuidFrom);
            syncPlayerTime(uuidTo);

            return ints.length != 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    private static void savePlayerTime(UUID uuid) {
        if (Maps.playtimePlayers.containsKey(uuid)) {
            updatePlaytime(Maps.playtimePlayers.get(uuid));
        }
    }

    private static void syncPlayerTime(UUID uuid) {
        if (Maps.playtimePlayers.containsKey(uuid)) {
            Maps.playtimePlayers.remove(uuid);
            Maps.playtimePlayers.put(uuid, getPlaytimePlayer(uuid));
        }
    }

    public static HashMap<String, Long> getExtraPlaytime(UUID uuid, long min, long max) {
        String sql = "SELECT SUM((session_end - session_start)) AS session_length, server_name FROM sessions " +
                "WHERE uuid = ? AND session_start > ? AND session_end < ? " +
                "GROUP BY server_name";

        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql);

            statement.setString(1, uuid.toString());
            statement.setLong(2, min);
            statement.setLong(3, max);

            return calculateSessionTime(statement.executeQuery());

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return new HashMap<>();
    }

    private static HashMap<String, Long> calculateSessionTime(ResultSet resultSet) throws SQLException {
        HashMap<String, Long> sessionLength = new HashMap<>();
        while (resultSet.next()) {
            sessionLength.put(resultSet.getString("server_name"), resultSet.getLong("session_length"));
        }
        return sessionLength;
    }
}
