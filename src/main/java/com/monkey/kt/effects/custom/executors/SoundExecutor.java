package com.monkey.kt.effects.custom.executors;

import com.monkey.kt.KT;
import com.monkey.kt.effects.custom.CustomEffectConfig;
import com.monkey.kt.utils.scheduler.SchedulerWrapper;
import org.bukkit.Location;
import org.bukkit.Sound;

import java.util.List;

public class SoundExecutor {

    private final KT plugin;

    public SoundExecutor(KT plugin) {
        this.plugin = plugin;
    }

    public void execute(List<CustomEffectConfig.SoundData> sounds, Location location) {
        if (sounds == null || sounds.isEmpty() || location.getWorld() == null) {
            return;
        }

        for (CustomEffectConfig.SoundData soundData : sounds) {
            if (soundData.getDelay() > 0) {
                SchedulerWrapper.runTaskLater(plugin, () -> {
                    playSound(soundData, location);
                }, soundData.getDelay());
            } else {
                playSound(soundData, location);
            }
        }
    }

    public void executeSound(CustomEffectConfig.SoundData soundData, Location location) {
        playSound(soundData, location);
    }

    private void playSound(CustomEffectConfig.SoundData soundData, Location location) {
        Sound sound = soundData.getSound();
        if (sound != null && location.getWorld() != null) {
            location.getWorld().playSound(
                    location,
                    sound,
                    (float) soundData.getVolume(),
                    (float) soundData.getPitch()
            );
        }
    }
}