package com.monkey.kt.effects.custom;

import com.monkey.kt.KT;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.custom.executors.*;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CustomKillEffect implements KillEffect {

    private final KT plugin;
    private final CustomEffectConfig config;

    private final SoundExecutor soundExecutor;
    private final ParticleExecutor particleExecutor;
    private final PatternExecutor patternExecutor;
    private final DamageExecutor damageExecutor;
    private final PotionExecutor potionExecutor;
    private final ProjectileExecutor projectileExecutor;
    private final BlockExecutor blockExecutor;
    private final EntityExecutor entityExecutor;
    private final SequenceExecutor sequenceExecutor;

    public CustomKillEffect(KT plugin, CustomEffectConfig config) {
        this.plugin = plugin;
        this.config = config;

        this.soundExecutor = new SoundExecutor(plugin);
        this.particleExecutor = new ParticleExecutor(plugin);
        this.patternExecutor = new PatternExecutor(plugin);
        this.damageExecutor = new DamageExecutor(plugin);
        this.potionExecutor = new PotionExecutor(plugin);
        this.projectileExecutor = new ProjectileExecutor(plugin);
        this.blockExecutor = new BlockExecutor(plugin);
        this.entityExecutor = new EntityExecutor(plugin);
        this.sequenceExecutor = new SequenceExecutor(plugin, this);
    }

    @Override
    public void play(Player killer, Location loc) {
        LivingEntity victim = null;
        if (loc.getWorld() != null) {
            for (LivingEntity entity : loc.getWorld().getLivingEntities()) {
                if (entity.getLocation().distance(loc) < 2 && entity != killer) {
                    victim = entity;
                    break;
                }
            }
        }

        sendMessages(killer, victim);

        if (config.isSequenceEnabled()) {
            sequenceExecutor.execute(config.getSequenceSteps(), killer, loc);
        } else {
            executeAllFeatures(killer, loc);
        }
    }

    private void executeAllFeatures(Player killer, Location loc) {
        if (config.isSoundsEnabled()) {
            soundExecutor.execute(config.getSounds(), loc);
        }

        if (config.isParticlesEnabled()) {
            particleExecutor.execute(config.getParticles(), loc);
        }

        if (config.isPatternsEnabled()) {
            patternExecutor.execute(config.getPatterns(), loc);
        }

        if (config.isDamageEnabled()) {
            int delay = config.getDamageDelay();
            if (delay > 0) {
                SchedulerWrapper.runTaskLater(plugin, () -> {
                    damageExecutor.execute(killer, loc, config.getDamageValue(), config.getDamageRadius());
                }, delay);
            } else {
                damageExecutor.execute(killer, loc, config.getDamageValue(), config.getDamageRadius());
            }
        }

        if (config.isPotionsEnabled()) {
            potionExecutor.execute(killer, loc, config);
        }

        if (config.isProjectilesEnabled()) {
            projectileExecutor.execute(config.getProjectiles(), killer, loc);
        }

        if (config.isBlocksEnabled()) {
            blockExecutor.execute(config.getBlockPlacements(), loc,
                    config.isBlocksTemporary(), config.getBlocksRestoreDelay());
        }

        if (config.isEntitiesEnabled()) {
            entityExecutor.execute(config.getEntities(), loc);
        }
    }

    private void sendMessages(Player killer, LivingEntity victim) {
        String killerMsg = config.getKillerMessage();
        if (killerMsg != null && !killerMsg.isEmpty()) {
            killerMsg = killerMsg.replace("%effect%", config.getName());
            killer.sendMessage(ChatColor.translateAlternateColorCodes('&', killerMsg));
        }

        if (config.isActionBarEnabled()) {
            String actionBar = config.getActionBarKiller();
            if (actionBar != null && !actionBar.isEmpty()) {
                actionBar = actionBar.replace("%effect%", config.getName());
                killer.sendActionBar(ChatColor.translateAlternateColorCodes('&', actionBar));
            }
        }

        if (victim instanceof Player) {
            Player playerVictim = (Player) victim;

            String victimMsg = config.getVictimMessage();
            if (victimMsg != null && !victimMsg.isEmpty()) {
                victimMsg = victimMsg.replace("%effect%", config.getName());
                playerVictim.sendMessage(ChatColor.translateAlternateColorCodes('&', victimMsg));
            }

            if (config.isActionBarEnabled()) {
                String actionBar = config.getActionBarVictim();
                if (actionBar != null && !actionBar.isEmpty()) {
                    actionBar = actionBar.replace("%effect%", config.getName());
                    playerVictim.sendActionBar(ChatColor.translateAlternateColorCodes('&', actionBar));
                }
            }
        }

        String broadcast = config.getBroadcastMessage();
        if (broadcast != null && !broadcast.isEmpty()) {
            broadcast = broadcast.replace("%killer%", killer.getName())
                    .replace("%effect%", config.getName());
            if (victim instanceof Player) {
                broadcast = broadcast.replace("%victim%", victim.getName());
            }
            plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));
        }
    }

    public CustomEffectConfig getConfig() {
        return config;
    }

    public void executeSound(CustomEffectConfig.SoundData sound, Location loc) {
        soundExecutor.executeSound(sound, loc);
    }

    public void executeParticle(CustomEffectConfig.ParticleData particle, Location loc) {
        particleExecutor.executeParticle(particle, loc);
    }

    public void executeDamage(Player killer, Location loc, double damage, double radius) {
        damageExecutor.execute(killer, loc, damage, radius);
    }

    public void executePattern(CustomEffectConfig.PatternData pattern, Location loc) {
        patternExecutor.executePattern(pattern, loc);
    }
}