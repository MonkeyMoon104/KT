package com.monkey.kt.utils.resourcepack;

import com.monkey.kt.KT;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class ResourcePack {
    private final KT plugin;

    public ResourcePack(KT plugin) {
        this.plugin = plugin;
    }

    public void sendPackToPlayer(Player player) {
        if (!plugin.getConfig().getBoolean("resource_pack.settings.enabled", true)) return;

        String url = plugin.getConfig().getString("resource_pack.settings.url");
        String sha = plugin.getConfig().getString("resource_pack.settings.sha1");

        if (url == null || sha == null) return;

        boolean required = plugin.getConfig().getBoolean("resource_pack.settings.required", false);
        String prompt = plugin.getConfig().getString("resource_pack.settings.prompt");

        byte[] shaBytes;
        try {
            shaBytes = hexToBytes(sha);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("SHA1 in config is malformed. It must be exactly 40 hex characters.");
            return;
        }

        UUID packId = UUID.fromString(Objects.requireNonNull(plugin.getConfig().getString("resource_pack.settings.uuid")));

        player.addResourcePack(
                packId,
                url,
                shaBytes,
                (prompt != null && !prompt.isEmpty()) ? prompt : null,
                required
        );

    }

    public void removePackFromPlayer(Player player) {
        String uuidStr = plugin.getConfig().getString("resource_pack.settings.uuid");

        if (uuidStr == null || uuidStr.isEmpty()) return;

        try {
            UUID uuid = UUID.fromString(uuidStr);

            player.removeResourcePack(uuid);

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid UUID in config.yml: " + uuidStr);
        } catch (NoSuchMethodError e) {
            plugin.getLogger().warning("removeResourcePack is not supported on this server version.");
        }
    }


    private byte[] hexToBytes(String hex) {
        if (hex.length() != 40) throw new IllegalArgumentException("SHA1 must be 40 characters long");

        byte[] bytes = new byte[20];
        for (int i = 0; i < 20; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
