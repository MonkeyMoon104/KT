package com.monkey.kt.effects.list.cryocore.animation;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.cryocore.animation.util.CryoCoreParticles;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.CryoCoreStructuresManager;
import com.monkey.kt.effects.list.cryocore.animation.util.structures.helper.BlockStateHolder;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CryoCoreLauncher {

    private static final int EXPLOSION_DURATION = 45;
    private static final int BURST_TICK = 46;
    private static final int SPIKES_TICK = 70;
    private static final int FADE_TICK = 180;
    private static final int RESTORE_TICK = 300;

    public static void launch(KT plugin, Location center, Player killer) {
        World world = center.getWorld();
        if (world == null) return;

        CryoCoreStructuresManager cryoManager = new CryoCoreStructuresManager(plugin);
        BlockStateHolder stateHolder = cryoManager.createBlockStateHolder();
        int spikeRadius = 16;

        boolean allowStructure = plugin.getConfig().getBoolean("effects.cryocore.structure", true);

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            int tick = 0;

            @Override
            public void run() {
                if (taskCompleted[0]) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                if (tick <= EXPLOSION_DURATION) {
                    if (tick % 2 == 0) {
                        CryoCoreParticles.spawnIceExplosion(world, center, tick, spikeRadius - 1, plugin, killer);
                    }

                    if (tick % 8 == 0) {
                        cryoManager.randomlyPlaceSnowBlocks(center, 15, stateHolder, tick / 4, allowStructure);
                    }

                } else if (tick == BURST_TICK) {
                    CryoCoreParticles.spawnInclinedBurst(plugin, center, spikeRadius - 1);
                    world.playSound(center, Sound.BLOCK_GLASS_BREAK, 2f, 0.8f);

                } else if (tick == SPIKES_TICK) {
                    cryoManager.spawnLargeIceSpikesBorder(center, spikeRadius, stateHolder, allowStructure);
                    world.playSound(center, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.8f, 1f);

                } else if (tick >= SPIKES_TICK + 20 && tick < FADE_TICK && tick % 30 == 0) {
                    world.playSound(center, Sound.BLOCK_POWDER_SNOW_STEP, 0.3f, 0.7f);

                } else if (tick == FADE_TICK) {
                    CryoCoreParticles.spawnFinalCrystalFade(plugin, world, center, spikeRadius - 1);
                    world.playSound(center, Sound.BLOCK_POWDER_SNOW_BREAK, 1.2f, 0.9f);

                } else if (tick == RESTORE_TICK) {
                    cryoManager.restoreGround(stateHolder);
                    world.playSound(center, Sound.BLOCK_SNOW_BREAK, 1.6f, 1.0f);

                    stateHolder.originalBlocks.clear();
                    stateHolder.iceSpikeBlocks.clear();

                    taskCompleted[0] = true;
                    SchedulerWrapper.safeCancelTask(this);
                }

                tick++;
            }
        }, center, 0L, 4L);
    }

    public static void launchWithCustomDuration(KT plugin, Location center, Player killer, int durationMultiplier) {
        World world = center.getWorld();
        if (world == null) return;

        CryoCoreStructuresManager cryoManager = new CryoCoreStructuresManager(plugin);
        BlockStateHolder stateHolder = cryoManager.createBlockStateHolder();
        int spikeRadius = 16;

        boolean allowStructure = plugin.getConfig().getBoolean("effects.cryocore.structure", true);

        final int explosionDuration = 30 * durationMultiplier;
        final int burstTick = 31 * durationMultiplier;
        final int spikesTick = 45 * durationMultiplier;
        final int fadeTick = 110 * durationMultiplier;
        final int restoreTick = 120 * durationMultiplier;

        final boolean[] taskCompleted = {false};

        SchedulerWrapper.runTaskTimerAtLocation(plugin, new Runnable() {
            int tick = 0;

            @Override
            public void run() {
                if (taskCompleted[0]) {
                    SchedulerWrapper.safeCancelTask(this);
                    return;
                }

                if (tick <= explosionDuration) {
                    if (tick % 3 == 0) {
                        CryoCoreParticles.spawnIceExplosion(world, center, tick, spikeRadius - 1, plugin, killer);
                    }

                    if (tick % (6 * durationMultiplier) == 0) {
                        cryoManager.randomlyPlaceSnowBlocks(center, 15, stateHolder, tick / (3 * durationMultiplier), allowStructure);
                    }

                } else if (tick == burstTick) {
                    CryoCoreParticles.spawnInclinedBurst(plugin, center, spikeRadius - 1);
                    world.playSound(center, Sound.BLOCK_GLASS_BREAK, 2f, 0.8f);

                } else if (tick == spikesTick) {
                    cryoManager.spawnLargeIceSpikesBorder(center, spikeRadius, stateHolder, allowStructure);
                    world.playSound(center, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.8f, 1f);

                } else if (tick == fadeTick) {
                    CryoCoreParticles.spawnFinalCrystalFade(plugin, world, center, spikeRadius - 1);

                } else if (tick == restoreTick) {
                    cryoManager.restoreGround(stateHolder);
                    world.playSound(center, Sound.BLOCK_SNOW_BREAK, 1.6f, 1.0f);

                    stateHolder.originalBlocks.clear();
                    stateHolder.iceSpikeBlocks.clear();

                    taskCompleted[0] = true;
                    SchedulerWrapper.safeCancelTask(this);
                }

                tick++;
            }
        }, center, 0L, 4L);
    }
}