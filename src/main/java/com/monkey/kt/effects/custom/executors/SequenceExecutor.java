package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import com.monkey.kt.effects.custom.CustomKillEffect;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class SequenceExecutor {

    private final KT plugin;
    private final CustomKillEffect effect;

    public SequenceExecutor(KT plugin, CustomKillEffect effect) {
        this.plugin = plugin;
        this.effect = effect;
    }

    public void execute(List<CustomEffectConfig.SequenceStep> steps, Player killer, Location location) {
        if (steps == null || steps.isEmpty()) {
            return;
        }

        for (CustomEffectConfig.SequenceStep step : steps) {
            int tick = step.getTick();

            SchedulerWrapper.runTaskLater(plugin, () -> {
                executeActions(step.getActions(), killer, location);
            }, tick);
        }
    }

    private void executeActions(List<?> actions, Player killer, Location location) {
        if (actions == null || actions.isEmpty()) {
            plugin.getLogger().warning("[CustomEffect] Actions list is null or empty!");
            return;
        }

        plugin.getLogger().info("[CustomEffect] Executing " + actions.size() + " actions");

        for (Object actionObj : actions) {
            plugin.getLogger().info("[CustomEffect] Action object type: " + actionObj.getClass().getName());

            ConfigurationSection action = null;

            if (actionObj instanceof ConfigurationSection) {
                action = (ConfigurationSection) actionObj;
                plugin.getLogger().info("[CustomEffect] Action is ConfigurationSection");
            } else if (actionObj instanceof Map) {
                // Converti Map in ConfigurationSection usando YamlConfiguration
                Map<?, ?> actionMap = (Map<?, ?>) actionObj;
                plugin.getLogger().info("[CustomEffect] Action is Map with " + actionMap.size() + " entries");

                YamlConfiguration tempConfig = new YamlConfiguration();

                for (Map.Entry<?, ?> entry : actionMap.entrySet()) {
                    plugin.getLogger().info("[CustomEffect]   Map entry: " + entry.getKey() + " = " + entry.getValue());
                    tempConfig.set(entry.getKey().toString(), entry.getValue());
                }
                action = tempConfig;
            } else {
                plugin.getLogger().warning("[CustomEffect] Unknown action type: " + actionObj.getClass().getName());
                continue;
            }

            String type = action.getString("type", "");
            plugin.getLogger().info("[CustomEffect] Action type RAW: '" + type + "'");
            plugin.getLogger().info("[CustomEffect] Action type UPPER: '" + type.toUpperCase() + "'");

            if (type == null || type.isEmpty()) {
                plugin.getLogger().warning("[CustomEffect] Action type is null or empty!");
                continue;
            }

            type = type.toUpperCase();

            switch (type) {
                case "SOUND":
                    plugin.getLogger().info("[CustomEffect] Executing SOUND action");
                    executeSound(action, location);
                    break;

                case "PARTICLE":
                    plugin.getLogger().info("[CustomEffect] Executing PARTICLE action");
                    executeParticle(action, location);
                    break;

                case "PARTICLE_PATTERN":
                    plugin.getLogger().info("[CustomEffect] Executing PARTICLE_PATTERN action");
                    executePattern(action, location);
                    break;

                case "DAMAGE":
                    plugin.getLogger().info("[CustomEffect] Executing DAMAGE action");
                    executeDamage(action, killer, location);
                    break;

                default:
                    plugin.getLogger().warning("[CustomEffect] Unknown sequence action type: '" + type + "'");
            }
        }
    }

    private void executeSound(ConfigurationSection action, Location location) {
        CustomEffectConfig.SoundData soundData = new CustomEffectConfig.SoundData(
                action.getString("sound"),
                action.getDouble("volume", 1.0),
                action.getDouble("pitch", 1.0),
                0
        );
        effect.executeSound(soundData, location);
    }

    private void executeParticle(ConfigurationSection action, Location location) {
        CustomEffectConfig.ParticleData particleData = new CustomEffectConfig.ParticleData(
                action.getString("particle"),
                action.getInt("count", 10),
                0, 0, 0,
                0.01,
                0, 1, 1
        );
        effect.executeParticle(particleData, location);
    }

    private void executePattern(ConfigurationSection action, Location location) {
        CustomEffectConfig.PatternData patternData = new CustomEffectConfig.PatternData(
                action.getString("pattern"),
                action.getString("particle"),
                action.getDouble("radius", 3.0),
                30, 20, 0.5, 0
        );
        effect.executePattern(patternData, location);
    }

    private void executeDamage(ConfigurationSection action, Player killer, Location location) {
        double radius = action.getDouble("radius", 5.0);
        double damage = action.getDouble("damage", 5.0);
        effect.executeDamage(killer, location, damage, radius);
    }
}