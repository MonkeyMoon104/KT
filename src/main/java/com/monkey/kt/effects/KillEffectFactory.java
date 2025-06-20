package com.monkey.kt.effects;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.*;

import java.util.HashMap;
import java.util.Map;

public class KillEffectFactory {

    private static final Map<String, KillEffect> effects = new HashMap<>();

    private final KT plugin;

    public KillEffectFactory(KT plugin) {
        this.plugin = plugin;
        effects.put("fire", new FireEffect(plugin));
        effects.put("lightning", new LightningEffect(plugin));
        effects.put("explosion", new ExplosionEffect(plugin));
        effects.put("hearts", new HeartsEffect(plugin));
        effects.put("notes", new NotesEffect(plugin));
        effects.put("cloud", new CloudEffect(plugin));
        effects.put("smoke", new SmokeEffect(plugin));
        effects.put("totem", new TotemEffect(plugin));
        effects.put("end", new EndEffect(plugin));
    }

    public static KillEffect getEffect(String name) {
        return effects.get(name.toLowerCase());
    }
}
