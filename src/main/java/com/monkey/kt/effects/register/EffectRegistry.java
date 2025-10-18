package com.monkey.kt.effects.register;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
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
        KillEffectFactory.clearEffects();
        Set<String> enabledEffects = new HashSet<>();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("effects");
        if (section == null) return;

        Reflections reflections = new Reflections("com.monkey.kt.effects.list");
        Set<Class<? extends KillEffect>> classes = reflections.getSubTypesOf(KillEffect.class);

        for (Class<? extends KillEffect> clazz : classes) {
            String simpleName = clazz.getSimpleName();
            String key = simpleName.replace("Effect", "").toLowerCase();

            boolean enabled = section.getBoolean(key + ".enabled", true);
            if (!enabled) continue;

            try {
                Constructor<? extends KillEffect> constructor = clazz.getConstructor(KT.class);
                KillEffect effect = constructor.newInstance(plugin);

                KillEffectFactory.registerEffect(key, effect);
                enabledEffects.add(key);
            } catch (Exception e) {
                plugin.getLogger().warning("Impossibile registrare l'effetto: " + clazz.getName());
                e.printStackTrace();
            }
        }

        if (plugin.getCustomEffectLoader() != null) {
            List<CustomEffectConfig> customConfigs =
                    plugin.getCustomEffectLoader().getLoadedEffects();

            for (com.monkey.kt.effects.custom.CustomEffectConfig config : customConfigs) {
                enabledEffects.add(config.getId());
            }
        }

        if (reloadGUI && plugin.getGuiManager() != null) {
            plugin.getGuiManager().reloadGUI(enabledEffects);
        }
    }

    public int getLoadedEffectsCount() {
        return KillEffectFactory.getRegisteredEffects().size();
    }

}