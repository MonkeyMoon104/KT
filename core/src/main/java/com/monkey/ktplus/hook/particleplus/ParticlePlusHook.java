package com.monkey.ktplus.hook.particleplus;

import java.util.logging.Logger;
import me.dominikhun250.dev.particleplus.api.ParticleMix;
import me.dominikhun250.dev.particleplus.api.ParticlePlusAPI;
import me.dominikhun250.dev.particleplus.api.ParticlePlusProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ParticlePlusHook {
    private static final String PLUGIN_NAME = "ParticlePlus";

    private final boolean pluginPresent;
    private final Logger logger;

    private ParticlePlusHook(boolean pluginPresent, Logger logger) {
        this.pluginPresent = pluginPresent;
        this.logger = logger;
    }

    public static ParticlePlusHook create(Logger logger) {
        boolean present = Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);
        if (present) {
            logger.info("[Hooks] ParticlePlus-Reloaded hooked");
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
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            if (api != null) {
                api.suspendPlayerParticle(killer);
            }
        } catch (Throwable error) {
            logger.warning("[Hooks] ParticlePlus suspendPlayerParticle failed: " + error.getMessage());
        }
    }

    public void resumeKillerParticle(Player killer) {
        if (!pluginPresent || killer == null) {
            return;
        }
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            if (api != null) {
                api.resumePlayerParticle(killer);
            }
        } catch (Throwable error) {
            logger.warning("[Hooks] ParticlePlus resumePlayerParticle failed: " + error.getMessage());
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
            logger.warning("[Hooks] ParticlePlus getActiveParticleName failed: " + error.getMessage());
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
            logger.warning("[Hooks] ParticlePlus getActiveParticleMix failed: " + error.getMessage());
            return null;
        }
    }
}