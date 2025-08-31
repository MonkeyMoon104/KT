package com.monkey.kt.storage;

import com.monkey.kt.utils.ColorUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EffectStorage {

    private static final ConcurrentHashMap<UUID, String> playerEffects = new ConcurrentHashMap<>();
    private static HikariDataSource dataSource;
    private static DatabaseDialect dialect;
    private static final Logger logger = Logger.getLogger(EffectStorage.class.getName());

    public static void init(HikariDataSource ds, DatabaseDialect dbDialect) {
        dataSource = ds;
        dialect = dbDialect;
        loadAllEffects();
    }

    private static void loadAllEffects() {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT uuid, effect FROM killeffects")) {

                int loaded = 0;
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        String effect = rs.getString("effect");
                        playerEffects.put(uuid, effect);
                        loaded++;
                    } catch (IllegalArgumentException e) {
                        logger.warning(ColorUtils.warning("Invalid UUID found in database: " + rs.getString("uuid")));
                    }
                }

                logger.info(ColorUtils.info("Loaded " + loaded + " player effects from database"));

            } catch (SQLException e) {
                logger.log(Level.SEVERE, ColorUtils.error("Error loading player effects from database"), e);
            }
        });
    }

    public static String getEffect(Player player) {
        return playerEffects.get(player.getUniqueId());
    }

    public static String getEffect(UUID uuid) {
        return playerEffects.get(uuid);
    }

    public static void setEffect(Player player, String effect) {
        setEffect(player.getUniqueId(), effect);
    }

    public static void setEffect(UUID uuid, String effect) {
        if (effect == null || effect.trim().isEmpty()) {
            removeEffect(uuid);
            return;
        }

        playerEffects.put(uuid, effect);

        saveEffectToDbAsync(uuid, effect);
    }

    private static void saveEffectToDbAsync(UUID uuid, String effect) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(dialect.getUpsertKillEffectQuery())) {

                ps.setString(1, uuid.toString());
                ps.setString(2, effect);
                ps.executeUpdate();

            } catch (SQLException e) {
                logger.log(Level.SEVERE, ColorUtils.error("Error saving effect to database for player " + uuid), e);
            }
        });
    }

    public static void removeEffect(Player player) {
        removeEffect(player.getUniqueId());
    }

    public static void removeEffect(UUID uuid) {
        playerEffects.remove(uuid);

        removeEffectFromDbAsync(uuid);
    }

    private static void removeEffectFromDbAsync(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("DELETE FROM killeffects WHERE uuid = ?")) {

                ps.setString(1, uuid.toString());
                ps.executeUpdate();

            } catch (SQLException e) {
                logger.log(Level.SEVERE, ColorUtils.error("Error removing effect from database for player " + uuid), e);
            }
        });
    }

    public static CompletableFuture<Void> batchSaveEffects(java.util.Map<UUID, String> effects) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);

                try (PreparedStatement ps = connection.prepareStatement(dialect.getUpsertKillEffectQuery())) {
                    for (java.util.Map.Entry<UUID, String> entry : effects.entrySet()) {
                        ps.setString(1, entry.getKey().toString());
                        ps.setString(2, entry.getValue());
                        ps.addBatch();

                        playerEffects.put(entry.getKey(), entry.getValue());
                    }

                    ps.executeBatch();
                    connection.commit();
                    logger.info(ColorUtils.batch("Batch saved " + effects.size() + " player effects"));

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }

            } catch (SQLException e) {
                logger.log(Level.SEVERE, ColorUtils.error("Error during batch save of effects"), e);
            }
        });
    }

    public static CompletableFuture<Void> batchRemoveEffects(java.util.Set<UUID> uuids) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);

                try (PreparedStatement ps = connection.prepareStatement("DELETE FROM killeffects WHERE uuid = ?")) {
                    for (UUID uuid : uuids) {
                        ps.setString(1, uuid.toString());
                        ps.addBatch();

                        playerEffects.remove(uuid);
                    }

                    ps.executeBatch();
                    connection.commit();
                    logger.info(ColorUtils.batch("Batch removed " + uuids.size() + " player effects"));

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }

            } catch (SQLException e) {
                logger.log(Level.SEVERE, ColorUtils.error("Error during batch removal of effects"), e);
            }
        });
    }

    public static java.util.Map<UUID, String> getAllEffects() {
        return new java.util.HashMap<>(playerEffects);
    }

    public static int getEffectsCount() {
        return playerEffects.size();
    }

    public static boolean hasEffect(UUID uuid) {
        return playerEffects.containsKey(uuid);
    }

    public static boolean hasEffect(Player player) {
        return hasEffect(player.getUniqueId());
    }

    public static CompletableFuture<Void> clearAllEffects() {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement stmt = connection.createStatement()) {

                int deleted = stmt.executeUpdate("DELETE FROM killeffects");
                playerEffects.clear();

                logger.info(ColorUtils.info("Cleared all player effects (" + deleted + " records)"));

            } catch (SQLException e) {
                logger.log(Level.SEVERE, ColorUtils.error("Error clearing all effects"), e);
            }
        });
    }

    public static void forceSyncAll() {
        logger.info(ColorUtils.database("Force syncing all player effects to database..."));

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("DELETE FROM killeffects");
            }

            try (PreparedStatement ps = connection.prepareStatement(dialect.getUpsertKillEffectQuery())) {
                for (java.util.Map.Entry<UUID, String> entry : playerEffects.entrySet()) {
                    ps.setString(1, entry.getKey().toString());
                    ps.setString(2, entry.getValue());
                    ps.addBatch();
                }

                ps.executeBatch();
                connection.commit();
                logger.info(ColorUtils.success("Force synced " + playerEffects.size() + " player effects"));

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, ColorUtils.error("Error during force sync of effects"), e);
        }
    }
}