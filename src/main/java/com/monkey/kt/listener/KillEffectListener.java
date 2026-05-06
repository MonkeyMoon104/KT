package com.monkey.kt.listener;

import com.monkey.kt.KT;
import com.monkey.kt.cooldown.CooldownManager;
import com.monkey.kt.effects.KillEffect;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.effects.list.skeleton.SkeletonEffect;
import com.monkey.kt.effects.permission.EffectPermissionResolver;
import com.monkey.kt.storage.EffectStorage;
import com.monkey.kt.utils.entity.EntityDataUtils;
import com.monkey.kt.utils.text.TextUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
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

        boolean effectsDisabledInWorld = plugin.getConfig().getStringList("disabled-worlds").stream()
                .anyMatch(worldName -> worldName.equalsIgnoreCase(victim.getWorld().getName()));
        if (!bypass && effectsDisabledInWorld) {
            return;
        }

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
                killer.sendMessage(TextUtils.legacySection(msg));
                return;
            }
            cooldownManager.setCooldown(killer, isMobKill);
        }

        String effectName = EffectStorage.getEffect(killer);
        if (effectName == null) return;

        if (!killer.isOp()) {
            boolean hasPermission = EffectPermissionResolver.hasPermission(killer, plugin, effectName);
            boolean explicitPermissionRule = EffectPermissionResolver.hasExplicitPermissionRule(plugin, effectName);
            boolean ecoEnabled = plugin.getEconomyManager().isEnabled();
            boolean hasBought = plugin.getEconomyManager().hasBoughtEffect(killer, effectName);

            if (explicitPermissionRule && !hasPermission) {
                return;
            }

            if (!hasPermission && (!ecoEnabled || !hasBought)) {
                return;
            }
        }

        if (plugin.getCustomEffectLoader() != null) {
            com.monkey.kt.effects.custom.CustomEffectConfig customConfig =
                    plugin.getCustomEffectLoader().getEffectConfig(effectName);

            if (customConfig != null) {
                if (customConfig.isWeaponRequired()) {
                    Material weapon = killer.getInventory().getItemInMainHand().getType();
                    if (!customConfig.getRequiredWeapons().contains(weapon)) {
                        return;
                    }
                }

                String requiredKillType = customConfig.getRequiredKillType();
                if (requiredKillType != null && !requiredKillType.equalsIgnoreCase("ANY")) {
                    boolean isPlayer = victim instanceof Player;
                    if (requiredKillType.equalsIgnoreCase("PLAYER") && !isPlayer) return;
                    if (requiredKillType.equalsIgnoreCase("MOB") && isPlayer) return;
                }
            }
        }

        if (effectName.equalsIgnoreCase("sniper") && !EntityDataUtils.hasBoolean(victim, plugin, "kt_last_hit_arrow")) return;
        if (effectName.equalsIgnoreCase("mace") && !EntityDataUtils.hasBoolean(victim, plugin, "kt_last_hit_mace")) return;

        KillEffect effect = KillEffectFactory.getEffect(effectName);
        if (effect == null) return;

         if (effect instanceof SkeletonEffect) {
            ((SkeletonEffect) effect).play((Player) victim, victim.getLocation());
        } else {
            effect.play(killer, victim.getLocation());
        }

        plugin.getEventManager().triggerRandomEvent(killer, victim);
    }

    @EventHandler
    public void onPigDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Pig && EntityDataUtils.hasBoolean(entity, plugin, "kt_pigstep")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL &&
                EntityDataUtils.hasBoolean(event.getEntity(), plugin, "no_fall_damage")) {

            event.setCancelled(true);
            EntityDataUtils.remove(event.getEntity(), plugin, "no_fall_damage");
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
