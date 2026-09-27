package com.monkey.ktplus.hook.particleplus;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;
import me.dominikhun250.dev.particleplus.api.ParticleMix;
import me.dominikhun250.dev.particleplus.api.ParticlePlusAPI;
import me.dominikhun250.dev.particleplus.api.ParticlePlusProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
            logger.info("[Hooks] ParticlePlus hooked");
        }
        return new ParticlePlusHook(present, logger);
    }

    public boolean available() {
        return pluginPresent;
    }

    public Suspension suspendNearby(
            Location location,
            double radius,
            boolean players,
            boolean mobs,
            boolean blocks,
            boolean items) {
        if (!pluginPresent || location == null || location.getWorld() == null) {
            return Suspension.EMPTY;
        }
        if (!players && !mobs && !blocks && !items) {
            return Suspension.EMPTY;
        }

        Set<UUID> suspendedPlayerParticles = new HashSet<>();
        Set<UUID> suspendedMobParticles = new HashSet<>();
        Set<UUID> suspendedBlockParticles = new HashSet<>();
        Set<UUID> suspendedItemParticles = new HashSet<>();

        double radiusSquared = Math.max(0.0D, radius) * Math.max(0.0D, radius);

        for (Player nearby : location.getWorld().getPlayers()) {
            if (nearby.getLocation().distanceSquared(location) > radiusSquared) {
                continue;
            }
            if (players && suspendPlayerParticle(nearby)) {
                suspendedPlayerParticles.add(nearby.getUniqueId());
            }
            if (mobs && suspendMobParticles(nearby)) {
                suspendedMobParticles.add(nearby.getUniqueId());
            }
            if (blocks && suspendBlockParticles(nearby)) {
                suspendedBlockParticles.add(nearby.getUniqueId());
            }
            if (items && suspendItemParticles(nearby)) {
                suspendedItemParticles.add(nearby.getUniqueId());
            }
        }

        return new Suspension(
                suspendedPlayerParticles, suspendedMobParticles, suspendedBlockParticles, suspendedItemParticles);
    }

    public void resume(Suspension suspension) {
        if (!pluginPresent || suspension == null || suspension.isEmpty()) {
            return;
        }
        resumeCategory(suspension.playerParticlePlayers, this::resumePlayerParticle);
        resumeCategory(suspension.mobParticlePlayers, this::resumeMobParticles);
        resumeCategory(suspension.blockParticlePlayers, this::resumeBlockParticles);
        resumeCategory(suspension.itemParticlePlayers, this::resumeItemParticles);
    }

    private void resumeCategory(Set<UUID> uuids, Consumer<Player> resumeFn) {
        for (UUID uuid : uuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                resumeFn.accept(player);
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
            logger.warning("[Hooks] ParticlePlus suspendPlayerParticle failed: " + error.getMessage());
        }
        return false;
    }

    private void resumePlayerParticle(Player player) {
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            if (api != null) {
                api.resumePlayerParticle(player);
            }
        } catch (Throwable error) {
            logger.warning("[Hooks] ParticlePlus resumePlayerParticle failed: " + error.getMessage());
        }
    }

    private boolean suspendMobParticles(Player player) {
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            if (api != null) {
                api.suspendMobParticles(player);
                return true;
            }
        } catch (Throwable error) {
            logger.warning("[Hooks] ParticlePlus suspendMobParticles failed: " + error.getMessage());
        }
        return false;
    }

    private void resumeMobParticles(Player player) {
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            if (api != null) {
                api.resumeMobParticles(player);
            }
        } catch (Throwable error) {
            logger.warning("[Hooks] ParticlePlus resumeMobParticles failed: " + error.getMessage());
        }
    }

    private boolean suspendBlockParticles(Player player) {
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            if (api != null) {
                api.suspendBlockParticles(player);
                return true;
            }
        } catch (Throwable error) {
            logger.warning("[Hooks] ParticlePlus suspendBlockParticles failed: " + error.getMessage());
        }
        return false;
    }

    private void resumeBlockParticles(Player player) {
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            if (api != null) {
                api.resumeBlockParticles(player);
            }
        } catch (Throwable error) {
            logger.warning("[Hooks] ParticlePlus resumeBlockParticles failed: " + error.getMessage());
        }
    }

    private boolean suspendItemParticles(Player player) {
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            if (api != null) {
                api.suspendItemParticles(player);
                return true;
            }
        } catch (Throwable error) {
            logger.warning("[Hooks] ParticlePlus suspendItemParticles failed: " + error.getMessage());
        }
        return false;
    }

    private void resumeItemParticles(Player player) {
        try {
            ParticlePlusAPI api = ParticlePlusProvider.getAPI();
            if (api != null) {
                api.resumeItemParticles(player);
            }
        } catch (Throwable error) {
            logger.warning("[Hooks] ParticlePlus resumeItemParticles failed: " + error.getMessage());
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

    public static final class Suspension {
        static final Suspension EMPTY = new Suspension(
                Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet());

        private final Set<UUID> playerParticlePlayers;
        private final Set<UUID> mobParticlePlayers;
        private final Set<UUID> blockParticlePlayers;
        private final Set<UUID> itemParticlePlayers;

        private Suspension(
                Set<UUID> playerParticlePlayers,
                Set<UUID> mobParticlePlayers,
                Set<UUID> blockParticlePlayers,
                Set<UUID> itemParticlePlayers) {
            this.playerParticlePlayers = Collections.unmodifiableSet(playerParticlePlayers);
            this.mobParticlePlayers = Collections.unmodifiableSet(mobParticlePlayers);
            this.blockParticlePlayers = Collections.unmodifiableSet(blockParticlePlayers);
            this.itemParticlePlayers = Collections.unmodifiableSet(itemParticlePlayers);
        }

        public boolean isEmpty() {
            return playerParticlePlayers.isEmpty()
                    && mobParticlePlayers.isEmpty()
                    && blockParticlePlayers.isEmpty()
                    && itemParticlePlayers.isEmpty();
        }
    }
}