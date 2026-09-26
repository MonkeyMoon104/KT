package com.monkey.ktplus.hook.particleplus;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import me.dominikhun250.dev.particleplus.api.ParticleMix;
import me.dominikhun250.dev.particleplus.api.ParticlePlusAPI;
import me.dominikhun250.dev.particleplus.api.ParticlePlusProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ParticlePlusHook {
    private static final String PLUGIN_NAME = "ParticlePlus";
    private static final double PARTICLE_SUSPEND_RADIUS = 20.0;

    private final boolean pluginPresent;
    private final Logger logger;

    private ParticlePlusHook(boolean pluginPresent, Logger logger) {
        this.pluginPresent = pluginPresent;
        this.logger = logger;
    }

    public static ParticlePlusHook create(Logger logger) {
        boolean present = Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);
        if (present) {
            logger.info("[Hooks] ParticlePlus hooked");
        }
        return new ParticlePlusHook(present, logger);
    }

    public boolean available() {
        return pluginPresent;
    }

    public void suspendKillerParticle(Player killer) {
        if (!pluginPresent || killer == null) {
            return;
        }

        suspendPlayerParticle(killer);
    }

    public Set<UUID> suspendNearbyPlayerParticles(Location location) {
        Set<UUID> suspendedPlayers = new HashSet<>();

        if (!pluginPresent || location == null || location.getWorld() == null) {
            return suspendedPlayers;
        }

        double radiusSquared = PARTICLE_SUSPEND_RADIUS * PARTICLE_SUSPEND_RADIUS;

        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) > radiusSquared) {
                continue;
            }

            if (suspendPlayerParticle(player)) {
                suspendedPlayers.add(player.getUniqueId());
            }
        }

        return suspendedPlayers;
    }

    public void resumeNearbyPlayerParticles(Set<UUID> players) {
        if (!pluginPresent || players == null || players.isEmpty()) {
            return;
        }

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null && player.isOnline()) {
                resumePlayerParticle(player);
            }
        }
    }

    private boolean suspendPlayerParticle(Player player) {
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();

            if (api != null) {
                api.suspendPlayerParticle(player);
                return true;
            }
        } catch (Throwable error) {
            logger.warning(
                    "[Hooks] ParticlePlus suspendPlayerParticle failed: "
                            + error.getMessage()
            );
        }

        return false;
    }

    public void resumeKillerParticle(Player killer) {
        if (!pluginPresent || killer == null) {
            return;
        }

        resumePlayerParticle(killer);
    }

    private void resumePlayerParticle(Player player) {
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();

            if (api != null) {
                api.resumePlayerParticle(player);
            }
        } catch (Throwable error) {
            logger.warning(
                    "[Hooks] ParticlePlus resumePlayerParticle failed: "
                            + error.getMessage()
            );
        }
    }

    public String getKillerActiveParticleName(Player killer) {
        if (!pluginPresent || killer == null) {
            return null;
        }

        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            return api != null ? api.getActiveParticleName(killer) : null;
        } catch (Throwable error) {
            logger.warning(
                    "[Hooks] ParticlePlus getActiveParticleName failed: "
                            + error.getMessage()
            );
            return null;
        }
    }

    public ParticleMix getKillerActiveMix(Player killer) {
        if (!pluginPresent || killer == null) {
            return null;
        }

        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            return api != null ? api.getActiveParticleMix(killer) : null;
        } catch (Throwable error) {
            logger.warning(
                    "[Hooks] ParticlePlus getActiveParticleMix failed: "
                            + error.getMessage()
            );
            return null;
        }
    }
}