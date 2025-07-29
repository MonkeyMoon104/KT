package com.monkey.kt.storage;

import com.monkey.kt.KT;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseManager {

    private final KT plugin;
    private Connection connection;

    public DatabaseManager(KT plugin) {
        this.plugin = plugin;
    }

    public void loadDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/kt.db");

            new SQLTableInitializer(connection).createTables();
            EffectStorage.init(connection);

        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not load database!");
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

}
