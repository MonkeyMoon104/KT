package com.monkey.kt.listener;

import com.monkey.kt.KT;
import com.monkey.kt.economy.KillCoinsEco;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KillRewardListener implements Listener {

    private final KT plugin;
    private final KillCoinsEco eco;

    public KillRewardListener(KT plugin, KillCoinsEco eco) {
        this.plugin = plugin;
        this.eco = eco;
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        if (!eco.isEnabled()) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        double reward = eco.killReward();
        if (reward <= 0) return;

        eco.add(killer, reward);

        String message = plugin.getConfig().getString("messages.kill_reward", "&a+%amount% %currency% &7(Kill reward)");
        message = message.replace("%amount%", String.valueOf((int) reward))
                .replace("%currency%", eco.currencySymbol());

        killer.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
