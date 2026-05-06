package com.monkey.kt.utils.entity;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class EntityDataUtils {

    private EntityDataUtils() {
    }

    public static void setBoolean(Entity entity, Plugin plugin, String key, boolean value) {
        container(entity).set(namespacedKey(plugin, key), PersistentDataType.BYTE, value ? (byte) 1 : (byte) 0);
    }

    public static boolean getBoolean(Entity entity, Plugin plugin, String key, boolean defaultValue) {
        Byte value = container(entity).get(namespacedKey(plugin, key), PersistentDataType.BYTE);
        return value != null ? value == 1 : defaultValue;
    }

    public static boolean hasBoolean(Entity entity, Plugin plugin, String key) {
        return container(entity).has(namespacedKey(plugin, key), PersistentDataType.BYTE);
    }

    public static void setDouble(Entity entity, Plugin plugin, String key, double value) {
        container(entity).set(namespacedKey(plugin, key), PersistentDataType.DOUBLE, value);
    }

    public static double getDouble(Entity entity, Plugin plugin, String key, double defaultValue) {
        Double value = container(entity).get(namespacedKey(plugin, key), PersistentDataType.DOUBLE);
        return value != null ? value : defaultValue;
    }

    public static boolean hasDouble(Entity entity, Plugin plugin, String key) {
        return container(entity).has(namespacedKey(plugin, key), PersistentDataType.DOUBLE);
    }

    public static void setString(Entity entity, Plugin plugin, String key, String value) {
        container(entity).set(namespacedKey(plugin, key), PersistentDataType.STRING, value);
    }

    public static String getString(Entity entity, Plugin plugin, String key) {
        return container(entity).get(namespacedKey(plugin, key), PersistentDataType.STRING);
    }

    public static boolean hasString(Entity entity, Plugin plugin, String key) {
        return container(entity).has(namespacedKey(plugin, key), PersistentDataType.STRING);
    }

    public static void remove(Entity entity, Plugin plugin, String key) {
        container(entity).remove(namespacedKey(plugin, key));
    }

    private static PersistentDataContainer container(Entity entity) {
        return entity.getPersistentDataContainer();
    }

    private static NamespacedKey namespacedKey(Plugin plugin, String key) {
        return new NamespacedKey(plugin, key);
    }
}
