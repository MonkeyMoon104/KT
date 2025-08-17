package com.monkey.kt.placeholder;

import com.monkey.kt.KT;
import com.monkey.kt.boost.AuraBoostManager;
import com.monkey.kt.economy.EconomyManager;
import com.monkey.kt.economy.KillCoinsEco;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.economy.storage.KillCoinsStorage;
import com.monkey.kt.storage.EffectStorage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KTPlaceholder extends PlaceholderExpansion {

    private final AuraBoostManager auraBoostManager;
    private final KT plugin;

    public KTPlaceholder(AuraBoostManager auraBoostManager, KT plugin) {
        this.auraBoostManager = auraBoostManager;
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "kt";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MonkeyMoon104";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";

        EconomyManager eco = plugin.getEconomyManager();
        int playerKill = (int) plugin.getConfig().getDouble("economy.reward.settings.player_kill", 0D);
        int mobKill = (int) plugin.getConfig().getDouble("economy.reward.settings.mob_kill", 0D);
        int effectsize = eco.getInternalEconomy().getStorage().getBoughtEffects(player.getUniqueId()).size();
        int effects = KillEffectFactory.getRegisteredEffects().size();
        String current = EffectStorage.getEffect(player);
        int amplifier = auraBoostManager.getDamageAmplifier(player);
        int bal = (int) eco.getBalance(player);

        switch (params.toLowerCase()) {
            case "amplifier":
                return String.valueOf(amplifier);

            case "current_effect":
                return current != null ? capitalize(current) : "None";

            case "effect_total":
                return String.valueOf(effects);

            case "effect_owned":
                return String.valueOf(effectsize);

            case "bal":
                return String.valueOf(bal);

            case "kill_reward":
                return "player (" + playerKill + ")\nmob (" + mobKill + ")";

            case "p_kill_reward":
                return String.valueOf(playerKill);

            case "m_kill_reward":
                return String.valueOf(mobKill);

            case "currency_symbol":
                return eco.currencySymbol();

            case "economy_type":
                return eco.isUsingInternal() ? "Internal" : "External";

            case "economy_provider":
                if (eco.isUsingInternal()) {
                    return "KillCoins";
                } else if (eco.getVaultEconomy() != null) {
                    return eco.getVaultEconomy().getName();
                } else {
                    return "KillCoins (Fallback)";
                }

            case "economy_enabled":
                return String.valueOf(eco.isEnabled());

            case "event_endtime":
                return plugin.getEventManager().getActiveEvent() != null
                        ? plugin.getEventManager().getActiveEvent().getRemainingTimeFormatted()
                        : "N/A";

            case "event_name":
                return plugin.getEventManager().getActiveEvent() != null
                        ? plugin.getEventManager().getActiveEvent().getEventName()
                        : "N/D";

            default:
                return null;
        }
    }
    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

}
