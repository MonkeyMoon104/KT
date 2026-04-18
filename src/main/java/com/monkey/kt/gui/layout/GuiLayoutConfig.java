package com.monkey.kt.gui.layout;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GuiLayoutConfig {

    private static final int MIN_ROWS = 1;
    private static final int MAX_ROWS = 6;

    private final int rows;
    private final int size;
    private final List<Integer> effectSlots;

    private final ButtonLayout closeButton;
    private final ButtonLayout currentEffectButton;
    private final ButtonLayout disableButton;
    private final ButtonLayout currencyButton;

    private GuiLayoutConfig(
            int rows,
            List<Integer> effectSlots,
            ButtonLayout closeButton,
            ButtonLayout currentEffectButton,
            ButtonLayout disableButton,
            ButtonLayout currencyButton
    ) {
        this.rows = rows;
        this.size = rows * 9;
        this.effectSlots = effectSlots;
        this.closeButton = closeButton;
        this.currentEffectButton = currentEffectButton;
        this.disableButton = disableButton;
        this.currencyButton = currencyButton;
    }

    public static GuiLayoutConfig fromConfig(FileConfiguration config) {
        int rows = clamp(config.getInt("gui.layout.rows", 6), MIN_ROWS, MAX_ROWS);
        int size = rows * 9;

        int closeDefault = clamp(size - 6, 0, size - 1);
        int currentDefault = clamp(size - 5, 0, size - 1);
        int disableDefault = clamp(size - 4, 0, size - 1);
        int currencyDefault = clamp(size - 3, 0, size - 1);

        ButtonLayout close = loadButton(config, size, "close", closeDefault, Material.GRAY_WOOL);
        ButtonLayout current = loadButton(config, size, "current_effect", currentDefault, Material.OAK_SIGN);
        ButtonLayout disable = loadButton(config, size, "disable", disableDefault, Material.RED_WOOL);
        ButtonLayout currency = loadButton(config, size, "currency", currencyDefault, Material.EMERALD);

        int start = clamp(config.getInt("gui.layout.effect_slots.start", 0), 0, size - 1);
        int end = clamp(config.getInt("gui.layout.effect_slots.end", Math.min(size - 1, 44)), 0, size - 1);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        Set<Integer> slots = new LinkedHashSet<>();
        for (int i = start; i <= end; i++) {
            slots.add(i);
        }

        List<Integer> extraSlots = config.getIntegerList("gui.layout.effect_slots.list");
        for (Integer slot : extraSlots) {
            if (slot != null && slot >= 0 && slot < size) {
                slots.add(slot);
            }
        }

        slots.remove(close.getSlot());
        slots.remove(current.getSlot());
        slots.remove(disable.getSlot());
        slots.remove(currency.getSlot());

        return new GuiLayoutConfig(
                rows,
                new ArrayList<>(slots),
                close,
                current,
                disable,
                currency
        );
    }

    private static ButtonLayout loadButton(
            FileConfiguration config,
            int size,
            String id,
            int defaultSlot,
            Material defaultMaterial
    ) {
        String basePath = "gui.layout.buttons." + id;
        int slot = clamp(config.getInt(basePath + ".slot", defaultSlot), 0, size - 1);
        String materialName = config.getString(basePath + ".material", defaultMaterial.name());
        Material material = parseMaterial(materialName, defaultMaterial);
        return new ButtonLayout(id, slot, material);
    }

    private static Material parseMaterial(String materialName, Material fallback) {
        if (materialName == null || materialName.trim().isEmpty()) {
            return fallback;
        }
        Material parsed = Material.matchMaterial(materialName.toUpperCase(Locale.ROOT));
        return parsed != null ? parsed : fallback;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public int getRows() {
        return rows;
    }

    public int getSize() {
        return size;
    }

    public List<Integer> getEffectSlots() {
        return new ArrayList<>(effectSlots);
    }

    public ButtonLayout getCloseButton() {
        return closeButton;
    }

    public ButtonLayout getCurrentEffectButton() {
        return currentEffectButton;
    }

    public ButtonLayout getDisableButton() {
        return disableButton;
    }

    public ButtonLayout getCurrencyButton() {
        return currencyButton;
    }

    public boolean isButtonSlot(int slot) {
        return closeButton.slot == slot
                || currentEffectButton.slot == slot
                || disableButton.slot == slot
                || currencyButton.slot == slot;
    }

    public static class ButtonLayout {
        private final String id;
        private final int slot;
        private final Material material;

        public ButtonLayout(String id, int slot, Material material) {
            this.id = id;
            this.slot = slot;
            this.material = material;
        }

        public String getId() {
            return id;
        }

        public int getSlot() {
            return slot;
        }

        public Material getMaterial() {
            return material;
        }
    }
}
