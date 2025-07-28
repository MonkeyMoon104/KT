package com.monkey.kt.config.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

public final class YamlPaths {

    private YamlPaths() {}

    public static Map<String, Object> flatten(FileConfiguration cfg) {
        Map<String, Object> map = new LinkedHashMap<>();
        flattenSection(cfg, "", map);
        return map;
    }

    private static void flattenSection(ConfigurationSection sec, String prefix, Map<String, Object> out) {
        for (String key : sec.getKeys(false)) {
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            Object value = sec.get(key);
            if (value instanceof ConfigurationSection) {
                flattenSection((ConfigurationSection) value, path, out);
            } else {
                out.put(path, value);
            }
        }
    }
}
