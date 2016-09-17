package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import org.bukkit.scheduler.BukkitRunnable;

import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;

public class Runnable1Tick extends BukkitRunnable {
    private final WeatherFronts plugin;
    private final MobSpawner mobSpawner;

    public Runnable1Tick(WeatherFronts instance, FrontsWorld frontsWorld) {
        this.plugin = instance;
        this.mobSpawner = new MobSpawner(instance, frontsWorld);
    }

    @Override
    public void run() {
        this.mobSpawner.spawnMobs();
    }
}
