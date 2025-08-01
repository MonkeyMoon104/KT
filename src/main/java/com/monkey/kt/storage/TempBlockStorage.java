package com.monkey.kt.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
}
