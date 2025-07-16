package com.monkey.kt.listener;

import com.monkey.kt.KT;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ResourcePackListenerJoin implements Listener {

    private final KT plugin;

    public ResourcePackListenerJoin(KT plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String url = plugin.getConfig().getString("resource_pack.url");
        String sha = plugin.getConfig().getString("resource_pack.sha1");

        if (url == null || sha == null) return;

        byte[] shaBytes;
        try {
            shaBytes = hexToBytes(sha);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("SHA1 malformato!");
            return;
        }

        event.getPlayer().setResourcePack(url, shaBytes);
    }

    private byte[] hexToBytes(String hex) {
        if (hex.length() != 40) throw new IllegalArgumentException("SHA1 length must be 40");

        byte[] bytes = new byte[20];
        for (int i = 0; i < 20; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
