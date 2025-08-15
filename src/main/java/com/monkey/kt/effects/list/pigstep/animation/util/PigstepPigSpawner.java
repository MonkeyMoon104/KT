package com.monkey.kt.effects.list.pigstep.animation.util;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.pigstep.animation.PigstepFollowTask;
import com.monkey.kt.utils.damage.DamageConfig;
import com.monkey.kt.utils.damage.DamageUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.metadata.FixedMetadataValue;

public class PigstepPigSpawner extends BukkitRunnable {
    private final KT plugin;
    private final Player killer;
    private final Location loc;

    public PigstepPigSpawner(KT plugin, Player killer, Location loc) {
        this.plugin = plugin;
        this.killer = killer;
        this.loc = loc;
    }

    @Override
    public void run() {
        if (loc.getWorld() == null) return;

        Pig pig = (Pig) loc.getWorld().spawn(loc, Pig.class, entity -> {
            entity.setMetadata("kt_pigstep", new FixedMetadataValue(plugin, true));
            entity.setMetadata("kt_bypass_spawn", new FixedMetadataValue(plugin, true));
            entity.setAI(true);
            entity.setInvulnerable(true);
            entity.setCollidable(false);
            entity.setSilent(true);
            entity.setFallDistance(0);
            entity.setVelocity(new Vector(0, 1.2, 0));
        });

        killer.stopSound(org.bukkit.Sound.MUSIC_DISC_PIGSTEP, org.bukkit.SoundCategory.RECORDS);
        killer.playSound(
                killer.getLocation(),
                Sound.MUSIC_DISC_PIGSTEP,
                SoundCategory.RECORDS,
                1.2f,
                1.0f
        );

        new PigstepFollowTask(plugin, pig, killer).runTaskTimer(plugin, 10L, 2L);

        DamageConfig damageConfig = DamageUtils.getDamageConfig("pigstep", plugin);

        if (damageConfig.isEnabled()) {
            DamageUtils.applyDamageAround(killer, loc, damageConfig.getRadius(), damageConfig.getValue());
        }
    }
}
