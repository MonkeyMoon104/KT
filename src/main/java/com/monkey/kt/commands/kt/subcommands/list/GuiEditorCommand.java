package com.monkey.kt.commands.kt.subcommands.list;

import com.monkey.kt.KT;
import com.monkey.kt.commands.kt.subcommands.inter.SubCommand;
import com.monkey.kt.utils.text.TextUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public class GuiEditorCommand implements SubCommand {

    private final KT plugin;

    public GuiEditorCommand(KT plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "guieditor";
    }

    @Override
    public String getPermission() {
        return "kt.guieditor";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "show":
                showLayout(sender);
                return;
            case "rows":
                setRows(sender, args);
                return;
            case "effectslots":
                setEffectSlots(sender, args);
                return;
            case "button":
                setButton(sender, args);
                return;
            case "reload":
                saveAndReloadGui(sender, "&aGUI layout reloaded from config.");
                return;
            default:
                sendHelp(sender);
        }
    }

    private void setRows(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(color("&cUsage: /kt guieditor rows <1-6>"));
            return;
        }

        Integer rows = parseInt(args[2]);
        if (rows == null || rows < 1 || rows > 6) {
            sender.sendMessage(color("&cRows must be between 1 and 6."));
            return;
        }

        plugin.getConfig().set("gui.layout.rows", rows);
        saveAndReloadGui(sender, "&aGUI rows updated to &e" + rows + "&a.");
    }

    private void setEffectSlots(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(color("&cUsage: /kt guieditor effectslots <start> <end>"));
            return;
        }

        Integer start = parseInt(args[2]);
        Integer end = parseInt(args[3]);
        if (start == null || end == null) {
            sender.sendMessage(color("&cStart and end must be valid integers."));
            return;
        }

        if (start < 0 || end < 0 || start > 53 || end > 53) {
            sender.sendMessage(color("&cSlots must be between 0 and 53."));
            return;
        }

        plugin.getConfig().set("gui.layout.effect_slots.start", start);
        plugin.getConfig().set("gui.layout.effect_slots.end", end);
        plugin.getConfig().set("gui.layout.effect_slots.list", java.util.Collections.emptyList());
        saveAndReloadGui(sender, "&aEffect slots range updated to &e" + start + " - " + end + "&a.");
    }

    private void setButton(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(color("&cUsage: /kt guieditor button <close|current_effect|disable|currency> <slot> [material]"));
            return;
        }

        String button = normalizeButton(args[2]);
        if (button == null) {
            sender.sendMessage(color("&cInvalid button. Use: close, current_effect, disable, currency"));
            return;
        }

        Integer slot = parseInt(args[3]);
        if (slot == null || slot < 0 || slot > 53) {
            sender.sendMessage(color("&cButton slot must be between 0 and 53."));
            return;
        }

        Material material = null;
        if (args.length >= 5) {
            material = Material.matchMaterial(args[4].toUpperCase(Locale.ROOT));
            if (material == null) {
                sender.sendMessage(color("&cInvalid material: &e" + args[4]));
                return;
            }
        }

        plugin.getConfig().set("gui.layout.buttons." + button + ".slot", slot);
        if (material != null) {
            plugin.getConfig().set("gui.layout.buttons." + button + ".material", material.name());
        }

        saveAndReloadGui(sender, "&aUpdated button &e" + button + "&a at slot &e" + slot + "&a.");
    }

    private void showLayout(CommandSender sender) {
        int rows = plugin.getConfig().getInt("gui.layout.rows", 6);
        int start = plugin.getConfig().getInt("gui.layout.effect_slots.start", 0);
        int end = plugin.getConfig().getInt("gui.layout.effect_slots.end", 44);

        sender.sendMessage(color("&6--- KT GUI Layout ---"));
        sender.sendMessage(color("&eRows: &f" + rows));
        sender.sendMessage(color("&eEffect slots range: &f" + start + " - " + end));
        sender.sendMessage(color("&eClose slot: &f" + plugin.getConfig().getInt("gui.layout.buttons.close.slot", 48)));
        sender.sendMessage(color("&eCurrent slot: &f" + plugin.getConfig().getInt("gui.layout.buttons.current_effect.slot", 49)));
        sender.sendMessage(color("&eDisable slot: &f" + plugin.getConfig().getInt("gui.layout.buttons.disable.slot", 50)));
        sender.sendMessage(color("&eCurrency slot: &f" + plugin.getConfig().getInt("gui.layout.buttons.currency.slot", 51)));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&6GUI Editor commands:"));
        sender.sendMessage(color("&e/kt guieditor show"));
        sender.sendMessage(color("&e/kt guieditor rows <1-6>"));
        sender.sendMessage(color("&e/kt guieditor effectslots <start> <end>"));
        sender.sendMessage(color("&e/kt guieditor button <close|current_effect|disable|currency> <slot> [material]"));
        sender.sendMessage(color("&e/kt guieditor reload"));
    }

    private void saveAndReloadGui(CommandSender sender, String message) {
        plugin.saveConfig();
        plugin.reloadConfig();
        if (plugin.getEffectRegistry() != null) {
            plugin.getEffectRegistry().loadEffects(true);
        }
        sender.sendMessage(color(message));
    }

    private String normalizeButton(String input) {
        String value = input.toLowerCase(Locale.ROOT);
        if (value.equals("current")) {
            return "current_effect";
        }
        if (value.equals("current_effect")
                || value.equals("close")
                || value.equals("disable")
                || value.equals("currency")) {
            return value;
        }
        return null;
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String color(String message) {
        return TextUtils.legacySection(message);
    }
}
