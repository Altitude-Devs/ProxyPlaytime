package com.playtime.database;

import com.playtime.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static Connection connection = null;

    public static void initiate() {
        openConnection();
        createPlaytimeTable();
        createSessionsTable();
        createOldSessionsTable();
    }

    private static void openConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            connection = DriverManager.getConnection(
                    "jdbc:" + Config.DRIVER + "://" + Config.IP + ":" + Config.PORT + "/" + Config.DATABASE, Config.USERNAME,
                    Config.PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) return connection;
            openConnection();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        return connection;
    }

    private static void createPlaytimeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS playtime (" +
                "uuid VARCHAR(36) NOT NULL, " +
                "server_name VARCHAR(32) NOT NULL, " +
                "playtime BIGINT(19) NOT NULL, " +
                "last_seen BIGINT(19) DEFAULT 0, " +
                "PRIMARY KEY(uuid, server_name)" +
                ");";
        try {
            Statement statement = getConnection().createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createSessionsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS sessions (" +
                "id int NOT NULL AUTO_INCREMENT, " +
                "uuid VARCHAR(36) NOT NULL, " +
                "server_name VARCHAR(32) NOT NULL, " +
                "session_start BIGINT(19) NOT NULL, " +
                "session_end BIGINT(19) NOT NULL, " +
                "PRIMARY KEY(id)" +
                ");";
        try {
            Statement statement = getConnection().createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createOldSessionsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS old_sessions (" +
                "id int NOT NULL AUTO_INCREMENT, " +
                "uuid VARCHAR(36) NOT NULL, " +
                "server_name VARCHAR(32) NOT NULL, " +
                "session_start BIGINT(19) NOT NULL, " +
                "session_end BIGINT(19) NOT NULL, " +
                "PRIMARY KEY(id)" +
                ");";
        try {
            Statement statement = getConnection().createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
