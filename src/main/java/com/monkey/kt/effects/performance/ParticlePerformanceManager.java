package com.monkey.kt.effects.performance;

import com.monkey.kt.KT;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ParticlePerformanceManager {

    private static final Set<String> DEFAULT_HEAVY_EFFECTS = Set.of(
            "dimensionalrift",
            "voidlotus",
            "cryocore",
            "wither",
            "prismaticnova",
            "stellarcollapse",
            "aurafarming",
            "warden"
    );

    private final KT plugin;
    private Settings settings;

    public ParticlePerformanceManager(KT plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("particle-performance");
        if (root == null) {
            this.settings = Settings.defaults();
            return;
        }

        boolean enabled = root.getBoolean("enabled", true);
        Preset preset = Preset.from(root.getString("preset", "BALANCED"));

        ConfigurationSection adaptiveSection = root.getConfigurationSection("adaptive");
        AdaptiveSettings adaptive = new AdaptiveSettings(
                adaptiveSection != null && adaptiveSection.getBoolean("enabled", true),
                adaptiveSection != null ? adaptiveSection.getDouble("tps_19", 0.90) : 0.90,
                adaptiveSection != null ? adaptiveSection.getDouble("tps_17", 0.75) : 0.75,
                adaptiveSection != null ? adaptiveSection.getDouble("tps_15", 0.55) : 0.55
        );

        ConfigurationSection advancedSection = root.getConfigurationSection("advanced");
        AdvancedSettings advanced = new AdvancedSettings(
                advancedSection != null ? advancedSection.getDouble("global-particle-multiplier", 0.75) : 0.75,
                advancedSection != null ? advancedSection.getDouble("global-loop-multiplier", 0.75) : 0.75,
                advancedSection != null ? advancedSection.getInt("min-particle-count", 1) : 1,
                advancedSection != null ? advancedSection.getDouble("heavy-effects-particle-multiplier", 0.55) : 0.55,
                advancedSection != null ? advancedSection.getDouble("heavy-effects-loop-multiplier", 0.60) : 0.60,
                advancedSection != null ? Math.max(1, advancedSection.getInt("repeating-tick-divider", 1)) : 1,
                loadOverrides(advancedSection != null ? advancedSection.getConfigurationSection("effects") : null)
        );

        this.settings = new Settings(enabled, preset, adaptive, advanced);
    }

    public int scaleParticleCount(String effectId, int originalCount, boolean heavyHint) {
        if (originalCount <= 0 || !settings.enabled) {
            return originalCount;
        }

        double multiplier = resolveParticleMultiplier(effectId, heavyHint);
        int scaled = (int) Math.round(originalCount * multiplier);
        return Math.max(settings.advanced.minParticleCount, scaled);
    }

    public int scaleLoopCount(String effectId, int originalCount, boolean heavyHint) {
        if (originalCount <= 1 || !settings.enabled) {
            return originalCount;
        }

        double multiplier = resolveLoopMultiplier(effectId, heavyHint);
        int scaled = (int) Math.round(originalCount * multiplier);
        return Math.max(1, scaled);
    }

    public long scaleTickInterval(String effectId, long originalInterval, boolean heavyHint) {
        if (originalInterval <= 0 || !settings.enabled) {
            return originalInterval;
        }

        int divider = settings.advanced.repeatingTickDivider;
        EffectOverride override = getOverride(effectId);
        if (override != null && override.tickDivider > 0) {
            divider = override.tickDivider;
        } else if (settings.preset == Preset.LOW && isHeavyEffect(effectId, heavyHint)) {
            divider = Math.max(divider, 2);
        }

        return Math.max(1L, originalInterval * divider);
    }

    public boolean isHeavyEffect(String effectId, boolean heavyHint) {
        return heavyHint || DEFAULT_HEAVY_EFFECTS.contains(normalize(effectId));
    }

    private double resolveParticleMultiplier(String effectId, boolean heavyHint) {
        if (!settings.enabled) {
            return 1.0;
        }

        EffectOverride override = getOverride(effectId);
        double base = override != null && override.particleMultiplier != null
                ? override.particleMultiplier
                : settings.preset.particleMultiplier(settings.advanced, isHeavyEffect(effectId, heavyHint));

        return clampMultiplier(base * adaptiveMultiplier());
    }

    private double resolveLoopMultiplier(String effectId, boolean heavyHint) {
        if (!settings.enabled) {
            return 1.0;
        }

        EffectOverride override = getOverride(effectId);
        double base = override != null && override.loopMultiplier != null
                ? override.loopMultiplier
                : settings.preset.loopMultiplier(settings.advanced, isHeavyEffect(effectId, heavyHint));

        return clampMultiplier(base * adaptiveMultiplier());
    }

    private double adaptiveMultiplier() {
        if (!settings.adaptive.enabled) {
            return 1.0;
        }

        double tps = currentTps();
        if (tps < 15.0) {
            return settings.adaptive.tps15;
        }
        if (tps < 17.0) {
            return settings.adaptive.tps17;
        }
        if (tps < 19.0) {
            return settings.adaptive.tps19;
        }
        return 1.0;
    }

    private double currentTps() {
        try {
            double[] tps = plugin.getServer().getTPS();
            if (tps != null && tps.length > 0 && !Double.isNaN(tps[0]) && tps[0] > 0) {
                return tps[0];
            }
        } catch (Throwable ignored) {
        }
        return 20.0;
    }

    private EffectOverride getOverride(String effectId) {
        if (effectId == null || effectId.isBlank()) {
            return null;
        }
        return settings.advanced.overrides.get(normalize(effectId));
    }

    private Map<String, EffectOverride> loadOverrides(ConfigurationSection section) {
        Map<String, EffectOverride> overrides = new HashMap<>();
        if (section == null) {
            return overrides;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection effectSection = section.getConfigurationSection(key);
            if (effectSection == null) {
                continue;
            }

            Double particleMultiplier = effectSection.contains("particle-multiplier")
                    ? effectSection.getDouble("particle-multiplier")
                    : null;
            Double loopMultiplier = effectSection.contains("loop-multiplier")
                    ? effectSection.getDouble("loop-multiplier")
                    : null;
            int tickDivider = Math.max(0, effectSection.getInt("tick-divider", 0));

            overrides.put(normalize(key), new EffectOverride(particleMultiplier, loopMultiplier, tickDivider));
        }

        return overrides;
    }

    private String normalize(String effectId) {
        return effectId == null ? "" : effectId.toLowerCase(Locale.ROOT);
    }

    private double clampMultiplier(double value) {
        return Math.max(0.05, Math.min(1.0, value));
    }

    private record Settings(boolean enabled, Preset preset, AdaptiveSettings adaptive, AdvancedSettings advanced) {
        private static Settings defaults() {
            return new Settings(
                    true,
                    Preset.BALANCED,
                    new AdaptiveSettings(true, 0.90, 0.75, 0.55),
                    new AdvancedSettings(0.75, 0.75, 1, 0.55, 0.60, 1, new HashMap<>())
            );
        }
    }

    private record AdaptiveSettings(boolean enabled, double tps19, double tps17, double tps15) {
    }

    private record AdvancedSettings(
            double globalParticleMultiplier,
            double globalLoopMultiplier,
            int minParticleCount,
            double heavyEffectsParticleMultiplier,
            double heavyEffectsLoopMultiplier,
            int repeatingTickDivider,
            Map<String, EffectOverride> overrides
    ) {
    }

    private record EffectOverride(Double particleMultiplier, Double loopMultiplier, int tickDivider) {
    }

    private enum Preset {
        OFF,
        LOW,
        BALANCED,
        HIGH,
        CUSTOM;

        private static Preset from(String raw) {
            if (raw == null || raw.isBlank()) {
                return BALANCED;
            }
            try {
                return Preset.valueOf(raw.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return BALANCED;
            }
        }

        private double particleMultiplier(AdvancedSettings advanced, boolean heavy) {
            return switch (this) {
                case OFF -> 1.0;
                case LOW -> heavy ? 0.28 : 0.35;
                case BALANCED -> heavy ? 0.48 : 0.60;
                case HIGH -> heavy ? 0.75 : 0.85;
                case CUSTOM -> heavy
                        ? advanced.globalParticleMultiplier * advanced.heavyEffectsParticleMultiplier
                        : advanced.globalParticleMultiplier;
            };
        }

        private double loopMultiplier(AdvancedSettings advanced, boolean heavy) {
            return switch (this) {
                case OFF -> 1.0;
                case LOW -> heavy ? 0.35 : 0.45;
                case BALANCED -> heavy ? 0.55 : 0.70;
                case HIGH -> heavy ? 0.80 : 0.90;
                case CUSTOM -> heavy
                        ? advanced.globalLoopMultiplier * advanced.heavyEffectsLoopMultiplier
                        : advanced.globalLoopMultiplier;
            };
        }
    }
}
