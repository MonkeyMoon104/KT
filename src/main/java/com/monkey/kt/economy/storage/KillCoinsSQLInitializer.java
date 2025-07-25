package com.monkey.kt.economy.storage;

import java.sql.Connection;
import java.sql.Statement;

public class KillCoinsSQLInitializer {

    private final Connection connection;

    public KillCoinsSQLInitializer(Connection connection) {
        this.connection = connection;
    }

    public void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS killcoins_balance (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "balance DOUBLE NOT NULL DEFAULT 0" +
                    ");");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS killcoins_purchases (" +
                    "uuid TEXT NOT NULL, " +
                    "effect TEXT NOT NULL, " +
                    "PRIMARY KEY (uuid, effect)" +
                    ");");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
