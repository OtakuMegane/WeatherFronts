package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import org.bukkit.scheduler.BukkitRunnable;

import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;

public class Runnable1Tick extends BukkitRunnable {
    private final WeatherFronts plugin;
    private final MobSpawner mobSpawner;
    private final EntityHandler entityHandler;

    public Runnable1Tick(WeatherFronts instance, FrontsWorld frontsWorld) {
        this.plugin = instance;
        this.mobSpawner = new MobSpawner(instance, frontsWorld);
        this.entityHandler = new EntityHandler(instance, frontsWorld);
        this.plugin.getServer().getPluginManager().registerEvents(this.entityHandler, instance);
    }

    @Override
    public void run() {
        this.mobSpawner.spawnMobs();
        this.entityHandler.affectArrows();
        this.entityHandler.affectWolves();
        this.entityHandler.changePlayerWeather();
    }
}
