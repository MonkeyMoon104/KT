package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.List;

public class PatternExecutor {

    private final KT plugin;

    public PatternExecutor(KT plugin) {
        this.plugin = plugin;
    }

    public void execute(List<CustomEffectConfig.PatternData> patterns, Location location) {
        if (patterns == null || patterns.isEmpty()) {
            return;
        }

        for (CustomEffectConfig.PatternData pattern : patterns) {
            executePattern(pattern, location);
        }
    }

    public void executePattern(CustomEffectConfig.PatternData pattern, Location location) {
        String type = pattern.getType().toUpperCase();

        switch (type) {
            case "CIRCLE":
                createCircle(pattern, location);
                break;
            case "SPHERE":
                createSphere(pattern, location);
                break;
            case "HELIX":
                createHelix(pattern, location);
                break;
            case "SPIRAL":
                createSpiral(pattern, location);
                break;
            case "RING":
                createRing(pattern, location);
                break;
            case "CUBE":
                createCube(pattern, location);
                break;
            case "WAVE":
                createWave(pattern, location);
                break;
            default:
                plugin.getLogger().warning("Unknown pattern type: " + type);
        }
    }

    private void createCircle(CustomEffectConfig.PatternData pattern, Location location) {
        final int[] tick = {0};
        int maxTicks = pattern.getDuration();

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (tick[0]++ >= maxTicks) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = pattern.getRadius();
                int points = pattern.getPoints();
                double angleOffset = tick[0] * pattern.getSpeed();

                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI * i / points) + angleOffset;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = location.clone().add(x, pattern.getHeight(), z);
                    location.getWorld().spawnParticle(pattern.getParticle(), particleLoc, 1, 0, 0, 0, 0);
                }
            }
        }, location, 0L, 1L);
    }

    private void createSphere(CustomEffectConfig.PatternData pattern, Location location) {
        final int[] tick = {0};
        int maxTicks = pattern.getDuration();

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (tick[0]++ >= maxTicks) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = pattern.getRadius();
                int points = pattern.getPoints();

                for (int i = 0; i < points; i++) {
                    double phi = Math.acos(1 - 2.0 * (i + 0.5) / points);
                    double theta = Math.PI * (1 + Math.sqrt(5)) * (i + 0.5) + tick[0] * pattern.getSpeed();

                    double x = radius * Math.cos(theta) * Math.sin(phi);
                    double y = radius * Math.sin(theta) * Math.sin(phi);
                    double z = radius * Math.cos(phi);

                    Location particleLoc = location.clone().add(x, y + pattern.getHeight(), z);
                    location.getWorld().spawnParticle(pattern.getParticle(), particleLoc, 1, 0, 0, 0, 0);
                }
            }
        }, location, 0L, 2L);
    }

    private void createHelix(CustomEffectConfig.PatternData pattern, Location location) {
        final int[] tick = {0};
        int maxTicks = pattern.getDuration();

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (tick[0]++ >= maxTicks) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = pattern.getRadius();
                int points = pattern.getPoints();
                double maxHeight = pattern.getHeight();

                for (int i = 0; i < points; i++) {
                    double progress = (double) i / points;
                    double angle = progress * Math.PI * 4 + tick[0] * pattern.getSpeed();
                    double height = progress * maxHeight;

                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = location.clone().add(x, height, z);
                    location.getWorld().spawnParticle(pattern.getParticle(), particleLoc, 1, 0, 0, 0, 0);
                }
            }
        }, location, 0L, 1L);
    }

    private void createSpiral(CustomEffectConfig.PatternData pattern, Location location) {
        final int[] tick = {0};
        int maxTicks = pattern.getDuration();

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (tick[0]++ >= maxTicks) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = pattern.getRadius();
                int points = pattern.getPoints();
                double angleOffset = tick[0] * pattern.getSpeed();

                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI * i / points) + angleOffset;
                    double currentRadius = radius * ((double) i / points);

                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;

                    Location particleLoc = location.clone().add(x, pattern.getHeight(), z);
                    location.getWorld().spawnParticle(pattern.getParticle(), particleLoc, 1, 0, 0, 0, 0);
                }
            }
        }, location, 0L, 1L);
    }

    private void createRing(CustomEffectConfig.PatternData pattern, Location location) {
        double radius = pattern.getRadius();
        int points = pattern.getPoints();

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location particleLoc = location.clone().add(x, pattern.getHeight(), z);
            location.getWorld().spawnParticle(pattern.getParticle(), particleLoc, 1, 0, 0, 0, 0);
        }
    }

    private void createCube(CustomEffectConfig.PatternData pattern, Location location) {
        double size = pattern.getRadius();
        int pointsPerEdge = pattern.getPoints() / 12;

        for (int edge = 0; edge < 12; edge++) {
            for (int i = 0; i <= pointsPerEdge; i++) {
                double progress = (double) i / pointsPerEdge;
                Location particleLoc = getCubeEdgePoint(location, size, edge, progress, pattern.getHeight());
                location.getWorld().spawnParticle(pattern.getParticle(), particleLoc, 1, 0, 0, 0, 0);
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

    private void createWave(CustomEffectConfig.PatternData pattern, Location location) {
        final int[] tick = {0};
        int maxTicks = pattern.getDuration();

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (tick[0]++ >= maxTicks) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                double radius = pattern.getRadius();
                int points = pattern.getPoints();
                double timeOffset = tick[0] * pattern.getSpeed();

                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = Math.sin(angle * 3 + timeOffset) * pattern.getHeight();

                    Location particleLoc = location.clone().add(x, y, z);
                    location.getWorld().spawnParticle(pattern.getParticle(), particleLoc, 1, 0, 0, 0, 0);
                }
            }
        }, location, 0L, 1L);
    }
}