package com.monkey.kt.storage;

import java.sql.Connection;
import java.sql.Statement;

public class SQLTableInitializer {

    private final Connection connection;

    public SQLTableInitializer(Connection connection) {
        this.connection = connection;
    }

    public void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS killeffects (uuid TEXT PRIMARY KEY, effect TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
