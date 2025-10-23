package com.monkey.kt.effects.custom;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffectFactory;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CustomEffectLoader {

    private final KT plugin;
    private final File customEffectsFolder;
    private final List<CustomEffectConfig> loadedEffects;

    public CustomEffectLoader(KT plugin) {
        this.plugin = plugin;
        this.customEffectsFolder = new File(plugin.getDataFolder(), "customeffects");
        this.loadedEffects = new ArrayList<>();

        setupCustomEffectsFolder();
    }

    private void setupCustomEffectsFolder() {
        if (!customEffectsFolder.exists()) {
            if (customEffectsFolder.mkdirs()) {
                plugin.getLogger().info("Created customeffects folder");
            }
        }

        File[] existingFiles = customEffectsFolder.listFiles((dir, name) ->
                name.endsWith(".yml") && !name.equals("defaulteffect.yml")
        );

        if (existingFiles == null || existingFiles.length == 0) {
            copyDefaultEffectFromResources();
        }
    }

    private void copyDefaultEffectFromResources() {
        File defaultEffectFile = new File(customEffectsFolder, "defaulteffect.yml");

        if (defaultEffectFile.exists()) {
            plugin.getLogger().info("defaulteffect.yml already exists, skipping...");
            return;
        }

        try (InputStream in = plugin.getResource("customeffects/defaulteffect.yml")) {
            if (in != null) {
                Files.copy(in, defaultEffectFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Created defaulteffect.yml template from resources");
            } else {
                plugin.getLogger().warning("defaulteffect.yml not found in plugin resources!");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to copy defaulteffect.yml from resources", e);
        }
    }

    public void loadAllCustomEffects() {
        int previousCount = loadedEffects.size();
        loadedEffects.clear();

        plugin.getLogger().info("Previous custom effects count: " + previousCount);

        File[] files = customEffectsFolder.listFiles((dir, name) ->
                name.endsWith(".yml") && !name.equals("defaulteffect.yml")
        );

        if (files == null || files.length == 0) {
            plugin.getLogger().info("No custom effects found in customeffects folder");
            return;
        }

        int loaded = 0;
        int failed = 0;

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                CustomEffectConfig effectConfig = new CustomEffectConfig(config, file.getName(), plugin);

                if (effectConfig.isValid()) {
                    loadedEffects.add(effectConfig);

                    CustomKillEffect effect = new CustomKillEffect(plugin, effectConfig);
                    KillEffectFactory.registerEffect(effectConfig.getId(), effect);

                    plugin.getLogger().info("Loaded custom effect: " + effectConfig.getId() +
                            " from " + file.getName());
                    loaded++;
                } else {
                    plugin.getLogger().warning("Invalid custom effect configuration in " + file.getName());
                    failed++;
                }

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to load custom effect from " + file.getName(), e);
                failed++;
            }
        }

        plugin.getLogger().info("Custom Effects Summary: " + loaded + " loaded, " + failed + " failed");
    }

    public List<CustomEffectConfig> getLoadedEffects() {
        return new ArrayList<>(loadedEffects);
    }

    public CustomEffectConfig getEffectConfig(String id) {
        return loadedEffects.stream()
                .filter(config -> config.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public void reloadCustomEffects() {
        plugin.getLogger().info("Reloading custom effects...");

        for (CustomEffectConfig config : loadedEffects) {
            com.monkey.kt.effects.KillEffectFactory.getRegisteredEffects().remove(config.getId().toLowerCase());
        }

        loadAllCustomEffects();
    }
}