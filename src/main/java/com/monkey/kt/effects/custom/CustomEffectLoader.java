package com.monkey.kt.effects.custom;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffectFactory;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
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

                createDefaultTemplate();
            }
        }
    }

    private void createDefaultTemplate() {
        File templateFile = new File(customEffectsFolder, "defaulteffect.yml");

        if (!templateFile.exists()) {
            try (InputStream in = plugin.getResource("defaulteffect.yml")) {
                if (in != null) {
                    Files.copy(in, templateFile.toPath());
                    plugin.getLogger().info("Created defaulteffect.yml template in customeffects folder");
                } else {
                    createBasicTemplate(templateFile);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to create defaulteffect.yml template", e);
                createBasicTemplate(templateFile);
            }
        }
    }

    private void createBasicTemplate(File file) {
        YamlConfiguration config = new YamlConfiguration();

        config.set("effect.id", "example_effect");
        config.set("effect.name", "&bExample Effect");
        config.set("effect.description", Arrays.asList("&7This is an example", "&8Customize it!"));
        config.set("effect.icon", "DIAMOND");
        config.set("effect.price", 1000);
        config.set("effect.permission", "");

        config.set("sounds.enabled", true);
        config.set("sounds.sounds", Arrays.asList(
                createSoundMap("ENTITY_FIREWORK_ROCKET_BLAST", 1.0, 1.0, 0)
        ));

        config.set("particles.enabled", true);
        List<Object> particles = new ArrayList<>();
        particles.add(createParticleMap("FLAME", 50, 1.5, 1.5, 1.5, 0.05, 0, 20, 2));
        config.set("particles.effects", particles);

        config.set("damage.enabled", false);
        config.set("damage.value", 5.0);
        config.set("damage.radius", 5.0);

        try {
            config.save(file);
            plugin.getLogger().info("Created basic template at " + file.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save basic template", e);
        }
    }

    private Object createSoundMap(String sound, double volume, double pitch, int delay) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("sound", sound);
        map.put("volume", volume);
        map.put("pitch", pitch);
        map.put("delay", delay);
        return map;
    }

    private Object createParticleMap(String type, int count, double x, double y, double z,
                                     double speed, int delay, int duration, int interval) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("type", type);
        map.put("count", count);

        java.util.Map<String, Double> offset = new java.util.HashMap<>();
        offset.put("x", x);
        offset.put("y", y);
        offset.put("z", z);
        map.put("offset", offset);

        map.put("speed", speed);
        map.put("delay", delay);
        map.put("duration", duration);
        map.put("interval", interval);
        return map;
    }

    public void loadAllCustomEffects() {
        loadedEffects.clear();

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
                CustomEffectConfig effectConfig = new CustomEffectConfig(config, file.getName());

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
        loadAllCustomEffects();
    }
}