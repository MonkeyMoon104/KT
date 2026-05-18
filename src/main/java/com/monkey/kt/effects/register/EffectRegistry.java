package com.monkey.kt.effects.register;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import com.monkey.kt.effects.custom.CustomKillEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EffectRegistry {

    private final KT plugin;

    public EffectRegistry(KT plugin) {
        this.plugin = plugin;
    }

    public void loadEffects() {
        loadEffects(true);
    }

    public void loadEffects(boolean reloadGUI) {
        Set<String> enabledEffects = new HashSet<>();
        KillEffectFactory.clearEffects();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("effects");
        if (section == null) return;

        Reflections reflections = new Reflections("com.monkey.kt.effects.list");
        Set<Class<? extends KillEffect>> classes = reflections.getSubTypesOf(KillEffect.class);

        for (Class<? extends KillEffect> clazz : classes) {
            String simpleName = clazz.getSimpleName();
            String key = simpleName.replace("Effect", "").toLowerCase(Locale.ROOT);

            boolean enabled = section.getBoolean(key + ".enabled", true);
            if (!enabled) continue;

            String configuredId = normalizeEffectId(section.getString(key + ".id", key));
            List<String> aliases = parseAliases(section, key + ".aliases");

            try {
                Constructor<? extends KillEffect> constructor = clazz.getConstructor(KT.class);
                KillEffect effect = constructor.newInstance(plugin);

                if (enabledEffects.contains(configuredId)) {
                    plugin.getLogger().warning("Duplicate effect id '" + configuredId
                            + "' detected for built-in effect '" + key + "'. Skipping duplicate.");
                    continue;
                }

                plugin.getEffectIdMapper().registerBuiltIn(key, configuredId, aliases);
                KillEffectFactory.registerEffect(configuredId, effect);
                enabledEffects.add(configuredId);
            } catch (Exception e) {
                plugin.getLogger().warning("Impossibile registrare l'effetto: " + clazz.getName());
                e.printStackTrace();
            }
        }

        if (plugin.getCustomEffectLoader() != null) {
            List<CustomEffectConfig> customConfigs =
                    plugin.getCustomEffectLoader().getLoadedEffects();

            for (com.monkey.kt.effects.custom.CustomEffectConfig config : customConfigs) {
                String customId = config.getId().toLowerCase(Locale.ROOT);
                if (enabledEffects.contains(customId)) {
                    plugin.getLogger().warning("Duplicate effect id '" + customId
                            + "' detected in custom effect '" + config.getName() + "'. Skipping duplicate.");
                    continue;
                }

                KillEffectFactory.registerEffect(customId, new CustomKillEffect(plugin, config));
                enabledEffects.add(customId);
                plugin.getLogger().fine("Added custom effect to enabled list: " + customId);
            }
        }

        plugin.getLogger().info("Total enabled effects before GUI reload: " + enabledEffects.size());

        if (reloadGUI && plugin.getGuiManager() != null) {
            plugin.getGuiManager().reloadGUI(enabledEffects);
        }
    }

    public int getLoadedEffectsCount() {
        return KillEffectFactory.getRegisteredEffects().size();
    }

    private List<String> parseAliases(ConfigurationSection section, String path) {
        List<String> aliases = new ArrayList<>();
        for (String rawAlias : section.getStringList(path)) {
            String normalizedAlias = normalizeEffectId(rawAlias);
            if (!normalizedAlias.isEmpty()) {
                aliases.add(normalizedAlias);
            }
        }
        return aliases;
    }

    private String normalizeEffectId(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

}
