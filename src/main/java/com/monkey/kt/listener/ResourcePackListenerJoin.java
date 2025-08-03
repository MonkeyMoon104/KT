package com.monkey.kt.listener;

import com.monkey.kt.KT;
import com.monkey.kt.utils.resourcepack.ResourcePack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;
import java.util.UUID;

public class ResourcePackListenerJoin implements Listener {

    private final KT plugin;

    public ResourcePackListenerJoin(KT plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getResourcePack().sendPackToPlayer(event.getPlayer());
    }
}
