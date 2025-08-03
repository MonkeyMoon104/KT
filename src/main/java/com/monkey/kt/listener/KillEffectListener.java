package com.monkey.kt.listener;

import com.monkey.kt.KT;
import com.monkey.kt.cooldown.CooldownManager;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.storage.EffectStorage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillEffectListener implements Listener {

    private final KT plugin;
    private final CooldownManager cooldownManager;

    public KillEffectListener(KT plugin) {
        this.plugin = plugin;
        this.cooldownManager = new CooldownManager();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || !killer.isOnline()) return;
        boolean bypass = killer.hasPermission("kt.admin.bypass");

        boolean isMobKill = !(victim instanceof Player);
        String sectionKey = isMobKill ? "mobs" : "players";

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("management-structure." + sectionKey);
        if (section == null) return;

        boolean isEnabled = section.getBoolean("enabled");
        if (!isEnabled && !bypass) {
            return;
        }

        ConfigurationSection cooldownSection = section.getConfigurationSection("cooldown");
        if (!bypass && cooldownSection != null && cooldownSection.getBoolean("enabled")) {
            int delay = cooldownSection.getInt("delay", 5);
            if (cooldownManager.isOnCooldown(killer, isMobKill, delay)) {
                String msg = plugin.getConfig().getString("messages.cooldown_active", "&cWait %delay% seconds before using your KillEffect again!");
                msg = msg.replace("%delay%", String.valueOf(delay));
                killer.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return;
            }
            cooldownManager.setCooldown(killer, isMobKill);
        }

        String effectName = EffectStorage.getEffect(killer);
        if (effectName == null) return;

        if (effectName.equalsIgnoreCase("sniper") && !victim.hasMetadata("kt_last_hit_arrow")) return;

        KillEffect effect = KillEffectFactory.getEffect(effectName);
        if (effect == null) return;

        effect.play(killer, victim.getLocation());
    }

    @EventHandler
    public void onPigDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Pig && entity.hasMetadata("kt_pigstep")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL &&
                event.getEntity().hasMetadata("no_fall_damage")) {

            event.setCancelled(true);
            event.getEntity().removeMetadata("no_fall_damage", plugin);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        int boost = plugin.getAuraBoostManager().getDamageAmplifier(damager);
        event.setDamage(event.getDamage() * boost);
    }

}
