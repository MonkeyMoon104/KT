package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import org.bukkit.Color;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.List;

public class PatternExecutor {

    private final KT plugin;

    public PatternExecutor(KT plugin) {
        this.plugin = plugin;
    }

    public void execute(String effectId, List<CustomEffectConfig.PatternData> patterns, Location location) {
        if (patterns == null || patterns.isEmpty()) {
            return;
        }

        for (CustomEffectConfig.PatternData pattern : patterns) {
            executePattern(effectId, pattern, location);
        }
    }

    public void executePattern(String effectId, CustomEffectConfig.PatternData pattern, Location location) {
        String type = pattern.getType().toUpperCase();
        Particle particle = pattern.getParticle();
        if (particle == null) {
            return;
        }

        Class<?> dataType = particle.getDataType();
        if (!isSupportedParticle(particle, dataType)) {
            plugin.getLogger().warning("Skipping unsupported pattern particle " + particle.name()
                    + " because it requires data type " + dataType.getSimpleName());
            return;
        }

        switch (type) {
            case "CIRCLE":
                createCircle(effectId, pattern, location);
                break;
            case "SPHERE":
                createSphere(effectId, pattern, location);
                break;
            case "HELIX":
                createHelix(effectId, pattern, location);
                break;
            case "SPIRAL":
                createSpiral(effectId, pattern, location);
                break;
            case "RING":
                createRing(effectId, pattern, location);
                break;
            case "CUBE":
                createCube(effectId, pattern, location);
                break;
            case "WAVE":
                createWave(effectId, pattern, location);
                break;
            default:
                plugin.getLogger().warning("Unknown pattern type: " + type);
        }
    }

