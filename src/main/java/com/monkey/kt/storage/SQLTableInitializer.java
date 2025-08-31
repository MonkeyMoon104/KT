package com.monkey.kt.storage;

import com.monkey.kt.utils.ColorUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLTableInitializer {

    private final Connection connection;
    private final DatabaseDialect dialect;
    private static final Logger logger = Logger.getLogger(SQLTableInitializer.class.getName());

    public SQLTableInitializer(Connection connection, DatabaseDialect dialect) {
        this.connection = connection;
        this.dialect = dialect;
    }

    public void createTables() throws SQLException {
        createKillEffectsTable();
        createTempBlocksTable();
        createKillCoinsBalanceTable();
        createKillCoinsPurchasesTable();
        logger.info(ColorUtils.database("Database tables created/verified for " + dialect.name()));
    }

    private void createKillEffectsTable() throws SQLException {
        String sql = dialect.getCreateKillEffectsTableQuery();
        executeTableCreation("killeffects", sql);
    }

    private void createTempBlocksTable() throws SQLException {
        String sql = dialect.getCreateTempBlocksTableQuery();
        executeTableCreation("temp_blocks", sql);
    }

    private void createKillCoinsBalanceTable() throws SQLException {
        String sql = dialect.getCreateKillCoinsBalanceTableQuery();
        executeTableCreation("killcoins_balance", sql);
    }

    private void createKillCoinsPurchasesTable() throws SQLException {
        String sql = dialect.getCreateKillCoinsPurchasesTableQuery();
        executeTableCreation("killcoins_purchases", sql);
    }

    private void executeTableCreation(String tableName, String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            if (dialect == DatabaseDialect.ORACLE) {
                stmt.execute(sql);
            } else if (dialect == DatabaseDialect.SQLSERVER) {
                stmt.execute(sql);
            } else if (dialect == DatabaseDialect.POSTGRES && sql.contains(";")) {
                String[] statements = sql.split(";");
                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        stmt.execute(statement.trim());
                    }
                }
            } else if (dialect == DatabaseDialect.H2 || dialect == DatabaseDialect.H2_SERVER) {
                String[] statements = sql.split(";");
                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        stmt.execute(statement.trim());
                    }
                }
            } else if (dialect == DatabaseDialect.DERBY) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    if (!e.getSQLState().equals("X0Y32")) {
                        throw e;
                    }
                }
            } else {
                stmt.execute(sql);
            }

            logger.fine(ColorUtils.debug("Table '" + tableName + "' created/verified successfully"));

        } catch (SQLException e) {
            logger.log(Level.SEVERE, ColorUtils.error("Failed to create/verify table '" + tableName + "'"), e);
            throw e;
        }
    }

    public void performOptimizations() throws SQLException {
        switch (dialect) {
            case MYSQL:
            case MARIADB:
                optimizeMySQL();
                break;
            case POSTGRES:
                optimizePostgreSQL();
                break;
            case SQLITE:
                optimizeSQLite();
                break;
            case ORACLE:
                optimizeOracle();
                break;
        }
    }

    private void optimizeMySQL() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ANALYZE TABLE killeffects, temp_blocks");
            logger.fine(ColorUtils.performance("MySQL tables analyzed for optimization"));
        } catch (SQLException e) {
            logger.log(Level.WARNING, ColorUtils.warning("Failed to analyze MySQL tables"), e);
        }
    }

    private void optimizePostgreSQL() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ANALYZE killeffects");
            stmt.execute("ANALYZE temp_blocks");
            logger.fine(ColorUtils.performance("PostgreSQL statistics updated"));
        } catch (SQLException e) {
            logger.log(Level.WARNING, ColorUtils.warning("Failed to analyze PostgreSQL tables"), e);
        }
    }

    private void optimizeSQLite() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ANALYZE");
            stmt.execute("REINDEX");
            logger.fine(ColorUtils.performance("SQLite database analyzed and reindexed"));
        } catch (SQLException e) {
            logger.log(Level.WARNING, ColorUtils.warning("Failed to optimize SQLite database"), e);
        }
    }

    private void optimizeOracle() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("BEGIN DBMS_STATS.GATHER_TABLE_STATS(USER, 'KILLEFFECTS'); END;");
            stmt.execute("BEGIN DBMS_STATS.GATHER_TABLE_STATS(USER, 'TEMP_BLOCKS'); END;");
            logger.fine(ColorUtils.performance("Oracle table statistics gathered"));
        } catch (SQLException e) {
            logger.log(Level.WARNING, ColorUtils.warning("Failed to gather Oracle statistics"), e);
        }
    }
}