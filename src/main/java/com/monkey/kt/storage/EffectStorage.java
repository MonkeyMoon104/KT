package com.monkey.kt.storage;

import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EffectStorage {

    private static final Map<UUID, String> playerEffects = new HashMap<>();
    private static Connection connection;

    public static void init(Connection conn) {
        connection = conn;
        loadAllEffects();
    }

    private static void loadAllEffects() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid, effect FROM killeffects")) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String effect = rs.getString("effect");
                playerEffects.put(uuid, effect);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getEffect(Player player) {
        return playerEffects.getOrDefault(player.getUniqueId(), null);
    }

    public static void setEffect(Player player, String effect) {
        playerEffects.put(player.getUniqueId(), effect);
        saveEffectToDb(player.getUniqueId(), effect);
    }

    private static void saveEffectToDb(UUID uuid, String effect) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO killeffects (uuid, effect) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET effect = excluded.effect")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, effect);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeEffect(Player player) {
        UUID uuid = player.getUniqueId();
        playerEffects.remove(uuid);

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM killeffects WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
