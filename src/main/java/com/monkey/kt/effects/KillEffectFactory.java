package com.monkey.kt.effects;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KillEffectFactory {

    private static final Map<String, KillEffect> effects = new HashMap<>();

    public static void registerEffect(String name, KillEffect effect) {
        effects.put(name.toLowerCase(), effect);
    }

    public static KillEffect getEffect(String name) {
        return effects.get(name.toLowerCase());
    }

    public static void clearEffects() {
        effects.clear();
    }

    public static Set<String> getRegisteredEffects() {
        return effects.keySet();
    }
}

