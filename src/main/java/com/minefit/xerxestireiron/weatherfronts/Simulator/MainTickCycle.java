package com.minefit.xerxestireiron.weatherfronts.Simulator;

import java.util.Map.Entry;

import org.bukkit.scheduler.BukkitRunnable;

import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.Storm.Storm;

public class MainTickCycle extends BukkitRunnable {
    private final WeatherFronts plugin;
    private final MobSpawner mobSpawner;
    private final EntityHandler entityHandler;
    private final FireHandler fireHandler;
    private final Simulator simulator;

    public MainTickCycle(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.mobSpawner = new MobSpawner(instance, simulator);
        this.entityHandler = new EntityHandler(instance, simulator);
        this.fireHandler = new FireHandler(instance, simulator);
        this.simulator = simulator;
        this.plugin.getServer().getPluginManager().registerEvents(this.entityHandler, instance);
        this.plugin.getServer().getPluginManager().registerEvents(this.fireHandler, instance);
    }

    @Override
    public void run() {
        this.mobSpawner.spawnMobs();
        this.entityHandler.affectArrows();
        this.entityHandler.affectWolves();
        this.entityHandler.affectBlazes();
        this.entityHandler.affectSnowmen();
        this.entityHandler.affectEndermen();
        this.entityHandler.changePlayerWeather();
        this.fireHandler.extinguishFire();

        for (Entry<String, Storm> entry : simulator.getStorms().entrySet()) {
            entry.getValue().genLightning();
            entry.getValue().tickStormChunks();
        }
    }
}
