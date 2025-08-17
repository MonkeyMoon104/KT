package com.monkey.kt.events;

import com.monkey.kt.KT;
import com.monkey.kt.events.helper.GameEvent;
import com.monkey.kt.events.meteorshower.MeteorShowerEvent;
import com.monkey.kt.events.meteorshower.MeteorShowerListener;
import com.monkey.kt.events.moneyfountain.MoneyFountainEvent;
import com.monkey.kt.events.moneyfountain.MoneyFountainListener;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;


import java.util.*;

public class EventManager {

    private final KT plugin;
    private final Random random = new Random();
    private GameEvent activeEvent;
    private MoneyFountainEvent activeMoneyFountain = null;
    private MeteorShowerEvent activeMeteor = null;

    public EventManager(KT plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new MoneyFountainListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MeteorShowerListener(plugin), plugin);
    }

    public String chooseRandomEvent() {
        if (!plugin.getConfig().getBoolean("events.enabled", true)) {
            return "none";
        }

        ConfigurationSection eventsSection = plugin.getConfig().getConfigurationSection("events.list");
        if (eventsSection == null) return "none";

        Map<String, Integer> activeEvents = new HashMap<>();
        int totalChance = 0;

        for (String eventName : eventsSection.getKeys(false)) {
            ConfigurationSection sec = eventsSection.getConfigurationSection(eventName);
            if (sec == null) continue;

            if (!sec.getBoolean("enabled", false)) continue;

            int chance = sec.getInt("chance", 0);
            if (chance <= 0) continue;

            activeEvents.put(eventName, chance);
            totalChance += chance;
        }

        if (activeEvents.isEmpty()) {
            return "none";
        }

        int noneChance = Math.max(0, 100 - totalChance);

        int roll = random.nextInt(100) + 1;
        int cumulative = 0;

        for (Map.Entry<String, Integer> entry : activeEvents.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                return entry.getKey();
            }
        }

        return "none";
    }

    public void triggerRandomEvent(Player killer, LivingEntity victim) {
        if (!(victim instanceof Player)) {
            return;
        }

        if (isMoneyFountainActive() || isMeteorActive()) return;

        String eventName = chooseRandomEvent();

        switch (eventName.toLowerCase()) {
            case "money_fountain":
                MoneyFountainEvent moneyFountainEvent = new MoneyFountainEvent(plugin);
                MoneyFountainListener.setCurrentEvent(moneyFountainEvent);
                moneyFountainEvent.start(killer, (Player) victim);
                setActiveMoneyFountain(moneyFountainEvent);
                break;

            case "meteor_shower":
                MeteorShowerEvent meteorEvent = new MeteorShowerEvent(plugin);
                MeteorShowerListener.setCurrentEvent(meteorEvent);
                meteorEvent.start(killer, (Player) victim);
                setActiveMeteor(meteorEvent);
                break;

            case "none":
            default:
                break;
        }
    }

    public boolean isMeteorActive() { return activeMeteor != null; }
    public void setActiveMeteor(MeteorShowerEvent event) {
        this.activeMeteor = event;
        this.activeEvent = event;
    }
    public void clearMeteorEvent() {
        this.activeMeteor = null;
        this.activeEvent = null;
    }

    public boolean isMoneyFountainActive() {
        return activeMoneyFountain != null;
    }

    public void setActiveMoneyFountain(MoneyFountainEvent event) {
        this.activeMoneyFountain = event;
        this.activeEvent = event;
    }

    public void clearFountainEvent() {
        this.activeMoneyFountain = null;
        this.activeEvent = null;
    }

    public GameEvent getActiveEvent() {
        return activeEvent;
    }

    public void stopAllEvents() {
        if (activeEvent != null) {
            activeEvent.endEvent(true);
            activeEvent = null;
        }
    }
}
