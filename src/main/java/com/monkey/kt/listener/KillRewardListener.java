package com.monkey.kt.listener;

import com.monkey.kt.KT;
import com.monkey.kt.economy.EconomyManager;
import com.monkey.kt.economy.KillCoinsEco;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KillRewardListener implements Listener {

    private final KT plugin;
    private EconomyManager eco;

    public KillRewardListener(KT plugin, EconomyManager eco) {
        this.plugin = plugin;
        this.eco = eco;
    }

    public void updateEconomyManager(EconomyManager economyManager) {
        this.eco = economyManager;
        plugin.getLogger().info("KillRewardListener updated with new EconomyManager: " +
                (economyManager.isUsingInternal() ? "Internal" : "External"));
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        if (eco.isEnabled()) {
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                if (plugin.getConfig().getBoolean("economy.reward.enabled", true)) {
                    double reward = eco.killReward(event.getEntity());

                    if (reward > 0) {
                        eco.add(killer, reward);

                        String message = plugin.getConfig().getString("messages.kill_reward", "&a+%amount% %currency% &7(Kill reward)");
                        message = message.replace("%amount%", String.valueOf((int) reward))
                                .replace("%currency%", eco.currencySymbol());

                        killer.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    }
                }
            }
        }
    }
}
