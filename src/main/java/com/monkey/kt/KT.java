package com.monkey.kt;

import com.monkey.kt.commands.kt.KTCommand;
import com.monkey.kt.commands.kt.tab.KillEffectTabCompleter;
import com.monkey.kt.effects.KillEffectFactory;
import com.monkey.kt.storage.DatabaseManager;
import com.monkey.kt.gui.GUIManager;
import com.monkey.kt.listener.InventoryClickListener;
import com.monkey.kt.listener.KillEffectListener;
import com.monkey.kt.utils.WorldGuardUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class KT extends JavaPlugin {

    private DatabaseManager databaseManager;
    private GUIManager guiManager;
    private KillEffectFactory factory;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.loadDatabase();

        WorldGuardUtils.setup();

        guiManager = new GUIManager(this, databaseManager);
        factory = new KillEffectFactory(this);

        getCommand("killeffect").setExecutor(new KTCommand(this, guiManager));
        getCommand("killeffect").setTabCompleter(new KillEffectTabCompleter(this));

        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new KillEffectListener(), this);
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnection();
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
