package com.monkey.kt.storage;

import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class TempBlockStorage {

    private static Connection connection;
    private static Plugin plugin;

    private static final List<TempBlockEntry> blocksToSave = new ArrayList<>();
    private static final List<Location> blocksToRemove = new ArrayList<>();

    public static void init(Connection conn, Plugin pl) {
        connection = conn;
        plugin = pl;
    }

    public static synchronized void saveTempBlock(Location loc, Material originalType) {
        if (loc.getWorld() == null) return;
        blocksToSave.add(new TempBlockEntry(loc, originalType));
    }

    public static synchronized void removeTempBlock(Location loc) {
        if (loc.getWorld() == null) return;
        blocksToRemove.add(loc);
    }

    public static synchronized void flush() {
        if (connection == null) return;

        try {
            connection.setAutoCommit(false);

            if (!blocksToSave.isEmpty()) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO temp_blocks (world, x, y, z, material) VALUES (?, ?, ?, ?, ?)")) {
                    for (TempBlockEntry entry : blocksToSave) {
                        Location loc = entry.loc;
                        ps.setString(1, loc.getWorld().getName());
                        ps.setInt(2, loc.getBlockX());
                        ps.setInt(3, loc.getBlockY());
                        ps.setInt(4, loc.getBlockZ());
                        ps.setString(5, entry.material.name());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                blocksToSave.clear();
            }

            if (!blocksToRemove.isEmpty()) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM temp_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
                    for (Location loc : blocksToRemove) {
                        ps.setString(1, loc.getWorld().getName());
                        ps.setInt(2, loc.getBlockX());
                        ps.setInt(3, loc.getBlockY());
                        ps.setInt(4, loc.getBlockZ());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                blocksToRemove.clear();
            }

            connection.commit();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error during batch saving/removing temp blocks", e);
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Error rolling back transaction", ex);
            }
        }
    }

    private static class TempBlockEntry {
        Location loc;
        Material material;

        TempBlockEntry(Location loc, Material material) {
            this.loc = loc;
            this.material = material;
        }
    }

    public static void removeAllTempBlocks() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM temp_blocks");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String worldName = rs.getString("world");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");

                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                String materialName = rs.getString("material");

                Location loc = new Location(world, x, y, z);
                Material originalMaterial = Material.getMaterial(materialName);

                if (originalMaterial != null) {
                    loc.getBlock().setType(originalMaterial);
                } else {
                    plugin.getLogger().warning("Materiale non riconosciuto: " + materialName);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error retrieving temporary blocks", e);
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM temp_blocks");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error clearing temporary blocks", e);
        }
    }

    public static void removeAllTempBlocksSafely() {
        if (connection == null) {
            plugin.getLogger().warning("Database connection is null, cannot restore temp blocks");
            return;
        }

        SchedulerWrapper.runTaskAsynchronously(plugin, () -> {
            Map<String, List<TempBlockData>> blocksByWorld = new HashMap<>();

            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM temp_blocks");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String worldName = rs.getString("world");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    String materialName = rs.getString("material");

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("World not found: " + worldName);
                        continue;
                    }

                    Material originalMaterial = Material.getMaterial(materialName);
                    if (originalMaterial == null) {
                        plugin.getLogger().warning("Material not found: " + materialName);
                        continue;
                    }

                    Location loc = new Location(world, x, y, z);
                    blocksByWorld.computeIfAbsent(worldName, k -> new ArrayList<>())
                            .add(new TempBlockData(loc, originalMaterial));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error retrieving temporary blocks", e);
                return;
            }

            if (!blocksByWorld.isEmpty()) {
                int totalBlocks = blocksByWorld.values().stream().mapToInt(List::size).sum();
                plugin.getLogger().info("Restoring " + totalBlocks + " temporary blocks across " + blocksByWorld.size() + " worlds...");

                restoreBlocksOptimized(blocksByWorld);
            }

            clearDatabase();
        });
    }

    private static void restoreBlocksOptimized(Map<String, List<TempBlockData>> blocksByWorld) {
        final int BLOCKS_PER_TICK = 120;
        final int TICKS_BETWEEN_BATCHES = 1;

        long currentDelay = 0;

        for (Map.Entry<String, List<TempBlockData>> worldEntry : blocksByWorld.entrySet()) {
            String worldName = worldEntry.getKey();
            List<TempBlockData> worldBlocks = worldEntry.getValue();

            Map<String, List<TempBlockData>> blocksByRegion = new HashMap<>();

            for (TempBlockData blockData : worldBlocks) {
                int regionX = blockData.location.getBlockX() >> 4;
                int regionZ = blockData.location.getBlockZ() >> 4;
                String regionKey = regionX + "," + regionZ;

                blocksByRegion.computeIfAbsent(regionKey, k -> new ArrayList<>())
                        .add(blockData);
            }

            plugin.getLogger().info("World " + worldName + ": " + worldBlocks.size() + " blocks in " + blocksByRegion.size() + " regions");

            for (Map.Entry<String, List<TempBlockData>> regionEntry : blocksByRegion.entrySet()) {
                List<TempBlockData> regionBlocks = regionEntry.getValue();

                final int totalBatches = (regionBlocks.size() + BLOCKS_PER_TICK - 1) / BLOCKS_PER_TICK;

                for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                    final int startIndex = batchIndex * BLOCKS_PER_TICK;
                    final int endIndex = Math.min(startIndex + BLOCKS_PER_TICK, regionBlocks.size());
                    final List<TempBlockData> batch = regionBlocks.subList(startIndex, endIndex);

                    Location regionLocation = batch.get(0).location;

                    SchedulerWrapper.runTaskLater(plugin, () -> {
                        processBatchInRegion(batch, regionLocation);
                    }, currentDelay);

                    currentDelay += TICKS_BETWEEN_BATCHES;
                }
            }
        }
    }

    private static void processBatchInRegion(List<TempBlockData> batch, Location regionLocation) {
        SchedulerWrapper.runTaskAtLocation(plugin, () -> {
            int processed = 0;
            int skipped = 0;

            for (TempBlockData blockData : batch) {
                try {
                    if (blockData.location.getBlock().getType() != blockData.material) {
                        blockData.location.getBlock().setType(blockData.material);
                        processed++;
                    } else {
                        skipped++;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to restore block at " +
                            blockData.location + ": " + e.getMessage());
                }
            }

            if (processed > 0 || skipped > 0) {
                plugin.getLogger().fine("Batch completed: " + processed + " restored, " + skipped + " skipped");
            }
        }, regionLocation, 0L);
    }

    private static void clearDatabase() {
        SchedulerWrapper.runTaskLater(plugin, () -> {
            SchedulerWrapper.runTaskAsynchronously(plugin, () -> {
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate("DELETE FROM temp_blocks");
                    plugin.getLogger().info("Temporary blocks database cleared successfully");
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error clearing temporary blocks", e);
                }
            });
        }, 200L);
    }

    public static boolean hasBlocksToRestore() {
        if (connection == null) return false;

        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM temp_blocks");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error checking temp blocks count", e);
        }
        return false;
    }

    public static void printRestoreStats() {
        if (connection == null) return;

        SchedulerWrapper.runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT world, COUNT(*) as count FROM temp_blocks GROUP BY world");
                 ResultSet rs = ps.executeQuery()) {

                plugin.getLogger().info("=== Temp Blocks Statistics ===");
                while (rs.next()) {
                    String worldName = rs.getString("world");
                    int count = rs.getInt("count");
                    plugin.getLogger().info("World " + worldName + ": " + count + " blocks");
                }
                plugin.getLogger().info("==============================");

            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error getting restore stats", e);
            }
        });
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