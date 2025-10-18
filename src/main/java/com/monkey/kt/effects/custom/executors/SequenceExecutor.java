package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import com.monkey.kt.effects.custom.CustomKillEffect;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

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
            return;
        }

        for (Object actionObj : actions) {
            if (!(actionObj instanceof ConfigurationSection)) {
                continue;
            }

            ConfigurationSection action = (ConfigurationSection) actionObj;
            String type = action.getString("type", "").toUpperCase();

            switch (type) {
                case "SOUND":
                    executeSound(action, location);
                    break;

                case "PARTICLE":
                    executeParticle(action, location);
                    break;

                case "PARTICLE_PATTERN":
                    executePattern(action, location);
                    break;

                case "DAMAGE":
                    executeDamage(action, killer, location);
                    break;

                default:
                    plugin.getLogger().warning("Unknown sequence action type: " + type);
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