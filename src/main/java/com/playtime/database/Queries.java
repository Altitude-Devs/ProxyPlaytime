package com.playtime.database;

import com.playtime.Playtime;
import com.playtime.maps.Maps;
import com.playtime.util.objects.*;
import com.velocitypowered.api.proxy.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement("SELECT server_name, last_seen FROM playtime WHERE uuid = ? AND last_seen IS NOT NULL ORDER BY last_seen DESC LIMIT 1");
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

    public static boolean resetPlaytime(UUID uuid) {
        String sql = "DELETE FROM playtime WHERE uuid = ?";
        String sql2 = "DELETE FROM sessions WHERE uuid = ?";
        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql);
            statement.setString(1, uuid.toString());
            PreparedStatement statement2 = DatabaseManager.getConnection().prepareStatement(sql2);
            statement2.setString(1, uuid.toString());

            return statement.executeUpdate() > 0 && statement2.executeUpdate() > 0;
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

    public static void updateTopPlayers(int amount, TopType topType, PlaytimeTop playtimeTop) {
        long calculateFromHere;
        switch (topType) {
            case TOTAL -> {
                updatePtTopTotal(amount, playtimeTop);
                return;
            }
            case MONTHLY -> {
                LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
                calculateFromHere = startOfMonth.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            }
            case WEEKLY -> {
                LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                calculateFromHere = startOfWeek.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            }
            case DAILY -> {
                LocalDate startOfDay = LocalDate.now().atStartOfDay().toLocalDate();
                calculateFromHere = startOfDay.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            }
            default -> calculateFromHere = -1;
        }
        if (calculateFromHere == -1) {
            Playtime.getInstance().getLogger().warn("Unable to update due to invalid calculate from here time");
            return;
        }
        calculateFromHere = TimeUnit.SECONDS.toMillis(calculateFromHere);
        Playtime.getInstance().getLogger().info("Calculating playtime top " + topType.name() + " starting at " + calculateFromHere);
        String sql = """
                SELECT uuid, SUM(
                    CASE
                        WHEN session_start < ? THEN session_end - ?
                        ELSE session_end - session_start
                    END
                ) AS total_online_time
                FROM sessions
                WHERE session_end >= ?
                GROUP BY uuid
                ORDER BY total_online_time DESC
                LIMIT ?;
                """;

        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql);

            statement.setLong(1, calculateFromHere);
            statement.setLong(2, calculateFromHere);
            statement.setLong(3, calculateFromHere);
            statement.setInt(4, amount);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                storeTopPlayer(resultSet, topType, playtimeTop);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void updatePtTopTotal(int amount, PlaytimeTop playtimeTop) {
        String sql = """
                SELECT
                    uuid,
                    SUM(playtime) AS total_online_time
                FROM
                    playtime
                GROUP BY
                    uuid
                ORDER BY `total_online_time` DESC
                LIMIT ?
                """;
        try {
            PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql);

            statement.setInt(1, amount);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                storeTopPlayer(resultSet, TopType.TOTAL, playtimeTop);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void storeTopPlayer(ResultSet resultSet, TopType topType, PlaytimeTop playtimeTop) throws SQLException {
        UUID uuid;
        Playtime instance = Playtime.getInstance();
        String stringUUID = resultSet.getString("uuid");
        try {
            uuid = UUID.fromString(stringUUID);
        } catch (IllegalArgumentException exception) {
            instance.getLogger().warn("Received invalid uuid while processing playtime top: [" + stringUUID + "]");
            return;
        }
        Optional<Player> optionalPlayer = instance.getServer().getPlayer(uuid);
        long totalOnlineMilliseconds = resultSet.getLong("total_online_time");
        if (optionalPlayer.isPresent()) {
            playtimeTop.addToTop(topType, new TopPlayer(optionalPlayer.get().getUsername(), uuid, totalOnlineMilliseconds, true));
            return;
        }
        CompletableFuture<String> stringCompletableFuture = instance.getLuckPerms().getUserManager().lookupUsername(uuid);
        stringCompletableFuture.handle((a, b) -> {
            playtimeTop.addToTop(topType, new TopPlayer(a == null ? stringUUID : a, uuid, totalOnlineMilliseconds, false));
            return a;
        });
    }

}
