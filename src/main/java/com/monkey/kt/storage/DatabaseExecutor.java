package com.monkey.kt.storage;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseExecutor {

    private static final Logger logger = Logger.getLogger(DatabaseExecutor.class.getName());
    private final HikariDataSource dataSource;

    public DatabaseExecutor(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @FunctionalInterface
    public interface DatabaseOperation {
        void execute(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface DatabaseQuery<T> {
        T execute(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface PreparedOperation {
        void execute(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    public interface PreparedQuery<T> {
        T execute(PreparedStatement statement) throws SQLException;
    }

    public boolean execute(DatabaseOperation operation) {
        if (dataSource == null || dataSource.isClosed()) {
            logger.warning("DataSource is not available");
            return false;
        }

        try (Connection connection = dataSource.getConnection()) {
            operation.execute(connection);
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database operation failed", e);
            return false;
        }
    }

    public <T> T query(DatabaseQuery<T> query) {
        return query(query, null);
    }

    public <T> T query(DatabaseQuery<T> query, T defaultValue) {
        if (dataSource == null || dataSource.isClosed()) {
            logger.warning("DataSource is not available");
            return defaultValue;
        }

        try (Connection connection = dataSource.getConnection()) {
            return query.execute(connection);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database query failed", e);
            return defaultValue;
        }
    }

    public boolean executeStatement(String sql, PreparedOperation operation) {
        if (dataSource == null || dataSource.isClosed()) {
            logger.warning("DataSource is not available");
            return false;
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            operation.execute(statement);
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Prepared statement execution failed: " + sql, e);
            return false;
        }
    }

    public <T> T queryStatement(String sql, PreparedQuery<T> query) {
        return queryStatement(sql, query, null);
    }

    public <T> T queryStatement(String sql, PreparedQuery<T> query, T defaultValue) {
        if (dataSource == null || dataSource.isClosed()) {
            logger.warning("DataSource is not available");
            return defaultValue;
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return query.execute(statement);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Prepared statement query failed: " + sql, e);
            return defaultValue;
        }
    }

    public boolean executeTransaction(DatabaseOperation operation) {
        if (dataSource == null || dataSource.isClosed()) {
            logger.warning("DataSource is not available");
            return false;
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                operation.execute(connection);
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Transaction failed", e);
            return false;
        }
    }

    public boolean executeBatch(String sql, List<PreparedOperation> operations) {
        if (dataSource == null || dataSource.isClosed()) {
            logger.warning("DataSource is not available");
            return false;
        }

        return executeTransaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (PreparedOperation operation : operations) {
                    operation.execute(statement);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
    }

    public boolean isAvailable() {
        return query(connection -> {
            return connection != null && !connection.isClosed();
        }, false);
    }

    public CompletableFuture<Boolean> executeAsync(DatabaseOperation operation) {
        return CompletableFuture.supplyAsync(() -> execute(operation));
    }

    public <T> CompletableFuture<T> queryAsync(DatabaseQuery<T> query, T defaultValue) {
        return CompletableFuture.supplyAsync(() -> query(query, defaultValue));
    }

    public int executeUpdate(String sql, Object... parameters) {
        return queryStatement(sql, statement -> {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            return statement.executeUpdate();
        }, 0);
    }

    public <T> List<T> selectList(String sql, Function<ResultSet, T> mapper, Object... parameters) {
        return queryStatement(sql, statement -> {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            List<T> results = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.apply(rs));
                }
            }
            return results;
        }, new ArrayList<>());
    }

    public <T> T selectOne(String sql, Function<ResultSet, T> mapper, Object... parameters) {
        return queryStatement(sql, statement -> {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapper.apply(rs);
                }
            }
            return null;
        });
    }

    public int count(String sql, Object... parameters) {
        return selectOne(sql, rs -> {
            try {
                return rs.getInt(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, parameters);
    }

    public boolean exists(String sql, Object... parameters) {
        return count(sql, parameters) > 0;
    }
}