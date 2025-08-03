package com.monkey.kt.placeholder;

import com.monkey.kt.KT;
import com.monkey.kt.boost.AuraBoostManager;
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

        KillCoinsEco eco = plugin.getKillCoinsEco();
        KillCoinsStorage storage = eco.getStorage();

        switch (params.toLowerCase()) {
            case "amplifier":
                int amplifier = auraBoostManager.getDamageAmplifier(player);
                return String.valueOf(amplifier);

            case "current_effect":
                String current = EffectStorage.getEffect(player);
                return current != null ? capitalize(current) : "None";

            case "effect_total":
                return String.valueOf(KillEffectFactory.getRegisteredEffects().size());

            case "effect_owned":
                return String.valueOf(storage.getBoughtEffects(player.getUniqueId()).size());

            case "killcoins_bal":
                return String.valueOf((int) eco.getBalance(player));

            case "killcoins_reward":
                return String.valueOf((int) eco.killReward());

            default:
                return null;
        }
    }
    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

}
