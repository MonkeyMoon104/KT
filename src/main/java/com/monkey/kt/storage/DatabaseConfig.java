package com.monkey.kt.storage;

public class DatabaseConfig {
    public final DatabaseDialect dialect;
    public final String host;
    public final int port;
    public final String database;
    public final String username;
    public final String password;
    public final String connectionString;

    public DatabaseConfig(DatabaseDialect dialect, String host, int port, String database,
                          String username, String password, String connectionString) {
        this.dialect = dialect;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.connectionString = connectionString;
    }

    public boolean requiresCredentials() {
        return dialect.requiresCredentials();
    }

    public boolean isFileDatabase() {
        return dialect.isFileDatabase();
    }
}