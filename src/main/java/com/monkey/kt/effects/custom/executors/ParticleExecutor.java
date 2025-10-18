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

    public void execute(List<CustomEffectConfig.ParticleData> particles, Location location) {
        if (particles == null || particles.isEmpty() || location.getWorld() == null) {
            return;
        }

        for (CustomEffectConfig.ParticleData particleData : particles) {
            if (particleData.getDelay() > 0) {
                SchedulerWrapper.runTaskLater(plugin, () -> {
                    startParticleEffect(particleData, location);
                }, particleData.getDelay());
            } else {
                startParticleEffect(particleData, location);
            }
        }
    }

    public void executeParticle(CustomEffectConfig.ParticleData particleData, Location location) {
        spawnParticle(particleData, location);
    }

    private void startParticleEffect(CustomEffectConfig.ParticleData particleData, Location location) {
        int duration = particleData.getDuration();
        int interval = particleData.getInterval();

        final int[] ticksElapsed = {0};

        SchedulerWrapper.ScheduledTask task = SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            @Override
            public void run() {
                if (ticksElapsed[0] >= duration) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                spawnParticle(particleData, location);
                ticksElapsed[0] += interval;
            }
        }, location, 0L, interval);
    }

    private void spawnParticle(CustomEffectConfig.ParticleData particleData, Location location) {
        Particle particle = particleData.getParticle();
        if (particle == null || location.getWorld() == null) {
            return;
        }

        try {
            if (particle == Particle.DUST) {
                Particle.DustOptions dustOptions = new Particle.DustOptions(
                        Color.fromRGB(
                                particleData.getRed(),
                                particleData.getGreen(),
                                particleData.getBlue()
                        ),
                        (float) particleData.getSize()
                );

                location.getWorld().spawnParticle(
                        particle,
                        location,
                        particleData.getCount(),
                        particleData.getOffsetX(),
                        particleData.getOffsetY(),
                        particleData.getOffsetZ(),
                        particleData.getSpeed(),
                        dustOptions
                );
            } else {
                location.getWorld().spawnParticle(
                        particle,
                        location,
                        particleData.getCount(),
                        particleData.getOffsetX(),
                        particleData.getOffsetY(),
                        particleData.getOffsetZ(),
                        particleData.getSpeed()
                );
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn particle " + particle.name() + ": " + e.getMessage());
        }
    }
}