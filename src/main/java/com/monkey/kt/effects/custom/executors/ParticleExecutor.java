package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.List;

public class ParticleExecutor {

    private final KT plugin;

    public ParticleExecutor(KT plugin) {
        this.plugin = plugin;
    }

    public void execute(String effectId, List<CustomEffectConfig.ParticleData> particles, Location location) {
        if (particles == null || particles.isEmpty() || location.getWorld() == null) {
            return;
        }

        for (CustomEffectConfig.ParticleData particleData : particles) {
            if (particleData.getDelay() > 0) {
                SchedulerWrapper.runTaskLater(plugin, () -> {
                    startParticleEffect(effectId, particleData, location);
                }, particleData.getDelay());
            } else {
                startParticleEffect(effectId, particleData, location);
            }
        }
    }

    public void executeParticle(String effectId, CustomEffectConfig.ParticleData particleData, Location location) {
        spawnParticle(effectId, particleData, location);
    }

    private void startParticleEffect(String effectId, CustomEffectConfig.ParticleData particleData, Location location) {
        int duration = Math.max(1, particleData.getDuration());
        long interval = plugin.getParticlePerformanceManager().scaleTickInterval(
                effectId,
                Math.max(1, particleData.getInterval()),
                false
        );

        final long[] ticksElapsed = {0};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (ticksElapsed[0] >= duration) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                spawnParticle(effectId, particleData, location);
                ticksElapsed[0] += interval;
            }
        }, location, 0L, interval);
    }

    private void spawnParticle(String effectId, CustomEffectConfig.ParticleData particleData, Location location) {
        Particle particle = particleData.getParticle();
        if (particle == null || location.getWorld() == null) {
            return;
        }

        try {
            Class<?> dataType = particle.getDataType();
            Color color = Color.fromRGB(
                    particleData.getRed(),
                    particleData.getGreen(),
                    particleData.getBlue()
            );

            if (!isSupportedParticle(particle, dataType)) {
                plugin.getLogger().warning("Skipping unsupported custom particle " + particle.name()
                        + " because it requires data type " + dataType.getSimpleName());
                return;
            }

            if (particle == Particle.DUST) {
                Particle.DustOptions dustOptions = new Particle.DustOptions(color, (float) particleData.getSize());
                int scaledCount = plugin.getParticlePerformanceManager().scaleParticleCount(effectId, particleData.getCount(), false);
                location.getWorld().spawnParticle(
                        particle,
                        location,
                        scaledCount,
                        particleData.getOffsetX(),
                        particleData.getOffsetY(),
                        particleData.getOffsetZ(),
                        particleData.getSpeed(),
                        dustOptions
                );
                return;
            }

            if (particle == Particle.DUST_COLOR_TRANSITION) {
                Particle.DustTransition dustTransition = new Particle.DustTransition(
                        color,
                        color,
                        (float) particleData.getSize()
                );
                int scaledCount = plugin.getParticlePerformanceManager().scaleParticleCount(effectId, particleData.getCount(), false);
                location.getWorld().spawnParticle(
                        particle,
                        location,
                        scaledCount,
                        particleData.getOffsetX(),
                        particleData.getOffsetY(),
                        particleData.getOffsetZ(),
                        particleData.getSpeed(),
                        dustTransition
                );
                return;
            }

            if (particle == Particle.ENTITY_EFFECT) {
                int scaledCount = plugin.getParticlePerformanceManager().scaleParticleCount(effectId, particleData.getCount(), false);
                location.getWorld().spawnParticle(
                        particle,
                        location,
                        scaledCount,
                        particleData.getOffsetX(),
                        particleData.getOffsetY(),
                        particleData.getOffsetZ(),
                        particleData.getSpeed(),
                        color
                );
                return;
            }

            int scaledCount = plugin.getParticlePerformanceManager().scaleParticleCount(effectId, particleData.getCount(), false);
            location.getWorld().spawnParticle(
                    particle,
                    location,
                    scaledCount,
                    particleData.getOffsetX(),
                    particleData.getOffsetY(),
                    particleData.getOffsetZ(),
                    particleData.getSpeed()
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn particle " + particle.name() + ": " + e.getMessage());
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
