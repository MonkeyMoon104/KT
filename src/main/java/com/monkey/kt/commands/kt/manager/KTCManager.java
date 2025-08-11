package com.monkey.kt.commands.kt.manager;

import com.monkey.kt.commands.kt.subcommands.list.*;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.KT;
import com.monkey.kt.economy.KillCoinsEco;
import com.monkey.kt.gui.GUIManager;

import java.util.HashMap;
import java.util.Map;

public class KTCManager {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public KTCManager(KT plugin, GUIManager guiManager, KillCoinsEco killCoinsEco) {
        registerSubCommand(new ReloadCommand(plugin, guiManager));
        registerSubCommand(new SetCommand(plugin));
        registerSubCommand(new TestCommand(plugin));
        registerSubCommand(new KillCoinsCommand(plugin, killCoinsEco));
        registerSubCommand(new SecureSuggestionCommand(plugin));
        registerSubCommand(new SecureReviewCommand(plugin));
    }

    private void registerSubCommand(SubCommand command) {
        subCommands.put(command.getName().toLowerCase(), command);
    }

    public SubCommand getSubCommand(String name) {
        return subCommands.get(name.toLowerCase());
    }
}
