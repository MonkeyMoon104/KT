package com.monkey.kt.commands.kt.manager;

import com.monkey.kt.commands.kt.subcommands.list.ReloadCommand;
import com.monkey.kt.commands.kt.subcommands.list.SetCommand;
import com.monkey.kt.commands.kt.subcommands.list.TestCommand;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.KT;
import com.monkey.kt.gui.GUIManager;

import java.util.HashMap;
import java.util.Map;

public class KTCManager {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public KTCManager(KT plugin, GUIManager guiManager) {
        registerSubCommand(new ReloadCommand(plugin, guiManager));
        registerSubCommand(new SetCommand(plugin));
        registerSubCommand(new TestCommand(plugin));
    }

    private void registerSubCommand(SubCommand command) {
        subCommands.put(command.getName().toLowerCase(), command);
    }

    public SubCommand getSubCommand(String name) {
        return subCommands.get(name.toLowerCase());
    }
}
