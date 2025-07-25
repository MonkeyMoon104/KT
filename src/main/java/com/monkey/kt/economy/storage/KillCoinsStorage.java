package com.monkey.kt.economy.storage;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class KillCoinsStorage {

    private static Connection connection;

    public static void init(Connection conn) {
        connection = conn;
    }

    public void createIfNotExists(UUID uuid, double starting) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO killcoins_balance (uuid, balance) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setDouble(2, starting);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getBalance(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT balance FROM killcoins_balance WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0D;
    }

    public void setBalance(UUID uuid, double value) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO killcoins_balance (uuid, balance) VALUES (?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET balance=excluded.balance")) {
            ps.setString(1, uuid.toString());
            ps.setDouble(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void add(UUID uuid, double delta) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE killcoins_balance SET balance = balance + ? WHERE uuid=?")) {
            ps.setDouble(1, delta);
            ps.setString(2, uuid.toString());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                setBalance(uuid, delta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasBought(UUID uuid, String effect) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM killcoins_purchases WHERE uuid=? AND effect=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, effect);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void markBought(UUID uuid, String effect) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO killcoins_purchases (uuid, effect) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, effect);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getBoughtEffects(UUID uuid) {
        Set<String> set = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT effect FROM killcoins_purchases WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return set;
    }
}
