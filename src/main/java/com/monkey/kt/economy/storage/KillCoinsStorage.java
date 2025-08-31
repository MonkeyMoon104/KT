package com.monkey.kt.economy.storage;

import com.monkey.kt.storage.DatabaseDialect;
import com.monkey.kt.storage.DatabaseExecutor;
import com.monkey.kt.utils.ColorUtils;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KillCoinsStorage {

    private static final Logger logger = Logger.getLogger(KillCoinsStorage.class.getName());
    private static final ConcurrentHashMap<UUID, Double> balanceCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Set<String>> purchaseCache = new ConcurrentHashMap<>();

    private final DatabaseExecutor executor;
    private final DatabaseDialect dialect;
    private final HikariDataSource dataSource;

    public KillCoinsStorage(DatabaseExecutor executor, DatabaseDialect dialect, HikariDataSource dataSource) {
        this.executor = executor;
        this.dialect = dialect;
        this.dataSource = dataSource;
        loadAllData();
    }

    private void loadAllData() {
        CompletableFuture.runAsync(() -> {
            loadBalances();
            loadPurchases();
        });
    }

    private void loadBalances() {
        executor.execute(connection -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT uuid, balance FROM killcoins_balance")) {

                int loaded = 0;
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        double balance = rs.getDouble("balance");
                        balanceCache.put(uuid, balance);
                        loaded++;
                    } catch (IllegalArgumentException e) {
                        logger.warning(ColorUtils.warning("Invalid UUID found in balance table: " + rs.getString("uuid")));
                    }
                }
                logger.info(ColorUtils.economy("Loaded " + loaded + " player balances from database"));
            } catch (SQLException e) {
                logger.log(Level.SEVERE, ColorUtils.error("Error loading player balances"), e);
            }
        });
    }

    private void loadPurchases() {
        executor.execute(connection -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT uuid, effect FROM killcoins_purchases")) {

                int loaded = 0;
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        String effect = rs.getString("effect");

                        purchaseCache.computeIfAbsent(uuid, k -> new HashSet<>()).add(effect);
                        loaded++;
                    } catch (IllegalArgumentException e) {
                        logger.warning(ColorUtils.warning("Invalid UUID found in purchases table: " + rs.getString("uuid")));
                    }
                }
                logger.info(ColorUtils.purchase("Loaded " + loaded + " player purchases from database"));
            } catch (SQLException e) {
                logger.log(Level.SEVERE, ColorUtils.error("Error loading player purchases"), e);
            }
        });
    }

    public void createIfNotExists(UUID uuid, double starting) {
        if (balanceCache.containsKey(uuid)) {
            return;
        }

        balanceCache.put(uuid, starting);

        String sql = getInsertOrIgnoreBalanceQuery();
        if (dialect == DatabaseDialect.SQLSERVER || dialect == DatabaseDialect.ORACLE) {
            executor.executeStatement(sql, statement -> {
                statement.setString(1, uuid.toString());
                if (dialect == DatabaseDialect.SQLSERVER) {
                    statement.setString(2, uuid.toString());
                    statement.setDouble(3, starting);
                } else {
                    statement.setDouble(2, starting);
                    statement.setString(3, uuid.toString());
                }
                statement.executeUpdate();
            });
        } else {
            executor.executeStatement(sql, statement -> {
                statement.setString(1, uuid.toString());
                statement.setDouble(2, starting);
                statement.executeUpdate();
            });
        }
    }

    public double getBalance(UUID uuid) {
        return balanceCache.getOrDefault(uuid, 0.0);
    }

    public void setBalance(UUID uuid, double value) {
        balanceCache.put(uuid, value);
        saveBalanceAsync(uuid, value);
    }

    public void add(UUID uuid, double delta) {
        if (delta == 0) return;

        double newBalance = balanceCache.compute(uuid, (k, v) -> (v == null ? 0.0 : v) + delta);

        CompletableFuture.runAsync(() -> {
            int updated = executor.executeUpdate(
                    "UPDATE killcoins_balance SET balance = balance + ? WHERE uuid = ?",
                    delta, uuid.toString()
            );

            if (updated == 0) {
                setBalance(uuid, newBalance);
            }
        });
    }

    private void saveBalanceAsync(UUID uuid, double balance) {
        CompletableFuture.runAsync(() -> {
            String sql = dialect.getUpsertBalanceQuery();
            executor.executeStatement(sql, statement -> {
                statement.setString(1, uuid.toString());
                statement.setDouble(2, balance);
                statement.executeUpdate();
            });
        });
    }

    public boolean hasBought(UUID uuid, String effect) {
        Set<String> userPurchases = purchaseCache.get(uuid);
        return userPurchases != null && userPurchases.contains(effect);
    }

    public void markBought(UUID uuid, String effect) {
        purchaseCache.computeIfAbsent(uuid, k -> new HashSet<>()).add(effect);
        savePurchaseAsync(uuid, effect);
    }

    private void savePurchaseAsync(UUID uuid, String effect) {
        CompletableFuture.runAsync(() -> {
            String sql = getInsertOrIgnorePurchaseQuery();

            if (dialect == DatabaseDialect.SQLSERVER || dialect == DatabaseDialect.ORACLE) {
                executor.executeStatement(sql, statement -> {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, effect);
                    if (dialect == DatabaseDialect.SQLSERVER) {
                        statement.setString(3, uuid.toString());
                        statement.setString(4, effect);
                    } else {
                        statement.setString(3, uuid.toString());
                        statement.setString(4, effect);
                    }
                    statement.executeUpdate();
                });
            } else {
                executor.executeStatement(sql, statement -> {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, effect);
                    statement.executeUpdate();
                });
            }
        });
    }

    public Set<String> getBoughtEffects(UUID uuid) {
        return new HashSet<>(purchaseCache.getOrDefault(uuid, new HashSet<>()));
    }

    public CompletableFuture<Void> batchSaveBalances(java.util.Map<UUID, Double> balances) {
        return CompletableFuture.runAsync(() -> {
            String sql = dialect.getUpsertBalanceQuery();

            executor.executeTransaction(connection -> {
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    for (java.util.Map.Entry<UUID, Double> entry : balances.entrySet()) {
                        ps.setString(1, entry.getKey().toString());
                        ps.setDouble(2, entry.getValue());
                        ps.addBatch();

                        balanceCache.put(entry.getKey(), entry.getValue());
                    }
                    ps.executeBatch();
                    logger.info(ColorUtils.batch("Batch saved " + balances.size() + " player balances"));
                }
            });
        });
    }

    public CompletableFuture<Void> batchSavePurchases(java.util.Map<UUID, Set<String>> purchases) {
        return CompletableFuture.runAsync(() -> {
            String sql = getInsertOrIgnorePurchaseQuery();

            executor.executeTransaction(connection -> {
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    for (java.util.Map.Entry<UUID, Set<String>> entry : purchases.entrySet()) {
                        UUID uuid = entry.getKey();
                        for (String effect : entry.getValue()) {
                            if (dialect == DatabaseDialect.SQLSERVER || dialect == DatabaseDialect.ORACLE) {
                                ps.setString(1, uuid.toString());
                                ps.setString(2, effect);
                                if (dialect == DatabaseDialect.SQLSERVER) {
                                    ps.setString(3, uuid.toString());
                                    ps.setString(4, effect);
                                } else {
                                    ps.setString(3, uuid.toString());
                                    ps.setString(4, effect);
                                }
                            } else {
                                ps.setString(1, uuid.toString());
                                ps.setString(2, effect);
                            }
                            ps.addBatch();

                            purchaseCache.computeIfAbsent(uuid, k -> new HashSet<>()).add(effect);
                        }
                    }
                    ps.executeBatch();
                    logger.info(ColorUtils.batch("Batch saved purchases for " + purchases.size() + " players"));
                }
            });
        });
    }

    public int getAccountsCount() {
        return balanceCache.size();
    }

    public int getPurchasesCount() {
        return purchaseCache.values().stream().mapToInt(Set::size).sum();
    }

    public double getTotalBalance() {
        return balanceCache.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public CompletableFuture<Void> clearAllBalances() {
        return CompletableFuture.runAsync(() -> {
            executor.execute(connection -> {
                try (Statement stmt = connection.createStatement()) {
                    int deleted = stmt.executeUpdate("DELETE FROM killcoins_balance");
                    balanceCache.clear();
                    logger.info(ColorUtils.info("Cleared all player balances (" + deleted + " records)"));
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, ColorUtils.error("Error clearing balances"), e);
                }
            });
        });
    }

    public CompletableFuture<Void> clearAllPurchases() {
        return CompletableFuture.runAsync(() -> {
            executor.execute(connection -> {
                try (Statement stmt = connection.createStatement()) {
                    int deleted = stmt.executeUpdate("DELETE FROM killcoins_purchases");
                    purchaseCache.clear();
                    logger.info(ColorUtils.info("Cleared all player purchases (" + deleted + " records)"));
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, ColorUtils.error("Error clearing purchases"), e);
                }
            });
        });
    }

    public void forceSyncAll() {
        logger.info(ColorUtils.database("Force syncing all economy data to database..."));

        CompletableFuture.allOf(
                batchSaveBalances(new java.util.HashMap<>(balanceCache)),
                batchSavePurchases(new java.util.HashMap<>(purchaseCache))
        ).join();

        logger.info(ColorUtils.success("Force sync completed"));
    }

    private String getInsertOrIgnoreBalanceQuery() {
        return dialect.getInsertOrIgnoreBalanceQuery();
    }

    private String getInsertOrIgnorePurchaseQuery() {
        return dialect.getInsertOrIgnorePurchaseQuery();
    }

    public java.util.Map<UUID, Double> getAllBalances() {
        return new java.util.HashMap<>(balanceCache);
    }

    public java.util.Map<UUID, Set<String>> getAllPurchases() {
        return new java.util.HashMap<>(purchaseCache);
    }
}