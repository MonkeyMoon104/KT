package com.monkey.kt.storage;

import com.monkey.kt.KT;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DatabaseManager {

    private final KT plugin;
    private HikariDataSource dataSource;
    private DatabaseConfig config;
    private DatabaseExecutor executor;

    public DatabaseManager(KT plugin) {
        this.plugin = plugin;
    }

    public void loadDatabase() {
        try {
            this.config = buildDatabaseConfig();

            Class.forName(config.dialect.getDriverClass());

            setupConnectionPool();

            this.executor = new DatabaseExecutor(dataSource);

            initializeStorage();

            plugin.getLogger().info("Successfully connected to " + config.dialect.name() + " database!");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load database (" +
                    config.dialect.name() + "): " + e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private DatabaseConfig buildDatabaseConfig() {
        String type = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        DatabaseDialect dialect = DatabaseDialect.fromString(type);

        String host = plugin.getConfig().getString("database.host", "localhost");
        int port = plugin.getConfig().getInt("database.port", dialect.getDefaultPort());
        String database = plugin.getConfig().getString("database.database", "kt");
        String username = plugin.getConfig().getString("database.username", "");
        String password = plugin.getConfig().getString("database.password", "");

        String connectionString = dialect.buildConnectionUrl(host, port, database,
                plugin.getDataFolder().getAbsolutePath());

        return new DatabaseConfig(dialect, host, port, database, username, password, connectionString);
    }

    private void setupConnectionPool() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(config.connectionString);

        if (config.requiresCredentials()) {
            hikariConfig.setUsername(config.username);
            hikariConfig.setPassword(config.password);
        }

        if (config.isFileDatabase()) {
            hikariConfig.setMaximumPoolSize(1);
            hikariConfig.setMinimumIdle(1);
        } else {
            hikariConfig.setMaximumPoolSize(8);
            hikariConfig.setMinimumIdle(2);
        }

        hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(20));
        hikariConfig.setIdleTimeout(TimeUnit.MINUTES.toMillis(5));
        hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
        hikariConfig.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(60));

        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setValidationTimeout(TimeUnit.SECONDS.toMillis(5));

        hikariConfig.setInitializationFailTimeout(-1);
        hikariConfig.setIsolateInternalQueries(false);
        hikariConfig.setAllowPoolSuspension(true);
        hikariConfig.setRegisterMbeans(true);

        configureDialectSpecificSettings(hikariConfig);

        hikariConfig.setPoolName("KT-Pool");

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    private void configureDialectSpecificSettings(HikariConfig config) {
        switch (this.config.dialect) {
            case MYSQL:
            case MARIADB:
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
                config.addDataSourceProperty("useLocalSessionState", "true");
                config.addDataSourceProperty("rewriteBatchedStatements", "true");
                config.addDataSourceProperty("cacheResultSetMetadata", "true");
                config.addDataSourceProperty("cacheServerConfiguration", "true");
                config.addDataSourceProperty("elideSetAutoCommits", "true");
                config.addDataSourceProperty("maintainTimeStats", "false");
                break;

            case POSTGRES:
            case COCKROACHDB:
                config.addDataSourceProperty("prepareThreshold", "1");
                config.addDataSourceProperty("preparedStatementCacheQueries", "256");
                config.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
                config.addDataSourceProperty("defaultRowFetchSize", "1000");
                break;

            case SQLITE:
                config.addDataSourceProperty("journal_mode", "WAL");
                config.addDataSourceProperty("synchronous", "NORMAL");
                config.addDataSourceProperty("temp_store", "MEMORY");
                config.addDataSourceProperty("mmap_size", "268435456");
                break;

            case H2:
            case H2_SERVER:
                config.addDataSourceProperty("DB_CLOSE_DELAY", "-1");
                config.addDataSourceProperty("DB_CLOSE_ON_EXIT", "FALSE");
                break;
        }
    }

    private void initializeStorage() throws SQLException {
        executor.execute(connection -> {
            new SQLTableInitializer(connection, config.dialect).createTables();
        });

        EffectStorage.init(dataSource, config.dialect);
        TempBlockStorage.init(dataSource, plugin);
    }

    @Deprecated
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }

    public DatabaseExecutor getExecutor() {
        return executor;
    }

    public DatabaseDialect getDialect() {
        return config != null ? config.dialect : null;
    }

    public DatabaseConfig getConfig() {
        return config;
    }

    public boolean isConnected() {
        if (executor != null) {
            return executor.isAvailable();
        }
        return dataSource != null && !dataSource.isClosed();
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                TempBlockStorage.flush();

                dataSource.close();
                plugin.getLogger().info("Database connection pool closed successfully");

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error while closing database connection", e);
            }
        }
    }

    public boolean testConnection() {
        if (executor != null) {
            return executor.isAvailable();
        }

        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Database health check failed", e);
            return false;
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public String getPoolStats() {
        if (dataSource == null) {
            return "DataSource not initialized";
        }

        return String.format("Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
}