    private void createCircle(String effectId, CustomEffectConfig.PatternData pattern, Location location) {
        final int[] tick = {0};
        int maxTicks = pattern.getDuration();
        long interval = plugin.getParticlePerformanceManager().scaleTickInterval(effectId, 1L, false);

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (tick[0]++ >= maxTicks) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = pattern.getRadius();
                int points = plugin.getParticlePerformanceManager().scaleLoopCount(effectId, pattern.getPoints(), false);
                double angleOffset = tick[0] * pattern.getSpeed();

                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI * i / points) + angleOffset;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = location.clone().add(x, pattern.getHeight(), z);
                    spawnPatternParticle(effectId, pattern, particleLoc);
                }
            }
        }, location, 0L, interval);
    }

    private void createSphere(String effectId, CustomEffectConfig.PatternData pattern, Location location) {
        final int[] tick = {0};
        int maxTicks = pattern.getDuration();
        long interval = plugin.getParticlePerformanceManager().scaleTickInterval(effectId, 2L, false);

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (tick[0]++ >= maxTicks) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = pattern.getRadius();
                int points = plugin.getParticlePerformanceManager().scaleLoopCount(effectId, pattern.getPoints(), false);

                for (int i = 0; i < points; i++) {
                    double phi = Math.acos(1 - 2.0 * (i + 0.5) / points);
                    double theta = Math.PI * (1 + Math.sqrt(5)) * (i + 0.5) + tick[0] * pattern.getSpeed();

                    double x = radius * Math.cos(theta) * Math.sin(phi);
                    double y = radius * Math.sin(theta) * Math.sin(phi);
                    double z = radius * Math.cos(phi);

                    Location particleLoc = location.clone().add(x, y + pattern.getHeight(), z);
                    spawnPatternParticle(effectId, pattern, particleLoc);
                }
            }
        }, location, 0L, interval);
    }

    private void createHelix(String effectId, CustomEffectConfig.PatternData pattern, Location location) {
        final int[] tick = {0};
        int maxTicks = pattern.getDuration();
        long interval = plugin.getParticlePerformanceManager().scaleTickInterval(effectId, 1L, false);

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (tick[0]++ >= maxTicks) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = pattern.getRadius();
                int points = plugin.getParticlePerformanceManager().scaleLoopCount(effectId, pattern.getPoints(), false);
                double maxHeight = pattern.getHeight();

                for (int i = 0; i < points; i++) {
                    double progress = (double) i / points;
                    double angle = progress * Math.PI * 4 + tick[0] * pattern.getSpeed();
                    double height = progress * maxHeight;

                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = location.clone().add(x, height, z);
                    spawnPatternParticle(effectId, pattern, particleLoc);
                }
            }
        }, location, 0L, interval);
    }

    private void createSpiral(String effectId, CustomEffectConfig.PatternData pattern, Location location) {
        final int[] tick = {0};
        int maxTicks = pattern.getDuration();
        long interval = plugin.getParticlePerformanceManager().scaleTickInterval(effectId, 1L, false);

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (tick[0]++ >= maxTicks) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = pattern.getRadius();
                int points = plugin.getParticlePerformanceManager().scaleLoopCount(effectId, pattern.getPoints(), false);
                double angleOffset = tick[0] * pattern.getSpeed();

                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI * i / points) + angleOffset;
                    double currentRadius = radius * ((double) i / points);

                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;

                    Location particleLoc = location.clone().add(x, pattern.getHeight(), z);
                    spawnPatternParticle(effectId, pattern, particleLoc);
                }
            }
        }, location, 0L, interval);
    }

    private void createRing(String effectId, CustomEffectConfig.PatternData pattern, Location location) {
        double radius = pattern.getRadius();
        int points = plugin.getParticlePerformanceManager().scaleLoopCount(effectId, pattern.getPoints(), false);

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location particleLoc = location.clone().add(x, pattern.getHeight(), z);
            spawnPatternParticle(effectId, pattern, particleLoc);
        }
    }

    private void createCube(String effectId, CustomEffectConfig.PatternData pattern, Location location) {
        double size = pattern.getRadius();
        int scaledPoints = plugin.getParticlePerformanceManager().scaleLoopCount(effectId, pattern.getPoints(), false);
        int pointsPerEdge = Math.max(1, scaledPoints / 12);

        for (int edge = 0; edge < 12; edge++) {
            for (int i = 0; i <= pointsPerEdge; i++) {
                double progress = (double) i / pointsPerEdge;
                Location particleLoc = getCubeEdgePoint(location, size, edge, progress, pattern.getHeight());
                spawnPatternParticle(effectId, pattern, particleLoc);
            }
        }
    }

    private Location getCubeEdgePoint(Location center, double size, int edge, double progress, double heightOffset) {
        double half = size / 2;
        double x = 0, y = 0, z = 0;

        switch (edge) {
            case 0: x = -half + progress * size; y = -half; z = -half; break;
            case 1: x = half; y = -half; z = -half + progress * size; break;
            case 2: x = half - progress * size; y = -half; z = half; break;
            case 3: x = -half; y = -half; z = half - progress * size; break;
            case 4: x = -half + progress * size; y = half; z = -half; break;
            case 5: x = half; y = half; z = -half + progress * size; break;
            case 6: x = half - progress * size; y = half; z = half; break;
            case 7: x = -half; y = half; z = half - progress * size; break;
            case 8: x = -half; y = -half + progress * size; z = -half; break;
            case 9: x = half; y = -half + progress * size; z = -half; break;
            case 10: x = half; y = -half + progress * size; z = half; break;
            case 11: x = -half; y = -half + progress * size; z = half; break;
        }

        return center.clone().add(x, y + heightOffset, z);
    }

    private void createWave(String effectId, CustomEffectConfig.PatternData pattern, Location location) {
        final int[] tick = {0};
        int maxTicks = pattern.getDuration();
        long interval = plugin.getParticlePerformanceManager().scaleTickInterval(effectId, 1L, false);

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (tick[0]++ >= maxTicks) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = pattern.getRadius();
                int points = plugin.getParticlePerformanceManager().scaleLoopCount(effectId, pattern.getPoints(), false);
                double timeOffset = tick[0] * pattern.getSpeed();

                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = Math.sin(angle * 3 + timeOffset) * pattern.getHeight();

                    Location particleLoc = location.clone().add(x, y, z);
                    spawnPatternParticle(effectId, pattern, particleLoc);
                }
            }
        }, location, 0L, interval);
    }

    private void spawnPatternParticle(String effectId, CustomEffectConfig.PatternData pattern, Location particleLoc) {
        if (particleLoc.getWorld() == null) {
            return;
        }

        Particle particle = pattern.getParticle();
        if (particle == null) {
            return;
        }

        try {
            Class<?> dataType = particle.getDataType();
            Color color = Color.fromRGB(pattern.getRed(), pattern.getGreen(), pattern.getBlue());

            if (particle == Particle.DUST) {
                particleLoc.getWorld().spawnParticle(
                        particle,
                        particleLoc,
                        plugin.getParticlePerformanceManager().scaleParticleCount(effectId, 1, false),
                        0,
                        0,
                        0,
                        0,
                        new Particle.DustOptions(color, (float) pattern.getSize())
                );
                return;
            }

            if (particle == Particle.DUST_COLOR_TRANSITION) {
                particleLoc.getWorld().spawnParticle(
                        particle,
                        particleLoc,
                        plugin.getParticlePerformanceManager().scaleParticleCount(effectId, 1, false),
                        0,
                        0,
                        0,
                        0,
                        new Particle.DustTransition(color, color, (float) pattern.getSize())
                );
                return;
            }

            if (particle == Particle.ENTITY_EFFECT) {
                particleLoc.getWorld().spawnParticle(
                        particle,
                        particleLoc,
                        plugin.getParticlePerformanceManager().scaleParticleCount(effectId, 1, false),
                        0, 0, 0, 0, color
                );
                return;
            }

            particleLoc.getWorld().spawnParticle(
                    particle,
                    particleLoc,
                    plugin.getParticlePerformanceManager().scaleParticleCount(effectId, 1, false),
                    0, 0, 0, 0
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn pattern particle " + particle.name() + ": " + e.getMessage());
        }
    }

    private boolean isSupportedParticle(Particle particle, Class<?> dataType) {
        if (dataType == Void.class) {
            return true;
        }

        return particle == Particle.DUST
                || particle == Particle.DUST_COLOR_TRANSITION
                || particle == Particle.ENTITY_EFFECT;
    }
}
