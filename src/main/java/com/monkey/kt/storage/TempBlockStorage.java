package com.monkey.kt.storage;

import com.monkey.kt.utils.ColorUtils;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TempBlockStorage {

    private static HikariDataSource dataSource;
    private static Plugin plugin;
    private static final Logger logger = Logger.getLogger(TempBlockStorage.class.getName());

    private static final ConcurrentLinkedQueue<TempBlockEntry> blocksToSave = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<Location> blocksToRemove = new ConcurrentLinkedQueue<>();

    private static final int BATCH_SIZE = 500;
    private static final long BATCH_INTERVAL_TICKS = 100L;

    public static void init(HikariDataSource ds, Plugin pl) {
        dataSource = ds;
        plugin = pl;

        startBatchProcessor();
    }

    private static void startBatchProcessor() {
        SchedulerWrapper.runTaskTimerAsynchronously(plugin, () -> {
            if (!blocksToSave.isEmpty() || !blocksToRemove.isEmpty()) {
                flush();
            }
        }, BATCH_INTERVAL_TICKS, BATCH_INTERVAL_TICKS);
    }

    public static void saveTempBlock(Location loc, Material originalType) {
        if (loc.getWorld() == null || originalType == null) {
            logger.warning(ColorUtils.warning("Attempted to save temp block with null world or material"));
            return;
        }

        blocksToSave.offer(new TempBlockEntry(loc.clone(), originalType));

        if (blocksToSave.size() >= BATCH_SIZE) {
            CompletableFuture.runAsync(() -> flush());
        }
    }

    public static void removeTempBlock(Location loc) {
        if (loc.getWorld() == null) {
            logger.warning(ColorUtils.warning("Attempted to remove temp block with null world"));
            return;
        }

        blocksToRemove.offer(loc.clone());

        if (blocksToRemove.size() >= BATCH_SIZE) {
            CompletableFuture.runAsync(() -> flush());
        }
    }

    public static synchronized void flush() {
        if (dataSource == null) {
            logger.warning(ColorUtils.warning("DataSource is null, cannot flush temp blocks"));
            return;
        }

        List<TempBlockEntry> saveQueue = new ArrayList<>();
        List<Location> removeQueue = new ArrayList<>();

        TempBlockEntry entry;
        while ((entry = blocksToSave.poll()) != null) {
            saveQueue.add(entry);
        }

        Location location;
        while ((location = blocksToRemove.poll()) != null) {
            removeQueue.add(location);
        }

        if (saveQueue.isEmpty() && removeQueue.isEmpty()) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                if (!saveQueue.isEmpty()) {
                    processSaves(connection, saveQueue);
                }

                if (!removeQueue.isEmpty()) {
                    processRemoves(connection, removeQueue);
                }

                connection.commit();

                if (!saveQueue.isEmpty() || !removeQueue.isEmpty()) {
                    logger.fine(ColorUtils.batch("Flushed " + saveQueue.size() + " saves and " + removeQueue.size() + " removes"));
                }

            } catch (SQLException e) {
                connection.rollback();
                logger.log(Level.SEVERE, ColorUtils.error("Error during batch processing temp blocks"), e);

                blocksToSave.addAll(saveQueue);
                blocksToRemove.addAll(removeQueue);

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, ColorUtils.error("Error getting database connection for temp blocks"), e);
        }
    }

    private static void processSaves(Connection connection, List<TempBlockEntry> saveQueue) throws SQLException {
        String sql = "INSERT INTO temp_blocks (world, x, y, z, material) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (TempBlockEntry entry : saveQueue) {
                ps.setString(1, entry.loc.getWorld().getName());
                ps.setInt(2, entry.loc.getBlockX());
                ps.setInt(3, entry.loc.getBlockY());
                ps.setInt(4, entry.loc.getBlockZ());
                ps.setString(5, entry.material.name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void processRemoves(Connection connection, List<Location> removeQueue) throws SQLException {
        String sql = "DELETE FROM temp_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Location loc : removeQueue) {
                ps.setString(1, loc.getWorld().getName());
                ps.setInt(2, loc.getBlockX());
                ps.setInt(3, loc.getBlockY());
                ps.setInt(4, loc.getBlockZ());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static class TempBlockEntry {
        final Location loc;
        final Material material;

        TempBlockEntry(Location loc, Material material) {
            this.loc = loc;
            this.material = material;
        }
    }

    public static CompletableFuture<Void> removeAllTempBlocks() {
        return CompletableFuture.runAsync(() -> {
            if (dataSource == null) {
                logger.warning(ColorUtils.warning("DataSource is null, cannot restore temp blocks"));
                return;
            }

            Map<String, List<TempBlockData>> blocksByWorld = new HashMap<>();

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT * FROM temp_blocks");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String worldName = rs.getString("world");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    String materialName = rs.getString("material");

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        logger.warning(ColorUtils.warning("World not found: " + worldName));
                        continue;
                    }

                    Material originalMaterial = Material.getMaterial(materialName);
                    if (originalMaterial == null) {
                        logger.warning(ColorUtils.warning("Material not found: " + materialName));
                        continue;
                    }

                    Location loc = new Location(world, x, y, z);
                    blocksByWorld.computeIfAbsent(worldName, k -> new ArrayList<>())
                            .add(new TempBlockData(loc, originalMaterial));
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, ColorUtils.error("Error retrieving temporary blocks"), e);
                return;
            }

            if (!blocksByWorld.isEmpty()) {
                int totalBlocks = blocksByWorld.values().stream().mapToInt(List::size).sum();
                logger.info(ColorUtils.info("Restoring " + totalBlocks + " temporary blocks across " + blocksByWorld.size() + " worlds..."));

                restoreBlocksOptimized(blocksByWorld);
            }

            SchedulerWrapper.runTaskLater(plugin, () -> {
                CompletableFuture.runAsync(() -> clearDatabase());
            }, 200L);
        });
    }

    public static void removeAllTempBlocksSafely() {
        removeAllTempBlocks();
    }

    private static void restoreBlocksOptimized(Map<String, List<TempBlockData>> blocksByWorld) {
        final int BLOCKS_PER_TICK = 120;
        final int TICKS_BETWEEN_BATCHES = 1;

        long currentDelay = 0;

        for (Map.Entry<String, List<TempBlockData>> worldEntry : blocksByWorld.entrySet()) {
            String worldName = worldEntry.getKey();
            List<TempBlockData> worldBlocks = worldEntry.getValue();

            Map<String, List<TempBlockData>> blocksByChunk = new HashMap<>();

            for (TempBlockData blockData : worldBlocks) {
                int chunkX = blockData.location.getBlockX() >> 4;
                int chunkZ = blockData.location.getBlockZ() >> 4;
                String chunkKey = chunkX + "," + chunkZ;

                blocksByChunk.computeIfAbsent(chunkKey, k -> new ArrayList<>())
                        .add(blockData);
            }

            logger.info(ColorUtils.info("World " + worldName + ": " + worldBlocks.size() + " blocks in " + blocksByChunk.size() + " chunks"));

            for (Map.Entry<String, List<TempBlockData>> chunkEntry : blocksByChunk.entrySet()) {
                List<TempBlockData> chunkBlocks = chunkEntry.getValue();

                final int totalBatches = (chunkBlocks.size() + BLOCKS_PER_TICK - 1) / BLOCKS_PER_TICK;

                for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                    final int startIndex = batchIndex * BLOCKS_PER_TICK;
                    final int endIndex = Math.min(startIndex + BLOCKS_PER_TICK, chunkBlocks.size());
                    final List<TempBlockData> batch = new ArrayList<>(chunkBlocks.subList(startIndex, endIndex));

                    Location chunkLocation = batch.get(0).location;

                    SchedulerWrapper.runTaskLater(plugin, () -> {
                        processBatchInChunk(batch, chunkLocation);
                    }, currentDelay);

                    currentDelay += TICKS_BETWEEN_BATCHES;
                }
            }
        }
    }

    private static void processBatchInChunk(List<TempBlockData> batch, Location chunkLocation) {
        SchedulerWrapper.runTaskAtLocation(plugin, () -> {
            int processed = 0;
            int skipped = 0;
            int errors = 0;

            for (TempBlockData blockData : batch) {
                try {
                    if (blockData.location.getChunk().isLoaded()) {
                        Material currentType = blockData.location.getBlock().getType();
                        if (currentType != blockData.material) {
                            blockData.location.getBlock().setType(blockData.material);
                            processed++;
                        } else {
                            skipped++;
                        }
                    } else {
                        blockData.location.getChunk().load();
                        blockData.location.getBlock().setType(blockData.material);
                        processed++;
                    }
                } catch (Exception e) {
                    errors++;
                    logger.warning(ColorUtils.warning("Failed to restore block at " + blockData.location + ": " + e.getMessage()));
                }
            }

            if (processed > 0 || skipped > 0 || errors > 0) {
                logger.fine(ColorUtils.debug("Batch completed: " + processed + " restored, " + skipped + " skipped, " + errors + " errors"));
            }
        }, chunkLocation, 0L);
    }

    private static void clearDatabase() {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {

            int deleted = stmt.executeUpdate("DELETE FROM temp_blocks");
            logger.info(ColorUtils.success("Temporary blocks database cleared successfully (" + deleted + " records)"));

        } catch (SQLException e) {
            logger.log(Level.SEVERE, ColorUtils.error("Error clearing temporary blocks"), e);
        }
    }

    public static CompletableFuture<Boolean> hasBlocksToRestore() {
        return CompletableFuture.supplyAsync(() -> {
            if (dataSource == null) return false;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM temp_blocks");
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, ColorUtils.warning("Error checking temp blocks count"), e);
            }
            return false;
        });
    }

    public static boolean hasBlocksToRestoreSync() {
        if (dataSource == null) return false;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM temp_blocks");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, ColorUtils.warning("Error checking temp blocks count"), e);
        }
        return false;
    }

    public static CompletableFuture<Void> printRestoreStats() {
        return CompletableFuture.runAsync(() -> {
            if (dataSource == null) {
                logger.warning(ColorUtils.warning("DataSource is null, cannot get restore stats"));
                return;
            }

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT world, COUNT(*) as count FROM temp_blocks GROUP BY world ORDER BY count DESC");
                 ResultSet rs = ps.executeQuery()) {

                logger.info(ColorUtils.header("Temp Blocks Statistics"));
                int totalBlocks = 0;

                while (rs.next()) {
                    String worldName = rs.getString("world");
                    int count = rs.getInt("count");
                    totalBlocks += count;
                    logger.info(ColorUtils.info("World " + worldName + ": " + count + " blocks"));
                }

                logger.info(ColorUtils.info("Total: " + totalBlocks + " blocks"));
                logger.info(ColorUtils.separator());

            } catch (SQLException e) {
                logger.log(Level.WARNING, ColorUtils.warning("Error getting restore stats"), e);
            }
        });
    }

    public static CompletableFuture<List<TempBlockData>> getTempBlocksForWorld(String worldName) {
        return CompletableFuture.supplyAsync(() -> {
            List<TempBlockData> blocks = new ArrayList<>();

            if (dataSource == null) return blocks;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT x, y, z, material FROM temp_blocks WHERE world = ?")) {

                ps.setString(1, worldName);

                try (ResultSet rs = ps.executeQuery()) {
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) return blocks;

                    while (rs.next()) {
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        String materialName = rs.getString("material");

                        Material material = Material.getMaterial(materialName);
                        if (material != null) {
                            Location loc = new Location(world, x, y, z);
                            blocks.add(new TempBlockData(loc, material));
                        }
                    }
                }

            } catch (SQLException e) {
                logger.log(Level.WARNING, ColorUtils.warning("Error getting temp blocks for world " + worldName), e);
            }

            return blocks;
        });
    }

    public static CompletableFuture<Integer> clearTempBlocksForWorld(String worldName) {
        return CompletableFuture.supplyAsync(() -> {
            if (dataSource == null) return 0;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("DELETE FROM temp_blocks WHERE world = ?")) {

                ps.setString(1, worldName);
                int deleted = ps.executeUpdate();

                logger.info(ColorUtils.info("Cleared " + deleted + " temp blocks for world " + worldName));
                return deleted;

            } catch (SQLException e) {
                logger.log(Level.SEVERE, ColorUtils.error("Error clearing temp blocks for world " + worldName), e);
                return 0;
            }
        });
    }

    public static String getQueueStats() {
        return String.format("Queue Stats - To Save: %d, To Remove: %d",
                blocksToSave.size(), blocksToRemove.size());
    }

    public static void forceFlushAll() {
        logger.info(ColorUtils.database("Force flushing all temp block operations..."));
        flush();
        logger.info(ColorUtils.success("Force flush completed"));
    }

    private static class TempBlockData {
        final Location location;
        final Material material;

        TempBlockData(Location location, Material material) {
            this.location = location;
            this.material = material;
        }
    }
}