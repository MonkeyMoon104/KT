package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.effects.permission.EffectPermissionResolver;
import com.monkey.kt.storage.EffectStorage;
import com.monkey.kt.utils.text.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SetCommand implements SubCommand {

    private final KT plugin;

    public SetCommand(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getPermission() {
        return "kt.set";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.set_usage")));
            return;
        }

        if (args.length >= 3) {
            handleTargetedSet(sender, args);
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.set_usage")));
            return;
        }

        String effect = EffectPermissionResolver.normalize(args[1]);
        if (isClearEffect(effect)) {
            EffectStorage.removeEffect(player);
            sender.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.effect_removed")));
            return;
        }

        if (!plugin.getGuiManager().getEffects().containsKey(effect)) {
            sender.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.effect_not_found")));
            return;
        }

        boolean hasPermission = EffectPermissionResolver.hasPermission(player, plugin, effect);
        boolean explicitPermissionRule = EffectPermissionResolver.hasExplicitPermissionRule(plugin, effect);
        boolean hasBought = plugin.getEconomyManager().hasBoughtEffect(player, effect);
        boolean ecoEnabled = plugin.getEconomyManager().isEnabled();

        if (!player.isOp() && explicitPermissionRule && !hasPermission) {
            player.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.no_permissions")));
            return;
        }

        if (!player.isOp() && !hasPermission && (!ecoEnabled || !hasBought)) {
            player.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.no_permissions")));
            return;
        }

        String current = EffectStorage.getEffect(player);
        if (effect.equalsIgnoreCase(current)) {
            player.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.effect_already_set")));
            return;
        }

        EffectStorage.setEffect(player, effect);
        sender.sendMessage(TextUtils.legacySection(
                        plugin.getConfig().getString("messages.effect_set"))
                .replace("%effect%", effect));
    }

    private void handleTargetedSet(CommandSender sender, String[] args) {
        OfflinePlayer target = findTarget(args[1]);
        if (target == null) {
            sender.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.player_not_found")));
            return;
        }

        String effect = EffectPermissionResolver.normalize(args[2]);
        if (isClearEffect(effect)) {
            EffectStorage.removeEffect(target.getUniqueId());
            sender.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.effect_removed_other"))
                    .replace("%player%", displayName(target, args[1])));

            notifyTarget(target, sender, plugin.getConfig().getString("messages.effect_removed_by_admin"), null);
            return;
        }

        if (!plugin.getGuiManager().getEffects().containsKey(effect)) {
            sender.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.effect_not_found")));
            return;
        }

        EffectStorage.setEffect(target.getUniqueId(), effect);
        sender.sendMessage(TextUtils.legacySection(plugin.getConfig().getString("messages.effect_set_other"))
                .replace("%player%", displayName(target, args[1]))
                .replace("%effect%", effect));

        notifyTarget(target, sender, plugin.getConfig().getString("messages.effect_set_by_admin"), effect);
    }

    private boolean isClearEffect(String effect) {
        return effect.equals("none") || effect.equals("clear");
    }

    private OfflinePlayer findTarget(String input) {
        Player onlineExact = Bukkit.getPlayerExact(input);
        if (onlineExact != null) {
            return onlineExact;
        }

        Player onlineLoose = Bukkit.getPlayer(input);
        if (onlineLoose != null) {
            return onlineLoose;
        }

        OfflinePlayer cachedOffline = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> player.getName() != null && player.getName().equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);

        if (cachedOffline != null && (cachedOffline.hasPlayedBefore() || cachedOffline.isOnline())) {
            return cachedOffline;
        }

        return null;
    }

    private String displayName(OfflinePlayer target, String fallback) {
        return target.getName() != null ? target.getName() : fallback;
    }

    private void notifyTarget(OfflinePlayer target, CommandSender sender, String message, String effect) {
        if (!(target instanceof Player onlineTarget)) {
            return;
        }

        if (sender instanceof Player senderPlayer && onlineTarget.getUniqueId().equals(senderPlayer.getUniqueId())) {
            return;
        }

        if (message == null || message.isBlank()) {
            return;
        }

        String resolved = message.replace("%sender%", sender.getName());
        if (effect != null) {
            resolved = resolved.replace("%effect%", effect);
        }
        onlineTarget.sendMessage(TextUtils.legacySection(resolved));
    }
}
