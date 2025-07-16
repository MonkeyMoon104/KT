package com.monkey.kt.effects;

import com.monkey.kt.KT;
import com.monkey.kt.effects.list.cloud.CloudEffect;
import com.monkey.kt.effects.list.enchantcolumn.EnchantColumnEffect;
import com.monkey.kt.effects.list.end.EndEffect;
import com.monkey.kt.effects.list.explosion.ExplosionEffect;
import com.monkey.kt.effects.list.fire.FireEffect;
import com.monkey.kt.effects.list.fireworks.FireworksEffect;
import com.monkey.kt.effects.list.glowmissile.GlowMissileEffect;
import com.monkey.kt.effects.list.hearts.HeartsEffect;
import com.monkey.kt.effects.list.lightning.LightningEffect;
import com.monkey.kt.effects.list.notes.NotesEffect;
import com.monkey.kt.effects.list.pigstep.PigstepEffect;
import com.monkey.kt.effects.list.smoke.SmokeEffect;
import com.monkey.kt.effects.list.sniper.SniperEffect;
import com.monkey.kt.effects.list.totem.TotemEffect;
import com.monkey.kt.effects.list.warden.WardenEffect;

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
        effects.put("pigstep", new PigstepEffect(plugin));
        effects.put("warden", new WardenEffect(plugin));
        effects.put("glowmissile", new GlowMissileEffect(plugin));
        effects.put("sniper", new SniperEffect(plugin));
        effects.put("enchantcolumn", new EnchantColumnEffect(plugin));
        effects.put("fireworks", new FireworksEffect(plugin));
    }

    public static KillEffect getEffect(String name) {
        return effects.get(name.toLowerCase());
    }

}
