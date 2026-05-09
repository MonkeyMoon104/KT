package com.monkey.kt.config;

import com.monkey.kt.KT;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PluginConfigManager {

    private final KT plugin;
    private final List<ConfigDefinition> definitions;

    public PluginConfigManager(KT plugin) {
        this.plugin = plugin;
        this.definitions = List.of(
                new ConfigDefinition("config.yml", List.of("config-version", "server-info", "disabled-worlds", "auto-update"), true),
                new ConfigDefinition("messages.yml", List.of("messages"), false),
                new ConfigDefinition("database.yml", List.of("database"), false),
                new ConfigDefinition("performance.yml", List.of("particle-performance"), false),
                new ConfigDefinition("management.yml", List.of("management-structure"), false),
                new ConfigDefinition("economy.yml", List.of("economy"), false),
                new ConfigDefinition("resource-pack.yml", List.of("resource_pack"), false),
                new ConfigDefinition("gui.yml", List.of("gui"), false),
                new ConfigDefinition("custom-effects.yml", List.of("custom-effects"), false),
                new ConfigDefinition("events.yml", List.of("events"), false),
                new ConfigDefinition("effects.yml", List.of("effects"), false)
        );
    }

    public void updateAndReload() {
        try {
            ensureDataFolder();

            YamlConfiguration legacyMain = loadConfiguration("config.yml");
            migrateLegacySections(legacyMain);

            for (ConfigDefinition definition : definitions) {
                new ConfigService(plugin, definition.fileName(), "config-version").updateFile();
            }

            plugin.reloadConfig();
            mergeSupplementalConfigs();
        } catch (Exception e) {
            plugin.getLogger().severe("Error during managed config update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveAll() {
        try {
            FileConfiguration runtimeConfig = plugin.getConfig();
            for (ConfigDefinition definition : definitions) {
                YamlConfiguration output = new YamlConfiguration();
                for (String root : definition.roots()) {
                    if (!runtimeConfig.contains(root)) {
                        continue;
                    }
                    copyPath(output, root, runtimeConfig.get(root));
                }
                output.save(new File(plugin.getDataFolder(), definition.fileName()));
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error saving managed configs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mergeSupplementalConfigs() {
        FileConfiguration runtimeConfig = plugin.getConfig();

        for (ConfigDefinition definition : definitions) {
            if (definition.mainFile()) {
                continue;
            }

            YamlConfiguration source = loadConfiguration(definition.fileName());
            for (String root : definition.roots()) {
                runtimeConfig.set(root, null);
                if (source.contains(root)) {
                    copyPath(runtimeConfig, root, source.get(root));
                }
            }
        }
    }

    private void migrateLegacySections(YamlConfiguration legacyMain) {
        for (ConfigDefinition definition : definitions) {
            if (definition.mainFile()) {
                continue;
            }

            File targetFile = new File(plugin.getDataFolder(), definition.fileName());
            if (targetFile.exists()) {
                continue;
            }

            YamlConfiguration migrated = new YamlConfiguration();
            boolean hasLegacyData = false;
            for (String root : definition.roots()) {
                if (!legacyMain.contains(root)) {
                    continue;
                }
                copyPath(migrated, root, legacyMain.get(root));
                hasLegacyData = true;
            }

            if (!hasLegacyData) {
                continue;
            }

            try {
                migrated.save(targetFile);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to migrate legacy section to " + definition.fileName() + ": " + e.getMessage());
            }
        }
    }

    private void copyPath(FileConfiguration target, String path, Object value) {
        if (value instanceof ConfigurationSection section) {
            if (!target.isConfigurationSection(path)) {
                target.createSection(path);
            }
            for (String key : section.getKeys(false)) {
                copyPath(target, path + "." + key, section.get(key));
            }
            return;
        }

        if (value instanceof List<?> list) {
            target.set(path, new ArrayList<>(list));
            return;
        }

        target.set(path, value);
    }

    private YamlConfiguration loadConfiguration(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        return YamlConfiguration.loadConfiguration(file);
    }

    private void ensureDataFolder() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }

    public record ConfigDefinition(String fileName, List<String> roots, boolean mainFile) {
    }
}
